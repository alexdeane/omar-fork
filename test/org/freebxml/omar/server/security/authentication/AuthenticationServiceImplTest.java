/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/security/authentication/AuthenticationServiceImplTest.java,v 1.12 2007/05/25 15:05:49 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.security.authentication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;
import junit.framework.TestSuite;

import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.exceptions.UserRegistrationException;
//import org.freebxml.omar.common.security.X509KeySelector;
import org.freebxml.omar.server.common.ServerTest;
import org.oasis.ebxml.registry.bindings.rim.User;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/**
 *
 * @author Diego Ballve
 */
public class AuthenticationServiceImplTest extends ServerTest {
        
    static final String aliasForTestRegisterUserCertificateWithSameCertTwice = "urn:freebxml:registry:test:user:testRegisterUserCertificateWithSameCertTwice";

    public AuthenticationServiceImplTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AuthenticationServiceImplTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new AuthenticationServiceImplTest("testGetTrustAnchorsKeyStore"));
        return suite;
    }
    
    /**
     * Test of getTrustAnchorsKeyStore method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testGetTrustAnchorsKeyStore() {
        System.out.println("testGetTrustAnchorsKeyStore");
        
        // TODO add your test code below by replacing the default call to fail.
        //fail("The test case is empty.");
    }
    
    /**
     * Test of getKeyStore method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testGetKeyStore() throws RegistryException {
        System.out.println("testGetKeyStore");

        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        auth.deleteUserCertificate(aliasForTestRegisterUserCertificateWithSameCertTwice);
        assertNotNull(auth.getKeyStore());
    }
    
    /**
     * Test of getPrivateKey method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testGetPrivateKey() throws RegistryException {
        System.out.println("testGetPrivateKey");
        
        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        PrivateKey pk = auth.getPrivateKey(auth.ALIAS_REGISTRY_OPERATOR, auth.ALIAS_REGISTRY_OPERATOR);
        assertNotNull(pk);
        pk = auth.getPrivateKey(auth.ALIAS_REGISTRY_GUEST, auth.ALIAS_REGISTRY_GUEST);
        assertNotNull(pk);
    }
    
    /**
     * Test of getCertificate method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testGetCertificate() throws RegistryException {
        System.out.println("testGetCertificate");
        
        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        assertNotNull(auth.getCertificate(auth.ALIAS_REGISTRY_OPERATOR));
        try {
            assertNull(auth.getCertificate("alias_not_in_the_keystore"));
            fail("Exception expected when certificate not found");
        } catch (RegistryException e) {}
    }
    
    /**
     * Test of getCertificateChain method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testGetCertificateChain() throws RegistryException {
        System.out.println("testGetCertificateChain");
        
        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        Certificate[] c1 = auth.getCertificateChain(auth.ALIAS_REGISTRY_OPERATOR);
        assertNotNull(c1);
        assertTrue("Certificate chain is empty", c1.length > 0);
        Certificate[] c2 = auth.getCertificateChain("alias_not_in_the_keystore");
        assertNull(c2);
    }
    
    /**
     * Test of getInstance method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testGetInstance() {
        System.out.println("testGetInstance");

        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        assertNotNull(auth);
    }
    
    /**
     * Test of getKeyStoreFileName method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testGetKeyStoreFileName() throws RegistryException {
        System.out.println("testGetKeyStoreFileName");
        
        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        assertNotNull(auth.getKeyStoreFileName());
    }
    
    /**
     * Test of getKeyStorePassword method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testGetKeyStorePassword() throws RegistryException {
        System.out.println("testGetKeyStorePassword");
        
        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        assertNotNull(auth.getKeyStorePassword());
    }
    
    /**
     * Test of validateCertificate method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testValidateCertificate() {
        System.out.println("testValidateCertificate");
        
        // TODO add your test code below by replacing the default call to fail.
        //fail("The test case is empty.");
    }
    
    /**
     * Test of getAliasFromUser method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testGetAliasFromUser() throws RegistryException {
        System.out.println("testGetAliasFromUser");
        
        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        String alias1 = auth.getAliasFromUser(auth.registryOperator);
        assertEquals(auth.ALIAS_REGISTRY_OPERATOR, alias1);
        String alias2 = auth.getAliasFromUser(auth.registryGuest);
        assertEquals(auth.ALIAS_REGISTRY_GUEST, alias2);
        assertTrue(!alias1.equals(alias2));
    }
    
    /**
     * Test of getCertificateFromUser method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testGetCertificateFromUser() throws RegistryException {
        System.out.println("testGetCertificateFromUser");
        
        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        Certificate cert1 = auth.getCertificateFromUser(auth.registryOperator);
        assertNotNull(cert1);
        Certificate cert2 = auth.getCertificateFromUser(auth.registryGuest);
        assertNotNull(cert2);
        assertTrue(!cert1.equals(cert2));
    }
    
    /**
     * Test of getUserFromAlias method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testGetUserFromAlias() throws RegistryException {
        System.out.println("testGetUserFromAlias");
        
        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        User expectedUser = auth.registryOperator;
        UserType returnedUser = auth.getUserFromAlias(auth.ALIAS_REGISTRY_OPERATOR);
        assertEquals(expectedUser.getId(), returnedUser.getId());

        expectedUser = auth.registryGuest;
        returnedUser = auth.getUserFromAlias(auth.ALIAS_REGISTRY_GUEST);
        assertEquals(expectedUser.getId(), returnedUser.getId());        
    }
    
    /**
     * Test of isRegistryAdministrator method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testIsRegistryAdministrator() throws RegistryException {
        System.out.println("testIsRegistryAdministrator");
        
        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        UserType returnedUser = auth.getUserFromAlias(auth.ALIAS_REGISTRY_OPERATOR);
        assertTrue(auth.hasRegistryAdministratorRole(returnedUser));

        returnedUser = auth.getUserFromAlias(auth.ALIAS_REGISTRY_GUEST);
        assertTrue(!auth.hasRegistryAdministratorRole(returnedUser));
    }
    
//    /**
//     * Test of getUserFromXMLSignature method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
//     */
//    public void testGetUserFromXMLSignature() throws Exception {
//        System.out.println("testGetUserFromXMLSignature");
//
//        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
//        SecurityUtil su = SecurityUtil.getInstance();
//        
//        // Get test credentials
//        String alias = auth.ALIAS_REGISTRY_GUEST;
//        User expectedUser = auth.registryGuest;
//        Certificate[] certs = auth.getCertificateChain(alias);
//        PrivateKey privateKey = auth.getPrivateKey(alias, alias);
//        String signingAlgo = privateKey.getAlgorithm();
//        if (signingAlgo.equalsIgnoreCase("DSA")) {
//            signingAlgo = SignatureMethod.DSA_SHA1;
//        } else if (signingAlgo.equalsIgnoreCase("RSA")) {
//            signingAlgo = SignatureMethod.RSA_SHA1;
//        } else if (signingAlgo.equalsIgnoreCase("RSA")) {
//            signingAlgo = SignatureMethod.HMAC_SHA1;
//        } else {
//            throw new NoSuchAlgorithmException("Algorithm not supported");
//        }
//
//        // produce a signed document, reusing SecurityUtil code.
//        StringBuffer soapText = new StringBuffer("<soap-env:Envelope");
//        soapText.append(" xmlns:soap-env=\"http://schemas.xmlsoap.org/soap/envelope/\">");
//        soapText.append("<soap-env:Header/>");
//        soapText.append("<soap-env:Body>");
//        soapText.append("</soap-env:Body>");
//        soapText.append("</soap-env:Envelope>");
//       
//        InputStream soapStream = 
//            new ByteArrayInputStream(soapText.toString().getBytes("utf-8"));
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        su.signSOAPMessage(soapStream, baos, alias, privateKey, certs, signingAlgo);
//            soapStream = new ByteArrayInputStream(baos.toByteArray());
//        // SecurityUtil requires a SOAPMessage
//        MessageFactory factory = MessageFactory.newInstance();
//        SOAPMessage msg = factory.createMessage();
//        SOAPPart soapPart = msg.getSOAPPart();
//        soapPart.setContent(new StreamSource(soapStream));        
//        XMLSignature xmlSignature = su.verifySOAPMessage(msg, new X509KeySelector(
//                    AuthenticationServiceImpl.getInstance().getKeyStore()));
//        
//        // Finally tests getUserFromXMLSignature
//        UserType returnedUser = auth.getUserFromXMLSignature(xmlSignature);
//        assertEquals(expectedUser.getId(), returnedUser.getId());
//    }
    
    /**
     * Test of getUserFromCertificate method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testGetUserFromCertificate() throws RegistryException {
        System.out.println("testGetUserFromCertificate");

        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        Certificate cert = auth.getCertificate(auth.ALIAS_REGISTRY_OPERATOR);
        assertNotNull(cert);
        User expectedUser = auth.registryOperator;
        UserType returnedUser = auth.getUserFromCertificate((X509Certificate)cert);
        assertEquals(expectedUser.getId(), returnedUser.getId());

        cert = auth.getCertificate(auth.ALIAS_REGISTRY_GUEST);
        assertNotNull(cert);
        expectedUser = auth.registryGuest;
        returnedUser = auth.getUserFromCertificate((X509Certificate)cert);
        assertEquals(expectedUser.getId(), returnedUser.getId());
    }
    
    /**
     * Test of registerUserCertificate method, of class org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.
     */
    public void testRegisterUserCertificateWithSameCertTwice() throws Exception {
        AuthenticationServiceImpl auth = AuthenticationServiceImpl.getInstance();
        Certificate cert = auth.getCertificate(auth.ALIAS_REGISTRY_OPERATOR);
        assertNotNull(cert);
        
        try {
            auth.deleteUserCertificate(aliasForTestRegisterUserCertificateWithSameCertTwice);
            auth.registerUserCertificate(aliasForTestRegisterUserCertificateWithSameCertTwice, (X509Certificate)cert);
            auth.deleteUserCertificate(aliasForTestRegisterUserCertificateWithSameCertTwice);
            fail("Did not get UserRegistrationException when registering with same cert twice.");
        } catch (UserRegistrationException e) {
            //Expected.
        }
    }
    
}
