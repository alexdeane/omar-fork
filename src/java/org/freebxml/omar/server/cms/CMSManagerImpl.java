/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/CMSManagerImpl.java,v 1.21 2007/01/12 21:31:08 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.exceptions.InvalidConfigurationException;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;


/**
 * Concrete Content Management Service implementation.  For each
 * registry object in a <code>RequestContext</code>, the
 * <code>CMSManager</code> determines the applicable content
 * management service or services to use and invokes them.
 */
public class CMSManagerImpl extends AbstractCMSManager {
    private static final Log log = LogFactory.getLog(CMSManagerImpl.class);
    private HashMap objectTypeToServicesMap = new HashMap();
    private HashMap objectTypeToServiceInvocationInfosMap = new HashMap();
    protected Map constructors = new TreeMap();
    protected Comparator serviceInvocationInfoComparator = new ServiceInvocationInfoComparator();
    public static final String CLASS_MAPPER_PREFIX = "omar.server.cms.classMap.";
    CMSTypeManager filteringManager = new ContentFilteringServiceManager();
    CMSTypeManager catalogingManager = new ContentCatalogingServiceManager();
    CMSTypeManager validationManager = new ContentValidationServiceManager();

    public CMSManagerImpl() {
        // get mapped constructors. log.warn on error and ignore.
        RegistryProperties props = RegistryProperties.getInstance();
        Iterator propsIter = props.getPropertyNamesStartingWith(CLASS_MAPPER_PREFIX);

        while (propsIter.hasNext()) {
            String prop = (String) propsIter.next();
            addConstructor(prop.substring(CLASS_MAPPER_PREFIX.length()),
                props.getProperty(prop));
        }
    }

    /** Adds a constructor to map */
    private void addConstructor(String typeName, String className) {
        try {
            Class clazz = Class.forName(className);
            Constructor c1 = clazz.getConstructor(new Class[] {  });
            constructors.put(typeName, c1);

            if (log.isDebugEnabled()) {
                log.debug(ServerResourceBundle.getInstance().
			  getString("message.RegisteredConstructorForServiceTypeOrInstanceForType",
				    new Object[]{className, typeName}));
            }
        } catch (Exception e) {
	    log.warn(ServerResourceBundle.getInstance().
		     getString("message.extension.load.failure",
			       new Object[]{className, typeName}),
		     e);
        }
    }

    /**
     * Gets the objects that the CM Service must process.
     * 
     * TODO: Not OO as current CMS frame does not allow 
     * any better because objectToProcess are needed before
     * we know which CMSTypeManager to use.
     */
    protected Set getObjectsToProcess(ServerRequestContext context) throws RegistryException {        
        Set objectsToProcess = null;
        
        if (context.getRegistryRequestStack().size() == 0) {
            objectsToProcess = new HashSet();
        } else {
            RegistryRequestType request = context.getCurrentRegistryRequest();

            if (request instanceof AdhocQueryRequest) {
                objectsToProcess = (new HashSet(((ServerRequestContext)context).getQueryResults()));
            } else if ((request instanceof SubmitObjectsRequest) || 
                (request instanceof UpdateObjectsRequest)) {            
                objectsToProcess = (new HashSet(((ServerRequestContext)context).getTopLevelObjectsMap().values()));
            }        
        }
        
        return objectsToProcess;
    }
    
