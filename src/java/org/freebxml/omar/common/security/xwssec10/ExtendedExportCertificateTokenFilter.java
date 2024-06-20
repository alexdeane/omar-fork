/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec10/ExtendedExportCertificateTokenFilter.java,v 1.1 2005/04/14 15:06:44 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec10;

import com.sun.xml.wss.MessageFilter;
import com.sun.xml.wss.SecurableSoapMessage;
import com.sun.xml.wss.SecurityHeader;
import com.sun.xml.wss.X509SecurityToken;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.filter.FilterBase;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import org.freebxml.omar.common.security.*;

/**
 * Extends the ExportCertificateTokenFilter to support pre-generated wsuId.
 *
 * NOTE: This class shall be replaced by the original once XWS-Security support
 * the required features.
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class ExtendedExportCertificateTokenFilter extends FilterBase implements MessageFilter {

    protected String alias;
    protected String wsuId;
    protected X509Certificate cert;
    
    public ExtendedExportCertificateTokenFilter(String alias, String wsuId) {
        this.alias = alias;
        this.wsuId = wsuId;
    }

    public ExtendedExportCertificateTokenFilter(X509Certificate cert, String wsuId) {
        this.cert = cert;
        this.wsuId = wsuId;
    }
    
    public void process(SecurableSoapMessage secureMessage)
    throws XWSSecurityException {
        SecurityHeader wsseSecurity = secureMessage.findOrCreateSecurityHeader();
        if (cert == null) {
            cert = secureMessage.getSecurityEnvironment().getCertificate(alias, true);
        }
        exportCertificateToken(secureMessage, wsseSecurity, cert, wsuId);
    }
    
    protected void exportCertificateToken(SecurableSoapMessage ssm, SecurityHeader wsseSecurity,
    X509Certificate cert, String wsuId) throws XWSSecurityException {
        X509SecurityToken token
                = new X509SecurityToken(ssm.getSOAPPart(), cert, wsuId);
        ssm.setToken(wsuId, token);
        wsseSecurity.insertHeaderBlock(token);
    }
}
