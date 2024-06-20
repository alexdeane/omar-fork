/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/BusinessLifeCycleManagerImpl.java,v 1.10 2007/03/23 18:38:59 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnexpectedObjectException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.User;

import org.freebxml.omar.client.xml.registry.infomodel.AssociationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryObjectImpl;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;

/**
 * Implements JAXR API interface named BusinessLifeCycleManager.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class BusinessLifeCycleManagerImpl extends LifeCycleManagerImpl
    implements BusinessLifeCycleManager {
    BusinessLifeCycleManagerImpl(RegistryServiceImpl service) {
        super(service);
    }

    /**
     * Saves specified Organizations.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     *
     */
    public BulkResponse saveOrganizations(Collection organizations)
        throws JAXRException {
        Iterator iter = organizations.iterator();

        while (iter.hasNext()) {
            Object org = iter.next();

            if (!(org instanceof Organization)) {
                throw new UnexpectedObjectException(
                    JAXRResourceBundle.getInstance().getString("message.error.expecting.organization",new Object[] {org.getClass().getName()}));
            }
        }

        return saveObjects(organizations);
    }

    /**
     * Saves specified Services.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveServices(Collection services)
        throws JAXRException {
        Iterator iter = services.iterator();

        while (iter.hasNext()) {
            Object service = iter.next();

            if (!(service instanceof Service)) {
                throw new UnexpectedObjectException(
                    JAXRResourceBundle.getInstance().getString("message.error.expecting.service",new Object[] {service.getClass().getName()}));
            }
        }

        return saveObjects(services);
    }

    /**
     * Saves specified ServiceBindings.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveServiceBindings(Collection bindings)
        throws JAXRException {
        Iterator iter = bindings.iterator();

        while (iter.hasNext()) {
            Object binding = iter.next();

            if (!(binding instanceof ServiceBinding)) {
                throw new UnexpectedObjectException(
                    JAXRResourceBundle.getInstance().getString("message.error.expecting.servicebinding",new Object[] {binding.getClass().getName()}));
            }
        }

        return saveObjects(bindings);
    }

    /**
     * Saves specified Concepts.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveConcepts(Collection concepts)
        throws JAXRException {
        Iterator iter = concepts.iterator();

        while (iter.hasNext()) {
            Object concept = iter.next();

            if (!(concept instanceof Concept)) {
                throw new UnexpectedObjectException(
                    JAXRResourceBundle.getInstance().getString("message.error.expecting.concept",new Object[] {concept.getClass().getName()}));
            }
        }

        return saveObjects(concepts);
    }

    /**
     * Saves specified ClassificationScheme instances.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveClassificationSchemes(Collection schemes)
        throws JAXRException {
        Iterator iter = schemes.iterator();

        while (iter.hasNext()) {
            Object scheme = iter.next();

            if (!(scheme instanceof ClassificationScheme)) {
                throw new UnexpectedObjectException(
                    JAXRResourceBundle.getInstance().getString("message.error.expecting.classScheme",new Object[] {scheme.getClass().getName()}));
            }
        }

        return saveObjects(schemes);
    }

    /**
     * Saves specified Association instances.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param replace If set to true then the specified associations replace any existing associations owned by the caller. If set to false specified associations are saved while preserving any existing associations that are not being updated by this call.
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveAssociations(Collection associations,
        boolean replace) throws JAXRException {
        Iterator iter = associations.iterator();

        while (iter.hasNext()) {
            Object association = iter.next();

            if (!(association instanceof Association)) {
                throw new UnexpectedObjectException(
                     JAXRResourceBundle.getInstance().getString("message.error.expecting.association",new Object[] {association.getClass().getName()}));
            }
        }

        //??What to do with replace
        return saveObjects(associations);
    }

    /**
     * Deletes the organizations corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteOrganizations(Collection organizationKeys)
        throws JAXRException {
        return deleteObjects(organizationKeys);
    }

    /**
     * Delete the services corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteServices(Collection serviceKeys)
        throws JAXRException {
        return deleteObjects(serviceKeys);
    }

    /**
     * Delete the ServiceBindings corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteServiceBindings(Collection bindingKeys)
        throws JAXRException {
        return deleteObjects(bindingKeys);
    }

    /**
     * Delete the Concepts corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteConcepts(Collection conceptKeys)
        throws JAXRException {
        return deleteObjects(conceptKeys);
    }

    /**
     * Delete the ClassificationSchemes corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteClassificationSchemes(Collection schemeKeys)
        throws JAXRException {
        return deleteObjects(schemeKeys);
    }

    /**
     * Delete the Associations corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first
     * DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those
     * objects that were deleted successfully and any DeleteException that
     * was encountered in case of partial commit.
     */
    public BulkResponse deleteAssociations(Collection assKeys)
        throws JAXRException {
        return deleteObjects(assKeys);
    }

    public void confirmAssociation(Association association)
        throws JAXRException, InvalidRequestException {
        if (!association.isExtramural()) {
            return;
        }

        if (association.isConfirmed()) {
            return;
        }

        RegistryObject src = association.getSourceObject();
        RegistryObject target = association.getTargetObject();
        User srcOwner = ((RegistryObjectImpl)src).getOwner();
        User targetOwner = ((RegistryObjectImpl)target).getOwner();
        
        // Confirm the Association by saving the same object again
        AssociationImpl assImpl = (AssociationImpl) association;
        User caller = ((BusinessQueryManagerImpl)(getRegistryService().getBusinessQueryManager())).getCallersUser();

        ArrayList slotValue = new ArrayList();
        slotValue.add("true");
        slotValue.add(caller.getKey().getId()); //Also remember id of user doing confirmation

        if (caller.equals(srcOwner)) {
            //Just in case it is already there, remove Slot before adding
            assImpl.removeSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER);
            assImpl.removeSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_BEING_CONFIRMED);
            
            //Add a special Slot as hint that this is a confirmation
            Slot slot1 = this.createSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_BEING_CONFIRMED,
                "true", null);
            assImpl.addSlot(slot1);
            Slot slot2 = this.createSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER,
                slotValue, null);
            assImpl.addSlot(slot2);
            
        } else if (caller.equals(targetOwner)) {
            //Just in case it is already there, remove Slot before adding
            assImpl.removeSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER);
            assImpl.removeSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_BEING_CONFIRMED);
            
            //Add a special Slot as hint that this is a confirmation
            Slot slot1 = this.createSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_BEING_CONFIRMED,
                "true", null);
            assImpl.addSlot(slot1);
            Slot slot2 = this.createSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER,
                slotValue, null);
            assImpl.addSlot(slot2);
            
        } else {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.confirm.association.exception"));
        }
    }

    public void unConfirmAssociation(Association association)
        throws JAXRException, InvalidRequestException {
        //??
        // Send a removeObjectsReq on ass similar to confirm and submitObjects
        // me = getMyUser
        // if (src owner is me) then unconfirm src
        // else if (target owner is me then unconfirm target
    }
}
