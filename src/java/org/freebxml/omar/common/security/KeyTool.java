/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/KeyTool.java,v 1.3 2005/06/29 06:09:50 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.MissingResourceException;
import sun.misc.BASE64Encoder;

import sun.security.pkcs.PKCS10;

import sun.security.provider.IdentityDatabase;
import sun.security.provider.SystemIdentity;
import sun.security.provider.SystemSigner;

import sun.security.util.DerOutputStream;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertAndKeyGen;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.X500Name;
import sun.security.x509.X500Signer;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PushbackInputStream;

import java.security.Identity;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.text.Collator;
import java.text.MessageFormat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Vector;

public final class KeyTool {
    private static final ResourceBundle rb = ResourceBundle.getBundle(
            "sun.security.util.Resources");
    private static final Collator collator;

    static {
        collator = Collator.getInstance();
        collator.setStrength(0);
    }

    private boolean debug;
    private String command;
    private String sigAlgName;
    private String keyAlgName;
    private boolean verbose;
    private int keysize;
    private boolean rfc;
    private int validity;
    private String alias;
    private String dname;
    private String keyAlias;
    private String dest;
    private String filename;
    private Vector providers;
    private String storetype;
    private char[] storePass;
    private char[] storePassNew;
    private char[] keyPass;
    private char[] keyPassNew;
    private char[] oldPass;
    private char[] newPass;
    private String ksfname;
    private File ksfile;
    private InputStream ksStream;
    private InputStream inStream;
    private KeyStore keyStore;
    private boolean kssave;
    private boolean noprompt;
    private boolean trustcacerts;
    private CertificateFactory cf;
    private KeyStore caks;

    public KeyTool() {
        debug = false;
        command = null;
        sigAlgName = null;
        keyAlgName = "RSA";
        verbose = false;
        keysize = 1024;
        rfc = false;
        validity = 365*2;
        alias = null;
        dname = null;
        keyAlias = "mykey";
        dest = null;
        filename = null;
        providers = null;
        storetype = null;
        storePass = null;
        storePassNew = null;
        keyPass = null;
        keyPassNew = null;
        oldPass = null;
        newPass = null;
        ksfname = null;
        ksfile = null;
        ksStream = null;
        inStream = null;
        keyStore = null;
        kssave = false;
        noprompt = false;
        trustcacerts = false;
        cf = null;
        caks = null;
    }

