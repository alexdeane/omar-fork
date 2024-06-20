/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec20/XWSSec20SoapSecurityUtil.java,v 1.13 2006/11/28 18:37:29 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec20;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.SecurableSoapMessage;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.core.SecurityHeader;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import javax.xml.registry.JAXRException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.CanonicalConstants;
import org.freebxml.omar.common.CommonResourceBundle;
import org.freebxml.omar.common.CommonProperties;
import org.freebxml.omar.common.CredentialInfo;
import org.freebxml.omar.common.security.SoapSecurityUtil;
import org.freebxml.omar.common.security.ReceivedCertificate;
import org.freebxml.omar.common.security.xwssec.SecurityCallbackHandler;

/**
 * This is the actual implementation for SoapSecurityUtil.
 * Uses xmldsig + extended filters.
 *
 * Requires all the ExtendedXXX classes in this same package, plus
 * TransformAttachmentContentOnly, indirectly.
 *
 * @author Farrukh S. Najmi
 */
public class XWSSec20SoapSecurityUtil extends SoapSecurityUtil {

    /** The log */
    private static final Log log = LogFactory.getLog(XWSSec20SoapSecurityUtil.class);

    /** Creates a new instance of SoapSecurityUtilImpl */
    public XWSSec20SoapSecurityUtil() {
    }

    /**
     * {@inheritDoc}
     */
    public SOAPMessage signSoapMessage(SOAPMessage msg, CredentialInfo credentialInfo)
    throws JAXRException {
        try {
            //Check if the server being communicated is a legacy server and property for it is set.
            boolean legacyServer = Boolean.valueOf(CommonProperties.getInstance().getProperty("omar.common.security.legacyServer", "false")).booleanValue();

            //Create XWSProcessor
            XWSSProcessorFactory factory = XWSSProcessorFactory.newInstance();
            XWSSProcessor processor = null;
            if (legacyServer) {
                processor = factory.createForSecurityConfiguration(
                                    getSecurityConfiguration(msg),
                                    new SecurityCallbackHandler(credentialInfo));
            } else {

                processor = factory.createForSecurityConfiguration(getSigningSecurityConfiguration(msg, credentialInfo), new SecurityCallbackHandler(credentialInfo));
            }
            ProcessingContext context = new ProcessingContext();

        // msg will be updated in place
            context.setSOAPMessage(msg);
            processor.secureOutboundMessage(context);

        // work around for SOAPMessage.writeTo() inconsistencies
        msg.saveChanges();
        } catch (Exception e) {
            throw new JAXRException(CommonResourceBundle.getInstance().getString("message.signSoapMessageFailed"), e);
        }

        return msg;
    }

