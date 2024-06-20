/*
 * SoapSecurityUtilTest.java
 * JUnit based test
 *
 * Created on 07 December 2004, 13:24
 */

package org.freebxml.omar.common.security.xwssec11;

import com.sun.xml.wss.WssSoapFaultException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;
import junit.framework.*;
import javax.xml.soap.SOAPMessage;
import org.freebxml.omar.common.CredentialInfo;
import javax.xml.registry.JAXRException;
import org.freebxml.omar.common.security.SoapSecurityUtil;
import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;
import com.sun.xml.wss.KeyInfoStrategy;
import com.sun.xml.wss.SecurableSoapMessage;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.X509SecurityToken;
import com.sun.xml.wss.filter.ExportCertificateTokenFilter;
import com.sun.xml.wss.filter.ExportSignatureFilter;
import com.sun.xml.wss.filter.FilterParameterConstants;
import com.sun.xml.wss.filter.SignFilter;
import com.sun.xml.wss.filter.VerifyFilter;
import com.sun.xml.wss.impl.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.keyinfo.DirectReferenceStrategy;
import com.sun.xml.wss.keyinfo.KeyNameStrategy;
import com.sun.xml.wss.keyinfo.X509IssuerSerialStrategy;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;

/**
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class XWSSec11SoapSecurityUtilTest extends TestCase {
 
    /** Test SOAP message, as String. */
    private static final String TEST_SOAP_MESSAGE_STRING = 
        "<soap-env:Envelope xmlns:soap-env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        "<soap-env:Header/>" +
        "<soap-env:Body>" +
        "data" +
        "</soap-env:Body>" +
        "</soap-env:Envelope>";
    
    /** Test credentials, created on the fly */
    private static CredentialInfo credentialInfo;

    /** Test SOAP message. */
    private static SOAPMessage soapMessage;

    /** Test SOAP message with attachment. */
    private static SOAPMessage soapMessageWA;
    
    public XWSSec11SoapSecurityUtilTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(XWSSec11SoapSecurityUtilTest.class);
        
        return suite;
    }

    /**
     * Test of signSoapMessage method with no attachments
     */
    public void testSignSoapMessageNoAttachment() throws Exception {
        System.out.println("testSignSoapMessageNoAttachment");
        
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();
        CredentialInfo credentialInfo = getCredentialInfo();
        
        InputStream soapStream = new ByteArrayInputStream(
                TEST_SOAP_MESSAGE_STRING.getBytes("utf-8"));
        MessageFactory factory = MessageFactory.newInstance();
        soapMessage = factory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        soapPart.setContent(new StreamSource(soapStream));

        soapMessage = ssu.signSoapMessage(soapMessage, credentialInfo);        
    }
    
    /**
     * Test of verifySoapMessage method with no attachments
     */
    public void testVerifySoapMessageNoAttachment() throws Exception {
        System.out.println("testVerifySoapMessageNoAttachment");                
        
        //Use new CredentialInfo because it will be set by verifySOAPMessage().
        CredentialInfo verifyCredentialInfo = credentialInfo; //new CredentialInfo();
        // Verify untouched msg
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();
        assertTrue(ssu.verifySoapMessage(soapMessage, verifyCredentialInfo));

        // Change message body and verify 
        try {
            //Use new CredentialInfo because it will be set by verifySOAPMessage().
            verifyCredentialInfo = new CredentialInfo();
            soapMessage.getSOAPBody().addChildElement("unsigned-element");
            soapMessage.saveChanges();
            assertTrue(!ssu.verifySoapMessage(soapMessage, verifyCredentialInfo));
            //TODO why does this not fail("Tampered message verification did not fail.");
        } catch (JAXRException e) {
            assertTrue(e.getCause() instanceof WssSoapFaultException);
            //TODO: verify fault code?!
        }
    }    

    /**
     * Test of signSoapMessage method with an attachment
     */
    public void testSignSoapMessageWithAttachment() throws Exception {
        System.out.println("testSignSoapMessageWithAttachment");
        
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();
        CredentialInfo credentialInfo = getCredentialInfo();
        
        InputStream soapStream = new ByteArrayInputStream(
                TEST_SOAP_MESSAGE_STRING.getBytes("utf-8"));
        MessageFactory factory = MessageFactory.newInstance();
        soapMessageWA = factory.createMessage();
        SOAPPart soapPart = soapMessageWA.getSOAPPart();
        soapPart.setContent(new StreamSource(soapStream));

        // Construct AttachmentPart and add it to SOAPMessage
        AttachmentPart ap = soapMessageWA.createAttachmentPart("test", "text/plain");
        ap.setContentId("<foo>");
        soapMessageWA.addAttachmentPart(ap);
        
        soapMessageWA = ssu.signSoapMessage(soapMessageWA, credentialInfo);
        
        soapMessageWA.writeTo(System.out);
        System.out.println();
    }
    
    /**
     * Test of verifySoapMessage method with an attachment
     */
    public void testVerifySoapMessageWithAttachment() throws Exception {
        System.out.println("testVerifySoapMessageWithAttachment");
        
        //Use new CredentialInfo because it will be set by verifySOAPMessage().
        CredentialInfo verifyCredentialInfo = credentialInfo; //new CredentialInfo();
        
        // Verify untouched msg
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();
        assertTrue(ssu.verifySoapMessage(soapMessageWA, verifyCredentialInfo));

        // Change message attachments and verify 
        // Construct AttachmentPart and add it to SOAPMessage
        try {
            AttachmentPart ap = soapMessageWA.createAttachmentPart("tampered test", "text/plain");
            ap.setContentId("<foo>");
            soapMessageWA.addAttachmentPart(ap);
            verifyCredentialInfo = new CredentialInfo();
            assertTrue(!ssu.verifySoapMessage(soapMessageWA, verifyCredentialInfo));
            //TODO why does this not fail("Tampered message verification did not fail.");
        } catch (JAXRException e) {
            assertTrue(e.getCause() instanceof WssSoapFaultException);
            //TODO: verify fault code?!
        }

        // Change message attachments and verify 
        // Construct AttachmentPart and add it to SOAPMessage
        try {
            soapMessageWA.removeAllAttachments();
            AttachmentPart ap = soapMessageWA.createAttachmentPart("tampered test", "text/plain");
            ap.setContentId("<foo>");
            soapMessageWA.addAttachmentPart(ap);
            verifyCredentialInfo = new CredentialInfo();
            assertTrue(!ssu.verifySoapMessage(soapMessageWA, verifyCredentialInfo));
            //TODO why does this not fail("Tampered message verification must fail.");
        } catch (JAXRException e) {
            assertTrue(e.getCause() instanceof WssSoapFaultException);
            //TODO: verify fault code?!
        }
        
        // Change message attachments and verify 
        try {
            soapMessageWA.removeAllAttachments();
            verifyCredentialInfo = new CredentialInfo();
            assertTrue(!ssu.verifySoapMessage(soapMessageWA, verifyCredentialInfo));
            //TODO why does this not fail("Tampered message verification must fail.");
        } catch (JAXRException e) {
            assertTrue(e.getCause() instanceof WssSoapFaultException);
            //TODO: verify fault code?!
        }
    }

    /**
     * Test of verifySoapMessage method, of class org.freebxml.omar.common.security.SoapSecurityUtil.
     */
    public void testVerifySoapMessage_NoSignature() throws Exception {
        System.out.println("testVerifySoapMessage_NoSignature");

        // Create unsigned message
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();
        //Use new CredentialInfo because it will be set by verifySOAPMessage().
        CredentialInfo verifyCredentialInfo = new CredentialInfo();
        
        InputStream soapStream = new ByteArrayInputStream(
                TEST_SOAP_MESSAGE_STRING.getBytes("utf-8"));
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage soapMessage = factory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        soapPart.setContent(new StreamSource(soapStream));
        
        // Verify msg
        assertTrue("verify should return false when no SecurityHeader present",
                !ssu.verifySoapMessage(soapMessage, verifyCredentialInfo));
    }
    
    /**
     * Test of handle method, of class org.freebxml.omar.common.security.SoapSecurityUtil.
     */
    public void testHandle() {
        System.out.println("testHandle");
        
        //NOOP
    }
    
    
    /** Convenience method to get test credentials. */
    private CredentialInfo getCredentialInfo() throws Exception {
        if (this.credentialInfo == null) {
            this.credentialInfo = createCredentialInfo();
        }
        return this.credentialInfo;
    }

    /** Convenience method to create test credentials. */
    private CredentialInfo createCredentialInfo() throws Exception {
        // xws-security SignFilter supports only RSA!
        // Generating 512 bit RSA key pair and self-signed certificate (SHA1WithRSA) for TestUser-DN"));
        CertAndKeyGen certandkeygen = new CertAndKeyGen("RSA", "SHA1WithRSA");
        X500Name x500name = new X500Name("Tester", "Test Unit", "OMAR", "JKL", "KS", "FI");
        certandkeygen.generate(512);
        PrivateKey privateKey = certandkeygen.getPrivateKey();
        X509Certificate[] ax509cert = new X509Certificate[1];
        ax509cert[0] = certandkeygen.getSelfCertificate(x500name,  90 * 24 * 60 * 60);

        //Create the CredentialInfo wrapper.
        CredentialInfo credentialInfo = new CredentialInfo("TestUserAlias",
            ax509cert[0], ax509cert, privateKey);
        return credentialInfo;
    }    
}