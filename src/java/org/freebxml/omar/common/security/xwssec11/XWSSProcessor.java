/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec11/XWSSProcessor.java,v 1.1 2005/04/16 19:02:43 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec11;


import javax.xml.soap.SOAPMessage;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.PolicyViolationException;


/**
 *<code>XWSSProcessor</code> interface defines methods for
 *<UL>
 *    <LI> Securing an outbound <code>SOAPMessage</Code>
 *    <LI> Verifying the security in an inbound <code>SOAPMessage</code>
 *</UL>
 *
 * An <code>XWSSProcessor</code> can add/verify Security in a 
 * <code>SOAPMessage</code> as defined by the OASIS WSS 1.0 specification.
 */
public interface XWSSProcessor {

    /**
     *  Adds Security to an outbound <code>SOAPMessage</Code> according to
     *  the Security Policy inferred from the <code>SecurityConfiguration</code>
     *  with which this <code>XWSSProcessor</code> was initialized.
     *
     * @param  messageCntxt the SOAP <code>ProcessingContext</code> containing 
     *         the outgoing  <code>SOAPMessage</code> to be secured
     *
     * @return the resultant Secure <code>SOAPMessage</code>
     *
     * @excpetion XWSSecurityException if there was an error in securing 
     *            the message.
     */
    public  SOAPMessage secureOutboundMessage(
        ProcessingContext messageCntxt) 
        throws XWSSecurityException;

    /**
     *  Verifies Security in an inbound <code>SOAPMessage</Code> according to
     *  the Security Policy inferred from the <code>SecurityConfiguration</code>
     *  with which this <code>XWSSProcessor</code> was initialized.
     *
     * @param  messageCntxt the SOAP <code>ProcessingContext</code> containing the 
     *         outgoing  <code>SOAPMessage</code> to be secured
     *
     * @return the resultant <code>SOAPMessage</code> after successful
     *         verification of security in the message
     * 
     *
     * @excpetion XWSSecurityException if there was an unexpected error 
     *     while verifying the message.
     * @exception PolicyViolationException if the security in the incoming 
     *     message violates the Security policy that was applied to the message.
     * @exception WssSoapFaultException when security in the incoming message
     *     is in direct violation of the OASIS WSS specification. 
     *     When a WssSoapFaultException is thrown the getFaultCode() method on it
     *     will return a <code>QName</code> which would correspond to the 
     *     WSS defined fault.
     */
    public SOAPMessage verifyInboundMessage(
        ProcessingContext messageCntxt) 
        throws XWSSecurityException, PolicyViolationException;

}
