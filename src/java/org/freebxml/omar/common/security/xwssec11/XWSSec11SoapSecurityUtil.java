/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec11/XWSSec11SoapSecurityUtil.java,v 1.21 2006/11/02 19:15:52 vikram_blr Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec11;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.SecurableSoapMessage;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.SecurityHeader;
import com.sun.xml.wss.impl.DefaultSecurityEnvironmentImpl;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import javax.xml.registry.JAXRException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
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
import org.w3c.dom.Element;

/**
 * This is the actual implementation for SoapSecurityUtil.
 * Uses xmldsig + extended filters.
 *
 * Requires all the ExtendedXXX classes in this same package, plus
 * TransformAttachmentContentOnly, indirectly.
 *
 * @author Farrukh S. Najmi
 */
public class XWSSec11SoapSecurityUtil extends SoapSecurityUtil { 

    /** The log */
    private static final Log log = LogFactory.getLog(XWSSec11SoapSecurityUtil.class);

    /** Creates a new instance of SoapSecurityUtilImpl */
    public XWSSec11SoapSecurityUtil() {
    }

    /**
     * {@inheritDoc}
     */
    public SOAPMessage signSoapMessage(SOAPMessage msg, CredentialInfo credentialInfo)
    throws JAXRException {
        try {
            //Check if the server being communicated is a legacy server and property for it is set.
            boolean legacyServer = Boolean.parseBoolean(CommonProperties.getInstance().getProperty("omar.common.security.legacyServer", "false"));
            //Create XWSProcessor
            XWSSProcessorFactory factory = XWSSProcessorFactory.newInstance();

            XWSSProcessor processor = factory.createForSecurityConfiguration(
                                      getSecurityConfiguration(msg, legacyServer),
                                        new SecurityCallbackHandler(credentialInfo));

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
       credentialInfo.cert = null;
        try {
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

            // Create a default security environment using a SecurityCallbackHandler
            SecurityCallbackHandler cbHandler =
               new SecurityCallbackHandler(tempCredentialInfo);
            SecurityEnvironment se = new DefaultSecurityEnvironmentImpl(cbHandler);

            SecurableSoapMessage secureMsg = null;
            if (msg instanceof SecurableSoapMessage) {
                secureMsg = (SecurableSoapMessage)msg;
            } else {
                // Wrap the SOAPMessage with a SecurableSoapMessage
                secureMsg = new SecurableSoapMessage(msg);
                secureMsg.setSecurityEnvironment(se);
            }

            // create XWSProcessor, use legacy policy if msg contains >1 signature
            XWSSProcessorFactory factory = XWSSProcessorFactory.newInstance();
            XWSSProcessor processor = factory.createForSecurityConfiguration(
                              getSecurityConfiguration(msg,
                                                       certInfo.isMultiple()), cbHandler);

            ProcessingContext context = new ProcessingContext();
            context.setSOAPMessage(secureMsg);

           processor.verifyInboundMessage(context);

           // all done and dusted, provide certificate we found
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

    private InputStream getSecurityConfiguration(SOAPMessage msg,
                                                boolean legacyPolicy)
       throws JAXRException {

        InputStream securityConfiguration = null;


       String requireOpen =       "  <xwss:RequireSignature>";
       String requireClose =      "  </xwss:RequireSignature>";
       String signOpen =         ("  <xwss:Sign>\n" +
                                  // Need to specify <xwss:X509Token> to
                                  // specify wsuId for BinarySecurityToken
                                  // per spec.  The alias is dummy since
                                  // it is already known to the
                                  // CallbackHandler
                                  "    <xwss:X509Token id='" +
                                  CanonicalConstants.CANONICAL_URI_SENDER_CERT +
                                  "' certificateAlias='dummy'/>\n");
       String signClose =         "  </xwss:Sign>\n";

        try {
           StringBuffer requirementsStr = new StringBuffer();
           StringBuffer signaturesStr = new StringBuffer();
           StringBuffer targetsStr = new StringBuffer();


            // Add a target and legacy Sign/RequireSignature for each of
            // the attachments
            for (Iterator it = msg.getAttachments(); it.hasNext(); ) {
                // Remove <> from contentId
                String contentId = ((AttachmentPart)it.next()).getContentId();
               String target =   ("    <xwss:Target type='uri'>cid:" +
                                  contentId.substring(1, contentId.length()-1) +
                                  "</xwss:Target>\n");

               if (legacyPolicy) {
                   // requirements string is built in reverse -- must
                   // verify multiple signatures in reverse order to their
                   // addition (an issue only when multiple attachments
                   // are in message)
                   requirementsStr.insert(0, requireOpen + target + requireClose);
                   signaturesStr.append(signOpen + target + signClose);
               } else {
                   targetsStr.append(target);
               }
            }

            String secConfigStr = ("<xwss:SecurityConfiguration dumpMessages='" +
                                  log.isTraceEnabled() +
                                  "' xmlns:xwss='http://java.sun.com/xml/ns/xwss/config'>\n" +

                                  signOpen +
                                  "    <xwss:Target type='xpath'>" +
                                  "//SOAP-ENV:Body</xwss:Target>\n" +
                                  targetsStr.toString() +
                                  signClose +

                                  // legacy Sign/Require elements go in
                                  // the middle to ensure Body is signed
                                  // first and verified last
                                  signaturesStr +
                                  requirementsStr +

                                  requireOpen +
                                  "    <xwss:Target type='xpath'>" +
                                  "//SOAP-ENV:Body</xwss:Target>\n" +
                                  targetsStr.toString() +
                                  requireClose +
                                  "</xwss:SecurityConfiguration>");

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

}