    /**
     * Invokes any applicable content management services for the
     * registry objects in the <code>RequestContext</code>.
     *
     * @param context a <code>RequestContext</code> value
     * @param objectsToProcess the objects that need to be processed by CM Services
     */
    public void invokeServices(ServerRequestContext context) throws RegistryException {
        //try {
            // Iterate over objectsToProcess and process them via CMS services
            Iterator iter = getObjectsToProcess(context).iterator();

            while (iter.hasNext()) {
                Object obj = iter.next();
                
                //Careful: obj could be an ObjectRefType for queries.
                if (obj instanceof RegistryObjectType) {
                    RegistryObjectType ro = (RegistryObjectType) obj;

                    if (log.isDebugEnabled()) {
                        log.debug("RegistryObject: " + ro.getId() + " (" + ro +
                            ")");
                    }

                    // Get information about any services that should be
                    // invoked for objects of this type.
                    String objectType = ro.getObjectType();
                    if (objectType == null) {
                        try {
                            objectType = BindingUtility.getInstance().getObjectType(ro);
                        } catch (Exception e) {
                            //Unlikely to happen
                            log.error(e);
                        }
                    }

                    if (objectType != null) {
                        Collection serviceInvocationInfos = getServiceInvocationInfos(context, objectType);

                        // If there are any services to be invoked, invoke
                        // each in turn.
                        Iterator invocationsIter = serviceInvocationInfos.iterator();

                        while (invocationsIter.hasNext()) {
                            ServiceInvocationInfo invocationInfo = (ServiceInvocationInfo) invocationsIter.next();
                            RepositoryItem ri = (RepositoryItem) context.getRepositoryItemsMap().get(ro.getId());

                            //Note that ri will be null for ExternalLink ro.
                            System.err.println("Invoking CMS service: " + invocationInfo.getService().getId() + "  for object: " + ro.getId());
                            invocationInfo.getManager().invokeServiceForObject(invocationInfo, ro, ri, context);
                        }
                    }
                }
            }
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}
    }

    /**
     * Gets the constructor to use for handling a Content Management
     * Service.
     * <p>
     * Selection is based on one or more of:
     * <ul>
     * <li>
     * The service's UUID -- e.g., for the built-in Canonical XML
     * Content Cataloging Service.
     * </li>
     * <li>
     * The service's service binding's accessURI value
     * </li>
     * <li>
     * The service's classification as either a validation or content
     * cataloging service.
     * </li>
     * </ul>
     *
     * @param cms a <code>ServiceType</code> value
     * @return a <code>Constructor</code> value
     */
    private Constructor getConstructor(ServiceType cms) {
        Object constructor = null;
        String reason = null;

        constructor = constructors.get(cms.getId());
        reason = "Service UUID";

        if (constructor == null) {
            Collection classifications = cms.getClassification();

            Iterator classificationsIter = classifications.iterator();

            while (classificationsIter.hasNext()) {
                ClassificationType classification = (ClassificationType) classificationsIter.next();

                constructor = constructors.get(classification.getClassificationNode());

                if (constructor != null) {
                    reason = "Service classification";

                    break;
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("getConstructor:: cms: " + cms.getId() +
                "; constructor: " + constructor + "; reason: " + reason);
        }

        return (Constructor) constructor;
    }

    /**
     * Get information about any content management services for an
     * object type.
     *
     * @param objectType a <code>String</code> value
     * @return a <code>Collection</code> value
     * @exception InvalidConfigurationException if an error occurs
     */
    private Collection getServiceInvocationInfos(ServerRequestContext context, String objectType)
        throws InvalidConfigurationException, RegistryException {
        // TODO: Remove or update map entry if new Service or
        // InvocationControlFile added for objectType.
        Collection serviceInvocationInfos = (Collection) objectTypeToServiceInvocationInfosMap.get(objectType);

        //TODO: Don't do lookup if objectType has invalid configuration
        if ((serviceInvocationInfos == null) &&
                (!(objectTypeToServiceInvocationInfosMap.containsKey(objectType)))) {
            //Lookup failed in map so look dynamically
            serviceInvocationInfos = new ArrayList(getServiceInvocationInfos(
                        context,
                        objectType,
                        BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_ValidationControlFileFor,
                        BindingUtility.CANONICAL_CONTENT_MANAGEMENT_SERVICE_ID_ContentValidationService,
                        validationManager));
            
            serviceInvocationInfos.addAll(getServiceInvocationInfos(
                    context,
                    objectType,
                    BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_CatalogingControlFileFor,
                    BindingUtility.CANONICAL_CONTENT_MANAGEMENT_SERVICE_ID_ContentCatalogingService,
                    catalogingManager));
            
            serviceInvocationInfos.addAll(getServiceInvocationInfos(
                    context,
                    objectType,
                    BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_FilteringControlFileFor,
                    BindingUtility.CANONICAL_CONTENT_MANAGEMENT_SERVICE_ID_ContentFilteringService,
                    filteringManager));
            
            ((ArrayList) serviceInvocationInfos).trimToSize();

            //Update the map to avoid dynamic resolution in future for this objectType
            objectTypeToServiceInvocationInfosMap.put(objectType,
                serviceInvocationInfos);
        }

        return serviceInvocationInfos;
    }

    /**
     * Get the <code>ServiceInvocationInfo</code>s of the specified
     * object type.
     *
     * @param objectType a <code>String</code> value
     * @param assocId a <code>String</code> value
     * @param cmsClassificationId a <code>String</code> value
     * @param manager a <code>CMSTypeManager</code> value
     * @return a <code>Collection</code> value
     */
    private Collection getServiceInvocationInfos(ServerRequestContext context, String objectType,
        String assocId, String cmsClassificationId,
        CMSTypeManager cmsTypeManager)
        throws InvalidConfigurationException, RegistryException {
        if (log.isDebugEnabled()) {
            log.debug("getServiceInfos:: " + "objectType: " + objectType +
                "; assocId: " + assocId + "; cmsClassificationId: " +
                cmsClassificationId + "; cmsTypeManager: " + cmsTypeManager);
        }

        // Order of invoking services is not defined by ebRS spec, but
        // this implementation should at least be consistent with
        // itself.
        Collection serviceInvocationInfos = new TreeSet(serviceInvocationInfoComparator);

        Collection invocationControllers = getInvocationControllers(context, objectType,
                assocId);

        if (invocationControllers.size() != 0) {
            ServiceType service = getServiceType(context, objectType, cmsClassificationId);

            if (service == null) {
                throw new InvalidConfigurationException(
                    ServerResourceBundle.getInstance()
                                        .getString("message.InvocationFileButNoService", 
                                            new Object[] {objectType}));
            }

            Constructor serviceConstructor = getConstructor(service);

            if (serviceConstructor == null) {
                throw new InvalidConfigurationException(
                    ServerResourceBundle.getInstance()
                                        .getString("message.NoManagerImplementedForObjectType", 
                                            new Object[] {objectType}));
            }

            Iterator controllersIter = invocationControllers.iterator();

            while (controllersIter.hasNext()) {
                serviceInvocationInfos.add(new ServiceInvocationInfo(service,
                        serviceConstructor,
                        (InvocationController) controllersIter.next(),
                        cmsTypeManager));
            }
        }

        return serviceInvocationInfos;
    }

    /**
     * Gets the <code>InvocationContoller</code>s of a specified
     * association type for a specified registry object type.  The
     * association type should be a subtype of the
     * 'InvocationControlFileFor' association type.
     *
     * @param objectType a <code>String</code> value
     * @param controlFileForAssocId Id of an InvocationControlFileFor association subtype
     * @return a <code>Collection</code> of <code>InvocationController</code>.
     * @exception InvalidConfigurationException if an error occurs
     */
    private Collection getInvocationControllers(ServerRequestContext context, String objectType,
        String controlFileForAssocId)
        throws InvalidConfigurationException, RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("getInvocationControllers:: " + "objectType: " +
                objectType + "; controlFileForAssocId: " +
                controlFileForAssocId);
        }

        Collection invocationControllers = new ArrayList();

        // Get associations of object type.
        Collection associations = getAssociations(context, objectType,
                controlFileForAssocId);

        // Iterate over the associations
        Iterator associationsIter = associations.iterator();

        while (associationsIter.hasNext()) {
            AssociationType1 assoc = (AssociationType1) associationsIter.next();

            RegistryObjectType invocationControlFileEO = null;

            if (assoc != null) {
                String src = assoc.getSourceObject();

                try {
                    invocationControlFileEO = pm.getRegistryObject(context, bu.getObjectId(
                                src), "ExtrinsicObject");
                } catch (Exception e) {
                    throw new InvalidConfigurationException(
                        ServerResourceBundle.getInstance()
                                        .getString("message.SourceObjectNotFoundForExtrinsicObject", 
                                            new Object[] {assoc.getId()}));
                }
            } else {
                throw new InvalidConfigurationException(
                    ServerResourceBundle.getInstance()
                                        .getString("message.InvocationControlFileIsNull"));    
            }

            // Check that source object is an ExtrinsicObject.
            if (!(invocationControlFileEO instanceof ExtrinsicObjectType)) {
                throw new InvalidConfigurationException(
                    ServerResourceBundle.getInstance()
                                        .getString("message.InvocationControlFileIsNotPresentAsAnExtrinsicObject"));
            }

            if (log.isDebugEnabled()) {
                log.debug("InvocationControlFile: " +
                    invocationControlFileEO.getId());
            }

            InvocationController ic = new InvocationController(controlFileForAssocId,
                    invocationControlFileEO.getId());
            invocationControllers.add(ic);
        }

        return invocationControllers;
    }

    /**
     * Gets the single service of the specified classification for the
     * registry object type.
     *
     * @param objectType a <code>String</code> value
     * @param cmsClassificationId a <code>String</code> value
     * @return a <code>ServiceType</code> value
     * @exception InvalidConfigurationException if an error occurs
     * @exception RegistryException if an error occurs
     */
    private ServiceType getServiceType(ServerRequestContext context, String objectType, String cmsClassificationId)
        throws InvalidConfigurationException, RegistryException {
        ServiceType serviceType = null;

        // TODO: Remove or update map entry if new Service added for
        // (any) objectType.
        Collection serviceTypes = getServiceTypes(context, objectType);

        Iterator serviceTypesIter = serviceTypes.iterator();

        while (serviceTypesIter.hasNext()) {
            ServiceType candidateServiceType = (ServiceType) serviceTypesIter.next();

            Collection candidateClassifications = candidateServiceType.getClassification();

            Iterator classificationsIter = candidateClassifications.iterator();

            while (classificationsIter.hasNext()) {
                ClassificationType classification = (ClassificationType) classificationsIter.next();

                if (log.isDebugEnabled()) {
                    log.debug("CandidateServiceType: " + candidateServiceType.getId() +
                        "; classificationNode: " +
                        classification.getClassificationNode());
                }

                if (classification.getClassificationNode().equals(cmsClassificationId)) {
                    serviceType = candidateServiceType;

                    // FIXME: need to break out of two loops.
                    break;
                }
            }
        }

        return serviceType;
    }

    private Collection getServiceTypes(ServerRequestContext context, String objectType)
        throws InvalidConfigurationException, RegistryException {
        Collection serviceTypes;

        // TODO: Remove or update map entry if new Service added for
        // (any) objectType.
        serviceTypes = (Collection) objectTypeToServicesMap.get(objectType);

        //TODO: Don't do lookup if objectType has invalid configuration
        if ((serviceTypes == null) &&
                (!(objectTypeToServicesMap.containsKey(objectType)))) {
            //Lookup failed in map so look dynamically
            serviceTypes = new ArrayList();

            // Get "ContentManagementServiceFor" associations of
            // each object
            Collection serviceAssocs = getAssociations(context, objectType,
                    BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_ContentManagementServiceFor);

            // Iterate over the services
            Iterator serviceAssocsIter = serviceAssocs.iterator();

            while (serviceAssocsIter.hasNext()) {
                AssociationType1 assoc = (AssociationType1) serviceAssocsIter.next();

                if (assoc != null) {
                    Object src = assoc.getSourceObject();

                    ServiceType serviceType = null;

                    try {
                        serviceType = (ServiceType) pm.getRegistryObject(context, bu.getObjectId(
                                    src), "Service");
                    } catch (Exception e) {
                        throw new InvalidConfigurationException(
                            ServerResourceBundle.getInstance()
                                                .getString("message.SourceObjectNotFoundForService", 
                                                    new Object[] {assoc.getId()}), e);
                    }

                    System.err.println("Service: " + serviceType.getId());

                    serviceTypes.add(serviceType);
                }
            }

            //Update the map to avoid dynamic resolution in future for this service type
            objectTypeToServicesMap.put(objectType, (Object) serviceTypes);
        }

        return serviceTypes;
    }

    /**
     *
     * Gets the Associations that have the specified assocType and have
     * as targetObject the ClassificationNode pointed to by objectType
     * or an ancestor of the ClassificationNode pointed to by
     * objectType.
     *
     */
    private Collection getAssociations(ServerRequestContext context, String objectType, String assocType)
        throws RegistryException {
        Collection associations = new ArrayList();

        try {
            RegistryObjectType ro = pm.getRegistryObject(context, objectType,
                    "ClassificationNode");
            ResponseOption responseOption = bu.queryFac.createResponseOption();
            responseOption.setReturnType(ReturnType.LEAF_CLASS);
            responseOption.setReturnComposedObjects(false);

            List objectRefs = new ArrayList();

            if ((ro != null) && (ro instanceof ClassificationNodeType)) {
                ClassificationNodeType node = (ClassificationNodeType) ro;

                String query = "SELECT ass.* from Association ass " +
                    "  WHERE ass.targetObject = '" + node.getId() +
                    "' AND ass.associationType = '" + assocType + "' ";

                objectRefs.clear();

                associations = pm.executeSQLQuery(context, query, responseOption,
                        "Association", objectRefs);

                //TODO: Fix following design bug...
                //If there is a CatalogingService defined at
                //sub-class of XML (e.g. CPP) level then it will
                //not look at XML level even for a different type
                //of service such as FilteringService.
                //Base class lookup should be done if no service
                //is found for a specific type of CMS service.
                if (associations.size() == 0) {
                    //Check if an Association exists for the parent node
                    Object parent = node.getParent();

                    if ((parent != null)) {
                        String parentId = bu.getObjectId(parent);
                        associations = getAssociations(context, parentId, assocType);
                    }
                }
            }
        } catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }

        return associations;
    }

    /**
     * Gets the <code>ExtrinsicObjectType</code> as a stream of XML markup.
     *
     * @param eo an <code>ExtrinsicObjectType</code> value
     * @return a <code>StreamSource</code> value
     * @exception RegistryException if an error occurs
     */
    protected static StreamSource getAsStreamSource(ExtrinsicObjectType eo)
        throws RegistryException {
        log.trace("getAsStreamSource(ExtrinsicObjectType ) entered");

        StreamSource src = null;

        try {
            StringWriter sw = new StringWriter();

            Marshaller marshaller = bu.getJAXBContext().createMarshaller();

            marshaller.marshal(eo, sw);

            StringReader reader = new StringReader(sw.toString());
            src = new StreamSource(reader);
        }
        // these Exceptions should already be caught by Binding
        catch (JAXBException e) {
            throw new RegistryException(e);
        }

        return src;
    }
}
