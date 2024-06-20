/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/security/authentication/CertificateAuthority.java,v 1.6 2006/08/24 20:42:36 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.security.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.security.auth.x500.X500Principal;
import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.RegistryResponseHolder;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.RepositoryItemImpl;
import org.freebxml.omar.common.exceptions.InvalidContentException;
import org.freebxml.omar.common.exceptions.MissingRepositoryItemException;
import org.freebxml.omar.common.security.KeyTool;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.rim.PersonNameType;
import org.oasis.ebxml.registry.bindings.rim.PostalAddressType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequest;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;

import sun.security.x509.X509CertInfo;
import sun.security.x509.X509CertImpl;
import sun.security.x509.*;


/**
 * CA that generates user certs signed by the RegistryOperator private key
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class CertificateAuthority {
    /*# private CertificateAuthority _certificateAuthority; */
    private static CertificateAuthority instance = null;
    private static final Log log = LogFactory.getLog(CertificateAuthority.class);

    private static AuthenticationServiceImpl ac = null;
    private static BindingUtility bu = BindingUtility.getInstance();

    private Certificate caCertificate = null;
    
    protected CertificateAuthority() {
    }
    
    public synchronized static CertificateAuthority getInstance() {
        if (instance == null) {
            instance = new CertificateAuthority();
            ac = AuthenticationServiceImpl.getInstance();
        }

        return instance;
    }
    
    private Certificate getCACertificate() throws RegistryException {
        if (caCertificate == null) {
            caCertificate = ac.getCertificate(ac.ALIAS_REGISTRY_OPERATOR);
        }
        return caCertificate;
    }
    
    /** Extension request to sign specified cert and return the signed cert. */
    public RegistryResponseHolder signCertificateRequest(UserType user,
        RegistryRequestType req, Map idToRepositoryItemMap) throws RegistryException {
        
        RegistryResponseHolder respHolder = null;
        RegistryResponse resp = null;
        ServerRequestContext context = null;

        try {
            context = new ServerRequestContext("CertificateAUthority.signCertificateRequest", req);
            context.setUser(user);
            
            if (idToRepositoryItemMap.keySet().size() == 0) {
                throw new MissingRepositoryItemException(ServerResourceBundle.getInstance().getString("message.KSRepItemNotFound"));
            }
                        
            
            String id = (String)idToRepositoryItemMap.keySet().iterator().next();
            
            Object obj = idToRepositoryItemMap.get(id);
            if (!(obj instanceof RepositoryItem)) {
                throw new InvalidContentException();
            }
            RepositoryItem ri = (RepositoryItem)obj;    //This is the JKS keystore containing cert to be signed            
            
            //Read original cert from keystore
            InputStream is = ri.getDataHandler().getInputStream();            
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(is, bu.FREEBXML_REGISTRY_KS_PASS_REQ.toCharArray());            
            is.close();            
            X509Certificate cert = (X509Certificate)keyStore.getCertificate(bu.FREEBXML_REGISTRY_USERCERT_ALIAS_REQ);
            
            //Sign the cert
            cert = signCertificate(cert);
            
            //Replace cert with signed cert in keystore
            keyStore.deleteEntry(bu.FREEBXML_REGISTRY_USERCERT_ALIAS_REQ);
            keyStore.setCertificateEntry(bu.FREEBXML_REGISTRY_USERCERT_ALIAS_RESP, cert);
            
            //Add CA root cert (RegistryOPerator's cert) to keystore.
            keyStore.setCertificateEntry(bu.FREEBXML_REGISTRY_CACERT_ALIAS, getCACertificate());
            
            Certificate[] certChain = new Certificate[2];
            certChain[0] = cert;
            certChain[1] = getCACertificate();
            validateChain(certChain);
            
            File repositoryItemFile = File.createTempFile(".omar-ca-resp", ".jks");
            repositoryItemFile.deleteOnExit();
            FileOutputStream fos = new java.io.FileOutputStream(repositoryItemFile);
            keyStore.store(fos, bu.FREEBXML_REGISTRY_KS_PASS_RESP.toCharArray());
            fos.flush();
            fos.close();                        

            DataHandler dh = new DataHandler(new FileDataSource(repositoryItemFile));
            RepositoryItemImpl riNew = new RepositoryItemImpl(id, dh);
            
            resp = bu.rsFac.createRegistryResponse();
            resp.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
            
            HashMap respIdToRepositoryItemMap = new HashMap();
            respIdToRepositoryItemMap.put(id, riNew);
            
            respHolder = new RegistryResponseHolder(resp, respIdToRepositoryItemMap);
            
        } catch (RegistryException e) {
            context.rollback();
            throw e;
        } catch (Exception e) {
            context.rollback();
            throw new RegistryException(e);
        }

        context.commit();        
        return respHolder;
    }    
    
    /**
     * Signed specified cert using the private key of RegistryOperator.
     * Warning this uses Sun's JDK impl specific classes and will not work
     * with other JDK impls.
     *
     */
    X509Certificate signCertificate(X509Certificate inCert) throws RegistryException {
        X509CertImpl signedCert = null;
        
        try {
            X509CertImpl caCert = (X509CertImpl)getCACertificate();
            X509CertInfo caCertInfo = new X509CertInfo(caCert.getTBSCertificate());            
            X509CertInfo inCertInfo = new X509CertInfo(inCert.getTBSCertificate());
            
            //Use catch (certs subject name as signed cert's issuer name
            CertificateSubjectName caCertSubjectName = (CertificateSubjectName)caCertInfo.get(X509CertInfo.SUBJECT);
            CertificateIssuerName signedCertIssuerName = new CertificateIssuerName((X500Name)caCertSubjectName.get(CertificateSubjectName.DN_NAME));
            inCertInfo.set(X509CertInfo.ISSUER, signedCertIssuerName);
            signedCert = new X509CertImpl(inCertInfo);
            
            //TODO: Need to remove hardcodeing below and isntead somehow use info.algId => algName
            signedCert.sign(ac.getPrivateKey(ac.ALIAS_REGISTRY_OPERATOR, ac.ALIAS_REGISTRY_OPERATOR), "MD5WithRSA");
        } catch (java.security.GeneralSecurityException e) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ErrorSigningRegIssuedCert"), e);
        } catch (java.io.IOException e) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ErrorSigningRegIssuedCert"), e);
        }
        
        return signedCert;
    }
    
    private boolean validateChain(Certificate[] certChain)
    {
        for (int i = 0; i < certChain.length-1; i++) {
            X500Principal issuerDN =
                ((X509Certificate)certChain[i]).getIssuerX500Principal();
            X500Principal subjectDN =
                ((X509Certificate)certChain[i+1]).getSubjectX500Principal();
            if (!(issuerDN.equals(subjectDN)))
                return false;
        }
        return true;
    }                                                                                   
        
    /** 
      * Generate a registry issued certificate signed by private key of RegistryOperator.
      */
    public X509Certificate generateRegistryIssuedCertificate(String dname) throws RegistryException {
        X509Certificate cert = null;
        
        File ksFile = null;
        try {
            String keystoreFileName = System.getProperty("java.io.tmpdir") + "/omar-temp-ks.jks";
            String keystoreType = "JKS";
            String alias = "ebxmlrr";
            String storePassStr = "ebxmlrr";
            String keyPassStr = "ebxmlrr";
            String keyAlg = "RSA"; //XWSS does not support DSA which is default is KeyTool. Hmm. Weird.

            String[] args = {
                "-genkey", "-keyAlg", keyAlg, "-alias", alias, "-keypass", keyPassStr,
                "-keystore", keystoreFileName, "-storepass", storePassStr,
                "-storetype", keystoreType, "-dname", dname
            };

            KeyTool keytool = new KeyTool();
            keytool.run(args, System.out);
                        
            ksFile = new File(keystoreFileName);
            
            //Now load the KeyStore and get the cert
            FileInputStream fis = new java.io.FileInputStream(ksFile);
            
            KeyStore keyStore = KeyStore.getInstance(keystoreType);
            keyStore.load(fis, storePassStr.toCharArray());
            
            cert = (X509Certificate)keyStore.getCertificate(alias);
            cert = signCertificate(cert);
        
        } catch (Exception e) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.CertGenError"), e);
        } finally {
            if (ksFile != null) {
                try {
                    ksFile.delete();
                } catch (Exception e) {
                    
                }
            }
        }
        
        
        return cert;
    }
    
    /**
     * Gets the DN for specified User object.
     *
     * @param user DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    private static String getDNameFromUser(UserType user) throws RegistryException {
        String dname = "CN=";

        try {
            Collection addresses = user.getAddress();
            PersonNameType personName = user.getPersonName();

            //CN=Farrukh Najmi, OU=freebxml.org, O=ebxmlrr, L=Islamabad, ST=Punjab, C=PK
            if (personName == null) {
                personName = bu.rimFac.createPersonName();
                personName.setFirstName(user.getId());
            }

            PostalAddressType address = null;
            if ((addresses != null) && (addresses.size() > 0)) {
                address = (PostalAddressType) (addresses.iterator().next());
            } else {
                address = bu.rimFac.createPostalAddress();
            }

            String city = address.getCity();

            if ((city == null) || (city.length() == 0)) {
                city = "Unknown";
            }

            String state = address.getStateOrProvince();

            if ((state == null) || (state.length() == 0)) {
                state = "Unknown";
            }

            String country = address.getCountry();

            if ((country == null) || (country.length() == 0)) {
                country = "Unknown";
            }

            if (country.length() > 0) {
                country = country.substring(0, 2);
            }

            dname += (personName.getFirstName() + " " + personName.getMiddleName() +
            " " + personName.getLastName() + ", OU=Unknown, O=Unknown, L=" + city +
            ", ST=" + state + ", C=" + country);
        } catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        }

        return dname;
    }
    
}
