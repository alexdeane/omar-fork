/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/BulkResponseImpl.java,v 1.34 2007/03/23 18:38:59 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.FindException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.registry.infomodel.RegistryObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 * Implements JAXR API interface named BulkResponse
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class BulkResponseImpl implements BulkResponse {
    private static Log log = LogFactory.getLog(BulkResponseImpl.class.getName());

    String requestId;
    private int status;
    private ArrayList collection = new ArrayList();
    private ArrayList registryExceptions;
    private RegistryResponseType ebResponse;

    /**
     * Construct an empty successful BulkResponse
     */
    BulkResponseImpl() throws JAXRException {
        status = STATUS_SUCCESS;
    }

    /**
     * Note: BulkResponseImpl is not an infomodel object even though this
     * constructor looks like constructors in the infomodel subpackage.
     * Therefore, the LifeCycleManagerImpl argument is not stored.
     */
    public BulkResponseImpl(LifeCycleManagerImpl lcm,
        RegistryResponseType ebResponse, Map responseAttachments)
        throws JAXRException {
        requestId = org.freebxml.omar.common.Utility.getInstance().createId();

        this.ebResponse = ebResponse;
        String ebStatus = ebResponse.getStatus();

        if (ebStatus.equals(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success)) {
            status = STATUS_SUCCESS;
        } else if (ebStatus.equals(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Unavailable)) {
            status = STATUS_UNAVAILABLE;
        } else {
            status = STATUS_FAILURE;
        }

        if (ebResponse instanceof AdhocQueryResponseType) {
            AdhocQueryResponseType aqr = (AdhocQueryResponseType) ebResponse;
            RegistryObjectListType queryResult = aqr.getRegistryObjectList();
            processQueryResult(queryResult, lcm, responseAttachments);
        }

        RegistryErrorListType errList = ebResponse.getRegistryErrorList();

        if (errList != null) {
            List errs = errList.getRegistryError();
            Iterator iter = errs.iterator();

            while (iter.hasNext()) {
                Object obj = iter.next();
                RegistryErrorType error = (RegistryErrorType) obj;

                // XXX Need to add additional error info to exception somehow
                addRegistryException(new FindException(error.getValue()));
            }

            // XXX What to do about optional highestSeverity attr???
            //             errList.getHighestSeverity();
        }

        ((RegistryServiceImpl)(lcm.getRegistryService())).setBulkResponse(this);
    }

    /**
     * Get the Collection of of objects returned as a response of a
     * bulk operation.
     * Caller thread will block here if result is not yet available.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public Collection getCollection() throws JAXRException {
        return collection;
    }

    /**
     * Sets the Collection of objects returned for the response
     * Package protected access meant to be called only by provider impl.
     *
     */
    void setCollection(Collection c) {
        collection.clear();
        collection.addAll(c);
    }

    /**
     * Get the JAXRException(s) Collection in case of partial commit.
     * Caller thread will block here if result is not yet available.
     * Return null if result is available and there is no JAXRException(s).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public Collection getExceptions() throws JAXRException {
        return registryExceptions;
    }

    /**
     * Returns true if the reponse is a partial response due to large result set
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public boolean isPartialResponse() throws JAXRException {
        // Write your code here
        return false;
    }

    /**
     * Returns the unique id for the request that generated this response.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public String getRequestId() throws JAXRException {
        // Write your code here
        return requestId;
    }

    /**
     * Returns the status for this response.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public int getStatus() throws JAXRException {
        return status;
    }

    void setStatus(int status) throws JAXRException {
        this.status = status;
    }

    /**
     * Returns true if a response is available, false otherwise.
     * This is a polling method and must not block.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public boolean isAvailable() throws JAXRException {
        //?? stub
        return true;
    }

    void addExceptions(Collection exes) {
        if (registryExceptions == null) {
            registryExceptions = new ArrayList();
        }

        registryExceptions.addAll(exes);
    }

    private void addRegistryException(RegistryException rex) {
        if (registryExceptions == null) {
            registryExceptions = new ArrayList();
        }

        registryExceptions.add(rex);
    }

    private void processQueryResult(RegistryObjectListType sqlResult,
        LifeCycleManagerImpl lcm, Map repositoryItemsMap) throws JAXRException {
        ObjectCache objCache = ((RegistryServiceImpl) (lcm.getRegistryService())).getObjectCache();
        List items = sqlResult.getIdentifiable();
        collection.addAll(JAXRUtility.getJAXRObjectsFromJAXBObjects(lcm, items, repositoryItemsMap));
    }

    RegistryObject getRegistryObject() throws JAXRException {
        RegistryObject ro = null;

        // check for errors
        Collection exceptions = getExceptions();

        if (exceptions != null) {
            Iterator iter = exceptions.iterator();
            Exception exception = null;

            while (iter.hasNext()) {
                exception = (Exception) iter.next();
                throw new JAXRException(exception);
            }
        }

        Collection results = getCollection();
        Iterator iter = results.iterator();

        if (iter.hasNext()) {
            ro = (RegistryObject) iter.next();
        }

        return ro;
    }

    public RegistryResponseType getRegistryResponse() {
        return ebResponse;
    }
}
