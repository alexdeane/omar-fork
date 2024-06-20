/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/util/CertificateUtil.java,v 1.9 2006/09/21 10:08:58 vikram_blr Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.RegistryResponseHolder;
import org.freebxml.omar.common.security.KeyTool;
import org.freebxml.omar.common.security.KeystoreMover;
import org.freebxml.omar.client.xml.registry.infomodel.PersonNameImpl;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.User;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequest;

import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Provides methods to check that jaxr-ebxml keystore properties are set
 * correctly and keystore file exists.
 *
 * @author Fabian Ritzmann
 */
public class CertificateUtil {
    private static final Log log = LogFactory.getLog(CertificateUtil.class);
    private static BindingUtility bu = BindingUtility.getInstance();

    
    /*
     * Return true if certificate exists in client keystore, false if not.
     *
     */
    public static boolean certificateExists(String alias, char[] storePass)
        throws JAXRException {
        boolean exists = false;

        try {
            File keystoreFile = KeystoreUtil.getKeystoreFile();
            KeystoreUtil.createKeystoreDirectory(keystoreFile);
            String keystoreType = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.storetype", "JKS");
            

            String[] args = {
                "-list", "-alias", alias, "-keystore",
                keystoreFile.getAbsolutePath(), "-storepass",
                new String(storePass), "-storetype", keystoreType
            };

            KeyTool keytool = new KeyTool();
            keytool.run(args, System.out);

            exists = true;

            log.debug(JAXRResourceBundle.getInstance().
		      getString("message.AliasExistsInKeyStore",
				new Object[]{alias,
					     keystoreFile.getAbsolutePath()}));
        } catch (Exception e) {
            //Cert does not exists.
            log.debug(e);
        }

        return exists;
    }

