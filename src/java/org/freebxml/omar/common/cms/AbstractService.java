/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/cms/AbstractService.java,v 1.2 2005/12/22 05:57:35 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.cms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.security.SoapSecurityUtil;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;

import org.w3c.dom.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.activation.DataHandler;

import javax.servlet.ServletContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * @author Tony Graham
 *
 */
public abstract class AbstractService implements ServiceLifecycle {
    private static final Log log = LogFactory.getLog(AbstractService.class);

    //Canonical ResponseStatusType ids
    protected static final String CANONICAL_RESPONSE_STATUS_TYPE_ID_Success =
	BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success;
    protected static final String CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure =
	BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure;
    protected static final ObjectFactory rimFac = new ObjectFactory();
    protected JAXBContext jaxbContext;
    protected ServletEndpointContext servletEndpointContext = null;
    protected ServletContext servletContext = null;

    public void init(Object context) {
        servletEndpointContext = (ServletEndpointContext) context;
        servletContext = servletEndpointContext.getServletContext();
    }

    public void destroy() {
        servletEndpointContext = null;
        servletContext = null;
    }

    protected JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(
                    "org.oasis.ebxml.registry.bindings.rim:" +
                    "org.oasis.ebxml.registry.bindings.rs:" +
                    "org.oasis.ebxml.registry.bindings.lcm:" +
                    "org.oasis.ebxml.registry.bindings.query:" +
                    "org.oasis.ebxml.registry.bindings.cms");
                    // "org.oasis.saml.bindings.protocol:" +
                    // "org.oasis.saml.bindings.assertion:" +
        }

        return jaxbContext;
    }

    protected Unmarshaller getUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        //unmarshaller.setValidating(true);
        unmarshaller.setEventHandler(new ValidationEventHandler() {
                public boolean handleEvent(ValidationEvent event) {
                    boolean keepOn = false;

                    return keepOn;
                }
            });

        return unmarshaller;
    }

    protected Object getBindingObjectFromNode(Node node)
        throws Exception {
        Object obj = null;

        Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
        obj = unmarshaller.unmarshal(node);

        return obj;
    }

    protected SOAPElement getSOAPElementFromBindingObject(Object obj)
        throws Exception {
        SOAPElement soapElem = null;

        SOAPElement parent = SOAPFactory.newInstance().createElement("dummy");

        Marshaller marshaller = getJAXBContext().createMarshaller();
        marshaller.marshal(obj, System.err);
        marshaller.marshal(obj, new DOMResult(parent));
        soapElem = (SOAPElement) parent.getChildElements().next();

        return soapElem;
    }

    protected static void printNodeToConsole(Node n) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            // also print to console
            transformer.transform(new DOMSource(n), new StreamResult(System.err));
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    protected HashMap getRepositoryItemDHMap() throws Exception {
        HashMap repositoryItemDHMap = new HashMap();
        MessageContext mc = servletEndpointContext.getMessageContext();
        Collection attachments = (Collection) mc.getProperty(com.sun.xml.rpc.server.ServerPropertyConstants.GET_ATTACHMENT_PROPERTY);

        Iterator attachmentsIter = attachments.iterator();

        while (attachmentsIter.hasNext()) {
            Object obj = attachmentsIter.next();

            System.err.println("getRepositoryItems:: Attachment: " +
                obj.getClass().getName());

            if (obj instanceof AttachmentPart) {
                AttachmentPart ap = (AttachmentPart) obj;

                String contentId = SoapSecurityUtil.convertContentIdToUUID(ap.getContentId());
                String contentType = ap.getContentType();

                System.err.println("getRepositoryItems:: contentId: " +
                    contentId + "; contentType: " + contentType);
                if (log.isDebugEnabled()) {
                    log.debug(
                        "Processing attachment (RepositoryItem):\n" +
                        ap.getContent().toString());
                }

                
                DataHandler dh = ap.getDataHandler();

                repositoryItemDHMap.put(contentId, dh);
            }
        }

        return repositoryItemDHMap;
    }
}
