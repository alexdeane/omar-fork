/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/ServiceImpl.java,v 1.19 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.Slot;

import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;


/**
 * Implements JAXR API interface named Service.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ServiceImpl extends RegistryEntryImpl implements Service {
    private static final String PROVIDING_ORGANIZATION_SLOT_NAME = "providingOrganization";
    private HashSet bindings = new HashSet();
    private RegistryObjectRef providingOrgRef = null;

    public ServiceImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);
    }

    public ServiceImpl(LifeCycleManagerImpl lcm, ServiceType ebService)
        throws JAXRException {
        super(lcm, ebService);

        Iterator ebBindings = ebService.getServiceBinding().iterator();

        while (ebBindings.hasNext()) {
            org.oasis.ebxml.registry.bindings.rim.ServiceBindingType ebBinding = (org.oasis.ebxml.registry.bindings.rim.ServiceBindingType) ebBindings.next();
            addServiceBinding(new ServiceBindingImpl(lcm, ebBinding));
        }
    }

    public void addServiceBinding(ServiceBinding binding)
        throws JAXRException {
        bindings.add(binding);
        ((ServiceBindingImpl) binding).setService(this);
    }

    public void addServiceBindings(Collection _bindings)
        throws JAXRException {
        Iterator iter = _bindings.iterator();

        while (iter.hasNext()) {
            Object obj = (ServiceBindingImpl) iter.next();

            if (!(obj instanceof ServiceBindingImpl)) {
                throw new InvalidRequestException(
                    JAXRResourceBundle.getInstance().getString("message.error.expected.serviceBindingImpl", new Object[] {obj}));
            }

            ServiceBindingImpl binding = (ServiceBindingImpl) obj;
            addServiceBinding(binding);
        }
    }

    public void removeServiceBinding(ServiceBinding binding)
        throws JAXRException {
        bindings.remove(binding);
        ((ServiceBindingImpl) binding).setService(null);
    }

    public void removeServiceBindings(Collection _bindings)
        throws JAXRException {
        Iterator iter = _bindings.iterator();

        while (iter.hasNext()) {
            Object obj = (ServiceBindingImpl) iter.next();

            if (!(obj instanceof ServiceBindingImpl)) {
                throw new InvalidRequestException(
                    JAXRResourceBundle.getInstance().getString("message.error.expected.serviceBindingImpl", new Object[] {obj}));
            }

            ServiceBindingImpl binding = (ServiceBindingImpl) obj;
            removeServiceBinding(binding);
        }
    }

    public Collection getServiceBindings() throws JAXRException {
        return bindings;
    }

    public void setProvidingOrganization(Organization org) throws JAXRException {
        Slot providingOrgSlot = getSlot(PROVIDING_ORGANIZATION_SLOT_NAME);
        
        if (providingOrgSlot != null) {
            removeSlot(PROVIDING_ORGANIZATION_SLOT_NAME);
        }
        
        String newOrgKey = null;
        
        if (org != null) {
            newOrgKey = org.getKey().getId();
            providingOrgSlot = lcm.createSlot(PROVIDING_ORGANIZATION_SLOT_NAME, newOrgKey, null);
            addSlot(providingOrgSlot);
        }
    }
        
    public Organization getProvidingOrganization()
        throws JAXRException {
        Organization org = null;
        
        Slot providingOrgSlot = getSlot(PROVIDING_ORGANIZATION_SLOT_NAME);
        
        if (providingOrgSlot != null) {
            Collection providingOrgValues = providingOrgSlot.getValues();
            
            if (providingOrgValues.size() != 1) {
                throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.service.more.providing.organization"));
            }
            
            Iterator providingOrgIter = providingOrgValues.iterator();
            providingOrgRef = new RegistryObjectRef(lcm, (String) providingOrgIter.next());
        } else {
            providingOrgRef = null;
        }

        if (providingOrgRef != null) {
            org = (Organization)providingOrgRef.getRegistryObject("Organization");
        }

        
        return org;
    }

    private Association getProviderOfAssociation() throws JAXRException {
        String id = getKey().getId();
	String queryStr =
	    "SELECT ass.* FROM Association ass WHERE targetObject = '" + id +
	    "' AND associationType ='" + BindingUtility.ASSOCIATION_TYPE_ID_ProviderOf + "'";
	Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
	BulkResponse response = dqm.executeQuery(query);
	checkBulkResponseExceptions(response);
	Collection associations = response.getCollection();
        
        Association assoc;
        switch (associations.size()) {
            case 0:
                assoc = null;
                break;
            case 1:
                Iterator iter = associations.iterator();
                assoc = (Association)iter.next();
                break;
            default:
               throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.service.more.providing.organization"));
        }
        
        return assoc;
    }
            
    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.Service ebService = factory.createService();

            setBindingObject(ebService);

            return ebService;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.ServiceType ebService)
        throws JAXRException {
        super.setBindingObject(ebService);

        Iterator iter = getServiceBindings().iterator();

        while (iter.hasNext()) {
            ServiceBindingImpl binding = (ServiceBindingImpl) iter.next();
            ebService.getServiceBinding().add(binding.toBindingObject());
        }
    }

    public HashSet getRIMComposedObjects()
        throws JAXRException {
        return getComposedObjects();
    }
    
    public HashSet getComposedObjects()
        throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();
        
        Collection bindings = getServiceBindings();
        composedObjects.addAll(bindings);
        
        return composedObjects;
    }
}
