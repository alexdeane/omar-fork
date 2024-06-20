/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/util/SOAPSender.java,v 1.31 2007/03/20 19:23:47 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.mail.MessagingException;
import javax.mail.internet.ParseException;
import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.freebxml.omar.common.CredentialInfo;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.Utility;
import org.freebxml.omar.common.security.SoapSecurityUtil;
import org.freebxml.omar.server.interfaces.soap.RegistrySOAPServlet;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.xml.sax.SAXException;


/**
 * Sends a SOAP 1.1 message with attachments to an ebXML Registry <br>
 *
 * It takes these command line parameters:<br>
 *
 * req - The file containing the ebXML registry request <br>
 *
 * alias - Optional. If it is provided the request will be signed with the
 * private key of the alias in the keystore<br>
 *
 * keyStore - Optional. The keystore is used for signing. If it is not provided
 * but the alias is provided, the keystore specified by
 * ebxmlrr.security.keystoreFile properties in ebxmlrr.properties is used for
 * signing. The supported keystore types are pkcs12 and jks <br>
 *
 * keyPassword - The key password for accessing the private key<br>
 *
 * keyStoreType - The type of the keystore. It may be pkcs12 or jks <br>
 *
 * keyStorePassword - The password for accessing the keystore <br>
 *
 * url - The URL of the ebXML registry server<br>
 *
 * attach - comma-delimited list of attached file, MIME type of the file and the UUID (either
 * temporary or real) of corresponding ExtrinsicObject in the SubmitObjectsRequest. More than
 * one attach parameter can be added<br>
 *
 * res - If this parameter exists, the response from registry will be saved to the file
 * of path specified by this parameter<br>
 *
 * TODO: Add option to add DONT_VERSION flag to sent SOAP message body.
 *
 * @see
 * @author Farrukh S. Najmi
 */
public class SOAPSender {
    private static AuthenticationServiceImpl authc = null;
    private static final String defaultRequestFileName = "c:/osws/ebxmlrr-spec/misc/samples/SubmitObjectsRequest_Sun.xml";
    private static final String defaultAlias = null;
    private static final boolean defaultLocalCall = true;
    private static boolean localCall = defaultLocalCall;
    private static boolean requestIsAScheme = false;
    private static boolean debug = false;
    private static List attachments = new java.util.ArrayList();
    String reqFileName = null;
    private String registryURL = null;
    private String alias = defaultAlias;
    private SOAPMessage msg = null;
    private HashMap attachmentMap = new HashMap();
    private KeyStore keyStore;
    private String keyPassword;
    private String signingAlgo;
    private java.security.cert.Certificate[] certs;
    private java.security.PrivateKey privateKey;

    public void setKeyStore(String keyStoreFile, String keyStoreType,
        String keyStorePassword, String keyPassword)
        throws KeyStoreException, IOException, NoSuchAlgorithmException, 
            CertificateException {
        keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(new FileInputStream(keyStoreFile),
            stringToCharArray(keyStorePassword));
    }

