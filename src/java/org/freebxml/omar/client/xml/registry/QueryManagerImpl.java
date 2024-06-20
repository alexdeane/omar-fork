/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/QueryManagerImpl.java,v 1.33 2007/03/23 18:38:59 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.QueryManager;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;

import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CredentialInfo;
import org.freebxml.omar.common.QueryManagerLocalProxy;
import org.freebxml.omar.common.QueryManagerSOAPProxy;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

/**
 * Implements JAXR API interface named QueryManager
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public abstract class QueryManagerImpl implements QueryManager {
    private HashMap schemeToValueMap;
    private HashMap valueToConceptMap;
    protected DeclarativeQueryManagerImpl dqm;
    protected RegistryServiceImpl regService;
    protected BusinessLifeCycleManagerImpl lcm;
    protected User callersUser;
    protected org.freebxml.omar.common.spi.QueryManager serverQMProxy;
        

    QueryManagerImpl(RegistryServiceImpl regService,
        BusinessLifeCycleManagerImpl lcm, DeclarativeQueryManagerImpl dqm)
        throws JAXRException {
        this.regService = regService;
        this.lcm = lcm;
        this.dqm = dqm;
        
        setCredentialInfo(((ConnectionImpl)regService.getConnection()).getCredentialInfo());
        
        if (this.dqm == null) {
            this.dqm = (DeclarativeQueryManagerImpl) this;
        }
    }

    /**
     * Gets the RegistryObject specified by the Id and type of object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param id is the id of the Key for a RegistryObject.
     * @param objectType is a constant definition from LifeCycleManager
     * that specifies the type of object desired.
     * @return RegistryObject Is the object is returned as their concrete
     * type (e.g. Organization, User etc.).
     */
    public RegistryObject getRegistryObject(String id, String objectType)
        throws JAXRException {
        RegistryObject ro = null;
        
        try {
            ClientRequestContext context = new ClientRequestContext("org.freebxml.omar.client.xml.registry.QueryManagerImpl:getRegistryObject", null);
            RegistryObjectType ebRO = serverQMProxy.getRegistryObject(context, id, org.freebxml.omar.common.Utility.getInstance().mapTableName(objectType));
            List jaxbObjects = new ArrayList();
            if (ebRO != null) {
                jaxbObjects.add(ebRO);
            }
            List ros = JAXRUtility.getJAXRObjectsFromJAXBObjects(lcm, jaxbObjects, null);
            if (ros.size() > 0) {
                ro = (RegistryObject)ros.get(0);
            }
        } catch (ObjectNotFoundException e) {
            //Maintain backward compatibility and return null instead of throwing exception.
        }
        
        return ro;
    }

    /**
     * Gets the RegistryObject specified by the Id.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @return RegistryObject Is the object is returned as their concrete
     * type (e.g. Organization, User etc.).
     */
    public RegistryObject getRegistryObject(String id)
        throws JAXRException {
        return getRegistryObject(id, "RegistryObject");
    }

    /**
     * Gets the specified RegistryObjects.  The objects are returned as
     * their concrete type (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     *
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getRegistryObjects(Collection objectKeys)
        throws JAXRException {
        return getRegistryObjects(objectKeys, "RegistryObject");
    }

    /**
     * Gets the RegistryObjects owned by the Caller.  The objects are returned as
     * their concrete type (e.g. Organization, User etc.).
     * For to JAXR 2.0??
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     *
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getCallersRegistryObjects() throws JAXRException {
        return getRegistryObjects((String)null);
    }

    /**
     * Gets the specified RegistryObjects of the specified object type.  The objects are returned as
     * their concrete type (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     *
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getRegistryObjects(Collection objectKeys,
        String objectType) throws JAXRException {
        StringBuffer queryStr = new StringBuffer("SELECT * FROM ");
        queryStr.append(org.freebxml.omar.common.Utility.getInstance().mapTableName(objectType));
        queryStr.append(" WHERE id in (");

        Iterator iter = objectKeys.iterator();

        while (iter.hasNext()) {
            String id = ((Key) iter.next()).getId();
            queryStr.append("'").append(id).append("'");

            if (iter.hasNext()) {
                queryStr.append(", ");
            }
        }

        queryStr.append(")");

        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr.toString());
        BulkResponse resp = dqm.executeQuery(query);

        return resp;
    }

    /**
     * Gets the RegistryObjects owned by the caller.  The objects are
     * returned as their concrete type (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public javax.xml.registry.BulkResponse getRegistryObjects()
        throws javax.xml.registry.JAXRException {
        //Rename this to getCallersRegistryObjects() in JAXR 2.0??
        return getCallersRegistryObjects();
    }

    /**
     * Gets the RegistryObjects owned by the caller, that are of the
     * specified type.  The objects are returned as their concrete type
     * (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param objectType Is a constant that defines the type of object
     * sought. See LifeCycleManager for constants for object types.
     * @see LifeCycleManager for supported objectTypes
     * @return BulkResponse containing a hetrogeneous Collection of
     * RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getRegistryObjects(String objectType)
        throws JAXRException {
        BulkResponse resp = null;

        
        String queryStr = "SELECT ro.* FROM " +
            "RegistryObject ro, AuditableEvent ae, AffectedObject ao, User_ u WHERE ae.user_ = $currentUser AND ao.id = ro.id AND ao.eventId = ae.id";
        
        if (objectType != null) {
            String newObjectType = BindingUtility.mapJAXRNameToEbXMLName(objectType);
            
            //??The following code is a little dodgy but works for now
            // Shouldn't this be searching for Concept.code instead of path?
            Concept objectTypeConcept;
            if ("User".equals(objectType)) {
                objectTypeConcept = regService.getBusinessQueryManager().
                findConceptByPath("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType + 
                    "/RegistryObject/Person/User");
            } else {
                objectTypeConcept = regService.getBusinessQueryManager().
                findConceptByPath("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType + 
                    "/RegistryObject/" + newObjectType);
            }
            if (objectTypeConcept == null) {
                //Try looking under ExtrinsicObject
                objectTypeConcept = regService.getBusinessQueryManager().
                findConceptByPath("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType + 
                    "/RegistryObject/ExtrinsicObjects/" + newObjectType);
            }
            
            if (objectTypeConcept != null) {
                queryStr += " AND ro.objectType = '" + objectTypeConcept.getKey().getId() + "'";                 
            } else {
                throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.invalid.objecttype",new Object[] {objectType}));
            }
        }
        
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        resp = dqm.executeQuery(query);

        return resp;
    }

    /*
     * Gets the specified pre-defined concept as defined in Appendix A
     * of the JAXR specification.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @return The pre-defined Concept
     *
     * Implementation internal
     *
    public Concept getPredefinedConcept(String schemeName, String value)
        throws JAXRException
    {
        if (schemeToValueMap == null) {
            schemeToValueMap = new HashMap();
        }
        HashMap valueToConceptMap = (HashMap)schemeToValueMap.get(schemeName);
        if (valueToConceptMap == null) {
            valueToConceptMap = new HashMap();
            schemeToValueMap.put(schemeName, valueToConceptMap);
        }
        ConceptImpl concept = (ConceptImpl)valueToConceptMap.get(value);
        if (concept == null) {
            // Existing ConceptImpl not found so create a new one
            concept = new ConceptImpl(lcm);
            concept.setValue(value);
            // XXX set other Concept parts, like path
            valueToConceptMap.put(value, concept);
        }
        return concept;
    }
     **/
    public javax.xml.registry.RegistryService getRegistryService()
        throws javax.xml.registry.JAXRException {
        return regService;
    }

    //Add as Level 1 call in JAXR 2.0??
    public User getCallersUser() throws JAXRException {
        if (callersUser == null) {
            HashMap paramsMap = new HashMap();
            paramsMap.put(BindingUtility.CANONICAL_SLOT_QUERY_ID, BindingUtility.CANONICAL_QUERY_GetCallersUser);
            Query getCallersUserQuery = dqm.createQuery(Query.QUERY_TYPE_SQL);
            BulkResponse br = dqm.executeQuery(getCallersUserQuery, paramsMap);
            
            callersUser = (User)((BulkResponseImpl)br).getRegistryObject();
        }

        return callersUser;
    }
    
    //TODO: Add to JAXR 2.0 API??
    public RepositoryItem getRepositoryItem(String id) throws JAXRException {
        ClientRequestContext context = new ClientRequestContext("org.freebxml.omar.client.xml.registry.QueryManagerImpl:getRepositoryItem", null);
        return serverQMProxy.getRepositoryItem(context, id);
    }
    
    void setCredentialInfo(CredentialInfo credentialInfo) throws JAXRException {
        // Since new credentials usually indicates a different rim:User,
        // clear the cached callersUser variable
        callersUser = null;
        boolean localCall = ((ConnectionImpl)(((RegistryServiceImpl)this.getRegistryService()).getConnection())).isLocalCallMode();

        if (localCall) {
            serverQMProxy = new QueryManagerLocalProxy(
                ((ConnectionImpl)regService.getConnection()).getQueryManagerURL(), 
                credentialInfo);
        }
        else {
            serverQMProxy = new QueryManagerSOAPProxy(
                ((ConnectionImpl)regService.getConnection()).getQueryManagerURL(), 
                credentialInfo);
        }        
    }
}
