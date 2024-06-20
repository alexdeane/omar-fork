/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec10/ExtendedVerifyFilter.java,v 1.3 2005/06/11 00:20:53 geomurr Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec10;

import com.sun.xml.wss.filter.FilterBase;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;

import javax.xml.transform.TransformerException;

import com.sun.org.apache.xml.security.exceptions.AlgorithmAlreadyRegisteredException;
import com.sun.org.apache.xml.security.signature.Reference;
import com.sun.org.apache.xml.security.signature.SignedInfo;
import com.sun.org.apache.xml.security.transforms.Transform;
import com.sun.org.apache.xml.security.utils.resolver.ResourceResolver;
import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.xml.wss.ExtendedMessageFilter;
import com.sun.xml.wss.KeyInfoHeaderBlock;
import com.sun.xml.wss.MessageConstants;
import com.sun.xml.wss.PolicyViolationException;
import com.sun.xml.wss.SecurableSoapMessage;
import com.sun.xml.wss.SecurityHeader;
import com.sun.xml.wss.SignatureHeaderBlock;
import com.sun.xml.wss.Target;
import com.sun.xml.wss.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.XWSSecurityRuntimeException;
import com.sun.xml.wss.configuration.AllowSignature;
import com.sun.xml.wss.configuration.SecurityRequirement;
import com.sun.xml.wss.configuration.SecurityRequirements;
import com.sun.xml.wss.configuration.VerifyRequirement;
import com.sun.xml.wss.helpers.KeyResolver;
import com.sun.xml.wss.helpers.ResolverId;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.freebxml.omar.common.security.*;
import org.freebxml.omar.common.CommonResourceBundle;


