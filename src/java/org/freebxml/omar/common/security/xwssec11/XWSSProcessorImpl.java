/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec11/XWSSProcessorImpl.java,v 1.2 2005/04/19 19:48:04 joehw Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec11;

import java.io.InputStream;

import javax.xml.soap.SOAPMessage;
import javax.security.auth.callback.CallbackHandler;

import com.sun.xml.soap.SOAPProcessorConstants;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.SecurityAnnotator;
import com.sun.xml.wss.SecurityRecipient;
import com.sun.xml.wss.SecurableSoapMessage;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.PolicyViolationException;
import com.sun.xml.wss.SecurityEnvironment;

import com.sun.xml.wss.impl.DefaultSecurityEnvironmentImpl;

import com.sun.xml.wss.configuration.DeclarativeSecurityConfiguration;
import com.sun.xml.wss.configuration.SecurityConfigurationXmlReader;
import org.freebxml.omar.common.CommonResourceBundle;

public class XWSSProcessorImpl implements XWSSProcessor {
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
    
    private DeclarativeSecurityConfiguration declSecConfig = null;
    private CallbackHandler handler = null;
    private SecurityEnvironment secEnv = null;
    
    protected XWSSProcessorImpl(
            InputStream securityConfig, CallbackHandler handler)
            throws XWSSecurityException {
        try {
            declSecConfig =
                    SecurityConfigurationXmlReader.createDeclarativeConfiguration(securityConfig);
            handler = handler;
            secEnv = new DefaultSecurityEnvironmentImpl(handler);
        }catch (Exception e) {
            // log
            throw new XWSSecurityException(e);
        }
    }
    
    
    protected XWSSProcessorImpl(
            InputStream securityConfig) throws XWSSecurityException {
        throw new UnsupportedOperationException(resourceBundle.getString("message.operationNotSupported"));
    }
    
    public SOAPMessage secureOutboundMessage(
            ProcessingContext context)
            throws XWSSecurityException {
        
        SecurityAnnotator annotator = null;
        try {
            annotator = declSecConfig.createConfiguration().createAnnotator();
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        
        SOAPMessage message =
                (SOAPMessage)context.getProperty(
                SOAPProcessorConstants.MESSAGE_PROPERTY);
        SecurableSoapMessage secSoapMsg =
                new SecurableSoapMessage(message);
        context.setProperty(
                SOAPProcessorConstants.MESSAGE_PROPERTY, secSoapMsg);
        secSoapMsg.setSecurityEnvironment(secEnv);
        
        try {
            annotator.annotateHeader(null, context);
        } catch (XWSSecurityException ex) {
            throw ex;
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        return secSoapMsg;
    }
    
    public SOAPMessage verifyInboundMessage(
            ProcessingContext context)
            throws XWSSecurityException, PolicyViolationException {
        
        SecurityRecipient recipient = null;
        try {
            recipient = declSecConfig.createConfiguration().createRecipient();
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        
        SOAPMessage message =
                (SOAPMessage)context.getProperty(
                SOAPProcessorConstants.MESSAGE_PROPERTY);
        SecurableSoapMessage secSoapMsg = null;
        
        if (message instanceof SecurableSoapMessage) {
            secSoapMsg = (SecurableSoapMessage)message;
        }else {
            secSoapMsg = new SecurableSoapMessage(message);
        }
        
        context.setProperty(
                SOAPProcessorConstants.MESSAGE_PROPERTY, secSoapMsg);
        secSoapMsg.setSecurityEnvironment(secEnv);
        
        try {
            recipient.acceptHeaderElement(null, context);
        } catch (PolicyViolationException pvex) {
            throw pvex;
        } catch (XWSSecurityException ex) {
            throw ex;
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        
        return secSoapMsg.getSOAPMessage();
    }
    
}
