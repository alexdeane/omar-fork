/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/SoapSecurityUtil.java,v 1.18 2007/03/15 17:18:15 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.CommonProperties;
import org.freebxml.omar.common.CredentialInfo;
import org.freebxml.omar.common.CommonResourceBundle;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;


/**
 * An util class / SPI for sign/verify SOAP messages.
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public abstract class SoapSecurityUtil {
    
    /** wsu:Id for BinarySecurityToken, defined by RS specs. */
    public static final String BINARY_SECURITY_TOKEN_WSU_ID =
            "urn:oasis:names:tc:ebxml-regrep:rs:security:SenderCert";
    
    public static boolean ignoreSignatureVerificationErrors = Boolean.valueOf(CommonProperties.getInstance()
    .getProperty("org.freebxml.omar.common.security.ignoreSignatureVerificationErrors", "false")).booleanValue();
    
    public static boolean logSignatureVerificationErrors = Boolean.valueOf(CommonProperties.getInstance()
    .getProperty("org.freebxml.omar.common.security.logSignatureVerificationErrors", "true")).booleanValue();
    
    /** The log */
    private static final Log log = LogFactory.getLog(SoapSecurityUtil.class);
    
    /** Date Formatter used for Timestamp Filter */
    protected static final SimpleDateFormat dateFormatter
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    /** Class which appears in XWSS 1.0 and 1.1, nothing later */
    private static final String XWSS_10_INDICATOR_CLASS =
            "com.sun.xml.wss.configuration.DeclarativeSecurityConfiguration";
    /** Class which appears first in XWSS 1.1 */
    private static final String XWSS_11_INDICATOR_CLASS =
            "com.sun.xml.wss.ProcessingContext";
    /** Class which appears only in XWSS 2.0 EA */
    private static final String XWSS_20EA_INDICATOR_CLASS =
            "com.sun.xml.wss.impl.configuration.DeclarativeSecurityConfiguration";
    /** Class which appears in XWSS 2.0 FCS (and later, we hope) */
    private static final String XWSS_20FCS_INDICATOR_CLASS =
            "com.sun.xml.wss.impl.config.DeclarativeSecurityConfiguration";
    
    /** All available XWSS indicator classes, starting with the most recent
     * and therefore likely most used XWSS version.  If the order were
     * reversed, indicator classes must be changed since
     * XWSS_11_INDICATOR_CLASS appears in all later XWSS implementations.
     */
    private static final String XWSS_INDICATOR_CLASSES[] = {
        XWSS_20FCS_INDICATOR_CLASS,
        XWSS_20EA_INDICATOR_CLASS,
        XWSS_11_INDICATOR_CLASS,
        XWSS_10_INDICATOR_CLASS
    };
    
    /** Omar-internal security provider class for XWSS 1.0 */
    private static final String XWSS_10_PROVIDER_CLASS =
            "org.freebxml.omar.common.security.xwssec10.XWSSec10SoapSecurityUtil";
    /** Omar-internal security provider class for XWSS 1.1 */
    private static final String XWSS_11_PROVIDER_CLASS =
            "org.freebxml.omar.common.security.xwssec11.XWSSec11SoapSecurityUtil";
    /** Omar-internal security provider class for XWSS 2.0 EA */
    private static final String XWSS_20EA_PROVIDER_CLASS =
            "org.freebxml.omar.common.security.xwssec20.XWSSec20SoapSecurityUtil";
    /** Omar-internal security provider class for XWSS 2.0 FCS (and later,
     * we hope)
     */
    private static final String XWSS_20FCS_PROVIDER_CLASS =
            "org.freebxml.omar.common.security.xwssec20FCS.XWSSec20SoapSecurityUtil";
    
    /** All available XWSS provider (shim, driver, whatever) classes.  The
     * order here must align with the element order in
     * XWSS_INDICATOR_CLASSES.
     */
    private static final String XWSS_PROVIDER_CLASSES[] = {
        XWSS_20FCS_PROVIDER_CLASS,
        XWSS_20EA_PROVIDER_CLASS,
        XWSS_11_PROVIDER_CLASS,
        XWSS_10_PROVIDER_CLASS
    };
    
    /** For debugging purposes, the chosen provider class name */
    private static String soapSecProviderClass = null;
    
    /** Singleton instance */
    private static SoapSecurityUtil instance = null;
    
    /** Creates a new instance of SoapSecurityUtil */
    protected SoapSecurityUtil() {
    }
    
    /**
     * Implements Singleton pattern w/ special case: Determine XWSS version
     * available in our classpath.  Then, load the version-specific "shim"
     * and return that instance.  If anything goes wrong, use no-op
     * StubSoapSecurityUtil.
     *
     * @return SoapSecurityUtil singleton instance.
     */
    public static synchronized SoapSecurityUtil getInstance() {
        if (instance == null) {
            
            // Try loading available indicators and associated providers.
            // Since each provider will work with just one XWSS version,
            // stop after first indicator is found.
            int classIndex = 0;
            for ( ; XWSS_INDICATOR_CLASSES.length > classIndex; classIndex++) {
                String className = XWSS_INDICATOR_CLASSES[classIndex];
                try {
                    if (null != Class.forName(className)) {
                        className = XWSS_PROVIDER_CLASSES[classIndex];
                        try {
                            Class clazz = Class.forName(className);
                            Constructor cnstr =
                                    clazz.getConstructor((Class[])null);
                            instance = (SoapSecurityUtil)cnstr.
                                    newInstance((Object[])null);
                        } catch (ClassNotFoundException e) {
                            // Not built with the appropriate provider!
                            log.error(CommonResourceBundle.getInstance().
                                    getString("message.SOAPSecurityProviderClassNotFound",
                                    new Object[]{className}), e);
                        } catch (Exception e) {
                            log.error(CommonResourceBundle.getInstance().
                                    getString("message.InternalErrorInSOAPSecurityProvider",
                                    new Object[]{className}), e);
                        }
                        
                        // All set (we hope), no reason to check further
                        break;
                    }
                } catch (ClassNotFoundException e) {
                    // No problem, try the next one
                } catch (Exception e) {
                    // Odd, let's hope another one works
                    log.warn(CommonResourceBundle.getInstance().getString(
                            "message.ExceptionWhileLoadingXWSSecurityClass",
                            new Object[]{className}),
                            e);
                }
            }
            
            if (null == instance) {
                // Fall back to our no-op stub
                instance = new StubSoapSecurityUtil();
                soapSecProviderClass = instance.getClass().getName();
                log.warn(CommonResourceBundle.getInstance().getString(
                        "message.UsingDefaultSOAPSecurityProviderClass",
                        new Object[]{soapSecProviderClass}));
            } else {
                // Save name of class we actually loaded, logging if requested
                soapSecProviderClass = instance.getClass().getName();
                if (log.isDebugEnabled()) {
                    log.debug(CommonResourceBundle.getInstance().getString(
                            "message.UsingSOAPSecurityProviderClass",
                            new Object[]{soapSecProviderClass}));
                }
            }
        }
        return instance;
    }
    
    /**
     * Signs the given SOAP message with the private key provided in
     * CredentialInfo.  Includes the X509Certificate as binary security
     * token for authentication by verifySoapMessage method.
     *
     * @param msg {@link SOAPMessage} to be signed
     * @param credentialInfo {@link CredentialInfo} wrapper for SOAP
     * annotator's private key
     * @return newly-signed {@link SOAPMessage}
     * @throws JAXRException if any problem occurs; more generally, wraps
     * any {@link Exception}
     */
    public abstract SOAPMessage signSoapMessage(SOAPMessage msg, CredentialInfo credentialInfo)
    throws JAXRException;
    
    /**
     * Verifies the given SOAP message's signature. Extracts the
     * X509Certificate provided as binary security token into
     * credentialInfo (for authentication).  It is <i>not</i> an error to
     * pass an unsigned message to this method.
     *
     * @param msg {@link SOAPMessage} to be verified
     * @param credentialInfo {@link CredentialInfo} placeholder for SOAP
     * annotator's private key; discovered credentials will be added to
     * this object
     * @return true if given message is signed and valid; false if message
     * contains no WSS Security token
     * @throws JAXRException if signature is invalid; more generally, wraps
     * any {@link Exception} which occurs
     */
    public abstract boolean verifySoapMessage(SOAPMessage msg, CredentialInfo credentialInfo)
    throws JAXRException;
    
    /**
     * Convenience method to extract an UUID/URN from a Content ID (CID). CIDs are
     * used for identifying attachments in signed SOAP messages.
     *
     * @param cid The attachment's Content-ID.
     * @return The corresponding UUID/URN.
     */
    public static String convertContentIdToUUID(String cid)
    throws RegistryException {
        if (!(cid.charAt(0) == '<' && cid.charAt(cid.length() - 1) == '>')) {
            // error, not a cid URI Scheme id.
            throw new RegistryException(CommonResourceBundle.getInstance().getString("message.CIDURIExpected", new Object[]{cid}));
        }
        
        String uuid = cid.substring(1,  cid.length() - 1);
        return uuid;
    }
    
    /**
     * Convenience method to turn an UUID/URN into a Content ID (CID). CIDs are
     * used for identifying attachments in signed SOAP messages.
     *
     * @param uuid The original UUID/URN.
     * @return The generated CID to be set as attachment Content-ID.
     */
    public static String convertUUIDToContentId(String uuid) {
        String cid = "<" + uuid + ">";
        return cid;
    }
    
    /** Dummy implementation for when XWSS or our shim is not available. */
    static class StubSoapSecurityUtil extends SoapSecurityUtil {
        /**
         * {@inheritDoc} This dummy implementation returns given {@code
         * msg} unchanged and ignores {@code credentialInfo}.
         */
        public SOAPMessage signSoapMessage(SOAPMessage msg, CredentialInfo credentialInfo)
        throws JAXRException {
            //NOOP
            
            // Some other code should authenticate the connection
            // so that soap annotator would identify itself
            return msg;
        }
        
        /**
         * {@inheritDoc} This dummy implementation always returns {@code
         * true} and ignores given parameters.
         */
        public boolean verifySoapMessage(SOAPMessage msg, CredentialInfo credentialInfo)
        throws JAXRException {
            //NOOP
            
            // Some other code should get X509Certificate into credentialInfo
            // so that soap annotator would be recognized
            return true;
        }
    }
}
