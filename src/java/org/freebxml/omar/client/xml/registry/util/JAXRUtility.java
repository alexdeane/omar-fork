/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.List;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStream;
import javax.activation.DataHandler;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.User;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.bind.JAXBException;
import org.freebxml.omar.client.xml.registry.InfomodelFactory;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.ObjectCache;
import org.freebxml.omar.client.xml.registry.RegistryServiceImpl;
import org.freebxml.omar.client.xml.registry.infomodel.AdhocQueryImpl;
import org.freebxml.omar.client.xml.registry.infomodel.AssociationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.AuditableEventImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ClassificationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ClassificationSchemeImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ConceptImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ExternalIdentifierImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ExternalLinkImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import org.freebxml.omar.client.xml.registry.infomodel.FederationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.OrganizationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.PersonImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryPackageImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ServiceBindingImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ServiceImpl;
import org.freebxml.omar.client.xml.registry.infomodel.SpecificationLinkImpl;
import org.freebxml.omar.client.xml.registry.infomodel.SubscriptionImpl;
import org.freebxml.omar.client.xml.registry.infomodel.UserImpl;

import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.FederationType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.OrganizationType;
import org.oasis.ebxml.registry.bindings.rim.PersonType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rim.SlotListType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;
import org.oasis.ebxml.registry.bindings.rim.Value;

