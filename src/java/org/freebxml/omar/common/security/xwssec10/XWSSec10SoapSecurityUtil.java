/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec10/XWSSec10SoapSecurityUtil.java,v 1.10 2006/09/23 21:03:23 chaeron Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec10;

import com.sun.xml.wss.KeyInfoStrategy;
import com.sun.xml.wss.SecurableSoapMessage;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.SecurityHeader;
import com.sun.xml.wss.Target;
import com.sun.xml.wss.configuration.DirectReferenceStrategyInfo;
import com.sun.xml.wss.filter.ExportTimestampFilter;
import com.sun.xml.wss.filter.ImportTimestampFilter;
import com.sun.xml.wss.impl.DefaultSecurityEnvironmentImpl;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import javax.xml.registry.JAXRException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.CommonResourceBundle;
import org.freebxml.omar.common.CredentialInfo;
import org.freebxml.omar.common.security.SoapSecurityUtil;
import org.freebxml.omar.common.security.xwssec.SecurityCallbackHandler;
import org.freebxml.omar.common.security.xwssec10.ExtendedImportCertificateTokenFilter;

/**
 * This is the actual implementation for SoapSecurityUtil.
 * Uses xmldsig + extended filters.
 *
 * Requires all the ExtendedXXX classes in this same package, plus
 * TransformAttachmentContentOnly, indirectly.
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class XWSSec10SoapSecurityUtil extends SoapSecurityUtil {
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();

    /** The log */
    private static final Log log = LogFactory.getLog(XWSSec10SoapSecurityUtil.class);

    /** Creates a new instance of SoapSecurityUtilImpl */
    public XWSSec10SoapSecurityUtil() {
    }

    /**
     * {@inheritDoc}
     */
    public SOAPMessage signSoapMessage(SOAPMessage msg, CredentialInfo credentialInfo)
    throws JAXRException {
        SecurableSoapMessage secureMsg = null;
        try {
            // Creates a default security environment using a SecurityCallbackHandler
            SecurityCallbackHandler cbHandler = new SecurityCallbackHandler(credentialInfo);
            SecurityEnvironment se = new DefaultSecurityEnvironmentImpl(cbHandler);

            // Wrap the SOAPMessage with a SecurableSoapMessage
            secureMsg = new SecurableSoapMessage(msg);
            secureMsg.setSecurityEnvironment(se);

            // manually generate a wsu id  for the BinarySecurityToken
            String wsuId = BINARY_SECURITY_TOKEN_WSU_ID;
            secureMsg.setFilterParameter("x509TokenId", wsuId);

            // Use the configuration to instantiate a DirectReferenceStrategy
            DirectReferenceStrategyInfo info = new DirectReferenceStrategyInfo();
            info.setInitializationParameter("forsigning", "true");
            info.setInitializationParameter("alias", credentialInfo.alias);
            KeyInfoStrategy keyInfoStrategy = info.createStrategy();

            ArrayList targets = new ArrayList();
            // Add a target for the soap envelope body
            targets.add(new Target(Target.TARGET_TYPE_VALUE_QNAME,
                "{http://schemas.xmlsoap.org/soap/envelope/}Body"));
            // Add target for timestamp
            targets.add(new Target(Target.TARGET_TYPE_VALUE_XPATH,
                "./env:Envelope/env:Header/wsse:Security/wsu:Timestamp"));
            // Add a target for each of the attachments
            for (Iterator it = secureMsg.getAttachments(); it.hasNext(); ) {
                // Remove <> from contentId
                String contentId = ((AttachmentPart)it.next()).getContentId();
                targets.add(new Target(Target.TARGET_TYPE_VALUE_URI,
                    "cid:" + contentId.substring(1, contentId.length()-1)));
            }

            // Start adding elements to wsse:Security in reverse order

            // 1. Add the signature element (without signing)
            ExtendedExportSignatureFilter esf = new ExtendedExportSignatureFilter(keyInfoStrategy,
                    credentialInfo.privateKey.getAlgorithm());
            esf.setParameter("x509TokenId", wsuId);
            esf.init();
            esf.process(secureMsg);

            // 2. Add the BinarySecurityToken to message
            ExtendedExportCertificateTokenFilter eectf =
                    new ExtendedExportCertificateTokenFilter(credentialInfo.alias, wsuId);
            eectf.init();
            eectf.process(secureMsg);

            // 3. Add timestamp (GMT!)
            long now = System.currentTimeMillis();
            TimeZone tzCur = TimeZone.getDefault();
            // NOTE: daylight savings are not considered if timezone in system
            // is set as offset and not by id (i.e., GMT+2 and not Europe/Helsinki)
            Date createdDate = new Date(now - tzCur.getOffset(now));
            String createdString = dateFormatter.format(createdDate);
            ExportTimestampFilter etf = new ExportTimestampFilter(createdString);
            etf.process(secureMsg);

            // Sign using wsuId point to our BinarySecurityToken. Also sign security token.
            ExtendedSignFilter sf = new ExtendedSignFilter(targets, keyInfoStrategy);
            sf.setParameter("x509TokenId", wsuId);
            sf.setParameter("strtransformxpath", "./env:Envelope/env:Header/wsse:Security/ds:Signature[1]/ds:KeyInfo/wsse:SecurityTokenReference");
            sf.init();
            sf.process(secureMsg);

        // work around for SOAPMessage.writeTo() inconsistencies
        secureMsg.saveChanges();
        } catch (Exception e) {
            throw new JAXRException(resourceBundle.getString("message.signSoapMessageFailed"), e);
        }

        return secureMsg;
    }

    /**
     * {@inheritDoc}
     */
    public boolean verifySoapMessage(SOAPMessage msg, CredentialInfo credentialInfo)
    throws JAXRException {
        try {
            // Creates a default security environment using a SecurityCallbackHandler
            SecurityCallbackHandler cbHandler = new SecurityCallbackHandler(credentialInfo);
            SecurityEnvironment se = new DefaultSecurityEnvironmentImpl(cbHandler);

            // Wrap the SOAPMessage with a SecurableSoapMessage
            SecurableSoapMessage secureMsg = new SecurableSoapMessage(msg);
            secureMsg.setSecurityEnvironment(se);

            // Verify that message has a SecurityHeader
            SecurityHeader secHeader = secureMsg.findSecurityHeader();
            if (secHeader == null) {
                // SOAP message had no wss:SecurityHeader
                return false;
            }

            // Verify timestamp
            ImportTimestampFilter itf = new ImportTimestampFilter();
            itf.process(secureMsg);

            // Get X509Certificate to SecurityEnvironment
            ExtendedImportCertificateTokenFilter ictf = new ExtendedImportCertificateTokenFilter();
            ictf.process(secureMsg);
            credentialInfo.cert = ictf.getLastCert();

            //TODO: add SecurityRequirements
            // - Security token signed
            // - timestamp was signed
            // - all attachments are signed
            ExtendedVerifyFilter vf = new ExtendedVerifyFilter();
            vf.enableOperationsLog(true);
            vf.process(secureMsg);

            return true;
        } catch (Exception e) {
            if (ignoreSignatureVerificationErrors) {
                if( logSignatureVerificationErrors ) {
                    log.error(CommonResourceBundle.getInstance().getString("message.verifySoapMessageFailed"), e);
                }
                return false;
            } else {
                throw new
                    JAXRException(CommonResourceBundle.getInstance().
                                  getString("message.verifySoapMessageFailed"), e);                
            }
        }
    }
}
