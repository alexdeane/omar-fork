/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/util/SecurityUtil.java,v 1.14 2007/03/23 18:59:36 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.security.auth.x500.X500PrivateCredential;
import javax.xml.registry.JAXRException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Provides various utility methods to facilitate certificate based authentication
 * between JAXR Client and JAXR Provider.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class SecurityUtil {
    
    private static final Log log = LogFactory.getLog(SecurityUtil.class);
    private static final SecurityUtil INSTANCE = new SecurityUtil();
    private Properties aliasTable;
    private KeyStore keyStore;

    public static SecurityUtil getInstance() {
        return INSTANCE;
    }

    private Properties loadAliasTable() throws JAXRException {
        String jaxrHome = ProviderProperties.getInstance().getProperty("jaxr-ebxml.home");

        if ((jaxrHome == null) || (jaxrHome.length() == 0)) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.undefined.Property"));
        }

        Properties aliasTable = new Properties();
        File aliasFile = new File(jaxrHome, "security/alias.properties");

        if (aliasFile.exists()) {
            try {
                aliasTable.load(new BufferedInputStream(
                        new FileInputStream(aliasFile)));
            } catch (IOException x) {
                log.error(x);
                throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.unexpected.IOException"));
            }
        }

        return aliasTable;
    }

    private KeyStore loadKeyStore() throws JAXRException {
        String storepass = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.storepass");

        try {
            keyStore = KeyStore.getInstance(ProviderProperties.getInstance()
                                                              .getProperty("jaxr-ebxml.security.storetype"));
        } catch (KeyStoreException x) {
            throw new JAXRException(x);
        }

        File keyStoreFile = KeystoreUtil.getKeystoreFile();

        if (!keyStoreFile.exists()) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.no.keystore.file",new Object[] {keyStoreFile.toString()}));
        }

        try {
            InputStream keyIS = new BufferedInputStream(new FileInputStream(
                        keyStoreFile));
            keyStore.load(keyIS, storepass.toCharArray());
            log.debug("Keystore loaded from '" + keyStoreFile.getCanonicalPath() + "'");
        } catch (IOException x) {
            throw new JAXRException(x);
        } catch (GeneralSecurityException x) {
            throw new JAXRException(x);
        }

        return keyStore;
    }

    public String aliasToObjectId(String alias) throws JAXRException {
        if (aliasTable == null) {
            aliasTable = loadAliasTable();
        }

        return aliasTable.getProperty(alias);
    }

    public KeyStore getKeyStore() throws JAXRException {
        if (keyStore == null) {
            keyStore = loadKeyStore();
        }

        return keyStore;
    }

    public X500PrivateCredential aliasToX500PrivateCredential(String alias)
    throws JAXRException {
        String keypass = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.keypass");
        return aliasToX500PrivateCredential(alias, keypass);
    }
    
    public String x500PrivateCredentialToAlias(X500PrivateCredential credential) {
        return credential.getAlias();
    }
    
    public X500PrivateCredential aliasToX500PrivateCredential(String alias, String keypass)
    throws JAXRException {
        getKeyStore();
        try {
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);

            if (cert == null) {
                //It may be that keystore h snot been reloaded since it was updated on disk.
                //Retry cert lookup after reloading keyStore.
                keyStore = loadKeyStore();
                cert = (X509Certificate) keyStore.getCertificate(alias);
                
                if (cert == null) {
                    throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.failed.entry.alias.keystore", new Object[] {alias, KeystoreUtil.getKeystoreFile().getAbsolutePath()}));
                }
            }

            // if keypass has not been provided, use property value
            if (keypass == null) {
                keypass = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.keypass");
                // if still null, use alias
                if (keypass == null) {
                    keypass = alias;
                }
            }
            
            // keytool utility requires a six character minimum password.
            // pad passwords with < six chars
            for (int i = 1; 0 < 6 - keypass.length(); i++) {
                keypass += String.valueOf(i);
            }
            
            if (log.isTraceEnabled()) {
                StringBuffer sb = new StringBuffer("Retrieving key entry with alias '");
                sb.append(alias).append("' with keypass '");
                for (int i = 0; i < keypass.length(); i++) {
                    sb.append("*");
                }
                sb.append("' from keystore loaded from '");
                sb.append(KeystoreUtil.getKeystoreFile().getAbsolutePath());
                sb.append("'.");
                log.trace(sb.toString());
            }
            
            PrivateKey privateKey = (PrivateKey)keyStore.getKey(alias, keypass.toCharArray());
                    
            return new X500PrivateCredential(cert, privateKey, alias);
        } catch (GeneralSecurityException x) {
            throw new JAXRException(x);
        }
    }

    public Certificate[] getCertificateChain(
        java.security.cert.X509Certificate cert) throws JAXRException {
        Certificate[] certChain = null;
        getKeyStore();

        try {
            String alias = keyStore.getCertificateAlias(cert);
            
            // Check if the alias is null and don't get the certificate chain
            // if it is as this will cause an NPE. This may be a bug in
            // the sun implementation of the KeyStore class 
            // (sun.security.provider.JavaKeyStore) as the javadoc indicates that
            // the method should return null if the alias is not found.
            // Under normal operation, the alias should never be null, but
            // it is possible to set credentials on the connection that are not
            // in the jaxr client keystore, and this works fine except that 
            // the getCertificateChain() method throws an NPE.
            if (alias != null) {
                certChain = keyStore.getCertificateChain(alias);
            }
            if (certChain == null) {
                certChain = new Certificate[1];
                certChain[0] = cert;
            }
        } catch (KeyStoreException x) {
            throw new JAXRException(x);
        }

        return certChain;
    }

    public void addAlias(String alias, String objId) {
        aliasTable.setProperty(alias, objId);
    }
}
