/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2007 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/query/ReferenceResolverImpl.java,v 1.1 2007/04/19 16:46:48 psterk Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.query;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.common.CommonResourceBundle;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

/**
 * The default implementation of the ReferenceResolver interface
 */
public class ReferenceResolverImpl implements ReferenceResolver {
    
    private static Log log = LogFactory.getLog(ReferenceResolverImpl.class);
    
    public static final String ASSOC_INCLUDE_FILTER_PROPERTY_PREFIX = "omar.server.referenceResolver.associations.includeFilterList";
    public static final String ASSOC_EXCLUDE_FILTER_PROPERTY_PREFIX = "omar.server.referenceResolver.associations.excludeFilterList";
    private Map assocIncludeFiltersMap = null;
    private Map assocExcludeFiltersMap = null;
    
    /**
     * This method prefetches referenced objects up to the default specified 
     * depth level: 0 implies only fetch matched objects.
     *
     * @param context 
     * The ServerRequestContext used in this request
     * @param ro
     * The RegistryObjecType for which reference resolution is requested
     */
    public Collection getReferencedObjects(ServerRequestContext context, 
                                           RegistryObjectType ro)
        throws RegistryException {
        return this.getReferencedObjects(context, ro, 0);
    }
                                      
    /**
     * This method prefetches referenced objects up to specified depth level.
     * Depth = 0 (default) implies only fetch matched objects.
     * Depth = n implies, also fetch all objects referenced by matched
     * objects upto depth of n
     * Depth = -1 implies, also fetch all objects referenced by matched
     * objects upto any level.
     *
     * @param context
     * The ServerRequestContext used in this request
     * @param ro
     * The target RegistryObjectType for which referenced objects are requested
     * @param depth int
     * The depth of the target RegistryObjectType dependency resolution
     */
    public Collection getReferencedObjects(ServerRequestContext context, 
                                           RegistryObjectType ro, 
                                           int depth)
        throws RegistryException {
        
        log.trace("start: getReferencedObjects");
        Collection refObjs = new ArrayList();
        internalGetReferencedObjects(context, ro, depth, new HashMap(), refObjs);        
        log.trace("end: getReferencedObjects");
        return refObjs;
    }
    
    /*
     * Gets the Collection of ReferenceInfos for all object references within specified RegistryObject.
     * TODO: replace with reflections API when JAXB bindings use special class for ReferenceURI.
     *
     * Reference attributes based on scanning rim.xsd for anyURI.
     *
     * @param ro specifies the RegistryObject whose ObjectRefs are being sought.
     *
     * @param idMap The Map with old temporary id to new permanent id mapping.
     *
     */
    private void internalGetReferencedObjects(ServerRequestContext serverContext,
                                              RegistryObjectType ro, 
                                              int depth,                                                     Map idMap, 
                                              Collection refObjs) 
        throws RegistryException {
        log.trace("start: internalGetReferencedObjects");
        try {
            if ((ro != null) && (!refObjs.contains(ro))) {
                if (log.isDebugEnabled()) {
                    log.debug("get references for this ro: "+ro.getId() + " "+ro.getObjectType());
                }
                refObjs.add(ro);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.RegistryObjectType", idMap, "ObjectType", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType", idMap, "Parent", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationType", idMap, "ClassificationNode", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationType", idMap, "ClassificationScheme", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationType", idMap, "ClassifiedObject", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType", idMap, "IdentificationScheme", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType", idMap, "RegistryObject", refObjs);

                //FederationType fed = (FederationType)ro;
                //TODO: Fix so it adds only Strings not ObjectRefType
                //refInfos.addAll(fed.getMembers().getObjectRef());

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.AssociationType1", idMap, "AssociationType", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.AssociationType1", idMap, "SourceObject", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.AssociationType1", idMap, "TargetObject", refObjs);


                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.AuditableEventType", idMap, "User", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.AuditableEventType", idMap, "RequestId", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.OrganizationType", idMap, "Parent", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.RegistryType", idMap, "Operator", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ServiceBindingType", idMap, "Service", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ServiceBindingType", idMap, "TargetBinding", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType", idMap, "ServiceBinding", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType", idMap, "SpecificationObject", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.SubscriptionType", idMap, "Selector", refObjs);
                
                if (depth != 0) {
                    depth--;
                    // Now process composed objects
                    Set composedObjects = BindingUtility.getInstance().getComposedRegistryObjects(ro, 1);
                    Collection composedNoDups = checkForDuplicates(refObjs, composedObjects);
                    // Now process associated objects
                    Collection associatedObjects = getAssociatedObjects(serverContext, ro, depth, idMap, refObjs);                                   
                    Collection assocNoDups = checkForDuplicates(refObjs, associatedObjects);
                    Collection relatedObjects = new ArrayList();
                    relatedObjects.addAll(composedNoDups);
                    relatedObjects.addAll(assocNoDups);
                    Iterator iter = relatedObjects.iterator();
                    while (iter.hasNext()) {
                        Object obj = iter.next();
                        if (obj instanceof RegistryObjectType) {
                            RegistryObjectType regObj = (RegistryObjectType)obj;
                            internalGetReferencedObjects(serverContext, regObj, depth, idMap, refObjs);
                        }
                    }
                }
            }
        } catch (RegistryException re) {
            throw re;
        } catch (Throwable t) {
            throw new RegistryException(t);
        }
        log.trace("end: internalGetReferencedObjects");
    }
    