    /**
     * {@inheritDoc}
     */
    public boolean verifySoapMessage(SOAPMessage msg, CredentialInfo credentialInfo)
    throws JAXRException {
        try {
            SecurableSoapMessage secureMsg = null;

            credentialInfo.cert = null;
           // manually parse incoming message, looking for certificates
            ReceivedCertificate certInfo = new ReceivedCertificate(msg);
           if (null == certInfo || null == certInfo.getCertificate()) {
                // SOAP message had no <wss:Security/> header or
                // appropriate <wss:BinarySecurityToken/>
               return false;
           }

           // don't mess with credentialInfo parameter 'till after verification
           CredentialInfo tempCredentialInfo = new CredentialInfo();
           tempCredentialInfo.cert = certInfo.getCertificate();

            SecurityCallbackHandler cbHandler = new SecurityCallbackHandler(tempCredentialInfo);
            SecurityEnvironment se = new DefaultSecurityEnvironmentImpl(cbHandler);

            // Creates a default security environment using a SecurityCallbackHandler
            if (msg instanceof SecurableSoapMessage) {
                secureMsg = (SecurableSoapMessage)msg;
            } else {
                // Wrap the SOAPMessage with a SecurableSoapMessage
                secureMsg = new SecurableSoapMessage(msg);
            }

            // Verify that message has a SecurityHeader
            SecurityHeader secHeader = secureMsg.findSecurityHeader();
            if (secHeader == null) {
                // SOAP message had no wss:SecurityHeader
                return false;
            }

            //There is a security header so verify message

            //Create XWSProcessor
            XWSSProcessorFactory factory = XWSSProcessorFactory.newInstance();
            XWSSProcessor processor =
                    factory.createForSecurityConfiguration(getVerificationSecurityConfiguration(msg, credentialInfo),  cbHandler);

            ProcessingContext context = new ProcessingContext();
            context.setSecurityEnvironment(se);
            context.setSOAPMessage(secureMsg);

            processor.verifyInboundMessage(context);
            credentialInfo.cert = certInfo.getCertificate();            
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

        return true;
    }

    private InputStream getSigningSecurityConfiguration(SOAPMessage msg, CredentialInfo credentialInfo) throws JAXRException {
        InputStream securityConfiguration = null;
        try {
            String secConfigStr = "<xwss:SecurityConfiguration dumpMessages=\"" + log.isTraceEnabled() + "\" xmlns:xwss=\"http://java.sun.com/xml/ns/xwss/config\" >";
            secConfigStr += "\n   <xwss:Sign>";

            secConfigStr +=
        // Need to specify <xwss:X509Token> to specify wsuId for
        // BinarySecurityToken per spec.  The alias is dummy since
        // it is already known to the CallbackHandler.
"\n       <xwss:X509Token id=\"" + CanonicalConstants.CANONICAL_URI_SENDER_CERT + "\" certificateAlias=\"dummy\"/>" +
        // Need to explicitly choose inclusive canonicalization to
        // avoid Apache XML Security bug 36640
        // <http://issues.apache.org/bugzilla/show_bug.cgi?id=36640>
        // which was present in all pre-2.0 XWSS implementations.
        // Using this canonicalization works around an on-the-wire
        // incompatibility between XWSS versions using the default
        // (exclusive) canonicalization.
"\n       <xwss:CanonicalizationMethod algorithm='http://www.w3.org/TR/2001/REC-xml-c14n-20010315'/>" +
"\n       <xwss:SignatureTarget type=\"xpath\" value=\"//SOAP-ENV:Body\">" +
"\n         <xwss:DigestMethod algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>" +
"\n       </xwss:SignatureTarget>";

            // Add a target for each of the attachments
            for (Iterator it = msg.getAttachments(); it.hasNext(); ) {
                // Remove <> from contentId
                String contentId = ((AttachmentPart)it.next()).getContentId();
                secConfigStr += "\n       <xwss:SignatureTarget type=\"uri\" value=\"cid:" + contentId.substring(1, contentId.length()-1) + "\">";
                secConfigStr += "\n         <xwss:DigestMethod algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>";
                secConfigStr += "\n         <xwss:Transform algorithm=\"http://docs.oasis-open.org/wss/2004/XX/oasis-2004XX-wss-swa-profile-1.0#Attachment-Content-Only-Transform\"/>";
                secConfigStr += "\n       </xwss:SignatureTarget>";
            }

            secConfigStr += "\n   </xwss:Sign>";

            secConfigStr += "\n   <xwss:RequireSignature>";
            secConfigStr += "\n       <xwss:SignatureTarget type=\"xpath\" value=\"//SOAP-ENV:Body\">";
            secConfigStr += "\n         <xwss:DigestMethod algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>";
            secConfigStr += "\n       </xwss:SignatureTarget>";

            // Add a target for each of the attachments
            for (Iterator it = msg.getAttachments(); it.hasNext(); ) {
                // Remove <> from contentId
                String contentId = ((AttachmentPart)it.next()).getContentId();
                secConfigStr += "\n       <xwss:SignatureTarget type=\"uri\" value=\"cid:" + contentId.substring(1, contentId.length()-1) + "\">";
                secConfigStr += "\n         <xwss:DigestMethod algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>";
                secConfigStr += "\n         <xwss:Transform algorithm=\"http://docs.oasis-open.org/wss/2004/XX/oasis-2004XX-wss-swa-profile-1.0#Attachment-Content-Only-Transform\"/>";
                secConfigStr += "\n       </xwss:SignatureTarget>";
            }

            secConfigStr += "\n   </xwss:RequireSignature>";
            secConfigStr += "\n</xwss:SecurityConfiguration>";

            if (log.isDebugEnabled()) {
                log.debug("Security Configuration: \n" + secConfigStr);
            }

            //URL cfgFileUrl = getClass().getResource("/org/freebxml/omar/common/security/xwssec11/xwssec-config.xml");
            byte[] bytes = secConfigStr.getBytes();
            securityConfiguration = new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            throw new JAXRException(e);
        }

        return securityConfiguration;
    }

   private InputStream getSecurityConfiguration(SOAPMessage msg)
       throws JAXRException {

        InputStream securityConfiguration = null;
        try {

            String secConfigStr =
            "<xwss:SecurityConfiguration dumpMessages=\"" + log.isTraceEnabled() + "\" xmlns:xwss=\"http://java.sun.com/xml/ns/xwss/config\" >";

            secConfigStr +=
            "\n   <xwss:Sign>" +
		// Need to specify <xwss:X509Token> to specify wsuId for
		// BinarySecurityToken per spec.  The alias is dummy since
		// it is already known to the CallbackHandler.
"\n       <xwss:X509Token id=\"" + CanonicalConstants.CANONICAL_URI_SENDER_CERT + "\" certificateAlias=\"dummy\"/>" +
		// Need to explicitly choose inclusive canonicalization to
		// avoid Apache XML Security bug 36640
		// <http://issues.apache.org/bugzilla/show_bug.cgi?id=36640>
		// which was present in all pre-2.0 XWSS implementations.
		// Using this canonicalization works around an on-the-wire
		// incompatibility between XWSS versions using the default
		// (exclusive) canonicalization.
"\n       <xwss:CanonicalizationMethod algorithm='http://www.w3.org/TR/2001/REC-xml-c14n-20010315'/>" +
"\n       <xwss:SignatureTarget type=\"xpath\" value=\"//SOAP-ENV:Body\">" +
"\n         <xwss:DigestMethod algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>" +
"\n       </xwss:SignatureTarget>" +
            "\n   </xwss:Sign>" ;

            for (Iterator it = msg.getAttachments(); it.hasNext(); ) {
                // Remove <> from contentId
                String contentId = ((AttachmentPart)it.next()).getContentId();
                secConfigStr += "\n   <xwss:Sign>";
                secConfigStr += "\n     <xwss:SignatureTarget type=\"uri\" value=\"cid:" + contentId.substring(1, contentId.length()-1) + "\">";
                secConfigStr += "\n        <xwss:DigestMethod algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>";
                secConfigStr += "\n        <xwss:Transform algorithm=\"http://docs.oasis-open.org/wss/2004/XX/oasis-2004XX-wss-swa-profile-1.0#Attachment-Content-Only-Transform\"/>";
                secConfigStr += "\n     </xwss:SignatureTarget>";
                secConfigStr += "\n   </xwss:Sign>";
            }

            secConfigStr += "\n   <xwss:RequireSignature>";
            secConfigStr += "\n       <xwss:SignatureTarget type=\"xpath\" value=\"//SOAP-ENV:Body\">";
            secConfigStr += "\n         <xwss:DigestMethod algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>";
            secConfigStr += "\n       </xwss:SignatureTarget>";
            secConfigStr += "\n   </xwss:RequireSignature>";
            
            StringBuffer requirementStr = new StringBuffer();
            // Add a target for each of the attachments
            for (Iterator it = msg.getAttachments(); it.hasNext(); ) {
                // Remove <> from contentId
                String contentId = ((AttachmentPart)it.next()).getContentId();
                String requireStr = "\n   <xwss:RequireSignature>";
                requireStr += "\n     <xwss:SignatureTarget type=\"uri\" value=\"cid:" + contentId.substring(1, contentId.length()-1) + "\">";
                requireStr += "\n        <xwss:DigestMethod algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>";
                requireStr += "\n        <xwss:Transform algorithm=\"http://docs.oasis-open.org/wss/2004/XX/oasis-2004XX-wss-swa-profile-1.0#Attachment-Content-Only-Transform\"/>";
                requireStr += "\n     </xwss:SignatureTarget>";
                requireStr += "\n   </xwss:RequireSignature>";
                requirementStr.insert(0, requireStr);
            }

            secConfigStr += requirementStr.toString();
            secConfigStr += "</xwss:SecurityConfiguration>";

            if (log.isDebugEnabled()) {
                log.debug("Security Configuration: \n" + secConfigStr);
            }

            byte[] bytes = secConfigStr.getBytes("UTF-8");
            securityConfiguration = new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            throw new JAXRException(e);
        }

        return securityConfiguration;
    }

    private InputStream getVerificationSecurityConfiguration(SOAPMessage msg, CredentialInfo credentialInfo) throws JAXRException {
        return getSigningSecurityConfiguration(msg, credentialInfo);
    }
}
