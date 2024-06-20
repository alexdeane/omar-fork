/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec11/ExtendedImportCertificateTokenFilter.java,v 1.2 2005/06/11 00:20:53 geomurr Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec11;


import com.sun.xml.wss.filter.FilterBase;
import java.security.cert.X509Certificate;
import java.util.logging.Level;

import com.sun.xml.wss.MessageConstants;
import com.sun.xml.wss.MessageFilter;
import com.sun.xml.wss.SecurableSoapMessage;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.SecurityHeader;
import com.sun.xml.wss.X509SecurityToken;
import com.sun.xml.wss.XWSSecurityException;
import org.freebxml.omar.common.security.*;
import org.freebxml.omar.common.CommonResourceBundle;

/**
 * Extends the ImportCertificateTokenFilter in order to be able to return the
 * imported certificate (getLastCert method).
 *
 * NOTE: This class shall be replaced by the original once XWS-Security support
 * the required features.
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class ExtendedImportCertificateTokenFilter extends FilterBase
    implements MessageFilter
{
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
    private boolean validateCert = true;
    private X509Certificate lastCert;
    
    public void process(SecurableSoapMessage secureMessage)
	throws XWSSecurityException {
        lastCert = null;
        X509Certificate cert;
	SecurityHeader wsseSecurity = secureMessage.findSecurityHeader();
	X509SecurityToken token = null;
	try {
	    token = (X509SecurityToken)wsseSecurity
                    .getCurrentHeaderBlock(X509SecurityToken.class);
	} catch (XWSSecurityException ex) {
	    throw SecurableSoapMessage.newSOAPFaultException
		      (MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
		       resourceBundle.getString("message.importingX509CertificateToken"), ex);
	}
	String tokenId = token.getId();
	secureMessage.setToken(tokenId, token);
	secureMessage.setFilterParameter("TokenId", tokenId);
	secureMessage.setFilterParameter("Token", token);
	try {
	    cert = token.getCertificate();
	    if (validateCert) {
		SecurityEnvironment secEnv
		    = secureMessage.getSecurityEnvironment();
		if (!secEnv.validateCertificate(cert))
		    throw new XWSSecurityException
			      (resourceBundle.getString("message.certificateValidationFailed"));
	    }
	} catch (XWSSecurityException xwsse) {
	    FilterBase.log.log(Level.SEVERE,
			       CommonResourceBundle.getInstance().getString("message.WSS0156.exceptionIn.cert.validate",
			       new Object[] { xwsse.getMessage() }));
	    throw SecurableSoapMessage.newSOAPFaultException
		      (MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
		       xwsse.getMessage(), xwsse);
	}
        lastCert = cert;
    }
    
    public X509Certificate getLastCert() {
        return lastCert;
    }
}