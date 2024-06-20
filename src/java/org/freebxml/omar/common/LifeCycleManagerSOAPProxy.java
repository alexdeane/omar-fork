/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/LifeCycleManagerSOAPProxy.java,v 1.11 2006/03/02 15:21:29 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequest;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;
import org.freebxml.omar.common.spi.RequestContext;
import org.freebxml.omar.common.spi.LifeCycleManager;

/**
 *
 * @author  najmi
 */
public class LifeCycleManagerSOAPProxy implements LifeCycleManager {
    
    private String registryURL = null;
    private CredentialInfo credentialInfo = null;
    private SOAPMessenger msgr = null;
        
    
    /** Creates a new instance of LifeCycleManagerLocalImpl */
    public LifeCycleManagerSOAPProxy(String registryURL, CredentialInfo credentialInfo) {
        msgr = new SOAPMessenger(registryURL, credentialInfo);
    }
    
    public RegistryResponse approveObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), null);

            return (RegistryResponse)resp.getRegistryResponse();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    /** Sets the status of specified objects. This is an extension request that will be adde to ebRR 3.1?? */
    public RegistryResponse setStatusOnObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), null);

            return (RegistryResponse)resp.getRegistryResponse();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    public RegistryResponse deprecateObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), null);

            return (RegistryResponse)resp.getRegistryResponse();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    public RegistryResponse unDeprecateObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), null);

            return (RegistryResponse)resp.getRegistryResponse();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    public RegistryResponse removeObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), null);

            return (RegistryResponse)resp.getRegistryResponse();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
       
    public RegistryResponse submitObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        Map idToRepositoryItemMap = context.getRepositoryItemsMap();
        
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            
            String requestString = sw.toString();
            logRequest(requestString);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), idToRepositoryItemMap);
            
            return (RegistryResponse)resp.getRegistryResponse();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    public RegistryResponse updateObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        Map idToRepositoryItemMap = context.getRepositoryItemsMap();
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), idToRepositoryItemMap);

            return (RegistryResponse)resp.getRegistryResponse();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }

    }
    
    public RegistryResponse relocateObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        Map idToRepositoryItemMap = context.getRepositoryItemsMap();
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), null);

            return (RegistryResponse)resp.getRegistryResponse();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    /** Sends an impl specific protocol extension request. */
    public RegistryResponseHolder extensionRequest(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        Map idToRepositoryItemMap = context.getRepositoryItemsMap();
        
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), idToRepositoryItemMap);

            return resp;
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    
    private void logRequest(String requestString) {
        boolean logRequests = Boolean.valueOf(CommonProperties.getInstance()
                                                                                   .getProperty("omar.common.soapMessenger.logSubmitRequests", "false"))
                                                        .booleanValue();

        if (logRequests) {
            PrintStream requestLogPS = null;
            try {
                requestLogPS = new PrintStream(new FileOutputStream(java.io.File.createTempFile(
                    "LifeCycleManagerSOAPProxy_submitLog", ".xml")));
                requestLogPS.println(requestString);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (requestLogPS != null) {
                    requestLogPS.close();
                }
            }            
        }
    }
    
}
