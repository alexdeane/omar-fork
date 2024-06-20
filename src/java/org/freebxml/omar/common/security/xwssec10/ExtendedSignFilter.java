/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec10/ExtendedSignFilter.java,v 1.3 2005/06/11 00:20:53 geomurr Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec10;

import com.sun.org.apache.xml.security.transforms.TransformationException;
import com.sun.xml.wss.filter.FilterBase;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.TransformerException;

import com.sun.org.apache.xml.security.exceptions.AlgorithmAlreadyRegisteredException;
import com.sun.org.apache.xml.security.transforms.Transform;
import com.sun.org.apache.xml.security.transforms.Transforms;
import com.sun.org.apache.xml.security.utils.resolver.ResourceResolver;
import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.xml.wss.KeyInfoHeaderBlock;
import com.sun.xml.wss.KeyInfoStrategy;
import com.sun.xml.wss.MessageFilter;
import com.sun.xml.wss.SecurableSoapMessage;
import com.sun.xml.wss.SignatureHeaderBlock;
import com.sun.xml.wss.Target;
import com.sun.xml.wss.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.helpers.ResolverId;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.freebxml.omar.common.security.*;
import org.freebxml.omar.common.CommonResourceBundle;

/**
 * Extends the SignFilter in order to:
 * - use ExtendedSignatureHeaderBlock 
 * - sign attachments
 *
 * NOTE: This class shall be replaced by the original once XWS-Security support
 * the required features.
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class ExtendedSignFilter extends FilterBase implements MessageFilter {
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
    private String strTransformXpath;
    private String x509TokenId;

    private static void registerTransforms() {
        try {
            // Register the non-standard Attachment-Content-Only-Transform
            String transformURI = TransformAttachmentContentOnly.implementedTransformURI;
            Transform.register(transformURI, TransformAttachmentContentOnly.class.getName());
        } catch (AlgorithmAlreadyRegisteredException e) {
            // ignore
        }
    }
    
    public ExtendedSignFilter() {
        registerTransforms();
    }
    
    public ExtendedSignFilter(KeyInfoStrategy keyInfoStrategy)
    throws XWSSecurityException {
        this("//S:Body", keyInfoStrategy);
    }
    
    public ExtendedSignFilter(String xpath, KeyInfoStrategy keyInfoStrategy)
    throws XWSSecurityException {
        if (xpath == null || keyInfoStrategy == null)
            throw new XWSSecurityException
                    (resourceBundle.getString("message.XPathandKeyInfoStrategy"));
        targets = new ArrayList();
        targets.add(new Target("xpath", xpath));
        this.keyInfoStrategy = keyInfoStrategy;
        registerTransforms();
    }
    
    public ExtendedSignFilter(ArrayList targets, KeyInfoStrategy keyInfoStrategy)
    throws XWSSecurityException {
        if (targets == null || keyInfoStrategy == null)
            throw new XWSSecurityException
                    (resourceBundle.getString("message.TargetListandKeyInfoStrategy"));
        this.targets = targets;
        this.keyInfoStrategy = keyInfoStrategy;
        registerTransforms();
    }
    
    public void init() throws XWSSecurityException {
        if (getParameter("x509TokenId") != null)
            x509TokenId = getParameter("x509TokenId");
	if (getParameter("strtransformxpath") != null)
	    strTransformXpath = getParameter("strtransformxpath");
    }

    public void process(SecurableSoapMessage secureMessage)
    throws XWSSecurityException {
        ExtendedSignatureHeaderBlock sigBlock = (ExtendedSignatureHeaderBlock)
			secureMessage.getFilterParameter("Signature");
	    if (null == sigBlock)
		throw new XWSSecurityException
			  (resourceBundle.getString("message.SIGNATUREBLOCKNotFound"));
	    keyInfoStrategy
		= ((KeyInfoStrategy)
		   secureMessage.getFilterParameter("KeyInfoStrategy"));
	    if (null == keyInfoStrategy)
		throw new XWSSecurityException
			  (resourceBundle.getString("message.KEYINFOSTRATEGYNotFound"));
        try {
            PrivateKey privKey = getAssociatedPrivateKey(secureMessage);
            if (privKey == null) {
                throw new XWSSecurityException(resourceBundle.getString("message.privateKey"));
            }
            // This is needed by ExtendedSignatureHeaderBlock
            sigBlock.setSoapMessage(secureMessage);
            if (targets == null && strTransformXpath == null)
                throw new XWSSecurityException(resourceBundle.getString("message.signTargets"));
            if (targets != null) {
                Iterator it = targets.iterator();
                while (it.hasNext()) {
                    Target target = (Target) it.next();
                    String targetType = target.getType();
                    String targetValue = target.getValue();
                    if (targetType.equals("qname"))
                        addTransforms(sigBlock, secureMessage,
                                convertToXpath(targetValue), false);
                    else if (targetType.equals("xpath"))
                        addTransforms(sigBlock, secureMessage, targetValue, false);
                    else if (targetType.equals("uri") && targetValue.startsWith("cid:")) {
                        processAttachment(sigBlock, secureMessage, targetValue,
                                createAttachmentTransforms(secureMessage));
                    } else if (targetType.equals("uri")) {
                        Element elem = secureMessage.getElementById(targetValue);
                        processElement(sigBlock, secureMessage, elem,
                                createTransforms(secureMessage, false));
                    }
                }
            }
            if (strTransformXpath != null)
                addTransforms(sigBlock, secureMessage, strTransformXpath, true);
            if (!SecurableSoapMessage.isWsuIdResolverAdded()) {
                ResourceResolver.registerAtStart(ResolverId.getResolverName());
                SecurableSoapMessage.setWsuIdResolverAdded(true);
            }
            sigBlock.sign(privKey);
        } finally {
            SecurableSoapMessage.removeDocMessageAssociation(sigBlock.getOwnerDocument());
        }
    }
    
    private void addTransforms
            (SignatureHeaderBlock sigBlock, SecurableSoapMessage secureMessage,
            String xp, boolean signTokenFlag)
            throws XWSSecurityException {
        javax.xml.soap.SOAPPart soapPart = secureMessage.getSOAPPart();
        NodeList elemsToSign;
        try {
            elemsToSign
                    = XPathAPI.selectNodeList(soapPart, xp,
                    secureMessage.getNSContext());
        } catch (TransformerException e) {
            throw new XWSSecurityException(e);
        }
        if (elemsToSign == null || elemsToSign.getLength() == 0)
            throw new XWSSecurityException(resourceBundle.getString("message.noElements", new String[]{xp}));
        for (int i = 0; i < elemsToSign.getLength(); i++) {
            Transforms transforms
                    = createTransforms(secureMessage, signTokenFlag);
            SOAPElement element = (SOAPElement) elemsToSign.item(i);
            processElement(sigBlock, secureMessage, element, transforms);
        }
        if (signTokenFlag)
            SecurableSoapMessage.setDocMessageAssociation
                    (sigBlock.getOwnerDocument(), secureMessage);
    }
    
    private String convertToXpath(String qname) {
        QName name = QName.valueOf(qname);
        if ("".equals(name.getNamespaceURI()))
            return "//" + name.getLocalPart();
        return ("//*[local-name()='" + name.getLocalPart()
        + "' and namespace-uri()='" + name.getNamespaceURI() + "']");
    }
    
    private String convertElemToXpath(Element elem) {
        if ("".equals(elem.getNamespaceURI()))
            return "//" + elem.getLocalName();
        return ("//*[local-name()='" + elem.getLocalName()
        + "' and namespace-uri()='" + elem.getNamespaceURI() + "']");
    }
    
    private PrivateKey getAssociatedPrivateKey
            (SecurableSoapMessage secureMsg) throws XWSSecurityException {
        String alias = keyInfoStrategy.getAlias();
        X509Certificate cert
                = secureMsg.getSecurityEnvironment().getCertificate(alias, true);
        if (cert == null)
            throw new XWSSecurityException
                    (resourceBundle.getString("message.X509CertificateNotFound"));
        keyInfoStrategy.setCertificate(cert);
        return secureMsg.getSecurityEnvironment().getPrivateKey(alias);
    }
    
    private Transforms createTransforms
            (SecurableSoapMessage secureMessage, boolean signTokenFlag)
            throws XWSSecurityException {
        Transforms transforms = new Transforms(secureMessage.getSOAPPart());
        try {
            if (signTokenFlag) {
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
                transforms.addTransform
                        ("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#STR-Transform");
                Transform strTransform = transforms.item(0);
                SOAPElement strTransformElement
                        = (SOAPElement) strTransform.getElement();
                SOAPElement transformationParameters
                        = (strTransformElement.addChildElement
                        ("TransformationParameters", "wsse",
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"));
                SOAPElement canonMethod
                        = (transformationParameters.addChildElement
                        ("CanonicalizationMethod", "ds",
                        "http://www.w3.org/2000/09/xmldsig#"));
                canonMethod.setAttribute
                        ("Algorithm", "http://www.w3.org/2001/10/xml-exc-c14n#");
            } else
                transforms
                    .addTransform("http://www.w3.org/2001/10/xml-exc-c14n#");
        } catch (Exception e) {
            throw new XWSSecurityException(e.getMessage(), e);
        }
        return transforms;
    }
    
    /** Create an Attachment-Content-Only-Transform according to wss-swa. */
    private Transforms createAttachmentTransforms
            (SecurableSoapMessage secureMessage)
            throws XWSSecurityException {
        Transforms transforms = new Transforms(secureMessage.getSOAPPart());
        String transformURI = TransformAttachmentContentOnly.implementedTransformURI;
        try {
            transforms.addTransform(transformURI);
        } catch (TransformationException e) {
            throw new XWSSecurityException(e);
        }
        return transforms;
    }
    
    private void processElement(SignatureHeaderBlock sigBlock, SecurableSoapMessage secureMessage,
            Element element, Transforms transforms) throws XWSSecurityException {
        if (element.getNodeType() != 1) {
            FilterBase.log.log(Level.SEVERE, CommonResourceBundle.getInstance().getString("message.WSS0165.unable.to.encrypt"));
            throw new XWSSecurityException(resourceBundle.getString("message.XPathNotDOMElement"));
        }
        String id = (element.getAttributeNS
                ("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
                "Id"));
        if (id.equals("")) {
            id = secureMessage.generateId();
            XMLUtil.setWsuIdAttr(element, id);
        }
        sigBlock.addSignedInfoReference("#" + id, transforms);
    }
    
    private void processAttachment (SignatureHeaderBlock sigBlock, SecurableSoapMessage secureMessage,
            String uri, Transforms transforms) throws XWSSecurityException {
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-ID", "<" + uri.substring("cid:".length()) + ">");
        Iterator attachments = secureMessage.getAttachments(headers);
        if (!attachments.hasNext()) {
            FilterBase.log.log(Level.SEVERE, CommonResourceBundle.getInstance().getString("message.WSS0165.unable.to.encrypt"));
            throw new XWSSecurityException
                    (resourceBundle.getString("message.noAttachmentforURI", new String[]{uri}));
        }
        sigBlock.addSignedInfoReference(uri, transforms);
    }
    
}