    public static void main(String[] args) {
        try {
            KeyTool keytool = new KeyTool();
            keytool.run(args, System.out);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e);
            System.exit(1);
        }
    }

    public void run(String[] as, PrintStream printstream)
        throws Exception {
        parseArgs(as);
        doCommands(printstream);

        if (storePass != null) {
            Arrays.fill(storePass, ' ');
            storePass = null;
        }

        if (storePassNew != null) {
            Arrays.fill(storePassNew, ' ');
            storePassNew = null;
        }

        if (keyPass != null) {
            Arrays.fill(keyPass, ' ');
            keyPass = null;
        }

        if (keyPassNew != null) {
            Arrays.fill(keyPassNew, ' ');
            keyPassNew = null;
        }

        if (oldPass != null) {
            Arrays.fill(oldPass, ' ');
            oldPass = null;
        }

        if (newPass != null) {
            Arrays.fill(newPass, ' ');
            newPass = null;
        }

        /*
        //break MISSING_BLOCK_LABEL_446;
        Exception exception = new Exception("Unknown 1");
        System.out.println(getMessageString("keytool error: ") + exception);
        if(debug)
            exception.printStackTrace();
        System.exit(1);
         */
        if (storePass != null) {
            Arrays.fill(storePass, ' ');
            storePass = null;
        }

        if (storePassNew != null) {
            Arrays.fill(storePassNew, ' ');
            storePassNew = null;
        }

        if (keyPass != null) {
            Arrays.fill(keyPass, ' ');
            keyPass = null;
        }

        if (keyPassNew != null) {
            Arrays.fill(keyPassNew, ' ');
            keyPassNew = null;
        }

        if (oldPass != null) {
            Arrays.fill(oldPass, ' ');
            oldPass = null;
        }

        if (newPass != null) {
            Arrays.fill(newPass, ' ');
            newPass = null;
        }

        //break MISSING_BLOCK_LABEL_446;
        Exception exception1 = new Exception("Unknown 2");

        if (storePass != null) {
            Arrays.fill(storePass, ' ');
            storePass = null;
        }

        if (storePassNew != null) {
            Arrays.fill(storePassNew, ' ');
            storePassNew = null;
        }

        if (keyPass != null) {
            Arrays.fill(keyPass, ' ');
            keyPass = null;
        }

        if (keyPassNew != null) {
            Arrays.fill(keyPassNew, ' ');
            keyPassNew = null;
        }

        if (oldPass != null) {
            Arrays.fill(oldPass, ' ');
            oldPass = null;
        }

        if (newPass != null) {
            Arrays.fill(newPass, ' ');
            newPass = null;
        }
    }

    void parseArgs(String[] as) {
        if (as.length == 0) {
            usage();
        }

        int i = 0;

        for (i = 0; (i < as.length) && as[i].startsWith("-"); i++) {
            String s = as[i];

            if (collator.compare(s, "-certreq") == 0) {
                command = "certreq";
            } else if (collator.compare(s, "-delete") == 0) {
                command = "delete";
            } else if (collator.compare(s, "-export") == 0) {
                command = "export";
            } else if (collator.compare(s, "-genkey") == 0) {
                command = "genkey";
            } else {                
                if (collator.compare(s, "-help") == 0) {
                    usage();

                    return;
                }

                if (collator.compare(s, "-identitydb") == 0) {
                    command = "identitydb";
                } else if (collator.compare(s, "-import") == 0) {
                    command = "import";
                } else if (collator.compare(s, "-keyclone") == 0) {
                    command = "keyclone";
                } else if (collator.compare(s, "-keypasswd") == 0) {
                    command = "keypasswd";
                } else if (collator.compare(s, "-list") == 0) {
                    command = "list";
                } else if (collator.compare(s, "-printcert") == 0) {
                    command = "printcert";
                } else if (collator.compare(s, "-selfcert") == 0) {
                    command = "selfcert";
                } else if (collator.compare(s, "-storepasswd") == 0) {
                    command = "storepasswd";
                } else if (collator.compare(s, "-keystore") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    ksfname = as[i];
                } else if (collator.compare(s, "-storepass") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    storePass = as[i].toCharArray();
                } else if (collator.compare(s, "-storetype") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    storetype = as[i];
                } else if (collator.compare(s, "-keypass") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    keyPass = as[i].toCharArray();
                } else if (collator.compare(s, "-new") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    newPass = as[i].toCharArray();
                } else if (collator.compare(s, "-alias") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    alias = as[i];
                } else if (collator.compare(s, "-dest") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    dest = as[i];
                } else if (collator.compare(s, "-dname") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    dname = as[i];
                } else if (collator.compare(s, "-keysize") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    keysize = Integer.parseInt(as[i]);
                } else if (collator.compare(s, "-keyalg") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    keyAlgName = as[i];
                } else if (collator.compare(s, "-sigalg") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    sigAlgName = as[i];
                } else if (collator.compare(s, "-validity") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    validity = Integer.parseInt(as[i]);
                } else if (collator.compare(s, "-file") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    filename = as[i];
                } else if (collator.compare(s, "-provider") == 0) {
                    if (++i == as.length) {
                        usage();
                    }

                    if (providers == null) {
                        providers = new Vector(3);                        
                    }

                    providers.add(as[i]);
                } else if (collator.compare(s, "-v") == 0) {
                    verbose = true;
                } else if (collator.compare(s, "-debug") == 0) {
                    debug = true;
                } else if (collator.compare(s, "-rfc") == 0) {
                    rfc = true;
                } else if (collator.compare(s, "-noprompt") == 0) {
                    noprompt = true;
                } else if (collator.compare(s, "-trustcacerts") == 0) {
                    trustcacerts = true;
                } else {
                    System.err.println(getMessageString("Illegal option:  ") + s);
                    usage();
                }
            }
        }

        if ((i < as.length) || (command == null)) {
            System.err.println("i=" + i + " as.length =" + as.length);
            usage();
        }
    }

    void doCommands(PrintStream printstream) throws Exception {
        
        if (validity <= 0) {
            throw new Exception(getMessageString(
                    "Validity must be greater than zero"));
        }

        if (providers != null) {
            ClassLoader classloader = ClassLoader.getSystemClassLoader();
            Object obj3;

            for (Enumeration enumeration = providers.elements();
                    enumeration.hasMoreElements();
                    Security.addProvider((Provider) obj3)) {
                String s1 = (String) enumeration.nextElement();
                Class class1;

                if (classloader != null) {
                    class1 = classloader.loadClass(s1);
                } else {
                    class1 = Class.forName(s1);
                }

                obj3 = class1.newInstance();

                if (!(obj3 instanceof Provider)) {
                    MessageFormat messageformat3 = new MessageFormat(getMessageString(
                                "provName not a provider"));
                    Object[] aobj3 = { s1 };
                    throw new Exception(messageformat3.format(
                            ((Object) (aobj3))));
                }
            }
        }

        if (command.equals("list") && verbose && rfc) {
            System.err.println(getMessageString(
                    "Must not specify both -v and -rfc with 'list' command"));
            usage();
        }

        if (command.equals("genkey") && (keyPass != null) &&
                (keyPass.length < 6)) {
            throw new Exception(getMessageString(
                    "Key password must be at least 6 characters"));
        }

        if ((newPass != null) && (newPass.length < 6)) {
            throw new Exception(getMessageString(
                    "New password must be at least 6 characters"));
        }

        if (!command.equals("printcert")) {
            if (ksfname == null) {
                ksfname = System.getProperty("user.home") + File.separator +
                    ".keystore";
            }

            try {
                ksfile = new File(ksfname);

                if (ksfile.exists() && (ksfile.length() == 0L)) {
                    throw new Exception(getMessageString(
                            "Keystore file exists, but is empty: ") + ksfname);
                }

                ksStream = new FileInputStream(ksfile);
            } catch (FileNotFoundException filenotfoundexception) {
                if (!command.equals("genkey") && !command.equals("identitydb") &&
                        !command.equals("import")) {
                    throw new Exception(getMessageString(
                            "Keystore file does not exist: ") + ksfname);
                }
            }
        }

        if (command.equals("keyclone") && (dest == null)) {
            dest = getAlias("destination");

            if (dest.equals("")) {
                throw new Exception(getMessageString(
                        "Must specify destination alias"));
            }
        }

        if (command.equals("delete") && (alias == null)) {
            alias = getAlias(null);

            if (alias.equals("")) {
                throw new Exception(getMessageString("Must specify alias"));
            }
        }

        if (storetype != null) {
            keyStore = KeyStore.getInstance(storetype);
        } else {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        }

        keyStore.load(ksStream, storePass);

        if (ksStream != null) {
            ksStream.close();
        }

        if (storePass != null) {
            if ((ksStream == null) && (storePass.length < 6)) {
                throw new Exception(getMessageString(
                        "Keystore password must be at least 6 characters"));
            }
        } else {
            if (command.equals("certreq") || command.equals("delete") ||
                    command.equals("genkey") || command.equals("import") ||
                    command.equals("keyclone") || command.equals("selfcert") ||
                    command.equals("storepasswd") ||
                    command.equals("keypasswd") ||
                    command.equals("identitydb")) {
                int i = 0;

                do {
                    System.err.print(getMessageString("Enter keystore password:  "));
                    System.err.flush();
                    storePass = readPasswd(System.in);

                    if ((ksStream == null) && (storePass.length < 6)) {
                        System.err.println(getMessageString(
                                "Keystore password is too short - must be at least 6 characters"));
                        storePass = null;
                    }

                    i++;
                } while ((storePass == null) && (i < 3));

                if (storePass == null) {
                    System.err.println(getMessageString(
                            "Too many failures - try later"));

                    return;
                }
            } else if (!command.equals("printcert")) {
                System.err.print(getMessageString("Enter keystore password:  "));
                System.err.flush();
                storePass = readPasswd(System.in);
            }

            if (ksStream != null) {
                ksStream = new FileInputStream(ksfile);
                keyStore.load(ksStream, storePass);
                ksStream.close();
            }
        }

        if (command.equals("printcert") || command.equals("import") ||
                command.equals("identitydb")) {
            cf = CertificateFactory.getInstance("X509");
        }

        if (trustcacerts) {
            caks = getCacertsKeyStore();
        }

        if (command.equals("certreq")) {
            if (filename != null) {
                PrintStream printstream1 = new PrintStream(new FileOutputStream(
                            filename));
                printstream = printstream1;
            }

            doCertReq(alias, sigAlgName, printstream);

            if (verbose && (filename != null)) {
                MessageFormat messageformat = new MessageFormat(getMessageString(
                            "Certification request stored in file <filename>"));
                Object[] aobj = { filename };
                System.err.println(messageformat.format(((Object) (aobj))));
                System.err.println(getMessageString("Submit this to your CA"));
            }
        } else if (command.equals("delete")) {
            doDeleteEntry(alias);
            kssave = true;
        } else if (command.equals("export")) {
            if (filename != null) {
                PrintStream printstream2 = new PrintStream(new FileOutputStream(
                            filename));
                printstream = printstream2;
            }

            doExportCert(alias, printstream);

            if (filename != null) {
                MessageFormat messageformat1 = new MessageFormat(getMessageString(
                            "Certificate stored in file <filename>"));
                Object[] aobj1 = { filename };
                System.err.println(messageformat1.format(((Object) (aobj1))));
            }
        } else if (command.equals("genkey")) {
            doGenKeyPair(alias, dname, keyAlgName, keysize, sigAlgName);
            kssave = true;
        } else if (command.equals("identitydb")) {
            Object obj = System.in;

            if (filename != null) {
                obj = new FileInputStream(filename);
            }

            doImportIdentityDatabase(((InputStream) (obj)));
        } else if (command.equals("import")) {
            Object obj1 = System.in;

            if (filename != null) {
                obj1 = new FileInputStream(filename);
            }

            String s = (alias == null) ? keyAlias : alias;

            if (keyStore.isKeyEntry(s)) {
                kssave = installReply(s, ((InputStream) (obj1)));

                if (kssave) {
                    System.err.println(getMessageString(
                            "Certificate reply was installed in keystore"));
                } else {
                    System.err.println(getMessageString(
                            "Certificate reply was not installed in keystore"));
                }
            } else {
                kssave = addTrustedCert(s, ((InputStream) (obj1)));

                if (kssave) {
                    System.err.println(getMessageString(
                            "Certificate was added to keystore"));
                } else {
                    System.err.println(getMessageString(
                            "Certificate was not added to keystore"));
                }
            }
        } else if (command.equals("keyclone")) {
            keyPassNew = newPass;
            doCloneKey(alias, dest);
            kssave = true;
        } else if (command.equals("keypasswd")) {
            keyPassNew = newPass;
            doChangeKeyPasswd(alias);
            kssave = true;
        } else if (command.equals("list")) {
            if (alias != null) {
                doPrintEntry(alias, printstream, true);
            } else {
                doPrintEntries(printstream);
            }
        } else if (command.equals("printcert")) {
            Object obj2 = System.in;

            if (filename != null) {
                obj2 = new FileInputStream(filename);
            }

            doPrintCert(((InputStream) (obj2)), printstream);
        } else if (command.equals("selfcert")) {
            doSelfCert(alias, dname, sigAlgName);
            kssave = true;
        } else if (command.equals("storepasswd")) {
            storePassNew = newPass;

            if (storePassNew == null) {
                storePassNew = getNewPasswd("keystore password", storePass);
            }

            kssave = true;
        }

        if (kssave) {
            if (verbose) {
                MessageFormat messageformat2 = new MessageFormat(getMessageString(
                            "[Saving ksfname]"));
                Object[] aobj2 = { ksfname };
                System.err.println(messageformat2.format(((Object) (aobj2))));
            }

            FileOutputStream fileoutputstream = new FileOutputStream(ksfname);
            keyStore.store(fileoutputstream,
                (storePassNew == null) ? storePass : storePassNew);
            fileoutputstream.close();
        }
    }

    private void doCertReq(String s, String s1, PrintStream printstream)
        throws Exception {
        if (s == null) {
            s = keyAlias;
        }

        Object[] aobj = recoverPrivateKey(s, storePass, keyPass);
        PrivateKey privatekey = (PrivateKey) aobj[0];

        if (keyPass == null) {
            keyPass = (char[]) aobj[1];
        }

        Certificate certificate = keyStore.getCertificate(s);

        if (certificate == null) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "alias has no public key (certificate)"));
            Object[] aobj1 = { s };
            throw new Exception(messageformat.format(((Object) (aobj1))));
        }

        PKCS10 pkcs10 = new PKCS10(certificate.getPublicKey());

        if (s1 == null) {
            String s2 = privatekey.getAlgorithm();

            if (s2.equalsIgnoreCase("DSA") || s2.equalsIgnoreCase("DSS")) {
                s1 = "SHA1WithDSA";
            } else if (s2.equalsIgnoreCase("RSA")) {
                s1 = "MD5WithRSA";
            } else {
                throw new Exception(getMessageString(
                        "Cannot derive signature algorithm"));
            }
        }

        Signature signature = Signature.getInstance(s1);
        signature.initSign(privatekey);

        X500Name x500name = new X500Name(((X509Certificate) certificate).getSubjectDN()
                                          .toString());
        X500Signer x500signer = new X500Signer(signature, x500name);
        pkcs10.encodeAndSign(x500signer);
        pkcs10.print(printstream);
    }

    private void doDeleteEntry(String s) throws Exception {
        if (!keyStore.containsAlias(s)) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "Alias <alias> does not exist"));
            Object[] aobj = { s };
            throw new Exception(messageformat.format(((Object) (aobj))));
        } else {
            keyStore.deleteEntry(s);

            return;
        }
    }

    private void doExportCert(String s, PrintStream printstream)
        throws Exception {
        if (storePass == null) {
            printWarning();
        }

        if (s == null) {
            s = keyAlias;
        }

        if (!keyStore.containsAlias(s)) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "Alias <alias> does not exist"));
            Object[] aobj = { s };
            throw new Exception(messageformat.format(((Object) (aobj))));
        }

        X509Certificate x509certificate = (X509Certificate) keyStore.getCertificate(s);

        if (x509certificate == null) {
            MessageFormat messageformat1 = new MessageFormat(getMessageString(
                        "Alias <alias> has no certificate"));
            Object[] aobj1 = { s };
            throw new Exception(messageformat1.format(((Object) (aobj1))));
        } else {
            dumpCert(x509certificate, printstream);

            return;
        }
    }

    /** Generate a public/private key pair.
      *
      * alias, dname, keyAlgName, keysize, sigAlgName
      * @param s 
      *     The alias to register the key pair under.
      * @param s1 
      *     The subject's distinguished name.
      * @param s2 
      *     The name of the key algorithm
      * @param i
      *     The key size
      * @param s3 
      *     The name of the signing algorithm.
      * @throws Exception
      */    
    private void doGenKeyPair(String _alias, String _dname, String _keyAlg, int _keySize, String _sigAlg)
        throws Exception 
    {
        if (_alias == null) {
            _alias = keyAlias;
        }

        if (keyStore.containsAlias(_alias)) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "Key pair not generated, alias <alias> already exists"));
            Object[] aobj = { _alias };
            throw new Exception(messageformat.format(((Object) (aobj))));
        }

        if (_sigAlg == null) {
            if (_keyAlg.equalsIgnoreCase("DSA")) {
                _sigAlg = "SHA1WithDSA";
            } else if (_keyAlg.equalsIgnoreCase("RSA")) {
                _sigAlg = "MD5WithRSA";
            } else {
                throw new Exception(getMessageString(
                        "Cannot derive signature algorithm"));
            }
        }

        CertAndKeyGen certandkeygen = new CertAndKeyGen(_keyAlg, _sigAlg);
        X500Name x500name;

        if (_dname == null) {
            x500name = getX500Name();
        } else {
            x500name = new X500Name(_dname);
        }

        if (verbose) {
            MessageFormat messageformat1 = new MessageFormat(getMessageString(
                        "Generating keysize bit keyAlgName key pair and self-signed certificate (sigAlgName)\n\tfor: x500Name"));
            Object[] aobj1 = { new Integer(_keySize), _keyAlg, _sigAlg, x500name };
            System.err.println(messageformat1.format(((Object) (aobj1))));
        }

        certandkeygen.generate(_keySize);

        PrivateKey privatekey = certandkeygen.getPrivateKey();
        X509Certificate[] ax509certificate = new X509Certificate[1];
        ax509certificate[0] = certandkeygen.getSelfCertificate(x500name,
                validity * 24 * 60 * 60);

        if (keyPass == null) {
            int j;

            for (j = 0; (j < 3) && (keyPass == null); j++) {
                MessageFormat messageformat2 = new MessageFormat(getMessageString(
                            "Enter key password for <alias>"));
                Object[] aobj2 = { _alias };
                System.err.println(messageformat2.format(((Object) (aobj2))));
                System.err.print(getMessageString(
                        "\t(RETURN if same as keystore password):  "));
                System.err.flush();
                keyPass = readPasswd(System.in);

                if (keyPass == null) {
                    keyPass = storePass;
                } else if (keyPass.length < 6) {
                    System.err.println(getMessageString(
                            "Key password is too short - must be at least 6 characters"));
                    keyPass = null;
                }
            }

            if (j == 3) {
                throw new Exception(getMessageString(
                        "Too many failures - key not added to keystore"));
            }
        }

        keyStore.setKeyEntry(_alias, privatekey, keyPass, ax509certificate);
    }

    private void doCloneKey(String s, String s1) throws Exception {
        if (s == null) {
            s = keyAlias;
        }

        if (keyStore.containsAlias(s1)) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "Destination alias <dest> already exists"));
            Object[] aobj1 = { s1 };
            throw new Exception(messageformat.format(((Object) (aobj1))));
        }

        Object[] aobj = recoverPrivateKey(s, storePass, keyPass);
        PrivateKey privatekey = (PrivateKey) aobj[0];

        if (keyPass == null) {
            keyPass = (char[]) aobj[1];
        }

        if (keyPassNew == null) {
            int i = 0;

            do {
                keyPassNew = getKeyPasswd(s1, s, keyPass);

                if (keyPassNew.length < 6) {
                    System.err.println(getMessageString(
                            "Password is too short - must be at least 6 characters"));
                    keyPassNew = null;
                }

                i++;
            } while ((keyPassNew == null) && (i < 3));

            if (keyPassNew == null) {
                throw new Exception(getMessageString(
                        "Too many failures. Key entry not cloned"));
            }
        }

        keyStore.setKeyEntry(s1, privatekey, keyPassNew,
            keyStore.getCertificateChain(s));
    }

    private void doChangeKeyPasswd(String s) throws Exception {
        if (s == null) {
            s = keyAlias;
        }

        Object[] aobj = recoverPrivateKey(s, storePass, keyPass);
        PrivateKey privatekey = (PrivateKey) aobj[0];

        if (keyPass == null) {
            keyPass = (char[]) aobj[1];
        }

        if (keyPassNew == null) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "key password for <alias>"));
            Object[] aobj1 = { s };
            keyPassNew = getNewPasswd(messageformat.format(((Object) (aobj1))),
                    keyPass);
        }

        keyStore.setKeyEntry(s, privatekey, keyPassNew,
            keyStore.getCertificateChain(s));
    }

    private void doImportIdentityDatabase(InputStream inputstream)
        throws Exception {
        Certificate[] acertificate = null;
        boolean flag = false;
        IdentityDatabase identitydatabase = IdentityDatabase.fromStream(inputstream);
        Enumeration enumeration = identitydatabase.identities();

        while (enumeration.hasMoreElements()) {
            Identity identity = (Identity) enumeration.nextElement();
            X509Certificate x509certificate = null;

            if ((!(identity instanceof SystemSigner) ||
                    !((SystemSigner) identity).isTrusted()) &&
                    (!(identity instanceof SystemIdentity) ||
                    !((SystemIdentity) identity).isTrusted())) {
                continue;
            }

            if (keyStore.containsAlias(identity.getName())) {
                MessageFormat messageformat = new MessageFormat(getMessageString(
                            "Keystore entry for <id.getName()> already exists"));
                Object[] aobj = { identity.getName() };
                System.err.println(messageformat.format(((Object) (aobj))));

                continue;
            }

            java.security.Certificate[] acertificate1 = identity.certificates();

            if ((acertificate1 == null) || (acertificate1.length <= 0)) {
                continue;
            }

            DerOutputStream deroutputstream = new DerOutputStream();
            acertificate1[0].encode(deroutputstream);

            byte[] abyte0 = deroutputstream.toByteArray();
            ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte0);
            x509certificate = (X509Certificate) cf.generateCertificate(bytearrayinputstream);
            bytearrayinputstream.close();

            if (isSelfSigned(x509certificate)) {
                java.security.PublicKey publickey = x509certificate.getPublicKey();

                try {
                    x509certificate.verify(publickey);
                } catch (Exception exception) {
                    continue;
                }
            }

            if (identity instanceof SystemSigner) {
                MessageFormat messageformat1 = new MessageFormat(getMessageString(
                            "Creating keystore entry for <id.getName()> ..."));
                Object[] aobj1 = { identity.getName() };
                System.err.println(messageformat1.format(((Object) (aobj1))));

                if (acertificate == null) {
                    acertificate = new Certificate[1];
                }

                acertificate[0] = x509certificate;

                PrivateKey privatekey = ((SystemSigner) identity).getPrivateKey();
                keyStore.setKeyEntry(identity.getName(), privatekey, storePass,
                    acertificate);
            } else {
                keyStore.setCertificateEntry(identity.getName(), x509certificate);
            }

            kssave = true;
        }

        if (!kssave) {
            System.err.println(getMessageString(
                    "No entries from identity database added"));
        }
    }

    private void doPrintEntry(String s, PrintStream printstream, boolean flag)
        throws Exception {
        if ((storePass == null) && flag) {
            printWarning();
        }

        if (!keyStore.containsAlias(s)) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "Alias <alias> does not exist"));
            Object[] aobj = { s };
            throw new Exception(messageformat.format(((Object) (aobj))));
        }

        if (verbose || rfc || debug) {
            MessageFormat messageformat1 = new MessageFormat(getMessageString(
                        "Alias name: alias"));
            Object[] aobj1 = { s };
            printstream.println(messageformat1.format(((Object) (aobj1))));
            messageformat1 = new MessageFormat(getMessageString(
                        "Creation date: keyStore.getCreationDate(alias)"));

            Object[] aobj3 = { keyStore.getCreationDate(s) };
            printstream.println(messageformat1.format(((Object) (aobj3))));
        } else {
            MessageFormat messageformat2 = new MessageFormat(getMessageString(
                        "alias, keyStore.getCreationDate(alias), "));
            Object[] aobj2 = { s, keyStore.getCreationDate(s) };
            printstream.print(messageformat2.format(((Object) (aobj2))));
        }

        if (keyStore.isKeyEntry(s)) {
            if (verbose || rfc || debug) {
                printstream.println(getMessageString("Entry type: keyEntry"));
            } else {
                printstream.println(getMessageString("keyEntry,"));
            }

            Certificate[] acertificate = keyStore.getCertificateChain(s);

            if (acertificate != null) {
                if (verbose || rfc || debug) {
                    printstream.println(getMessageString(
                            "Certificate chain length: ") +
                        acertificate.length);

                    for (int i = 0; i < acertificate.length; i++) {
                        MessageFormat messageformat3 = new MessageFormat(getMessageString(
                                    "Certificate[(i + 1)]:"));
                        Object[] aobj4 = { new Integer(i + 1) };
                        printstream.println(messageformat3.format(
                                ((Object) (aobj4))));

                        if (verbose &&
                                (acertificate[i] instanceof X509Certificate)) {
                            printX509Cert((X509Certificate) acertificate[i],
                                printstream);
                        } else if (debug) {
                            printstream.println(acertificate[i].toString());
                        } else {
                            dumpCert(acertificate[i], printstream);
                        }
                    }
                } else {
                    printstream.println(getMessageString(
                            "Certificate fingerprint (MD5): ") +
                        getCertFingerPrint("MD5", acertificate[0]));
                }
            }
        } else {
            Certificate certificate = keyStore.getCertificate(s);

            if (verbose && (certificate instanceof X509Certificate)) {
                printstream.println(getMessageString(
                        "Entry type: trustedCertEntry\n"));
                printX509Cert((X509Certificate) certificate, printstream);
            } else if (rfc) {
                printstream.println(getMessageString(
                        "Entry type: trustedCertEntry\n"));
                dumpCert(certificate, printstream);
            } else if (debug) {
                printstream.println(certificate.toString());
            } else {
                printstream.println(getMessageString("trustedCertEntry,"));
                printstream.println(getMessageString(
                        "Certificate fingerprint (MD5): ") +
                    getCertFingerPrint("MD5", certificate));
            }
        }
    }

    private void doPrintEntries(PrintStream printstream)
        throws Exception {
        if (storePass == null) {
            printWarning();
        } else {
            printstream.println();
        }

        printstream.println(getMessageString("Keystore type: ") +
            keyStore.getType());
        printstream.println(getMessageString("Keystore provider: ") +
            keyStore.getProvider().getName());
        printstream.println();

        MessageFormat messageformat = (keyStore.size() != 1)
            ? new MessageFormat(getMessageString(
                    "Your keystore contains keyStore.size() entries"))
            : new MessageFormat(getMessageString(
                    "Your keystore contains keyStore.size() entry"));
        Object[] aobj = { new Integer(keyStore.size()) };
        printstream.println(messageformat.format(((Object) (aobj))));
        printstream.println();

        for (Enumeration enumeration = keyStore.aliases();
                enumeration.hasMoreElements();) {
            String s = (String) enumeration.nextElement();
            doPrintEntry(s, printstream, false);

            if (verbose || rfc) {
                printstream.println(getMessageString("\n"));
                printstream.println(getMessageString(
                        "*******************************************"));
                printstream.println(getMessageString(
                        "*******************************************\n\n"));
            }
        }
    }

    private void doPrintCert(InputStream inputstream, PrintStream printstream)
        throws Exception {
        Collection collection = null;

        try {
            collection = cf.generateCertificates(inputstream);
        } catch (CertificateException certificateexception) {
            throw new Exception(getMessageString("Failed to parse input"),
                certificateexception);
        }

        if (collection.isEmpty()) {
            throw new Exception(getMessageString("Empty input"));
        }

        Certificate[] acertificate = (Certificate[]) collection.toArray(new Certificate[collection.size()]);

        for (int i = 0; i < acertificate.length; i++) {
            X509Certificate x509certificate = null;

            try {
                x509certificate = (X509Certificate) acertificate[i];
            } catch (ClassCastException classcastexception) {
                throw new Exception(getMessageString("Not X.509 certificate"));
            }

            if (acertificate.length > 1) {
                MessageFormat messageformat = new MessageFormat(getMessageString(
                            "Certificate[(i + 1)]:"));
                Object[] aobj = { new Integer(i + 1) };
                printstream.println(messageformat.format(((Object) (aobj))));
            }

            printX509Cert(x509certificate, printstream);

            if (i < (acertificate.length - 1)) {
                printstream.println();
            }
        }
    }

    private void doSelfCert(String s, String s1, String s2)
        throws Exception 
    {
        if (s == null) {
            s = keyAlias;
        }

        Object[] aobj = recoverPrivateKey(s, storePass, keyPass);
        PrivateKey privatekey = (PrivateKey) aobj[0];

        if (keyPass == null) {
            keyPass = (char[]) aobj[1];
        }

        if (s2 == null) {
            String s3 = privatekey.getAlgorithm();

            if (s3.equalsIgnoreCase("DSA") || s3.equalsIgnoreCase("DSS")) {
                s2 = "SHA1WithDSA";
            } else if (s3.equalsIgnoreCase("RSA")) {
                s2 = "MD5WithRSA";
            } else {
                throw new Exception(getMessageString(
                        "Cannot derive signature algorithm"));
            }
        }

        Certificate certificate = keyStore.getCertificate(s);

        if (certificate == null) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "alias has no public key"));
            Object[] aobj1 = { s };
            throw new Exception(messageformat.format(((Object) (aobj1))));
        }

        if (!(certificate instanceof X509Certificate)) {
            MessageFormat messageformat1 = new MessageFormat(getMessageString(
                        "alias has no X.509 certificate"));
            Object[] aobj2 = { s };
            throw new Exception(messageformat1.format(((Object) (aobj2))));
        }

        byte[] abyte0 = certificate.getEncoded();
        X509CertImpl x509certimpl = new X509CertImpl(abyte0);
        X509CertInfo x509certinfo = (X509CertInfo) x509certimpl.get("x509.info");
        Date date = new Date();
        Date date1 = new Date();
        date1.setTime(date1.getTime() +
            ((long) validity * 1000L * 24L * 60L * 60L));

        CertificateValidity certificatevalidity = new CertificateValidity(date,
                date1);
        x509certinfo.set("validity", certificatevalidity);
        x509certinfo.set("serialNumber",
            new CertificateSerialNumber((int) (date.getTime() / 1000L)));

        X500Name x500name;

        if (s1 == null) {
            x500name = (X500Name) x509certinfo.get("subject.dname");
        } else {
            x500name = new X500Name(s1);
            x509certinfo.set("subject.dname", x500name);
        }

        x509certinfo.set("issuer.dname", x500name);

        X509CertImpl x509certimpl1 = new X509CertImpl(x509certinfo);
        x509certimpl1.sign(privatekey, s2);

        AlgorithmId algorithmid = (AlgorithmId) x509certimpl1.get(
                "x509.algorithm");
        x509certinfo.set("algorithmID.algorithm", algorithmid);
        x509certimpl1 = new X509CertImpl(x509certinfo);
        x509certimpl1.sign(privatekey, s2);
        keyStore.setKeyEntry(s, privatekey,
            (keyPass == null) ? storePass : keyPass,
            new Certificate[] { x509certimpl1 });

        if (verbose) {
            System.err.println(getMessageString("New certificate (self-signed):"));
            System.err.print(x509certimpl1.toString());
            System.err.println();
        }
    }

    private boolean installReply(String s, InputStream inputstream)
        throws Exception {
        if (s == null) {
            s = keyAlias;
        }

        Object[] aobj = recoverPrivateKey(s, storePass, keyPass);
        PrivateKey privatekey = (PrivateKey) aobj[0];

        if (keyPass == null) {
            keyPass = (char[]) aobj[1];
        }

        Certificate certificate = keyStore.getCertificate(s);

        if (certificate == null) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "alias has no public key (certificate)"));
            Object[] aobj1 = { s };
            throw new Exception(messageformat.format(((Object) (aobj1))));
        }

        Collection collection = cf.generateCertificates(inputstream);

        if (collection.isEmpty()) {
            throw new Exception(getMessageString("Reply has no certificates"));
        }

        Certificate[] acertificate = (Certificate[]) collection.toArray(new Certificate[collection.size()]);
        Certificate[] acertificate1;

        if (acertificate.length == 1) {
            acertificate1 = establishCertChain(certificate, acertificate[0]);
        } else {
            acertificate1 = validateReply(s, certificate, acertificate);
        }

        if (acertificate1 != null) {
            keyStore.setKeyEntry(s, privatekey,
                (keyPass == null) ? storePass : keyPass, acertificate1);

            return true;
        } else {
            return false;
        }
    }

    private boolean addTrustedCert(String s, InputStream inputstream)
        throws Exception {
        X509Certificate x509certificate;

        if (s == null) {
            throw new Exception(getMessageString("Must specify alias"));
        }

        if (keyStore.containsAlias(s)) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "Certificate not imported, alias <alias> already exists"));
            Object[] aobj = { s };
            throw new Exception(messageformat.format(((Object) (aobj))));
        }

        x509certificate = null;

        try {
            x509certificate = (X509Certificate) cf.generateCertificate(inputstream);
        } catch (ClassCastException classcastexception) {
            throw new Exception(getMessageString("Input not an X.509 certificate"));
        } catch (CertificateException certificateexception) {
            throw new Exception(getMessageString("Input not an X.509 certificate"));
        }

        boolean flag = false;

        if (isSelfSigned(x509certificate)) {
            x509certificate.verify(x509certificate.getPublicKey());
            flag = true;
        }

        if (noprompt) {
            keyStore.setCertificateEntry(s, x509certificate);

            return true;
        }

        String s1 = null;
        String s3 = keyStore.getCertificateAlias(x509certificate);

        if (s3 != null) {
            MessageFormat messageformat1 = new MessageFormat(getMessageString(
                        "Certificate already exists in keystore under alias <trustalias>"));
            Object[] aobj1 = { s3 };
            System.err.println(messageformat1.format(((Object) (aobj1))));
            s1 = getYesNoReply(getMessageString(
                        "Do you still want to add it? [no]:  "));
        } else if (flag) {
            if (trustcacerts && (caks != null) &&
                    ((s3 = caks.getCertificateAlias(x509certificate)) != null)) {
                MessageFormat messageformat2 = new MessageFormat(getMessageString(
                            "Certificate already exists in system-wide CA keystore under alias <trustalias>"));
                Object[] aobj2 = { s3 };
                System.err.println(messageformat2.format(((Object) (aobj2))));
                s1 = getYesNoReply(getMessageString(
                            "Do you still want to add it to your own keystore? [no]:  "));
            }

            if (s3 == null) {
                printX509Cert(x509certificate, System.out);
                s1 = getYesNoReply(getMessageString(
                            "Trust this certificate? [no]:  "));
            }
        }

        if (s1 != null) {
            if (s1.equals("YES")) {
                keyStore.setCertificateEntry(s, x509certificate);

                return true;
            } else {
                return false;
            }
        }

        Certificate[] acertificate = establishCertChain(null, x509certificate);

        //if(acertificate == null)
        //break MISSING_BLOCK_LABEL_469;
        keyStore.setCertificateEntry(s, x509certificate);

        return true;

        //Exception exception = new Exception("Unknown 3");

        /*
        printX509Cert(x509certificate, System.out);
        String s2 = getYesNoReply(getMessageString("Trust this certificate? [no]:  "));
        if(s2.equals("YES"))
        {
            keyStore.setCertificateEntry(s, x509certificate);
            return true;
        } else
        {
            return false;
        }
        //return false;
         */
    }

    private char[] getNewPasswd(String s, char[] ac) throws Exception {
        Object obj = null;
        char[] ac2 = null;

        for (int i = 0; i < 3; i++) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "New prompt: "));
            Object[] aobj = { s };
            System.err.print(messageformat.format(((Object) (aobj))));

            char[] ac1 = readPasswd(System.in);

            if (ac1.length < 6) {
                System.err.println(getMessageString(
                        "Password is too short - must be at least 6 characters"));
            } else if (Arrays.equals(ac1, ac)) {
                System.err.println(getMessageString("Passwords must differ"));
            } else {
                MessageFormat messageformat1 = new MessageFormat(getMessageString(
                            "Re-enter new prompt: "));
                Object[] aobj1 = { s };
                System.err.print(messageformat1.format(((Object) (aobj1))));
                ac2 = readPasswd(System.in);

                if (!Arrays.equals(ac1, ac2)) {
                    System.err.println(getMessageString(
                            "They don't match; try again"));
                } else {
                    Arrays.fill(ac2, ' ');

                    return ac1;
                }
            }

            if (ac1 != null) {
                Arrays.fill(ac1, ' ');
                ac1 = null;
            }

            if (ac2 != null) {
                Arrays.fill(ac2, ' ');
                ac2 = null;
            }
        }

        throw new Exception(getMessageString("Too many failures - try later"));
    }

    private String getAlias(String s) throws Exception {
        if (s != null) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "Enter prompt alias name:  "));
            Object[] aobj = { s };
            System.err.print(messageformat.format(((Object) (aobj))));
        } else {
            System.err.print(getMessageString("Enter alias name:  "));
        }

        return (new BufferedReader(new InputStreamReader(System.in))).readLine();
    }

    private char[] getKeyPasswd(String s, String s1, char[] ac)
        throws Exception {
        int i = 0;
        char[] ac1 = null;

        do {
            if (ac != null) {
                MessageFormat messageformat = new MessageFormat(getMessageString(
                            "Enter key password for <alias>"));
                Object[] aobj = { s };
                System.err.println(messageformat.format(((Object) (aobj))));
                messageformat = new MessageFormat(getMessageString(
                            "\t(RETURN if same as for <otherAlias>)"));

                Object[] aobj2 = { s1 };
                System.err.print(messageformat.format(((Object) (aobj2))));
            } else {
                MessageFormat messageformat1 = new MessageFormat(getMessageString(
                            "Enter key password for <alias>"));
                Object[] aobj1 = { s };
                System.err.print(messageformat1.format(((Object) (aobj1))));
            }

            System.err.flush();
            ac1 = readPasswd(System.in);

            if (ac1 == null) {
                ac1 = ac;
            }

            i++;
        } while ((ac1 == null) && (i < 3));

        if (ac1 == null) {
            throw new Exception(getMessageString("Too many failures - try later"));
        } else {
            return ac1;
        }
    }

    private char[] readPasswd(InputStream inputstream)
        throws IOException {
        char[] ac;
        char[] ac1 = ac = new char[128];
        int i = ac1.length;
        int j = 0;
        int k;
label0: 
        do

            switch (k = inputstream.read()) {
            case -1:
            case 10: // '\n'
                break label0;

            case 13: // '\r'

                int l = inputstream.read();

                if ((l == 10) || (l == -1)) {
                    break label0;
                }

                if (!(inputstream instanceof PushbackInputStream)) {
                    inputstream = new PushbackInputStream(inputstream);
                }

                ((PushbackInputStream) inputstream).unread(l);

            // fall through
            default:

                if (--i < 0) {
                    ac1 = new char[j + 128];
                    i = ac1.length - j - 1;
                    System.arraycopy(ac, 0, ac1, 0, j);
                    Arrays.fill(ac, ' ');
                    ac = ac1;
                }

                ac1[j++] = (char) k;

                break;
            }
         while (true);

        if (j == 0) {
            return null;
        } else {
            char[] ac2 = new char[j];
            System.arraycopy(ac1, 0, ac2, 0, j);
            Arrays.fill(ac1, ' ');

            return ac2;
        }
    }

    private void printX509Cert(X509Certificate x509certificate,
        PrintStream printstream) throws Exception {
        MessageFormat messageformat = new MessageFormat(getMessageString(
                    "*PATTERN* printX509Cert"));
        Object[] aobj = {
            x509certificate.getSubjectDN().toString(),
            x509certificate.getIssuerDN().toString(),
            x509certificate.getSerialNumber().toString(16),
            x509certificate.getNotBefore().toString(),
            x509certificate.getNotAfter().toString(),
            getCertFingerPrint("MD5", x509certificate),
            getCertFingerPrint("SHA1", x509certificate)
        };
        printstream.println(messageformat.format(((Object) (aobj))));
    }

    private boolean isSelfSigned(X509Certificate x509certificate) {
        return x509certificate.getSubjectDN().equals(x509certificate.getIssuerDN());
    }

    private boolean isTrusted(Certificate certificate)
        throws Exception {
        if (keyStore.getCertificateAlias(certificate) != null) {
            return true;
        }

        return trustcacerts && (caks != null) &&
        (caks.getCertificateAlias(certificate) != null);
    }

    private X500Name getX500Name() throws IOException {
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(
                    System.in));
        String s = "Unknown";
        String s1 = "Unknown";
        String s2 = "Unknown";
        String s3 = "Unknown";
        String s4 = "Unknown";
        String s5 = "Unknown";
        String s6 = null;
        X500Name x500name;

        do {
            s = inputString(bufferedreader,
                    getMessageString("What is your first and last name?"), s);
            s1 = inputString(bufferedreader,
                    getMessageString(
                        "What is the name of your organizational unit?"), s1);
            s2 = inputString(bufferedreader,
                    getMessageString("What is the name of your organization?"), s2);
            s3 = inputString(bufferedreader,
                    getMessageString("What is the name of your City or Locality?"),
                    s3);
            s4 = inputString(bufferedreader,
                    getMessageString("What is the name of your State or Province?"),
                    s4);
            s5 = inputString(bufferedreader,
                    getMessageString(
                        "What is the two-letter country code for this unit?"),
                    s5);
            x500name = new X500Name(s, s1, s2, s3, s4, s5);

            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "Is <name> correct?"));
            Object[] aobj = { x500name };
            s6 = inputString(bufferedreader,
                    messageformat.format(((Object) (aobj))), getMessageString("no"));
        } while ((collator.compare(s6, getMessageString("yes")) != 0) &&
                (collator.compare(s6, getMessageString("y")) != 0));

        System.err.println();

        return x500name;
    }

    private String inputString(BufferedReader bufferedreader, String s,
        String s1) throws IOException {
        System.err.println(s);

        MessageFormat messageformat = new MessageFormat(getMessageString(
                    "  [defaultValue]:  "));
        Object[] aobj = { s1 };
        System.err.print(messageformat.format(((Object) (aobj))));
        System.err.flush();

        String s2 = bufferedreader.readLine();

        if ((s2 == null) || (collator.compare(s2, "") == 0)) {
            s2 = s1;
        }

        return s2;
    }

    private void dumpCert(Certificate certificate, PrintStream printstream)
        throws IOException, CertificateException {
        if (rfc) {
            BASE64Encoder base64encoder = new BASE64Encoder();
            printstream.println("-----BEGIN CERTIFICATE-----");
            base64encoder.encodeBuffer(certificate.getEncoded(), printstream);
            printstream.println("-----END CERTIFICATE-----");
        } else {
            printstream.write(certificate.getEncoded());
        }
    }

    private void byte2hex(byte byte0, StringBuffer stringbuffer) {
        char[] ac = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F'
        };
        int i = (byte0 & 0xf0) >> 4;
        int j = byte0 & 0xf;
        stringbuffer.append(ac[i]);
        stringbuffer.append(ac[j]);
    }

    private String toHexString(byte[] abyte0) {
        StringBuffer stringbuffer = new StringBuffer();
        int i = abyte0.length;

        for (int j = 0; j < i; j++) {
            byte2hex(abyte0[j], stringbuffer);

            if (j < (i - 1)) {
                stringbuffer.append(":");
            }
        }

        return stringbuffer.toString();
    }

    private Object[] recoverPrivateKey(String s, char[] ac, char[] ac1)
        throws Exception {
        java.security.Key key = null;

        if (!keyStore.containsAlias(s)) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "Alias <alias> does not exist"));
            Object[] aobj = { s };
            throw new Exception(messageformat.format(((Object) (aobj))));
        }

        if (!keyStore.isKeyEntry(s)) {
            MessageFormat messageformat1 = new MessageFormat(getMessageString(
                        "Alias <alias> has no (private) key"));
            Object[] aobj1 = { s };
            throw new Exception(messageformat1.format(((Object) (aobj1))));
        }

        if (ac1 == null) {
            try {
                key = keyStore.getKey(s, ac);
                ac1 = ac;
            } catch (UnrecoverableKeyException unrecoverablekeyexception) {
                ac1 = getKeyPasswd(s, null, null);
                key = keyStore.getKey(s, ac1);
            }
        } else {
            key = keyStore.getKey(s, ac1);
        }

        if (!(key instanceof PrivateKey)) {
            throw new Exception(getMessageString(
                    "Recovered key is not a private key"));
        } else {
            return (new Object[] { (PrivateKey) key, ac1 });
        }
    }

    private String getCertFingerPrint(String s, Certificate certificate)
        throws Exception {
        byte[] abyte0 = certificate.getEncoded();
        MessageDigest messagedigest = MessageDigest.getInstance(s);
        byte[] abyte1 = messagedigest.digest(abyte0);

        return toHexString(abyte1);
    }

    private void printWarning() {
        System.err.println();
        System.err.println(getMessageString(
                "*****************  WARNING WARNING WARNING  *****************"));
        System.err.println(getMessageString(
                "* The integrity of the information stored in your keystore  *"));
        System.err.println(getMessageString(
                "* has NOT been verified!  In order to verify its integrity, *"));
        System.err.println(getMessageString(
                "* you must provide your keystore password.                  *"));
        System.err.println(getMessageString(
                "*****************  WARNING WARNING WARNING  *****************"));
        System.err.println();
    }

    private Certificate[] validateReply(String s, Certificate certificate,
        Certificate[] acertificate) throws Exception {
        java.security.PublicKey publickey = certificate.getPublicKey();
        int i;

        for (i = 0; i < acertificate.length; i++)
            if (publickey.equals(acertificate[i].getPublicKey())) {
                break;
            }

        if (i == acertificate.length) {
            MessageFormat messageformat = new MessageFormat(getMessageString(
                        "Certificate reply does not contain public key for <alias>"));
            Object[] aobj = { s };
            throw new Exception(messageformat.format(((Object) (aobj))));
        }

        Certificate certificate1 = acertificate[0];
        acertificate[0] = acertificate[i];
        acertificate[i] = certificate1;

        Principal principal = ((X509Certificate) acertificate[0]).getIssuerDN();

        for (int j = 1; j < (acertificate.length - 1); j++) {
            int l;

            for (l = j; l < acertificate.length; l++) {
                Principal principal1 = ((X509Certificate) acertificate[l]).getSubjectDN();

                if (!principal1.equals(principal)) {
                    continue;
                }

                Certificate certificate2 = acertificate[j];
                acertificate[j] = acertificate[l];
                acertificate[l] = certificate2;
                principal = ((X509Certificate) acertificate[j]).getIssuerDN();

                break;
            }

            if (l == acertificate.length) {
                throw new Exception(getMessageString(
                        "Incomplete certificate chain in reply"));
            }
        }

        for (int k = 0; k < (acertificate.length - 1); k++) {
            java.security.PublicKey publickey1 = acertificate[k + 1].getPublicKey();

            try {
                acertificate[k].verify(publickey1);
            } catch (Exception exception) {
                throw new Exception(getMessageString(
                        "Certificate chain in reply does not verify: ") +
                    exception.getMessage());
            }
        }

        if (noprompt) {
            return acertificate;
        }

        Certificate certificate3 = acertificate[acertificate.length - 1];

        if (!isTrusted(certificate3)) {
            boolean flag = false;
            Certificate certificate4 = null;

            if (trustcacerts && (caks != null)) {
                Enumeration enumeration = caks.aliases();

                while (enumeration.hasMoreElements()) {
                    String s2 = (String) enumeration.nextElement();
                    certificate4 = caks.getCertificate(s2);

                    if (certificate4 == null) {
                        continue;
                    }

                    try {
                        certificate3.verify(certificate4.getPublicKey());
                        flag = true;

                        break;
                    } catch (Exception exception1) {
                    }
                }
            }

            if (!flag) {
                System.err.println();
                System.err.println(getMessageString(
                        "Top-level certificate in reply:\n"));
                printX509Cert((X509Certificate) certificate3, System.out);
                System.err.println();
                System.err.print(getMessageString("... is not trusted. "));

                String s1 = getYesNoReply(getMessageString(
                            "Install reply anyway? [no]:  "));

                if (s1.equals("NO")) {
                    return null;
                }
            } else if (!isSelfSigned((X509Certificate) certificate3)) {
                Certificate[] acertificate1 = new Certificate[acertificate.length +
                    1];
                System.arraycopy(acertificate, 0, acertificate1, 0,
                    acertificate.length);
                acertificate1[acertificate1.length - 1] = certificate4;
                acertificate = acertificate1;
            }
        }

        return acertificate;
    }

    private Certificate[] establishCertChain(Certificate certificate,
        Certificate certificate1) throws Exception {
        if (certificate != null) {
            java.security.PublicKey publickey = certificate.getPublicKey();
            java.security.PublicKey publickey1 = certificate1.getPublicKey();

            if (!publickey.equals(publickey1)) {
                throw new Exception(getMessageString(
                        "Public keys in reply and keystore don't match"));
            }

            if (certificate1.equals(certificate)) {
                throw new Exception(getMessageString(
                        "Certificate reply and certificate in keystore are identical"));
            }
        }

        Hashtable hashtable = null;

        if (keyStore.size() > 0) {
            hashtable = new Hashtable(11);
            keystorecerts2Hashtable(keyStore, hashtable);
        }

        if (trustcacerts && (caks != null) && (caks.size() > 0)) {
            if (hashtable == null) {
                hashtable = new Hashtable(11);
            }

            keystorecerts2Hashtable(caks, hashtable);
        }

        Vector vector = new Vector(2);

        if (buildChain((X509Certificate) certificate1, vector, hashtable)) {
            Certificate[] acertificate = new Certificate[vector.size()];
            int i = 0;

            for (int j = vector.size() - 1; j >= 0; j--) {
                acertificate[i] = (Certificate) vector.elementAt(j);
                i++;
            }

            return acertificate;
        } else {
            throw new Exception(getMessageString(
                    "Failed to establish chain from reply"));
        }
    }

    private boolean buildChain(X509Certificate x509certificate, Vector vector,
        Hashtable hashtable) {
        Principal principal = x509certificate.getSubjectDN();
        Principal principal1 = x509certificate.getIssuerDN();

        if (principal.equals(principal1)) {
            vector.addElement(x509certificate);

            return true;
        }

        Vector vector1 = (Vector) hashtable.get(principal1);

        if (vector1 == null) {
            return false;
        }

        Enumeration enumeration = vector1.elements();

        while (enumeration.hasMoreElements()) {
            X509Certificate x509certificate1 = (X509Certificate) enumeration.nextElement();
            java.security.PublicKey publickey = x509certificate1.getPublicKey();

            try {
                x509certificate.verify(publickey);
            } catch (Exception exception) {
                continue;
            }

            if (buildChain(x509certificate1, vector, hashtable)) {
                vector.addElement(x509certificate);

                return true;
            }
        }

        return false;
    }

    private String getYesNoReply(String s) throws IOException {
        String s1 = null;

        do {
            System.err.print(s);
            System.err.flush();
            s1 = (new BufferedReader(new InputStreamReader(System.in))).readLine();

            if ((collator.compare(s1, "") == 0) ||
                    (collator.compare(s1, getMessageString("n")) == 0) ||
                    (collator.compare(s1, getMessageString("no")) == 0)) {
                s1 = "NO";
            } else if ((collator.compare(s1, getMessageString("y")) == 0) ||
                    (collator.compare(s1, getMessageString("yes")) == 0)) {
                s1 = "YES";
            } else {
                System.err.println(getMessageString("Wrong answer, try again"));
                s1 = null;
            }
        } while (s1 == null);

        return s1;
    }

    private KeyStore getCacertsKeyStore() throws Exception {
        String s = File.separator;
        File file = new File(System.getProperty("java.home") + s + "lib" + s +
                "security" + s + "cacerts");

        if (!file.exists()) {
            return null;
        } else {
            FileInputStream fileinputstream = new FileInputStream(file);
            KeyStore keystore = KeyStore.getInstance("jks");
            keystore.load(fileinputstream, null);
            fileinputstream.close();

            return keystore;
        }
    }

    private void keystorecerts2Hashtable(KeyStore keystore, Hashtable hashtable)
        throws Exception {
        for (Enumeration enumeration = keystore.aliases();
                enumeration.hasMoreElements();) {
            String s = (String) enumeration.nextElement();
            Certificate certificate = keystore.getCertificate(s);

            if (certificate != null) {
                Principal principal = ((X509Certificate) certificate).getSubjectDN();
                Vector vector = (Vector) hashtable.get(principal);

                if (vector == null) {
                    vector = new Vector();
                    vector.addElement(certificate);
                } else if (!vector.contains(certificate)) {
                    vector.addElement(certificate);
                }

                hashtable.put(principal, vector);
            }
        }
    }
    
    private String getMessageString(String key) {
        String msg = key;
        try {
            msg = rb.getString(key);
        } catch (MissingResourceException e) {            
        }
        
        return msg;
    }

    private void usage() {
        System.err.println(getMessageString("keytool usage:\n"));
        System.err.println(getMessageString(
                "-certreq     [-v] [-alias <alias>] [-sigalg <sigalg>]"));
        System.err.println(getMessageString(
                "\t     [-file <csr_file>] [-keypass <keypass>]"));
        System.err.println(getMessageString(
                "\t     [-keystore <keystore>] [-storepass <storepass>]"));
        System.err.println(getMessageString(
                "\t     [-storetype <storetype>] [-provider <provider_class_name>] ..."));
        System.err.println();
        System.err.println(getMessageString("-delete      [-v] -alias <alias>"));
        System.err.println(getMessageString(
                "\t     [-keystore <keystore>] [-storepass <storepass>]"));
        System.err.println(getMessageString(
                "\t     [-storetype <storetype>] [-provider <provider_class_name>] ..."));
        System.err.println();
        System.err.println(getMessageString(
                "-export      [-v] [-rfc] [-alias <alias>] [-file <cert_file>]"));
        System.err.println(getMessageString(
                "\t     [-keystore <keystore>] [-storepass <storepass>]"));
        System.err.println(getMessageString(
                "\t     [-storetype <storetype>] [-provider <provider_class_name>] ..."));
        System.err.println();
        System.err.println(getMessageString(
                "-genkey      [-v] [-alias <alias>] [-keyalg <keyalg>]"));
        System.err.println(getMessageString(
                "\t     [-keysize <keysize>] [-sigalg <sigalg>]"));
        System.err.println(getMessageString(
                "\t     [-dname <dname>] [-validity <valDays>]"));
        System.err.println(getMessageString(
                "\t     [-keypass <keypass>] [-keystore <keystore>]"));
        System.err.println(getMessageString(
                "\t     [-storepass <storepass>] [-storetype <storetype>]"));
        System.err.println(getMessageString(
                "\t     [-provider <provider_class_name>] ..."));
        System.err.println();
        System.err.println(getMessageString("-help"));
        System.err.println();
        System.err.println(getMessageString(
                "-identitydb  [-v] [-file <idb_file>] [-keystore <keystore>]"));
        System.err.println(getMessageString(
                "\t     [-storepass <storepass>] [-storetype <storetype>]"));
        System.out.println(getMessageString(
                "\t     [-provider <provider_class_name>] ..."));
        System.err.println();
        System.err.println(getMessageString(
                "-import      [-v] [-noprompt] [-trustcacerts] [-alias <alias>]"));
        System.err.println(getMessageString(
                "\t     [-file <cert_file>] [-keypass <keypass>]"));
        System.err.println(getMessageString(
                "\t     [-keystore <keystore>] [-storepass <storepass>]"));
        System.err.println(getMessageString(
                "\t     [-storetype <storetype>] [-provider <provider_class_name>] ..."));
        System.err.println();
        System.err.println(getMessageString(
                "-keyclone    [-v] [-alias <alias>] -dest <dest_alias>"));
        System.err.println(getMessageString(
                "\t     [-keypass <keypass>] [-new <new_keypass>]"));
        System.err.println(getMessageString(
                "\t     [-keystore <keystore>] [-storepass <storepass>]"));
        System.err.println(getMessageString(
                "\t     [-storetype <storetype>] [-provider <provider_class_name>] ..."));
        System.err.println();
        System.err.println(getMessageString("-keypasswd   [-v] [-alias <alias>]"));
        System.err.println(getMessageString(
                "\t     [-keypass <old_keypass>] [-new <new_keypass>]"));
        System.err.println(getMessageString(
                "\t     [-keystore <keystore>] [-storepass <storepass>]"));
        System.err.println(getMessageString(
                "\t     [-storetype <storetype>] [-provider <provider_class_name>] ..."));
        System.err.println();
        System.err.println(getMessageString(
                "-list        [-v | -rfc] [-alias <alias>]"));
        System.err.println(getMessageString(
                "\t     [-keystore <keystore>] [-storepass <storepass>]"));
        System.err.println(getMessageString(
                "\t     [-storetype <storetype>] [-provider <provider_class_name>] ..."));
        System.err.println();
        System.err.println(getMessageString("-printcert   [-v] [-file <cert_file>]"));
        System.err.println();
        System.err.println(getMessageString(
                "-selfcert    [-v] [-alias <alias>] [-sigalg <sigalg>]"));
        System.err.println(getMessageString(
                "\t     [-dname <dname>] [-validity <valDays>]"));
        System.err.println(getMessageString(
                "\t     [-keypass <keypass>] [-keystore <keystore>]"));
        System.err.println(getMessageString(
                "\t     [-storepass <storepass>] [-storetype <storetype>]"));
        System.err.println(getMessageString(
                "\t     [-provider <provider_class_name>] ..."));
        System.err.println();
        System.err.println(getMessageString(
                "-storepasswd [-v] [-new <new_storepass>]"));
        System.err.println(getMessageString(
                "\t     [-keystore <keystore>] [-storepass <storepass>]"));
        System.err.println(getMessageString(
                "\t     [-storetype <storetype>] [-provider <provider_class_name>] ..."));
        System.err.println();
        throw new UndeclaredThrowableException(new Throwable("Error in command line: command=" + command));
    }
}
