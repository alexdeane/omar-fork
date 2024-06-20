/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/KeyToolStripped.java,v 1.2 2005/07/01 03:44:28 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.io.File;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class KeyToolStripped {

    private static Log log = LogFactory.getLog("org.freebxml.omar.common.KeyToolStripped");    
    
    public KeyToolStripped() {
    }
    
    public static void dumpProviderInfo(String msg) {
        Provider[] providers = Security.getProviders();
        System.err.println(msg);
        for (int i = 0; i < providers.length; i++) {

            Provider provider = providers[i];
            System.err.println("Provider name: " + provider.getName());
            //System.err.println("Provider class: " + provider.getClass().getName());
            //System.err.println("Provider information: " + provider.getInfo());
            //System.err.println("Provider version: " + provider.getVersion());
            Set entries = provider.entrySet();
            Iterator iterator = entries.iterator();

            /*
            while (iterator.hasNext()) {
                System.err.println("    Property entry: " + iterator.next());
            }
             */
        }        
    }        

    /** 
      * Generate a public/private key pair.
      *
      * @throws Exception
      */    
    public static void generateKeyPair(KeyStore keyStore, char[] storePass, String alias, char[] keyPass, String dname, String keyAlg, int validity)
        throws Exception 
    {
        int keySize = 1024;
        if (keyStore.containsAlias(alias)) {
            MessageFormat messageformat = new MessageFormat(
                        "Key pair not generated, alias <alias> already exists");
            Object[] aobj = { alias };
            throw new Exception(messageformat.format(((Object) (aobj))));
        }

        String sigAlg = null;
        
        if (keyAlg.equalsIgnoreCase("DSA")) {
            sigAlg = "SHA1WithDSA";
        } else if (keyAlg.equalsIgnoreCase("RSA")) {
            sigAlg = "MD5WithRSA";
        } else {
            throw new Exception("Cannot derive signature algorithm");
        }

        //Must specify provider "SunRsaSign" otherwise it gets some weird NSS specific provider
        //when running in AppServer EE.
        CertAndKeyGen certandkeygen = new CertAndKeyGen(keyAlg, sigAlg);
        X500Name x500name;

        if (dname == null) {
            throw new Exception("Key pair not generated, dname is null.");
        } else {
            x500name = new X500Name(dname);
        }

        certandkeygen.generate(keySize);

        PrivateKey privatekey = certandkeygen.getPrivateKey();
        X509Certificate[] ax509certificate = new X509Certificate[1];
        ax509certificate[0] = certandkeygen.getSelfCertificate(x500name,
                validity * 24 * 60 * 60);


        keyStore.setKeyEntry(alias, privatekey, keyPass, ax509certificate);
    }

}
