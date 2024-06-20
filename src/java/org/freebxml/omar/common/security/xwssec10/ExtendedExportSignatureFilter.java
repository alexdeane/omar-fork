/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec10/ExtendedExportSignatureFilter.java,v 1.3 2005/06/11 00:20:53 geomurr Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec10;

import com.sun.xml.wss.filter.FilterBase;
import java.util.logging.Level;

import com.sun.xml.wss.KeyInfoHeaderBlock;
import com.sun.xml.wss.KeyInfoStrategy;
import com.sun.xml.wss.MessageFilter;
import com.sun.xml.wss.SecurableSoapMessage;
import com.sun.xml.wss.SignatureHeaderBlock;
import com.sun.xml.wss.XWSSecurityException;
import org.freebxml.omar.common.security.*;
import org.freebxml.omar.common.CommonResourceBundle;

/**
 * Extends the ExportSignatureFilter to support dsa-sha1 signature method.
 *
 * NOTE: This class shall be replaced by the original once XWS-Security support
 * the required features.
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class ExtendedExportSignatureFilter extends FilterBase implements MessageFilter
{
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
    private String algorithm;
    private String x509TokenId;
    
    public ExtendedExportSignatureFilter() throws XWSSecurityException {
	/* empty */
    }
    
    public ExtendedExportSignatureFilter(KeyInfoStrategy keyInfoStrategy, String algorithm)
	throws XWSSecurityException {
	this.keyInfoStrategy = keyInfoStrategy;
	this.algorithm = algorithm;
    }
    
    public void init() throws XWSSecurityException {
	if (getParameter("x509TokenId") != null)
	    x509TokenId = getParameter("x509TokenId");
	if (algorithm == null) {
	    FilterBase.log.log(Level.SEVERE, CommonResourceBundle.getInstance().getString("message.WSS0185.filterparameter.not.set",
			       new Object[] { "algorithm" }));
	    throw new XWSSecurityException
		      (resourceBundle.getString("message.filterAlgorithmNotSet"));
        }
	if (keyInfoStrategy == null) {
	    FilterBase.log.log(Level.SEVERE, CommonResourceBundle.getInstance().getString("message.WSS0185.filterparameter.not.set",
			       new Object[] { "keyinfostrategy" }));
	    throw new XWSSecurityException
		      (resourceBundle.getString("message.filterKeyinfostrategyNotSet"));
	}
    }
    
    public void process(SecurableSoapMessage secureMessage)
	throws XWSSecurityException {
	javax.xml.soap.SOAPPart soapPart = secureMessage.getSOAPPart();
	String signMethod = null;
	if (algorithm.equalsIgnoreCase("DSA")) {
	    signMethod = "http://www.w3.org/2000/09/xmldsig#dsa-sha1";
        } else if (algorithm.equalsIgnoreCase("RSA")) {
	    signMethod = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
        } else {
	    FilterBase.log.log(Level.SEVERE,
			       CommonResourceBundle.getInstance().getString("message.WSS0198.unsupported.signature.algorithm",
			       new String[]{signMethod}));
	    throw new XWSSecurityException(resourceBundle.getString("message.unsupportedSignAlgorithm",
					   new String[]{algorithm}));
	}
	ExtendedSignatureHeaderBlock sigBlock
	    = new ExtendedSignatureHeaderBlock(soapPart, signMethod);
	KeyInfoHeaderBlock keyInfoBlock = sigBlock.getKeyInfoHeaderBlock();
	String alias = keyInfoStrategy.getAlias();
	keyInfoStrategy.setCertificate(secureMessage.getSecurityEnvironment
					   ().getCertificate(alias, true));
	keyInfoStrategy.insertKey(keyInfoBlock, secureMessage, x509TokenId);
	secureMessage.findOrCreateSecurityHeader().insertHeaderBlock(sigBlock);
	secureMessage.setFilterParameter("Signature", sigBlock);
	secureMessage.setFilterParameter("KeyInfoStrategy", keyInfoStrategy);
    }
    
}