    /*
     * This method gets all objects that are associated with the ro. These
     * objects are placed in the ref
     */
    private Collection getAssociatedObjects(ServerRequestContext serverContext, 
                                            RegistryObjectType ro, 
                                            int depth,                                             Map idMap,
                                            Collection refObjs) 
        throws RegistryException {
        log.trace("start: getAssociatedObjects");
        Collection results = new ArrayList();
        try {
            String id = ro.getId();
            String sqlQuery = getSQLStringForGettingAssociatedObjects(ro);
            List queryParams = new ArrayList();
            queryParams.add(id.toUpperCase());
            ResponseOption responseOption = BindingUtility.getInstance().queryFac.createResponseOption();
            responseOption.setReturnComposedObjects(true);
            responseOption.setReturnType(ReturnType.LEAF_CLASS);
            List objectRefs = new ArrayList();
            Collection queryResults = PersistenceManagerFactory.getInstance()
                                               .getPersistenceManager()
                                               .executeSQLQuery(serverContext,
					                        sqlQuery,
                                                                queryParams,
                                                                responseOption,
                                                                "RegistryObject",
                                                                objectRefs);
            if (queryResults != null) {
                results = queryResults; 
            }
        } catch (Throwable t) {
            throw new RegistryException(t);
        }
        log.trace("end: getAssociatedObjects");
        return results;
    }
    
    /*
     * This method checks to see if any of the members of the results collection
     * are contained in the running total refObjs collection.  If they are
     * contained, identify them as a duplicate and do not include in new 
     * no dups collection.
     */
    private Collection checkForDuplicates(Collection refObjects, Collection results) {
        log.trace("start: checkForDuplicates");
        Collection resultsWithNoDups = new ArrayList(results);
        if (refObjects != null && results != null) {
            Iterator resultsItr = results.iterator();
            while (resultsItr.hasNext()) {
                Object obj = resultsItr.next();
                RegistryObjectType ro = null;
                if (obj instanceof RegistryObjectType) {
                    ro = (RegistryObjectType)obj;
                    String roId = ro.getId();
                    Iterator refItr = refObjects.iterator();
                    while (refItr.hasNext()) {
                        RegistryObjectType refRO = (RegistryObjectType)refItr.next();
                        if (refRO.getId().equalsIgnoreCase(roId)) {
                            resultsWithNoDups.remove(ro);
                        }
                    }
                }
            }
        }
        log.trace("end: checkForDuplicates");
        return resultsWithNoDups;
    }
    
