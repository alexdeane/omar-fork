/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec10/ExtendedSignatureHeaderBlock.java,v 1.2 2005/06/11 00:20:53 geomurr Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec10;

import com.sun.org.apache.xml.security.signature.XMLSignature;
import com.sun.org.apache.xml.security.signature.XMLSignatureException;
import com.sun.org.apache.xml.security.signature.XMLSignatureInput;
import com.sun.org.apache.xml.security.utils.resolver.ResourceResolver;
import com.sun.org.apache.xml.security.utils.resolver.ResourceResolverException;
import com.sun.org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import com.sun.xml.wss.SignatureHeaderBlock;
import com.sun.xml.wss.XWSSecurityException;
import java.io.IOException;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.freebxml.omar.common.security.*;
import org.freebxml.omar.common.CommonResourceBundle;

/**
 * Extends SignatureHeaderBlock in order to:
 * - Add a custom ResourceResolver to XMLSignature before sign/verify. This will
 *   resolve CID Schema URIs (used for SOAP attachment parts).
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class ExtendedSignatureHeaderBlock  extends SignatureHeaderBlock {
    
    /** The log. */
    private static Logger log
            = Logger.getLogger("javax.enterprise.resource.webservices.security",
            "com.sun.xml.wss.LogStrings");
    
    /** The parent SOAP message, initialized by the constructors. */
    protected SOAPMessage soapMessage;
    
    public ExtendedSignatureHeaderBlock(XMLSignature signature)
    throws XWSSecurityException {
        super(signature);
    }
    
    public ExtendedSignatureHeaderBlock(SOAPElement elem)
    throws XWSSecurityException {
        super(elem);
    }
    
    public ExtendedSignatureHeaderBlock(Document doc, String signatureMethodURI)
    throws XWSSecurityException {
        super(doc, signatureMethodURI);
    }
    
    public void sign(Key signingKey) throws XWSSecurityException {
        try {
            XMLSignature signature = getSignature();
            signature.addResourceResolver(new ResourceResolver(new CIDResourceResolverSpi()));
            signature.sign(signingKey);
            // dirty = true
            saveChanges();
        } catch (XMLSignatureException e) {
            log.log(Level.SEVERE, CommonResourceBundle.getInstance().getString("message.WSS0323.exception.while.signing"), e);
            throw new XWSSecurityException(e);
        }
    }
    
    public boolean checkSignatureValue(Key pk) throws XWSSecurityException {
        boolean bool;
        try {
            XMLSignature signature = getSignature();
            signature.addResourceResolver(new ResourceResolver(new CIDResourceResolverSpi()));
            bool = signature.checkSignatureValue(pk);
        } catch (XMLSignatureException e) {
            log.log(Level.SEVERE, CommonResourceBundle.getInstance().getString("message.WSS0326.exception.verifying.signature"), e);
            throw new XWSSecurityException(e);
        }
        return bool;
    }
    
    public boolean checkSignatureValue(X509Certificate cert)
    throws XWSSecurityException {
        boolean bool;
        try {
            XMLSignature signature = getSignature();
            signature.addResourceResolver(new ResourceResolver(new CIDResourceResolverSpi()));
            bool = signature.checkSignatureValue(cert);
        } catch (XMLSignatureException e) {
            log.log(Level.SEVERE, CommonResourceBundle.getInstance().getString("message.WSS0326.exception.verifying.signature"), e);
            throw new XWSSecurityException(e);
        }
        return bool;
    }
    
    /**
     * Getter for property soapMessage.
     * @return Value of property soapMessage.
     */
    public SOAPMessage getSoapMessage() {
        
        return this.soapMessage;
    }
    
    /**
     * Setter for property soapMessage.
     * @param soapMessage New value of property soapMessage.
     */
    public void setSoapMessage(SOAPMessage soapMessage) {
        
        this.soapMessage = soapMessage;
    }
    
    /**
     * A ResourceResolverSpi for the CID Schema URL:
     *
     * A content id of "foo" may be specified in a MIME part with MIME header
     * "Content-ID: <foo>" and be referenced using the CID Schema URL "cid:foo"
     **/
    class CIDResourceResolverSpi extends ResourceResolverSpi {
        
        public boolean engineCanResolve(org.w3c.dom.Attr uri, java.lang.String base) {
            return uri.getValue().startsWith("cid:");
        }
        
        public XMLSignatureInput engineResolve(org.w3c.dom.Attr uri, java.lang.String base) {
            try {
                MimeHeaders headers = new MimeHeaders();
                headers.addHeader("Content-ID", "<" + uri.getValue().substring("cid:".length()) + ">");
                Iterator attachments = ExtendedSignatureHeaderBlock.this.soapMessage.getAttachments(headers);
                if (!attachments.hasNext()) {
                    log.log(Level.SEVERE, CommonResourceBundle.getInstance().getString("message.NoAttachmentResourceForUR", new Object[]{ uri}));
                } else {
                    AttachmentPart attachment = (AttachmentPart)attachments.next();
                    if (attachments.hasNext()) {
                        log.log(Level.SEVERE, CommonResourceBundle.getInstance().getString("message.MultipleAttachmentResourceForURI", new Object[]{ uri}));
                    } else {
                        return new XMLSignatureInput(attachment.getDataHandler().getInputStream());
                    }
                }
            } catch (SOAPException e) {
                log.log(Level.SEVERE, CommonResourceBundle.getInstance().getString("message.ExceptionResolvingURI", new Object[]{ uri}), e);
            } catch (IOException e) {
                log.log(Level.SEVERE, CommonResourceBundle.getInstance().getString("message.ExceptionResolvingURI", new Object[]{ uri}), e);
            }
            return new XMLSignatureInput("");
        }
    }
}