/**
 * Extends the SignFilter in order to be able to:
 * - use ExtendedSignatureHeaderBlock 
 * - verify attachments
 *
 * NOTE: This class shall be replaced by the original once XWS-Security support
 * the required features.
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class ExtendedVerifyFilter extends FilterBase implements ExtendedMessageFilter {
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();

    ArrayList optionalTargets = null;
    ArrayList optionalNodeList = null;
    private boolean enableLogging = false;
    SecurityRequirement receiverRequirement = null;

    private static void registerTransforms() {
        try {
            // Register the non-standard Attachment-Content-Only-Transform
            String transformURI = TransformAttachmentContentOnly.implementedTransformURI;
            Transform.register(transformURI, TransformAttachmentContentOnly.class.getName());
        } catch (AlgorithmAlreadyRegisteredException e) {
            // ignore
        }
    }
    
    public ExtendedVerifyFilter() {
        registerTransforms();
    }
    
    public ExtendedVerifyFilter(SecurityRequirement receiverRequirement,
            ArrayList optionalTargets) {
        this.receiverRequirement = receiverRequirement;
        this.optionalTargets = optionalTargets;
        registerTransforms();
    }
    
    public void init() throws XWSSecurityException {
        /* empty */
    }
    
    public void enableOperationsLog(boolean enable) {
        enableLogging = enable;
    }
    
    public void process(SecurableSoapMessage secureMessage)
    throws XWSSecurityException {
        if (!verify(secureMessage)) {
            FilterBase.log.log(Level.SEVERE,
                    CommonResourceBundle.getInstance().getString("message.WSS0167.SignatureVerificationFailed"));
            XWSSecurityException xwsse
                    = new XWSSecurityException(resourceBundle.getString("message.signatureVerificationFailed"));
            throw SecurableSoapMessage.newSOAPFaultException
                    (MessageConstants.WSSE_FAILED_CHECK,
                    resourceBundle.getString("message.signatureVerificationFailed"), xwsse);
        }
    }
    
    private boolean verify(SecurableSoapMessage secureMessage)
    throws XWSSecurityException, PolicyViolationException {
        if (!SecurableSoapMessage.isStrTransformAdded()) {
            try {
                Transform.register
                        ("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#STR-Transform",
                        "com.sun.xml.wss.helpers.TransformSTR");
            } catch (AlgorithmAlreadyRegisteredException e) {
                throw new XWSSecurityException(e);
            }
            SecurableSoapMessage.setStrTransformAdded(true);
        }
        SecurityHeader secHeader = secureMessage.findSecurityHeader();
        ExtendedSignatureHeaderBlock sigBlock = null;
        try {
            sigBlock = ((ExtendedSignatureHeaderBlock)(secHeader
                    .getCurrentHeaderBlock(ExtendedSignatureHeaderBlock.class)));
            sigBlock.setSoapMessage(secureMessage);
        } catch (XWSSecurityException e) {
            FilterBase.log.log(Level.SEVERE,
                    CommonResourceBundle.getInstance().getString("message.WSS0129.noDssignatureInSecurityHeaderblock"),
                    new Object[] { e });
                    secureMessage.generateSecurityHeaderException
                            (resourceBundle.getString("message.retrieveSignatureFailed"));
        }
        try {
            SecurableSoapMessage.setDocMessageAssociation(sigBlock
                    .getOwnerDocument(), secureMessage);
            if (receiverRequirement != null) {
                if (optionalTargets != null)
                    optionalNodeList = getOptionalNodeList(secureMessage);
                checkIfReceiverReqsAreMet(receiverRequirement, sigBlock, secureMessage);
            }
            checkForDsCanonicalizationMethod(sigBlock, secureMessage);
            KeyInfoHeaderBlock keyInfoBlock = sigBlock.getKeyInfoHeaderBlock();
            PublicKey publicKey = ((PublicKey)
                    KeyResolver.getKey(keyInfoBlock, true, secureMessage));
            if (publicKey == null) {
                FilterBase.log.log(Level.SEVERE,
                        CommonResourceBundle.getInstance().getString("message.WSS0336.cannotLocatePublickeyForSignatureVerification"));
                throw new XWSSecurityException
                        (resourceBundle.getString("message.locatePublicKeyFailed"));
            }
            if (!SecurableSoapMessage.isWsuIdResolverAdded()) {
                ResourceResolver.registerAtStart(ResolverId.getResolverName());
                SecurableSoapMessage.setWsuIdResolverAdded(true);
            }
            boolean bool;
            try {
                boolean retVal = sigBlock.checkSignatureValue(publicKey);
                try {
                    if (enableLogging)
                        updateOperationsLog(secureMessage, sigBlock);
                } catch (Exception e) {
                    throw new XWSSecurityRuntimeException(e);
                }
                bool = retVal;
            } catch (XWSSecurityException e) {
                FilterBase.log.log(Level.SEVERE,
                        CommonResourceBundle.getInstance().getString("message.WSS0133.exceptionWhileVerifyingSignature",
                        new Object[] { e.getCause().getMessage() }));
                 throw SecurableSoapMessage.newSOAPFaultException
                        (MessageConstants.WSSE_FAILED_CHECK,
                        resourceBundle.getString("message.signatureVerificationFailed"), e.getCause());
            }
            return bool;
        } finally {
            if (sigBlock != null) {
                SecurableSoapMessage.removeDocMessageAssociation(sigBlock.getOwnerDocument());
            }
        }
    }
    
    public void setReceiverRequirement
            (SecurityRequirement receiverRequirement) {
        this.receiverRequirement = receiverRequirement;
    }
    
    public void setReceiverRequirements
            (SecurityRequirements receiverRequirements)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(resourceBundle.getString("message.operationNotSupported"));
    }
    
    public void checkForDsCanonicalizationMethod
            (SignatureHeaderBlock sigBlock, SecurableSoapMessage secureMessage)
            throws XWSSecurityException {
        String strTranformXpath = "ds:SignedInfo/ds:Reference/ds:Transforms/ds:Transform[" +
                "@Algorithm=\"http://docs.oasis-open.org/wss/2004/01/" +
                "oasis-200401-wss-soap-message-security-1.0#STR-Transform\"]";
        Node strTransformNode = null;
        do {
            try {
                strTransformNode = XPathAPI.selectSingleNode(sigBlock.getAsSoapElement(),
                        strTranformXpath, secureMessage.getNSContext());
                if (null != strTransformNode)
                    break;
            } catch (TransformerException te) {
                throw new XWSSecurityException(te.getMessage(), te);
            }
            return;
        } while (false);
        try {
            Node canonicalizationMethod = XPathAPI.selectSingleNode(strTransformNode,
                    "wsse:TransformationParameters/ds:CanonicalizationMethod[@Algorithm=" +
                    "\"http://www.w3.org/2001/10/xml-exc-c14n#\"]", secureMessage.getNSContext());
            if (null == canonicalizationMethod) {
                XWSSecurityException xwsse = new XWSSecurityException("http://www.w3.org/2001/10/" +
                        "xml-exc-c14n# CanonicalizationMethod was expected as a TransformationParameter" +
                        " inside the STR-Transform");
                throw SecurableSoapMessage.newSOAPFaultException(MessageConstants
                        .WSSE_UNSUPPORTED_ALGORITHM, xwsse.getMessage(), xwsse);
            }
        } catch (TransformerException te) {
            throw new XWSSecurityException(te);
        }
    }
    
    private void updateOperationsLog
            (SecurableSoapMessage secureMessage, SignatureHeaderBlock sigBlock)
            throws Exception {
        java.util.Set references = new HashSet();
        SignedInfo signedInfo = sigBlock.getDSSignedInfo();
        int length = signedInfo.getLength();
        for (int i = 0; i < length; i++) {
            Reference reference = signedInfo.item(i);
            String uri = reference.getURI();
            String wsuId = uri.substring(1);
            org.w3c.dom.Element element
                    = secureMessage.getElementByWsuId(wsuId);
            references.add(element);
        }
        secureMessage.logSignatureReferences(references);
    }
    
    private ArrayList getOptionalNodeList(SecurableSoapMessage ssm)
    throws XWSSecurityException {
        if (optionalTargets == null)
            return null;
        ArrayList optionalNodes = new ArrayList();
        Iterator i = optionalTargets.iterator();
        while (i.hasNext()) {
            Target target = (Target) i.next();
            if (target.getType().equals("uri"))
                optionalNodes.add((Node) ssm.getMessageParts(target));
            else {
                NodeList nlist = (NodeList) ssm.getMessageParts(target);
                for (int j = 0; j < nlist.getLength(); j++)
                    optionalNodes.add(nlist.item(j));
            }
        }
        return optionalNodes;
    }
    
    private ArrayList convertToArrayList(NodeList nodeList) {
        ArrayList result = new ArrayList();
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++)
                result.add(nodeList.item(i));
        }
        return result;
    }
    
    private void checkIfReceiverReqsAreMet
            (SecurityRequirement receiverRequirement,
            SignatureHeaderBlock sigBlock, SecurableSoapMessage secureMessage)
            throws PolicyViolationException {
        ArrayList targets = null;
        if (receiverRequirement instanceof VerifyRequirement)
            targets = ((VerifyRequirement) receiverRequirement).getTargets();
        else if (receiverRequirement instanceof AllowSignature)
            targets = ((AllowSignature) receiverRequirement).getTargets();
        SignedInfo signedInfo = sigBlock.getDSSignedInfo();
        ArrayList signedElements
                = getListOfSignedElements(signedInfo, secureMessage);
        Iterator it = targets.iterator();
        int numberOfTargetElements = 0;
        while (it.hasNext()) {
            Target target = (Target) it.next();
            String targetType = target.getType();
            String targetValue = target.getValue();
            if (targetType.equals("qname")) {
                String xpath = XMLUtil.convertToXpath(targetValue);
                NodeList targetsToCheckFor = null;
                try {
                    targetsToCheckFor
                            = XPathAPI.selectNodeList(secureMessage.getSOAPPart(),
                            xpath,
                            secureMessage
                            .getNSContext());
                } catch (Exception e) {
                    throw new XWSSecurityRuntimeException(e);
                }
                numberOfTargetElements += targetsToCheckFor.getLength();
                verifyIfTargetsSigned(convertToArrayList(targetsToCheckFor),
                        signedElements, xpath);
            } else if (targetType.equals("xpath")) {
                NodeList targetsToCheckFor = null;
                try {
                    targetsToCheckFor
                            = XPathAPI.selectNodeList(secureMessage.getSOAPPart(),
                            targetValue,
                            secureMessage
                            .getNSContext());
                } catch (Exception e) {
                    throw new XWSSecurityRuntimeException(e);
                }
                numberOfTargetElements += targetsToCheckFor.getLength();
                verifyIfTargetsSigned(convertToArrayList(targetsToCheckFor),
                        signedElements, targetValue);
            } else if (targetType.equals("uri")) {
                org.w3c.dom.Element elem;
                try {
                    elem = XMLUtil.getElementById(secureMessage.getSOAPPart(),
                            targetValue);
                } catch (Exception e) {
                    throw new XWSSecurityRuntimeException(e);
                }
                ArrayList targetToCheckFor = new ArrayList();
                if (elem != null) {
                    targetToCheckFor.add(elem);
                    numberOfTargetElements++;
                }
                verifyIfTargetsSigned(targetToCheckFor, signedElements,
                        targetValue);
            }
        }
        Iterator i = signedElements.iterator();
        while (i.hasNext()) {
            Node n = (Node) i.next();
            if (!optionalNodeList.contains(n))
                throw new PolicyViolationException
                        (resourceBundle.getString("message.receiverRequirement", new String[]{n.getLocalName()}));
        }
    }
    
    private void verifyIfTargetsSigned
            (ArrayList targetsToCheckFor, ArrayList signedElements,
            String concernedTargetXpath)
            throws PolicyViolationException {
        String errorMessage = (resourceBundle.getString("message.receiverRequirementNotMet", 
                new String[]{concernedTargetXpath}));
        if (targetsToCheckFor.isEmpty())
            throw new PolicyViolationException(errorMessage);
        for (int i = 0; i < targetsToCheckFor.size(); i++) {
            if (!signedElements.contains(targetsToCheckFor.get(i)))
                throw new PolicyViolationException(errorMessage);
            signedElements.remove(targetsToCheckFor.get(i));
        }
    }
    
    private ArrayList getListOfSignedElements
            (SignedInfo signedInfo, SecurableSoapMessage secureMessage) {
        ArrayList list = new ArrayList();
        try {
            for (int i = 0; i < signedInfo.getLength(); i++) {
                Reference reference = signedInfo.item(i);
                String uri = reference.getURI();
                String wsuId = uri.substring(1);
                org.w3c.dom.Element signedElem
                        = XMLUtil.getElementById(secureMessage.getSOAPPart(),
                        wsuId);
                list.add(signedElem);
            }
        } catch (Exception e) {
            throw new XWSSecurityRuntimeException(e);
        }
        return list;
    }
}