    /** Generate a self signed certificate and store it in the keystore.
      *
      * @param userRegInfo
      * @throws JAXRException
      */
    public static void generateRegistryIssuedCertificate(UserRegistrationInfo userRegInfo) throws JAXRException {
        User user = userRegInfo.getUser();
        LifeCycleManager lcm = user.getLifeCycleManager();
        String dname = getDNameFromUser(userRegInfo);
        File keystoreFile = KeystoreUtil.getKeystoreFile();
        KeystoreUtil.createKeystoreDirectory(keystoreFile);
        String keystoreType = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.storetype", "JKS");
        String storePassStr = new String(userRegInfo.getStorePassword());
        String keyPassStr = new String(userRegInfo.getKeyPassword());
        String alias = userRegInfo.getAlias();
        String keyAlg = "RSA"; //XWSS does not support DSA which is default is KeyTool. Hmm. Weird.
        
        String[] args = {
            "-genkey", "-keyAlg", keyAlg, "-alias", alias, "-keypass", keyPassStr,
            "-keystore", keystoreFile.getAbsolutePath(), "-storepass",
            storePassStr, "-storetype", keystoreType, "-dname", dname
        };

        try {
            KeyTool keytool = new KeyTool();
            keytool.run(args, System.out);

            //Now load the KeyStore and get the cert
            FileInputStream fis = new java.io.FileInputStream(keystoreFile);

            KeyStore keyStore = KeyStore.getInstance(keystoreType);
            keyStore.load(fis, storePassStr.toCharArray());
            fis.close();

            X509Certificate cert = (X509Certificate)keyStore.getCertificate(alias);
            Certificate[] certChain = getCertificateSignedByRegistry(lcm, cert);
            Key key = keyStore.getKey(alias, userRegInfo.getKeyPassword());

            //Now overwrite original cert with signed cert
            keyStore.deleteEntry(alias);
            
            //keyStore.setCertificateEntry(alias, cert);
            keyStore.setKeyEntry(alias, key, userRegInfo.getKeyPassword(), certChain);
            FileOutputStream fos = new java.io.FileOutputStream(keystoreFile);
            keyStore.store(fos, storePassStr.toCharArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.CertGenFailed"), e);
        }
        
        log.debug(JAXRResourceBundle.getInstance().
		  getString("message.StoredUserInKeyStore",
			    new Object[]{alias,
					 keystoreFile.getAbsolutePath()}));

        try {
            //Export registry issued cert to certFile so it can be available for import into a web browser for SSL access to registry
            exportRegistryIssuedCert(userRegInfo);
        } catch (Exception e) {
            String msg = JAXRResourceBundle.getInstance().getString("message.UnableToExportCertificateSeeNextExceptionNoteThatThisFeatureRequiresUseOfJDK5");
            log.warn(msg, e);
            //Do not throw exception as user reg can be done despite not exporting the p12 file for the web browser.
        }
    }
    
    private static Certificate[] getCertificateSignedByRegistry(LifeCycleManager lcm, X509Certificate inCert) throws JAXRException {
        Certificate[] certChain = new Certificate[2];
        
        try {
            //Save cert in a temporary keystore file which is sent as repository item to server so it can be signed
            KeyStore tmpKeystore = KeyStore.getInstance("JKS");
            tmpKeystore.load(null, bu.FREEBXML_REGISTRY_KS_PASS_REQ.toCharArray());

            tmpKeystore.setCertificateEntry(bu.FREEBXML_REGISTRY_USERCERT_ALIAS_REQ, inCert);
            File repositoryItemFile = File.	createTempFile(".omar-ca-req", ".jks");
            repositoryItemFile.deleteOnExit();
            FileOutputStream fos = new java.io.FileOutputStream(repositoryItemFile);
            tmpKeystore.store(fos, bu.FREEBXML_REGISTRY_KS_PASS_REQ.toCharArray());
            fos.flush();
            fos.close();        

            //Now have server sign the cert using extensionRequest
            javax.activation.DataHandler repositoryItem = 
                    new DataHandler(new FileDataSource(repositoryItemFile));
            String id = org.freebxml.omar.common.Utility.getInstance().createId();
            HashMap idToRepositoryItemsMap = new HashMap();
            idToRepositoryItemsMap.put(id, repositoryItem);

            HashMap slotsMap = new HashMap();
            slotsMap.put(BindingUtility.FREEBXML_REGISTRY_PROTOCOL_SIGNCERT, "true");

            RegistryRequest req = bu.rsFac.createRegistryRequest();
            bu.addSlotsToRequest(req, slotsMap);


            RegistryResponseHolder respHolder = ((org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl)lcm).extensionRequest(req, idToRepositoryItemsMap);
            DataHandler responseRepositoryItem = (DataHandler)respHolder.getAttachmentsMap().get(id);

            InputStream is = responseRepositoryItem.getInputStream();            
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(is, bu.FREEBXML_REGISTRY_KS_PASS_RESP.toCharArray());            
            is.close();            

            certChain[0] = keyStore.getCertificate(bu.FREEBXML_REGISTRY_USERCERT_ALIAS_RESP);
            if (certChain[0] == null) {
                throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.CannotFindUserCert"));
            }
            certChain[1] = keyStore.getCertificate(bu.FREEBXML_REGISTRY_CACERT_ALIAS);
            if (certChain[1] == null) {
                throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.CannotFindCARootCert"));
            }
        } catch (Exception e) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.CertSignFailed"), e);
        }
        
        return certChain;
    }
        