    /*
     * This method gets any configured association filters as a String[].
     * These filters are configured in omar.properties.
     */
    private String[] getAssocFilterStrings(RegistryObjectType ro) {
        log.trace("start: getAssocFilterStrings");
        String type = ro.getObjectType();
        // Use object type to get any configured filters
        // Includes filter takes precedence over excludes
        String[] filters = (String[])getAssociationIncludeFiltersMap().get(type);
        if (filters == null || filters.length > 0) {
            // If no includes filters, look for excludes
            filters = (String[])getAssociationExcludeFiltersMap().get(type);
        }
        log.trace("end: getAssocFilterStrings");
        return filters;
    }
    
    /*
     * This method is used to get the SQL string used in resolving any 
     * assocations of the target RO
     */
    private String getSQLStringForGettingAssociatedObjects(RegistryObjectType targetRO) {
        log.trace("start: getSQLString");
        StringBuffer sb = new StringBuffer();
        String sqlQueryFragment = "SELECT * FROM registryobject WHERE id IN "+
                               "(SELECT targetObject FROM association WHERE UPPER(sourceobject) = "+
                               "UPPER(?)";
        sb.append(sqlQueryFragment);
        // Since include filters take precedence over exclude filter, look
        // for them first
        String[] filters = (String[])getAssociationIncludeFiltersMap().get(targetRO.getObjectType());
        if (filters == null || filters.length == 0) {
            // Since no include filters, look for exclude filters
            filters = (String[])getAssociationExcludeFiltersMap().get(targetRO.getObjectType());
            // Process exclude filters
            sb.append(getAssocationTypeSQLPredicate(filters, true));
        } else {
            // Process includes filters
            sb.append(getAssocationTypeSQLPredicate(filters, false));
        }
        sb.append(")");        
        log.trace("end: getSQLString");
        return sb.toString();
    }
    
    /*
     * This method gets the correct assocationtype SQL predicate statement.
     * Association excludes filter adds a 'NOT' SQL keyword to the predicate.
     */
    private String getAssocationTypeSQLPredicate(String[] filters, boolean excludesFilter) {        
        log.trace("start: getAssocationTypeSQLPredicate");
        StringBuffer sb = new StringBuffer();
        if (filters != null && filters.length > 0) {
            sb.append(" and associationtype");
            if (excludesFilter) {
                sb.append(" not");
            }
            sb.append(" in (");
            for (int i= 0; i < filters.length;i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("'").append(filters[i]).append("'");
            }
            sb.append(")");
        }      
        log.trace("end: getAssocationTypeSQLPredicate");
        return sb.toString();
    }
    
