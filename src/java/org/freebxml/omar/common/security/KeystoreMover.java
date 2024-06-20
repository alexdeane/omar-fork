/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/KeystoreMover.java,v 1.9 2006/06/14 16:21:53 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.CommonResourceBundle;
/**
 * Moves cert from source to target keystore. Source MUST be JKS or PKCS12 while
 * target MUST be JKS.
 *
 * Based upon code from http://forums.java.sun.com/thread.jspa?forumID=2&threadID=4210
 *
 * @author jszatmary
 * @author Farrukh S. Najmi
 *
 */
public class KeystoreMover {
    private static final Log log = LogFactory.getLog(KeystoreMover.class);
        
    public KeystoreMover() {
        java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    }
    
    public static void main(String args[]) throws Throwable {
        
        KeystoreMover ksm = new KeystoreMover();
        
        String sourceKeystoreType = "PKCS12";
        String sourceKeystorePath = null;
        String sourceKeystorePassword = null;
        String sourceAlias = null;
        String sourceKeyPassword = null;
        
        String destinationKeystoreType = "JKS";
        String destinationKeystorePath = null;
        String destinationKeystorePassword = null;
        String destinationAlias = null;
        String destinationKeyPassword = null;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-help")) {
                printUsage();
            } else if (args[i].equalsIgnoreCase("-sourceKeystoreType")) {
                sourceKeystoreType = args[++i];
            } else if (args[i].startsWith("-sourceKeystorePath")) {
                sourceKeystorePath = args[++i];
            } else if (args[i].startsWith("-sourceKeystorePassword")) {
                sourceKeystorePassword = args[++i];
            } else if (args[i].startsWith("-sourceAlias")) {
                sourceAlias = args[++i];
            } else if (args[i].startsWith("-sourceKeyPassword")) {
                sourceKeyPassword = args[++i];
            } else if (args[i].startsWith("-destinationKeystoreType")) {
                destinationKeystoreType = args[++i];
            } else if (args[i].startsWith("-destinationKeystorePath")) {
                destinationKeystorePath = args[++i];
            } else if (args[i].startsWith("-destinationKeystorePassword")) {
                destinationKeystorePassword = args[++i];
            } else if (args[i].startsWith("-destinationAlias")) {
                destinationAlias = args[++i];
            } else if (args[i].startsWith("-destinationKeyPassword")) {
                destinationKeyPassword = args[++i];
            } else {
                log.error(CommonResourceBundle.getInstance().getString("message.UnknownParameterAtPosition", new Object[]{args[i], new Integer(i)}));
                
                if (i > 0) {
                    log.error(CommonResourceBundle.getInstance().getString("message.LastValidParameterWas", new Object[]{args[i - 1]}));
                }
                
                printUsage();
            }
        }
        
        if (sourceKeystorePath == null) {
            log.error(CommonResourceBundle.getInstance().getString("message.ErrorMissingSourceKeystorePath"));
            printUsage();
        }
        if (sourceKeystorePassword == null) {
            log.error(CommonResourceBundle.getInstance().getString("message.ErrorMissingSourceKeystorePassword"));
            printUsage();
        }
        if (destinationKeystorePath == null) {
            log.error(CommonResourceBundle.getInstance().getString("message.ErrorMissingDestinationKeystorePath"));
            printUsage();
        }
        if (destinationKeystorePassword == null) {
            log.error(CommonResourceBundle.getInstance().getString("message.ErrorMissingDestinationKeystorePassword"));
            printUsage();
        }
        
        try {
            ksm.move(sourceKeystoreType, sourceKeystorePath, sourceKeystorePassword, sourceAlias, sourceKeyPassword, 
                destinationKeystoreType, destinationKeystorePath, destinationKeystorePassword, destinationAlias, destinationKeyPassword);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }
    
    private static void printUsage() {
        log.info(CommonResourceBundle.getInstance().getString("message.KeystoreMoverUsage"));
        System.exit(0);        
    }
    
    public void move(
        String sourceKeystoreType,
        String sourceKeystorePath,
        String sourceKeystorePassword,
        String sourceAlias,
        String sourceKeyPassword,
        String destinationKeystoreType,
        String destinationKeystorePath,
        String destinationKeystorePassword,
        String destinationAlias,
        String destinationKeyPassword) throws Exception {
                        
        char[] sourceKeystorePasswordArr = null;            
        if (sourceKeystorePassword != null) {
            sourceKeystorePasswordArr = sourceKeystorePassword.toCharArray();
        }

        char[] sourceKeyPasswordArr = sourceKeystorePasswordArr;
        if (sourceKeyPassword != null) {
            sourceKeyPasswordArr = sourceKeyPassword.toCharArray();
        }

        char[] destinationKeystorePasswordArr = null;            
        if (destinationKeystorePassword != null) {
            destinationKeystorePasswordArr = destinationKeystorePassword.toCharArray();
        }
        
        char[] destinationKeyPasswordArr = destinationKeystorePasswordArr;
        if (destinationKeyPassword != null) {
            destinationKeyPasswordArr = destinationKeyPassword.toCharArray();
        }

        FileInputStream in;                
        
        // --------  Load source keystore to memory ---------
        in = new FileInputStream(sourceKeystorePath);
        KeyStore ksin = KeyStore.getInstance(sourceKeystoreType);                        

        ksin.load(in,sourceKeystorePasswordArr);
        in.close();


        // --------  Load destination keystore initial contents to memory ---------
        KeyStore ksout = KeyStore.getInstance(destinationKeystoreType);

        try {
            in = new FileInputStream(destinationKeystorePath);
            ksout.load(in,destinationKeystorePasswordArr);
        } catch (java.io.FileNotFoundException e) {
            ksout.load(null,destinationKeystorePasswordArr);
        } finally {          
            in.close();
        }

        Enumeration en = ksin.aliases();
        while (en.hasMoreElements()) {
            String alias = (String) en.nextElement();

            if ((sourceAlias == null) || (sourceAlias.equalsIgnoreCase(alias))) {

                if (ksout.containsAlias(alias)) {
                    log.info(CommonResourceBundle.getInstance().getString("message.destinationKeystorePathAlreadyContains", new Object[]{destinationKeystorePath, alias}));
                    continue;
                }

                //Use existing alias if no destinationAlias specified
                if (destinationAlias == null){
                    destinationAlias = alias;
                }

                if (ksin.isCertificateEntry(alias)) {
                    log.debug(CommonResourceBundle.getInstance().
			      getString("message.importingCertificate",
					new Object[]{alias}));
                    ksout.setCertificateEntry(destinationAlias, ksin.getCertificate(alias));
                }

                if (ksin.isKeyEntry(alias)) {
                    log.debug(CommonResourceBundle.getInstance().
			      getString("message.importingKey",
					new Object[]{alias}));
                    Certificate[] certChain = ksin.getCertificateChain(alias);
                    ksout.setKeyEntry(destinationAlias, ksin.getKey(alias,sourceKeyPasswordArr), destinationKeyPasswordArr, certChain);
                }
            }

        }

        //---------  Overwrite the destination keystore with new keys/certs which is a merge of source and original destination keystores--------------
        FileOutputStream out = new FileOutputStream(destinationKeystorePath);
        ksout.store(out,destinationKeystorePasswordArr);
        out.close();
        log.debug(CommonResourceBundle.getInstance().
		  getString("message.keystoreCopySuccessful")) ;
    }
    
}