    private char[] stringToCharArray(String str) {
        char[] arr = null;

        if (str != null) {
            arr = str.toCharArray();
        }

        return arr;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public void setRequestFileName(String reqFileName) {
        this.reqFileName = reqFileName;
    }

    public void setRegistryURL(String registryURL) {
        this.registryURL = registryURL;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setLocalCall(boolean localCall) {
        SOAPSender.localCall = localCall;
    }

    private static void printUsage() {
        System.err.println(
            "...SOAPSender [-help] req=<requestFile.xml>|scheme=<schemeFile.xml> keyStore=<KeyStore> " +
            "keyStoreType=<jks|pkcs12> keyStorePassword=<password> keyPassword=<password> alias=<aliasInKeyStore> url=<registryURL> " +
            "attach=<file>,mimeType,id localCall=<true|false> " +
            "res=<responseFile.xml>");
        System.exit(-1);
    }

    public static void main(String[] args) {
        String url = null;
        try {
            String reqFileName = defaultRequestFileName;
            String resFileName = null;
            String alias = defaultAlias;
            boolean localCall = defaultLocalCall;
            String keyStoreFile = null;
            String keyStoreType = null;
            String keyStorePassword = null;
            String keyPassword = null;
            boolean isPredefinedUser = false;

            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("-help")) {
                    printUsage();
                } else if (args[i].equalsIgnoreCase("-debug")) {
                    debug = true;
                } else if (args[i].startsWith("req=")) {
                    reqFileName = args[i].substring(4, args[i].length());
                } else if (args[i].startsWith("res=")) {
                    resFileName = args[i].substring(4, args[i].length());
                } else if (args[i].startsWith("scheme=")) {
                    reqFileName = args[i].substring(7, args[i].length());
                    requestIsAScheme = true;
                } else if (args[i].startsWith("alias=")) {
                    alias = args[i].substring(6, args[i].length());

                    if (alias.equalsIgnoreCase("RegistryOperator")) {
                        alias = AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR;
                        isPredefinedUser = true;
                    } else if (alias.equalsIgnoreCase("RegistryGuest")) {
                        alias = AuthenticationServiceImpl.ALIAS_REGISTRY_GUEST;
                        isPredefinedUser = true;
                    } else if (alias.equalsIgnoreCase("Farrukh")) {
                        alias = AuthenticationServiceImpl.ALIAS_FARRUKH;
                        isPredefinedUser = true;
                    } else if (alias.equalsIgnoreCase("Nikola")) {
                        alias = AuthenticationServiceImpl.ALIAS_NIKOLA;
                        isPredefinedUser = true;
                    }
                } else if (args[i].startsWith("url=")) {
                    url = args[i].substring(4, args[i].length());
                } else if (args[i].startsWith("attach=")) {
                    StringTokenizer tokenizer = new StringTokenizer(args[i],
                            "=,");

                    String attachFileName = null;
                    String mimeType = "text/plain";
                    String attachId = "id";

                    int j = 0;

                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();

                        if (j == 1) {
                            attachFileName = token;
                        } else if (j == 2) {
                            mimeType = token;
                        }

                        if (j == 3) {
                            attachId = token;
                        }

                        j++;
                    }

                    AttachmentInfo ai = new AttachmentInfo(attachFileName,
                            mimeType, attachId);
                    attachments.add(ai);
                } else if (args[i].startsWith("localCall=")) {
                    String localCallStr = args[i].substring(10, args[i].length());
                    if (localCallStr.trim().equalsIgnoreCase("false")) {
                        localCall = false;
                    }
                } else if (args[i].startsWith("keyStore=")) {
                    keyStoreFile = args[i].substring(9, args[i].length());
                } else if (args[i].startsWith("keyStoreType=")) {
                    keyStoreType = args[i].substring(13, args[i].length());
                } else if (args[i].startsWith("keyStorePassword=")) {
                    keyStorePassword = args[i].substring(17, args[i].length());
                } else if (args[i].startsWith("keyPassword=")) {
                    keyPassword = args[i].substring(12, args[i].length());
                } else {
                    System.err.println("Unknown parameter: '" + args[i] +
                        "' at position " + i);

                    if (i > 0) {
                        System.err.println("Last valid parameter was '" +
                            args[i - 1] + "'");
                    }

                    printUsage();
                }
            }

            // checking whether enough parameters are provided
            if (reqFileName == null) {
                System.err.println("'req' is mandatory!");
                printUsage();
            }

            if (url == null) {
                System.err.println("'url' is mandatory!");
                printUsage();
            }

            if (keyStoreFile != null) {
                if (keyStoreType == null) {
                    System.err.println(
                        "'keyStoreType' is mandatory for signing if keyStore parameter is provided!");
                    printUsage();
                }

                if ((keyPassword == null) || (keyPassword.length() == 0)) {
                    if (!isPredefinedUser) {
                        System.err.println(
                            "'keyPassword' is mandatory for signing if keyStore parameter is provided and user (alias) is not predefined!");
                        printUsage();
                    } else {
                        keyPassword = alias;
                    }
                }

                if (keyStorePassword == null) {
                    System.err.println(
                        "'keyStorePassword' is mandatory for signing if keyStore parameter is provided!");
                    printUsage();
                }
            }

            SOAPSender sender = new SOAPSender();
            sender.setRequestFileName(reqFileName);
            sender.setRegistryURL(url);
            sender.setAlias(alias);
            sender.setLocalCall(localCall);

            if (keyStoreFile != null) {
                sender.setKeyStore(keyStoreFile, keyStoreType,
                    keyStorePassword, keyPassword);
            }

            sender.setKeyPassword(keyPassword);

            SOAPMessage reply = sender.send();

            if ((resFileName != null) && (reply != null)) {
                FileOutputStream responseFileStream = new FileOutputStream(resFileName);
                reply.writeTo(responseFileStream);
            }

            SOAPPart sp = reply.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            SOAPBody body = se.getBody();

            Iterator iter = body.getChildElements();

            //Skip any text nodes
            Object obj = null;
            SOAPElement rootElem = null;

            while (iter.hasNext()) {
                obj = iter.next();

                if (obj instanceof SOAPElement) {
                    rootElem = (SOAPElement) obj;

                    break;
                }
            }

            if (rootElem != null) {
                String rootElemName = rootElem.getElementName().getLocalName();

                //System.err.println("rootElem = " + rootElemName);
                if (rootElemName.equalsIgnoreCase("RegistryResponse")) {
                    iter = rootElem.getChildElements();

                    //Skip any text nodes
                    obj = null;

                    SOAPElement childElem = null;

                    while (iter.hasNext()) {
                        obj = iter.next();

                        if (obj instanceof SOAPElement) {
                            childElem = (SOAPElement) obj;

                            break;
                        }
                    }

                    if (childElem != null) {
                        String childElemName = childElem.getElementName()
                                                        .getLocalName();

                        //System.err.println("childElem = " + childElemName);
                        if (childElemName.equalsIgnoreCase("RegistryErrorList")) {
                            System.exit(-1);
                        }
                    }
                }
            }

            System.exit(0);
        } catch (RegistryException e) {
            e.printStackTrace();

            Throwable t = e.getCause();

            if (t != null) {
                System.err.println("Nested exception was: ");
                t.printStackTrace();
            }

            System.exit(-1);
        } catch (Exception e) {
            System.out.println("Exception connecting to URL: " + url);
            System.out.println("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private SOAPMessage createSOAPMessage(String alias, String reqFileName)
        throws RegistryException, IOException, FileNotFoundException, 
            SOAPException, ParseException, KeyStoreException, 
            NoSuchAlgorithmException, UnrecoverableKeyException, JAXRException {
        SOAPMessage msg = null;
        File file = new File(reqFileName);
        FileInputStream fis = new FileInputStream(file);

        if (requestIsAScheme) {
        }

        InputStream soapStream = org.freebxml.omar.server.common.Utility.getInstance()
                                        .createSOAPStreamFromRequestStream(fis);

        // Utility.getInstance().createSOAPMessageFromRequestStream(docInStream);
        fis.close();

        msg = org.freebxml.omar.server.common.Utility.getInstance().createSOAPMessageFromSOAPStream(soapStream);

        // System.err.println(alias + "!!!!!");
        if (alias != null) {
            // use the keystore specified in ebxmlrr.properties
            if (keyStore == null) {
                // Lazy initialize AuthenticationServiceImpl
                if (authc == null) {
                    authc = AuthenticationServiceImpl.getInstance();
                }
                certs = authc.getCertificateChain(alias);
                privateKey = authc.getPrivateKey(alias, alias);
            } else {
                // use other keystore
                certs = keyStore.getCertificateChain(alias);

                //System.err.println(keyPassword);
                privateKey = (PrivateKey) keyStore.getKey(alias,
                        stringToCharArray(keyPassword));
            }

            if (privateKey == null) {
                System.err.println("No private key with alias " + "'" + alias +
                    "'");
                System.exit(-1);
            }

            File soapFile = new File("signedSOAPRequest.xml");
            
            //Now add attachments. Do so before signing.
            Iterator attachIter = attachments.iterator();
            try{
            while (attachIter.hasNext()) {
                AttachmentInfo ai = (AttachmentInfo) attachIter.next();
                addAttachment(ai.id, ai.fileName, ai.mimeType);
            }
            Iterator iter = attachmentMap.keySet().iterator();

            while (iter.hasNext()) {
                String id = (String) iter.next();
                DataHandler dh = (DataHandler) attachmentMap.get(id);
                String cid = SoapSecurityUtil.convertUUIDToContentId(id);
                AttachmentPart ap = msg.createAttachmentPart(dh);
                ap.setContentId(cid);
                msg.addAttachmentPart(ap);
            }

	}catch(Exception ex){
            ex.printStackTrace();
        }
            CredentialInfo credentialInfo = new CredentialInfo(null,
                    (X509Certificate)certs[0], certs, privateKey);
            //(new SOAPMessagePersister()).save(msg,"/tmp/msg2.txt");
            msg = SoapSecurityUtil.getInstance().signSoapMessage(msg, credentialInfo);
        }

        return msg;
    }

    public void addAttachment(String id, String fileName, final String mimeType)
        throws FileNotFoundException, MessagingException, RegistryException {
        // Create a DataHandler for the attachment
        File attachFile = new File(fileName);
        FileDataSource ds = new FileDataSource(attachFile);
        DataHandler dh = new DataHandler(ds);
        if (mimeType != null) {
            ds.setFileTypeMap(
                new FileTypeMap() {
                    public String getContentType(File file) { return mimeType; }
                    public String getContentType(String fileName) { return mimeType; }
                }
            );
        }
        attachmentMap.put(id, dh);
    }

    public SOAPMessage send()
        throws RegistryException, ServletException, SOAPException, IOException, 
            ParseException, MessagingException, KeyStoreException, 
            NoSuchAlgorithmException, UnrecoverableKeyException, 
            ParserConfigurationException, SAXException, JAXRException {
        msg = createSOAPMessage(alias, reqFileName);

        return sendSOAPMessage(msg, this.registryURL);
    }

    private static SOAPMessage sendSOAPMessage(SOAPMessage msg, String url)
        throws ServletException, SOAPException, MessagingException, 
            RegistryException, IOException {
        if (url == null) {
            throw new RegistryException(
                "Destination URL for SOAP message not set.");
        }

        //Send the SOAPMesssage
        SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
        SOAPConnection connection = scf.createConnection();

        //System.err.println("Sending folowing SOAP request:\n");
        FileOutputStream os = new FileOutputStream("senderReq.mime");
        msg.writeTo(os);
        os.close();

        long t1 = System.currentTimeMillis();

        SOAPMessage reply = null;

        if (localCall) {
            RegistrySOAPServlet servlet = new RegistrySOAPServlet();
            servlet.init();
            reply = servlet.onMessage(msg, null, null);
        } else {
            reply = connection.call(msg, url);
        }

        long t2 = System.currentTimeMillis();

        if (reply != null) {
            processReplyAttachments(reply);
            reply.writeTo(System.err);
        }

        System.err.println("Elapsed time in seconds: " + ((t2 - t1) / 1000));

        return reply;
    }

    private static void processReplyAttachments(SOAPMessage reply)
        throws SOAPException, MessagingException, RegistryException {
            
        try {
            Iterator apIter = reply.getAttachments();

            while (apIter.hasNext()) {
                AttachmentPart ap = (AttachmentPart) apIter.next();

                String cid = ap.getContentId();
                String id = SoapSecurityUtil.convertContentIdToUUID(cid);
                
                System.err.println("Processing repository item with contentId: '" +
                    id + "'");

                DataHandler dh = ap.getDataHandler();

                Object o = dh.getContent();

                String contentType = dh.getContentType();
                System.out.println("DataHandler, name=" + dh.getName() + ", type=" +
                     contentType + ", content: (" + o.getClass().getName() + ")\n" +
                    o);

                DataSource ds = dh.getDataSource();

                String extension = ".out";
                int slashIndex = contentType.indexOf("/");
                int contentTypeLen = contentType.length();

                if ((slashIndex >= 0) && (contentTypeLen > (slashIndex + 1))) {
                    extension = contentType.substring(slashIndex + 1,
                            contentTypeLen);
                }

                //write attachment to file
                //with appropriate ending.
                File attachFile = File.createTempFile("attachFile-" + Utility.getInstance().stripId(id), "." + extension);
                attachFile.deleteOnExit();
                
                InputStream inputStream = ds.getInputStream();
                FileOutputStream attachFileOs = new FileOutputStream(attachFile);
                OutputStream attachOs = new BufferedOutputStream(attachFileOs);

                ByteArrayOutputStream byteArrayOs = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int len;

                while (true) {
                    len = inputStream.read(buf);

                    if (len < 0) {
                        break;
                    }

                    byteArrayOs.write(buf, 0, len);
                }

                byte[] data = byteArrayOs.toByteArray();
                attachOs.write(data);
                attachOs.flush();
                attachOs.close();
            }

            /*
            If we do not remove the attachment, the SOAPMessage.writeTo() method
            will output a MIME-encoded form of the message, not pure SOAP part.
            */
            if (reply.countAttachments() > 0) {
                reply.removeAllAttachments();

                if (reply.saveRequired()) {
                    reply.saveChanges();
                }
            }
        } catch (Exception e) {
            String errmsg = "[SOAPSender::processReplyAttachments()] -->" +
                " Exception writing attachment to file ..." + e;
            System.err.println(errmsg);
            throw new RegistryException(errmsg, e);
        }
    }

    private static class AttachmentInfo {
        String id = null;
        String fileName = null;
        String mimeType = null;

        public AttachmentInfo(String fileName, String mimeType, String id) {
            this.id = id;
            this.fileName = fileName;
            this.mimeType = mimeType;
        }
    }
}