    public static void importCAIssuedCert(UserRegistrationInfo userRegInfo) throws JAXRException {
        try {
            String storePassStr = new String(userRegInfo.getStorePassword());
            String keyPassStr = new String(userRegInfo.getKeyPassword());
            File keystoreFile = KeystoreUtil.getKeystoreFile();
            String alias = userRegInfo.getAlias();

            //Import CA issued cert to certFile into client keystore
            KeystoreMover ksm = new KeystoreMover();
            ksm.move("PKCS12", userRegInfo.getP12File(), keyPassStr, null, keyPassStr, 
                    "JKS", keystoreFile.getAbsolutePath(), storePassStr, alias, keyPassStr);  
        } catch (Exception e) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.ImportCAIssuedCertFailed"), e);
        }
    }
    
    public static void exportRegistryIssuedCert(UserRegistrationInfo userRegInfo) throws JAXRException {
        try {
            String storePassStr = new String(userRegInfo.getStorePassword());
            String keyPassStr = new String(userRegInfo.getKeyPassword());
            File keystoreFile = KeystoreUtil.getKeystoreFile();
            String alias = userRegInfo.getAlias();
            
            //Delete existing p12 file if any otherwise new cert will not be written
            File p12File = new File(userRegInfo.getP12File());
            if (p12File.exists()) {
                p12File.delete();
            }
            
            //Export registry issued cert to certFile so it can be available for import into a web browser for SSL access to registry
            KeystoreMover ksm = new KeystoreMover();
            ksm.move("JKS", keystoreFile.getAbsolutePath(), storePassStr, alias, keyPassStr, 
                    "PKCS12", userRegInfo.getP12File(), keyPassStr, alias, keyPassStr);
        } catch (Exception e) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.ExportRegistryIssuedCertFailed"), e);
        }
    }
    

    /** Remove an alias from the keystore.
      * <p>
      * Currently, this is only used to "backout" a generated key when self
      * registration fails.
      */
    public static void removeCertificate(String alias, char[] storePass)
        throws JAXRException {
        
        try {
            File keystoreFile = KeystoreUtil.getKeystoreFile();
            String keystoreType = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.storetype", "JKS");

            String[] args = {
                "-delete", "-alias", alias, "-keystore",
                keystoreFile.getAbsolutePath(), "-storepass", new String(storePass),
                "-storetype", keystoreType, "-validity", "365"
            };
            KeyTool keytool = new KeyTool();
            keytool.run(args, System.out);
            log.debug(JAXRResourceBundle.getInstance().
		      getString("message.RemovedUserFromKeyStore",
				new Object[]{alias,
					     keystoreFile.getAbsolutePath()}));
        } catch (Exception e) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.RemoveCertFailed"), e);            
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param user DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    private static String getDNameFromUser(UserRegistrationInfo userRegInfo) throws JAXRException {
        User user = userRegInfo.getUser();
        String dname = "CN=";

        LifeCycleManager lcm = user.getLifeCycleManager();
        Collection addresses = user.getPostalAddresses();
        PostalAddress address;
        PersonName personName = user.getPersonName();

        //CN=Farrukh Najmi, OU=freebxml.org, O=ebxmlrr, L=Islamabad, ST=Punjab, C=PK
        if (personName == null) {
            personName = lcm.createPersonName("firstName", "middleName",
                    "lastName");
        }

        if ((addresses != null) && (addresses.size() > 0)) {
            address = (PostalAddress) (addresses.iterator().next());
        } else {
            address = lcm.createPostalAddress("number", "street", "city",
                    "state", "country", "postalCode", "Office");
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
            country = "US";
        }

        if (country.length() > 0) {
            country = country.substring(0, 2);
        }
        
        String organization = userRegInfo.getOrganization();
        
        if (organization == null || organization.trim().length() == 0) {
            organization = "Unknown";
        }
        
        String unit = userRegInfo.getOrganizationUnit();
        
        if (unit == null || unit.trim().length() == 0) {
            unit = "Unknown";
        }

        //Escape "," in formattedName per section 2.4 of RFC 2253. \u002c is hex code for ","
        String formattedName = ((PersonNameImpl)personName).getFormattedName();
        formattedName = formattedName.replaceAll(",", "\\\\,");
        
        dname += (formattedName +
        ", OU=" + unit + ", O="+organization+", L=" + city +
        ", ST=" + state + ", C=" + country);
        
        return dname;
    }
    
}
