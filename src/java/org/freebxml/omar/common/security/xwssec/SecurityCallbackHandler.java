/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec/SecurityCallbackHandler.java,v 1.4 2006/06/05 20:38:00 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;
import com.sun.xml.wss.impl.callback.PropertyCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureVerificationKeyCallback;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.CommonProperties;
import org.freebxml.omar.common.CredentialInfo;
import org.freebxml.omar.common.CommonResourceBundle;

/**
 * A CallbackHandler for XWS-Security Callbacks. This implementation receives a
 * CredentialInfo object (in constructor) and uses that to provide keys.
 * 
 * This might evolve into a class that can access keystores directly.

 * @author Diego Ballve / Digital Artefacts Europe
 */
public class SecurityCallbackHandler implements CallbackHandler{
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
    
    private static Log log = LogFactory.getLog(SecurityCallbackHandler.class);

    /** Key for configuration property 'max clock skew', passed to xws-security API.
     *  "The assumed maximum skew (milliseconds) between the local times of any two systems."
     */
    public static final String KEY_MAX_CLOCK_SKEW = "omar.common.security.maxClockSkew";
    
    /** The credentials for this handler, provided by constructor. */
    protected CredentialInfo credentialInfo;
    
    /** Creates a new instance of SecurityCalbackHandler */
    public SecurityCallbackHandler(CredentialInfo credentialInfo) {
        if (credentialInfo == null) {
            throw new IllegalArgumentException(resourceBundle.getString("message.credentialInfo"));
        }
        this.credentialInfo = credentialInfo;
    }
    
    /**
     * Retrieve the information requested in the provided Callbacks. Handles the
     * XW-Security Callbacks providing information from the CredentialInfo.
     *
     * @param callbacks - an array of Callback objects.
     */
    public void handle(Callback callbacks[])
	throws UnsupportedCallbackException {

        for (int i = 0; i < callbacks.length; i++) {
            if (log.isTraceEnabled()) {
                log.trace("Handling callback: " +
			  callbacks[i].getClass().getName());
            }
            
            // Sign
            if (callbacks[i] instanceof SignatureKeyCallback) {
                SignatureKeyCallback.PrivKeyCertRequest request =
                        (SignatureKeyCallback.PrivKeyCertRequest)
                        ((SignatureKeyCallback)callbacks[i]).getRequest();
                request.setPrivateKey(credentialInfo.privateKey);
                request.setX509Certificate(credentialInfo.cert);
                
            // Verify signature
            } else if (callbacks[i] instanceof SignatureVerificationKeyCallback) {
                try {
                    SignatureVerificationKeyCallback.X509CertificateRequest request =
                            (SignatureVerificationKeyCallback.X509CertificateRequest)
                            ((SignatureVerificationKeyCallback)callbacks[i]).getRequest();
                    //request.setX509Certificate(se.getDefaultCertificate());
                    request.setX509Certificate(credentialInfo.cert);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                
            // verify certificate
            } else if (callbacks[i] instanceof CertificateValidationCallback) {
                CertificateValidationCallback cb = (CertificateValidationCallback)callbacks[i];
                cb.setValidator(new X509CertificateValidatorImpl());
                
            // property callback (for timestamp)
            } else if (callbacks[i] instanceof PropertyCallback) {
                String maxClockSkew = CommonProperties.getInstance().getProperty(KEY_MAX_CLOCK_SKEW);
                if (maxClockSkew != null) {
                    try {
                        PropertyCallback cb = (PropertyCallback)callbacks[i];
                        cb.setMaxClockSkew(Long.parseLong(maxClockSkew));
                    } catch (NumberFormatException e) {
                        log.warn(CommonResourceBundle.getInstance().getString("message.InvalidlongValueForProperty", new Object[]{maxClockSkew}), e);
                    }
                }
                
                
            // unsuported
            } else {
                log.debug(CommonResourceBundle.getInstance().
			  getString("message.UNSUPPORTEDCallback",
				    new String[]{callbacks[i].getClass().
						 getName()}));
		throw new UnsupportedCallbackException(callbacks[i]);
            }
        }
    }
    
    // From xws-signature simple sample
    private class X509CertificateValidatorImpl implements CertificateValidationCallback.CertificateValidator {
        
        public boolean validate(X509Certificate certificate)
        throws CertificateValidationCallback.CertificateValidationException {
            
            if (isSelfCert(certificate)) {
                return true;
            }
            
            try {
                certificate.checkValidity();
            } catch (CertificateExpiredException e) {
                e.printStackTrace();
                throw new CertificateValidationCallback.CertificateValidationException(resourceBundle.getString("message.X509CertificateExpired"), e);
            } catch (CertificateNotYetValidException e) {
                e.printStackTrace();
                throw new CertificateValidationCallback.CertificateValidationException(resourceBundle.getString("message.X509CertificateNotValid"), e);
            }
            
            //            X509CertSelector certSelector = new X509CertSelector();
            //            certSelector.setCertificate(certificate);
            //
            //            PKIXBuilderParameters parameters;
            //            CertPathBuilder builder;
            //            try {
            //                if (trustStore == null)
            //                    initTrustStore();
            //                parameters = new PKIXBuilderParameters(trustStore, certSelector);
            //                parameters.setRevocationEnabled(false);
            //                builder = CertPathBuilder.getInstance("PKIX");
            //            } catch (Exception e) {
            //                e.printStackTrace();
            //                throw new CertificateValidationCallback.CertificateValidationException(e.getMessage(), e);
            //            }
            //
            //            try {
            //                PKIXCertPathBuilderResult result =
            //                    (PKIXCertPathBuilderResult) builder.build(parameters);
            //            } catch (Exception e) {
            //                e.printStackTrace();
            //                return false;
            //            }
            return true;
        }
        
        private boolean isSelfCert(X509Certificate cert)
        throws CertificateValidationCallback.CertificateValidationException {
            //            try {
            //                if (keyStore == null)
            //                    initKeyStore();
            //                Enumeration aliases = keyStore.aliases();
            //                while (aliases.hasMoreElements()) {
            //                    String alias = (String) aliases.nextElement();
            //                    if (keyStore.isKeyEntry(alias)) {
            //                        X509Certificate x509Cert =
            //                            (X509Certificate) keyStore.getCertificate(alias);
            //                        if (x509Cert != null) {
            //                            if (x509Cert.equals(cert))
            return true;
            //                        }
            //                    }
            //                }
            //                return false;
            //            } catch (Exception e) {
            //                e.printStackTrace();
            //                throw new CertificateValidationCallback.CertificateValidationException(e.getMessage(), e);
            //            }
        }
    }
}