import org.freebxml.omar.client.xml.registry.ConnectionFactoryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Miscellaneous utility methods useful to JAXR client programs.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class JAXRUtility {

    private static Properties bundledProperties = null;
    private static final Log log = LogFactory.getLog(JAXRUtility.class);
    private static InfomodelFactory imFactory = InfomodelFactory.getInstance();


    /**
     * Retrieves id from RegistryObject.
     *
     * @param obj
     * @return
     * @throws JAXRException
     */
    public static String toId(Object obj) throws JAXRException {
        if (obj instanceof RegistryObject) {
            RegistryObject ro = (RegistryObject) obj;
            return ro.getKey().getId();
        } else {
            return "[toId: not a RegistryObject, obj=" + obj + "]";
        }
    }

    /**
     * Retrieves first object from a Collection.
     *
     * @param col
     * @return
     */
    public static Object getFirstObject(Collection col) {
        if (col == null) {
            return null;
        }
        Iterator it = col.iterator();
        if (!it.hasNext()) {
            return null;
        }
        return it.next();
    }

    /**
     * Convert JAXB generated java binding objects for RIM classes to equivalent JAXR Objects.
     * This version does not set repositoryItemMap and is therefor deprecated.
     *
     * @deprecated As of release 3.0-final, replaced by {@link #getJAXRObjectsFromJAXBObjects(LifeCycleManagerImpl, List, Map)}
     */
    public static List getJAXRObjectsFromJAXBObjects(LifeCycleManagerImpl lcm, List jaxbObjects) throws JAXRException {
        return getJAXRObjectsFromJAXBObjects(lcm, jaxbObjects, null);
    }

    /**
     * Convert JAXB generated java binding objects for RIM classes to equivalent JAXR Objects.
     * Also sets the repositoryItemMap.
     *
     */
    public static List getJAXRObjectsFromJAXBObjects(LifeCycleManagerImpl lcm, List jaxbObjects, Map repositoryItemsMap) throws JAXRException {
        List jaxrObjects = new ArrayList();
        ObjectCache objCache = ((RegistryServiceImpl) (lcm.getRegistryService())).getObjectCache();


        Iterator iter = jaxbObjects.iterator();
        while (iter.hasNext()) {
            IdentifiableType obj = (IdentifiableType) iter.next();

            if (obj instanceof ClassificationSchemeType) {
                ClassificationSchemeImpl scheme = new ClassificationSchemeImpl(lcm, (ClassificationSchemeType) obj);
                objCache.putRegistryObject(scheme);
                jaxrObjects.add(scheme);
                continue;
            } else if (obj instanceof ClassificationType) {
                ClassificationType classType = (ClassificationType)obj;
                // Get classified RegistryObject
                String classObjId = classType.getClassifiedObject();
                RegistryObject classifiedRO = lcm.getRegistryService()
                                       .getBusinessQueryManager()
                                       .getRegistryObject(classObjId);

                ClassificationImpl cls = new ClassificationImpl(lcm, (ClassificationType) obj, classifiedRO);
                objCache.putRegistryObject(cls);
                jaxrObjects.add(cls);

                continue;
            } else if (obj instanceof OrganizationType) {
                OrganizationImpl org = new OrganizationImpl(lcm, (OrganizationType) obj);
                objCache.putRegistryObject(org);
                jaxrObjects.add(org);

                continue;
            } else if (obj instanceof AssociationType1) {
                AssociationImpl ass =
                    imFactory.createAssociation(lcm, (AssociationType1)obj);
                objCache.putRegistryObject(ass);
                jaxrObjects.add(ass);

                continue;
            } else if (obj instanceof RegistryPackageType) {
                RegistryPackageImpl pkg = new RegistryPackageImpl(lcm,
                        (RegistryPackageType) obj);
                objCache.putRegistryObject(pkg);
                jaxrObjects.add(pkg);

                continue;
            } else if (obj instanceof ExternalLinkType) {
                ExternalLinkImpl extLink = new ExternalLinkImpl(lcm,
                        (ExternalLinkType) obj);
                objCache.putRegistryObject(extLink);
                jaxrObjects.add(extLink);

                continue;
            } else if (obj instanceof ExternalIdentifierType) {
                //Pass parent object and not null
                ExternalIdentifierType ebExtIdentifier = (ExternalIdentifierType) obj;
                String parentId = ebExtIdentifier.getRegistryObject();
                ExternalIdentifierImpl extIdentifier = new ExternalIdentifierImpl(lcm,
                        (ExternalIdentifierType) obj);
                objCache.putRegistryObject(extIdentifier);
                jaxrObjects.add(extIdentifier);

                continue;
            } else if (obj instanceof ExtrinsicObjectType) {
                ExtrinsicObjectImpl extrinsicObj =
                    imFactory.createExtrinsicObject(lcm, (ExtrinsicObjectType)obj);
                objCache.putRegistryObject(extrinsicObj);
                jaxrObjects.add(extrinsicObj);

                if ((repositoryItemsMap != null) && (repositoryItemsMap.containsKey(extrinsicObj.getKey().getId()))) {
                   DataHandler repositoryItem = (DataHandler)repositoryItemsMap.get(extrinsicObj.getKey().getId());
                   extrinsicObj.setRepositoryItemInternal(repositoryItem);
                }

                continue;
            } else if (obj instanceof AdhocQueryType) {
                    AdhocQueryImpl adhocQueryObj = new AdhocQueryImpl(lcm,
                        (AdhocQueryType) obj);
                objCache.putRegistryObject(adhocQueryObj);
                jaxrObjects.add(adhocQueryObj);

                continue;
            } else if (obj instanceof ServiceType) {
                ServiceImpl service = new ServiceImpl(lcm, (ServiceType) obj);
                objCache.putRegistryObject(service);
                jaxrObjects.add(service);

                continue;
            } else if (obj instanceof ServiceBindingType) {
                ServiceBindingImpl binding = new ServiceBindingImpl(lcm,
                        (ServiceBindingType) obj);
                objCache.putRegistryObject(binding);
                jaxrObjects.add(binding);

                continue;
            } else if (obj instanceof SubscriptionType) {
                SubscriptionImpl subscription = new SubscriptionImpl(lcm,
                        (SubscriptionType) obj);
                objCache.putRegistryObject(subscription);
                jaxrObjects.add(subscription);

                continue;
            } else if (obj instanceof SpecificationLinkType) {
                SpecificationLinkImpl specLink = new SpecificationLinkImpl(lcm,
                        (SpecificationLinkType) obj);
                objCache.putRegistryObject(specLink);
                jaxrObjects.add(specLink);

                continue;
            } else if (obj instanceof ClassificationNodeType) {
                ConceptImpl concept = new ConceptImpl(lcm,
                        (ClassificationNodeType) obj);
                objCache.putRegistryObject(concept);
                jaxrObjects.add(concept);

                continue;
            } else if (obj instanceof ObjectRefType) {
                // ObjectRef-s are processed by leaf components
                continue;
            } else if (obj instanceof AuditableEventType) {
                AuditableEventImpl ae = new AuditableEventImpl(lcm,
                        (AuditableEventType) obj);
                objCache.putRegistryObject(ae);
                jaxrObjects.add(ae);

                continue;
            } else if (obj instanceof UserType) {
                UserImpl user = new UserImpl(lcm, (UserType) obj);
                objCache.putRegistryObject(user);
                jaxrObjects.add(user);

                continue;
            } else if (obj instanceof PersonType) {
                PersonImpl person = new PersonImpl(lcm, (PersonType) obj);
                objCache.putRegistryObject(person);
                jaxrObjects.add(person);

                continue;
            } else if (obj instanceof RegistryType) {
                RegistryImpl registry = new RegistryImpl(lcm,
                        (RegistryType) obj);
                objCache.putRegistryObject(registry);
                jaxrObjects.add(registry);

                continue;
            } else if (obj instanceof FederationType) {
                FederationImpl federation = new FederationImpl(lcm,
                        (FederationType) obj);
                objCache.putRegistryObject(federation);
                jaxrObjects.add(federation);

                continue;
            }
            log.error(JAXRResourceBundle.getInstance().getString("message.NotImplemented", new Object[]{obj.getClass().getName()}));
        }

        return jaxrObjects;
    }

    /**
     * Throws exception if BulkResponse contains any exceptions.
     *
     * @param response
     * @throws JAXRException
     */
    public static void checkBulkResponse(BulkResponse response)
    throws JAXRException {
        Collection exes = response.getExceptions();
        if (exes == null) {
            return;
        }
        throw new JAXRException((JAXRException) getFirstObject(exes));
    }

    /**
     * Retrieves owner of RegistryObject.
     *
     * @param ro RegistryObject to get the owner of
     * @return owner, ie. creator or null if this is a new RegistryObject
     */
    public static User getOwner(RegistryObject ro) throws JAXRException {
        return ((org.freebxml.omar.client.xml.registry.infomodel.RegistryObjectImpl)ro).getOwner();
    }


    public static Collection getKeysFromObjects(Collection registryObjects) throws JAXRException {
        ArrayList keys = new ArrayList();

        Iterator iter = registryObjects.iterator();

        while (iter.hasNext()) {
            RegistryObject obj = (RegistryObject) iter.next();
            Key key = obj.getKey();
            keys.add(key);
        }
        return keys;
    }

    public static Properties getBundledClientProperties() {
        if (bundledProperties == null) {
            bundledProperties = new Properties();

            BufferedReader in = null;

            try {
                InputStream propInputStream = JAXRUtility.class.getClassLoader()
                                                  .getResourceAsStream("jaxr-ebxml.properties");

                if (propInputStream != null) {
                    bundledProperties.load(propInputStream);
                }
            } catch (Throwable t) {
                log.error(JAXRResourceBundle.getInstance().getString("message.ProblemReadingBundledOmarpropertiesFile"));
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception ex) {
                    }
                }
            }
        }
        return bundledProperties;
    }

    /**
     * Adds slots specified by slotsMap to RegistryObject ro.
     *
     */
    public static void addSlotsToRegistryObject(RegistryObject ro, Map slotsMap) throws JAXRException {
        ArrayList slots = new ArrayList();

        Iterator iter = slotsMap.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            Object slotValue = slotsMap.get(key);

            Slot slot = null;

            //slotValue must either be a String or a Collection of Strings
            if (slotValue instanceof String) {
                slot = ro.getLifeCycleManager().createSlot((String)key, (String)slotValue, (String)null);
            } else if (slotValue instanceof Collection) {
                Collection c = (Collection)slotValue;
                slot = ro.getLifeCycleManager().createSlot((String)key, c, (String)null);
            } else {
                //??throw new IllegalArgumentException(resourceBundle.getString("message.addingParameter",
                //        new String[]{slotValue.getClass().getName()}));
            }
            if (slot != null) {
                slots.add(slot);
            }
        }

        ro.addSlots(slots);
    }

    /**
     * This method is used to add special rim:Slot to the RegistryRequestType
     * object to indicate that the server should create a secure session.
     *
     * @param req
     *   The RegistryRequestType to which the rim:Slot will be added
     * @param connection
     *   A javax.registry.Connection implementation.  This method will use this
     *   class to determine if a secure session should be created:
     *   If the connection's credential set is empty, do not create session.
     *   Empty credentials means the user has not authenticated. Thus, the
     *   user defaults to RegistryGuest and secure session is not needed.
     */
    public static void addCreateSessionSlot(RegistryRequestType req,
        Connection connection)
        throws JAXBException {

        // check if there are credentials.  If not, there is no need for a
        // secure session as the UserType is RegistryGuest
        boolean isEmpty = true;
        try {
            isEmpty = connection.getCredentials().isEmpty();
        } catch (JAXRException ex) {
            log.error(JAXRResourceBundle.getInstance().getString("message.CouldNotGetCredentialsFromConnectionObjectPresumeCorruptedCredentials"));
        }

        // This property allows the user to control whether or not to use
        // secure sessions.  Default is 'true'.
        String createSecureSession =
            ProviderProperties.getInstance()
                              .getProperty("jaxr-ebxml.security.createSecureSession",
                                           "true");
        if (isEmpty == false && createSecureSession.equalsIgnoreCase("true")) {
            HashMap slotMap = new HashMap();
            slotMap.put("urn:javax:xml:registry:connection:createHttpSession", "true");
            BindingUtility.getInstance().addSlotsToRequest(req, slotMap);
        }
    }

    /**
     * This method is used to create an instance of the
     * javax.xml.registry.ConnectionFactory interface. By default, it will create
     * an instance of org.freebxml.omar.client.xml.registry.ConnectionFactoryImpl
     *
     * @return
     *   A org.freebxml.omar.client.xml.registry.ConnectionFactoryImpl instance
     * @see
     *   org.freebxml.omar.client.xml.registry.ConnectionFactoryImpl
     */
    public static ConnectionFactory getConnectionFactory() throws JAXRException {
        return new ConnectionFactoryImpl();
    }

}