    /**
     * Gets the Set of ReferenceInfo for specified reference attribute within RegistryObject.
     *
     * Reference attributes based on scanning rim.xsd for anyURI.
     *
     * @param ro specifies the RegistryObject whose reference attribute is being sought.
     *
     * @param idMap The HashMap with old temporary id to new permanent id mapping.
     *
     */
    private void processRefAttribute(ServerRequestContext serverContext, 
                                     RegistryObjectType ro, 
                                     String className, 
                                     Map idMap, 
                                     String attribute, 
                                     Collection refObjs) throws JAXRException {
        log.trace("start: processRefAttribute");
        try {
            //Use reflections API to get the attribute value, check if it needs to be mapped
            //and set it with mapped value if needed and add the targetObject in refObjs
            Class clazz = Class.forName(className);
            if (!(clazz.isInstance(ro))) {
                return;
            }

            //Get the attribute value by calling get method
            String getMethodName = "get" + attribute;
            Method getMethod = clazz.getMethod(getMethodName, (java.lang.Class[])null);

            //Invoke getMethod to get the reference target object's id
            String targetObjectId = (String)getMethod.invoke(ro, (java.lang.Object[])null);

            if (targetObjectId != null) {
                //Check if id has been mapped to a new id
                if (idMap.containsKey(targetObjectId)) {
                    //Replace old id with new id
                    targetObjectId = (String)idMap.get(targetObjectId);

                    //Use set method to set new value on ro
                    Class[] parameterTypes = new Class[1];
                    Object[] parameterValues = new Object[1];
                    parameterTypes[0] = Class.forName("java.lang.String");
                    parameterValues[0] = targetObjectId;
                    String setMethodName = "set" + attribute;
                    Method setMethod = clazz.getMethod(setMethodName, parameterTypes);
                    setMethod.invoke(ro, parameterValues);
                }
                log.debug("sourceObject: "+ro.getId()+" targetObject: " + targetObjectId + " attribute: " + attribute);
                RegistryObjectType refObj = QueryManagerFactory.getInstance().getQueryManager().getRegistryObject(serverContext, targetObjectId);
                if (refObj != null && ! refObjs.contains(refObj)) {
                    refObjs.add(refObj);
                }
            }

        }
        catch (Exception e) {
            //throw new OMARExeption("Class = " ro.getClass() + " attribute = " + attribute", e);
            log.error(CommonResourceBundle.getInstance().getString("message.ErrorClassAttribute", new Object[]{ro.getClass(), attribute}));
            e.printStackTrace();
        }        
        log.trace("end: processRefAttribute");
    }
    
    /*
     * This method is used to get a java.util.Map of association include filters
     * configured in omar.properties
     */
    private Map getAssociationIncludeFiltersMap() {
        log.trace("start: getAssociationIncludeFiltersMap");
        if (assocIncludeFiltersMap == null) {
            synchronized(this) {
                assocIncludeFiltersMap = new HashMap();
                RegistryProperties props = RegistryProperties.getInstance();
                Iterator propsIter = props.getPropertyNamesStartingWith(ASSOC_INCLUDE_FILTER_PROPERTY_PREFIX);

                while (propsIter.hasNext()) {
                    String prop = (String) propsIter.next();
                    String objectTypeForFilter = prop.substring(ASSOC_INCLUDE_FILTER_PROPERTY_PREFIX.length()+1);
                    String assocFilters = props.getProperty(prop);
                    String[] assocFilterArray = assocFilters.split("\\|");
                    assocIncludeFiltersMap.put(objectTypeForFilter, assocFilterArray);
                }
            }
        }
        log.trace("end: getAssociationIncludeFiltersMap");
        return assocIncludeFiltersMap;
    }
    
    /*
     * This method is used to get a java.util.Map of association exclude filters
     * configured in omar.properties
     */
    private Map getAssociationExcludeFiltersMap() {
        log.trace("start: getAssociationExcludeFiltersMap");
        if (assocExcludeFiltersMap == null) {
            synchronized(this) {
                assocExcludeFiltersMap = new HashMap();                
                // The Include Filter Map has precedence over the excludes. If an
                // includes filter is set, ignore the excludes filters
                if (getAssociationIncludeFiltersMap().size() == 0) {
                    RegistryProperties props = RegistryProperties.getInstance();
                    Iterator propsIter = props.getPropertyNamesStartingWith(ASSOC_EXCLUDE_FILTER_PROPERTY_PREFIX);

                    while (propsIter.hasNext()) {
                        String prop = (String) propsIter.next();
                        String objectTypeForFilter = prop.substring(ASSOC_EXCLUDE_FILTER_PROPERTY_PREFIX.length()+1);
                        String assocFilters = props.getProperty(prop);
                        String[] assocFilterArray = assocFilters.split("\\|");
                        assocExcludeFiltersMap.put(objectTypeForFilter, assocFilterArray);
                    }
                }
            }
        }
        log.trace("end: getAssociationExcludeFiltersMap");
        return assocExcludeFiltersMap;
    }    
   
}
