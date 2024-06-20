/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/soap/SOAPSender.java,v 1.4 2006/02/20 16:37:44 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.soap;

import java.io.IOException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.JAXRException;
import org.w3c.dom.Document;
import org.freebxml.omar.common.CommonResourceBundle;

/**
 * Enables sending of SOAP requests to a SOAP endpoint over HTTP
 *
 * @author Farrukh S. Najmi
 */
public class SOAPSender {
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
        
    private Log log = LogFactory.getLog(SOAPSender.class);
    
    /** Creates a new instance of SOAPSender */
    public SOAPSender() {
    }
    
    /**
     *
     * Send specified SOAPMessage to specified SOAP endpoint.
     */
    public SOAPMessage send(SOAPMessage msg, String endpoint) throws SOAPException {
        
        SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
        SOAPConnection connection = scf.createConnection();

        long t1 = System.currentTimeMillis();

        msg.saveChanges();
        
        dumpMessage("Request:", msg);
        SOAPMessage reply = connection.call(msg, endpoint);
        
        long t2 = System.currentTimeMillis();

        dumpMessage("Response:", reply);

        double secs = ((double) t2 - t1) / 1000;
        log.debug("Call elapsed time in seconds: " + secs);

        return reply;
    }
    
    /**
     *
     * Creates a SOAPMessage with bodyDoc as only child.
     */
    public SOAPMessage createSOAPMessage(Document bodyDoc) throws JAXRException {
        SOAPMessage msg = null;
        
        try {
            MessageFactory factory = MessageFactory.newInstance();
            msg = factory.createMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            //SOAPHeader sh = se.getHeader(); 
            SOAPBody sb = se.getBody();

            sb.addDocument(bodyDoc);
            msg.saveChanges();
        }
        catch (SOAPException e) {
            e.printStackTrace();
            throw new JAXRException(resourceBundle.getString("message.URLNotFound"), e);
        } 
        return msg;
    }        

    void dumpMessage(String info, SOAPMessage msg) throws SOAPException {
        //if (log.isTraceEnabled()) {
            if (info != null) {
                System.err.print(info);
            }

            try {
                msg.writeTo(System.err);
                System.err.println();
            } 
            catch (IOException x) {
            }
        //}
    }
    
}
