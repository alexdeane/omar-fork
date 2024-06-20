/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec10/TransformAttachmentContentOnly.java,v 1.1 2005/04/14 15:06:44 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec10;

import com.sun.org.apache.xml.security.c14n.CanonicalizationException;
import com.sun.org.apache.xml.security.c14n.InvalidCanonicalizerException;
import com.sun.org.apache.xml.security.signature.XMLSignatureInput;
import com.sun.org.apache.xml.security.transforms.TransformSpi;
import com.sun.org.apache.xml.security.transforms.TransformationException;
import java.io.IOException;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;

/**
 * A dummy TransformSpi for wss-swa Attachment-Content-Only-Transform. This class
 * simply return the provided XMLSignatureInput, performing no operation on it.
 *
 * This class can be removed once xmlsec API provides a transform impl for 
 * "http://docs.oasis-open.org/wss/2004/XX/oasis-2004XX-wss-swa-profile-1.0#Attachment-Content-Only-Transform"

 * @author Diego Ballve / Digital Artefacts Europe
 */
public class TransformAttachmentContentOnly extends TransformSpi
{
    public static final String implementedTransformURI =
        "http://docs.oasis-open.org/wss/2004"
        + "/XX/oasis-2004XX-wss-swa-profile-1.0#Attachment-Content-Only-Transform";
    
    public class AlwaysAcceptNodeFilter implements NodeFilter
    {
	public short acceptNode(Node node) {
	    return (short) 1;
	}
    }
    
    public boolean returnsNodeSet() {
	return false;
    }
    
    public boolean returnsOctetStream() {
	return true;
    }
    
    public boolean wantsNodeSet() {
	return false;
    }
    
    public boolean wantsOctetStream() {
	return true;
    }
    
    protected String engineGetURI() {
	return implementedTransformURI;
    }
    
    protected XMLSignatureInput enginePerformTransform
	(XMLSignatureInput xmlsignatureinput)
	throws IOException, CanonicalizationException, TransformationException,
	       InvalidCanonicalizerException {
	return xmlsignatureinput;
    }
    
}
