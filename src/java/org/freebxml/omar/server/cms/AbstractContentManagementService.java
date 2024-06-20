/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/AbstractContentManagementService.java,v 1.9 2007/01/12 21:31:08 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import java.util.List;
import javax.activation.DataHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.exceptions.InvalidConfigurationException;
import org.freebxml.omar.common.security.SoapSecurityUtil;
import org.freebxml.omar.server.repository.RepositoryManager;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.util.ServerResourceBundle;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;


/**
 * Abstract superclass of cataloging and validation (and other)
 * services.
 * @see
 * @author Tony Graham
 *
 */
public abstract class AbstractContentManagementService
    implements ContentManagementService {
    private static final Log log = LogFactory.getLog(AbstractContentManagementService.class.getName());
    protected static BindingUtility bu = BindingUtility.getInstance();
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                                    .getRepositoryManager();
    protected static QueryManager qm = QueryManagerFactory.getInstance()
                                                          .getQueryManager();
    protected static MessageFactory mf;

    static {
        try {
            mf = MessageFactory.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the <code>RegistryObjectType</code> as a stream of XML markup.
     *
     * @param ro an <code>RegistryObjectType</code> value
     * @return a <code>StreamSource</code> value
     * @exception RegistryException if an error occurs
     */
    protected StreamSource getAsStreamSource(RegistryObjectType ro)
        throws RegistryException {
        log.debug("getAsStreamSource(RegistryObjectType) entered");

        StreamSource src = null;

        try {
            StringWriter sw = new StringWriter();

            Marshaller marshaller = bu.getJAXBContext().createMarshaller();

            marshaller.marshal(ro, sw);

            StringReader reader = new StringReader(sw.toString());
            src = new StreamSource(reader);
        }
        // these Exceptions should already be caught by Binding
        catch (JAXBException e) {
            throw new RegistryException(e);
        }

        return src;
    }

    protected AttachmentPart getRepositoryItemAsAttachmentPart(String id)
        throws RegistryException {
        RepositoryItem ri = rm.getRepositoryItem(id);

        AttachmentPart ap = null;

        try {
            SOAPMessage m = mf.createMessage();
            DataHandler dh = ri.getDataHandler();
            String cid = SoapSecurityUtil.convertUUIDToContentId(id);
            ap = m.createAttachmentPart(dh);
            ap.setContentId(cid);
            
        } catch (Exception e) {
            throw new RegistryException(e);
        }

        return ap;
    }
    
    /**
     * Select one accessURI from the possibly multiple ServiceBindings of
     * <code>service</code>, where a ServiceBinding may have either an
     * accessURI or a targetBinding that refers to another ServiceBinding.
     *
     * @param service The service from which to select an accessURI
     * @throws RegistryException if an error occurs
     * @return String for URI of remote service
     */
    protected String selectAccessURI(ServiceType service) throws RegistryException {
        List serviceBindings = service.getServiceBinding();

        if (log.isDebugEnabled()) {
            if (serviceBindings == null) {
                log.debug("ServiceBindings is null");
            } else {
                Iterator debugIter = serviceBindings.iterator();
                while (debugIter.hasNext()) {
                    ServiceBindingType binding = (ServiceBindingType) debugIter.next();
                    log.debug("URL: " + binding.getAccessURI());
                }
            }
        }

        String accessURI = null;

        // Use the first ServiceBinding having an accessURI.
        if (serviceBindings.size() > 0) {
            Iterator sbIter = serviceBindings.iterator();
            while (sbIter.hasNext()) {
                accessURI = ((ServiceBindingType) sbIter.next()).getAccessURI();
                
                if ((accessURI != null) && (!accessURI.equals(""))) {
                    break;
                }
            }
            
            if ((accessURI == null) || accessURI.equals("")) {
                throw new InvalidConfigurationException(
                    ServerResourceBundle.getInstance()
                                        .getString("message.NoAccessURIForService", 
                                            new Object[] {service.getId()}));
            }
        } else {
            throw new InvalidConfigurationException(
                ServerResourceBundle.getInstance()
                                        .getString("message.NoServiceBindingsForService", 
                                            new Object[] {service.getId()}));
        }

        return accessURI;
    }
}
