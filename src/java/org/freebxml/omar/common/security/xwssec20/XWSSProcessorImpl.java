package org.freebxml.omar.common.security.xwssec20;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.soap.SOAPMessage;
import javax.security.auth.callback.CallbackHandler;


import com.sun.xml.wss.impl.configuration.MessagePolicy;
import com.sun.xml.wss.impl.configuration.DeclarativeSecurityConfiguration;
import com.sun.xml.wss.impl.configuration.SecurityConfigurationXmlReader;

import com.sun.xml.wss.SecurityRecipient;
import com.sun.xml.wss.SecurityAnnotator;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.ProcessingContext;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class XWSSProcessorImpl implements XWSSProcessor {

    /** The log */
    private static Log log = LogFactory.getLog(XWSSProcessorImpl.class);
    
    private DeclarativeSecurityConfiguration declSecConfig = null;
    private CallbackHandler handler = null;
    private SecurityEnvironment secEnv = null;
    
    protected XWSSProcessorImpl(
        InputStream securityConfig, CallbackHandler handler) 
        throws XWSSecurityException {
        try {
            declSecConfig = 
                SecurityConfigurationXmlReader.createDeclarativeConfiguration(securityConfig);
            this.handler = handler;
            secEnv = new DefaultSecurityEnvironmentImpl(handler);
        }catch (Exception e) {
            // log
            throw new XWSSecurityException(e);
        }
    }


    protected XWSSProcessorImpl(
        InputStream securityConfig) throws XWSSecurityException {
        throw new UnsupportedOperationException("Operation Not Supported");
    }

    public SOAPMessage secureOutboundMessage(
        ProcessingContext context) 
        throws XWSSecurityException {

        //resolve the policy first
        MessagePolicy resolvedPolicy = null;

        if (declSecConfig != null) {
            resolvedPolicy = declSecConfig.senderSettings();
        } else {
            //log
            throw new XWSSecurityException("Security Policy Unknown");
        }
                                                                                                      
        if (resolvedPolicy == null) {
            // log that no outbound security specified ?
            return context.getSOAPMessage();
        }

        if (log.isDebugEnabled()) {
            log.debug("Policy is " + resolvedPolicy.getClass().getName());
        }
        context.setSecurityEnvironment(secEnv);
        context.setSecurityPolicy(resolvedPolicy);
 
        try {
            SecurityAnnotator.secureMessage(context);
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }

        try {
            SOAPMessage msg = context.getSOAPMessage();
            if (log.isDebugEnabled()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write("\n Secure Message Start .........\n\n".getBytes("utf-8"));
                msg.writeTo(baos);
                baos.write("\n Secure Message End .........\n\n".getBytes("utf-8"));
                //use platform default encoding. Don't know what was used for above write
                log.debug(baos.toString());
            }
            return msg;
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }

    }

    public SOAPMessage verifyInboundMessage(
        ProcessingContext context) 
        throws XWSSecurityException {

        MessagePolicy resolvedPolicy = null;

        if (declSecConfig != null) {
            resolvedPolicy = declSecConfig.receiverSettings();
        } else {
            //log
            throw new XWSSecurityException("Security Policy Unknown");
        }

        context.setSecurityEnvironment(secEnv);
        context.setSecurityPolicy(resolvedPolicy);
        try {
            SecurityRecipient.validateMessage(context);
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }

        try {
            SOAPMessage msg = context.getSOAPMessage();
            if (log.isDebugEnabled()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write("\n Verified Message Start .........\n\n".getBytes("utf-8"));
                msg.writeTo(baos);
                baos.write("\n Verified Message End .........\n\n".getBytes("utf-8"));
                //use platform default encoding. Don't know what was used for above write
                log.debug(baos.toString());
            }
            return msg;
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }

    }
}
