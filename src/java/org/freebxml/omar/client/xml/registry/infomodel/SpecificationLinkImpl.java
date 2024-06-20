/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/SpecificationLinkImpl.java,v 1.17 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.exceptions.MissingParentReferenceException;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;


/**
 * Implements JAXR API interface named SpecificationLink.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class SpecificationLinkImpl extends RegistryObjectImpl
    implements SpecificationLink {
    private RegistryObjectRef specificationObjectRef = null;
    private InternationalString usageDescription = null;
    private ArrayList usageParams = new ArrayList();
    private RegistryObjectRef bindingRef = null;
   
    public SpecificationLinkImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public SpecificationLinkImpl(LifeCycleManagerImpl lcm,
        SpecificationLinkType ebSpecLink) throws JAXRException {
        super(lcm, ebSpecLink);

        //In version 2.1 there is no servicebinding on specLink du to a spec bug.
        //SO instead of a lazy fetck we will fetch up front
        bindingRef = new RegistryObjectRef(lcm, ebSpecLink.getServiceBinding());        
        if (ebSpecLink.getSpecificationObject() != null) {
            specificationObjectRef = new RegistryObjectRef(lcm,
                    ebSpecLink.getSpecificationObject());
        }

        if (ebSpecLink.getUsageDescription() != null) {
            usageDescription = new InternationalStringImpl(lcm,
                    ebSpecLink.getUsageDescription());
        }

        Iterator usageParamsIt = ebSpecLink.getUsageParameter().iterator();

        while (usageParamsIt.hasNext()) {
                org.oasis.ebxml.registry.bindings.rim.impl.UsageParameterImpl usageParam = new org.oasis.ebxml.registry.bindings.rim.impl.UsageParameterImpl();
                usageParam.setValue((String)usageParamsIt.next());
                usageParams.add(usageParam.getValue());
        }
    }

    public RegistryObject getSpecificationObject() throws JAXRException {
        RegistryObject specificationObject = null;

        if (specificationObjectRef != null) {
            specificationObject = specificationObjectRef.getRegistryObject(
                    "RegistryObject");
        }

        return specificationObject;
    }

    public void setSpecificationObject(RegistryObject specificationObject)
        throws JAXRException {
        specificationObjectRef = new RegistryObjectRef(lcm, specificationObject);
        setModified(true);
    }

    public InternationalString getUsageDescription() throws JAXRException {
        if (usageDescription == null) {
            usageDescription = lcm.createInternationalString();
        }

        return usageDescription;
    }

    public void setUsageDescription(InternationalString desc)
        throws JAXRException {
        usageDescription = desc;
        setModified(true);
    }

    public Collection getUsageParameters() throws JAXRException {
        return usageParams;
    }

    public void setUsageParameters(Collection par1) throws JAXRException {
        usageParams.clear();
        usageParams.addAll(par1);
        setModified(true);
    }

    //??JAXR 2.0
    public RegistryObjectRef getServiceBindingRef() throws JAXRException {
        return bindingRef;
    }

    public ServiceBinding getServiceBinding() throws JAXRException {
        ServiceBinding binding = null;

        if (bindingRef != null) {
            binding = (ServiceBinding) bindingRef.getRegistryObject(
                    "ServiceBinding");
        } else {
            if (!isNew()) {
                //Do a query to get the parent ServiceBinding from server
                //???
                throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.unfinished.code"));
            }
        }

        return binding;
    }

    /**
     * Internal method to set the sourceObject
     */
    void setServiceBindingInternal(ServiceBinding binding)
        throws JAXRException {
        if (binding == null) {
            bindingRef = null;
        } else {
            bindingRef = new RegistryObjectRef(lcm, binding);
        }
        setModified(true);
    }

    /**
     * Not to be used by clients. Expected to be used only by ServiceBindingImpl.
     */
    void setServiceBinding(ServiceBinding _binding) throws JAXRException {
        RegistryObjectRef serviceBindingRef = getServiceBindingRef();

        if ((_binding == null && bindingRef != null) || ((serviceBindingRef == null) || (!(_binding.getKey().getId().equals(serviceBindingRef.getId()))))) {
            setServiceBindingInternal(_binding);
        }
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.SpecificationLink ebSpecLink = factory.createSpecificationLink();

            setBindingObject(ebSpecLink);

            return ebSpecLink;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType ebSpecLink)
        throws JAXRException {
        super.setBindingObject(ebSpecLink);

        //private ArrayList usageParams = new ArrayList();

        /*
         * Version 2.1 does not have serviceBinding attribute on SpecificationLink
        if (binding != null) {
            org.oasis.ebxml.registry.bindings.rim.ObjectRef ebServiceBindingRef =
            new org.oasis.ebxml.registry.bindings.rim.ObjectRef();
            ebServiceBindingRef.setId(binding.getKey().getId());
            ebSpecLink.setServiceBinding(ebServiceBindingRef);
        }
         */
        if (specificationObjectRef != null) {
            ebSpecLink.setSpecificationObject(specificationObjectRef.getId());
        }

        if (bindingRef != null) {
            ebSpecLink.setServiceBinding(bindingRef.getId());
        } else {
            throw new MissingParentReferenceException(
                JAXRResourceBundle.getInstance().getString("message.error.missing.servicebinding.id",new Object[] {getId()}));
        }

        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.UsageDescription ebUsageDesc = factory.createUsageDescription();
            ((InternationalStringImpl) getUsageDescription()).setBindingObject(ebUsageDesc);
            ebSpecLink.setUsageDescription(ebUsageDesc);
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }

        Iterator iter = getUsageParameters().iterator();

        while (iter.hasNext()) {
            String usageParam = (String) iter.next();
            ebSpecLink.getUsageParameter().add(usageParam);
        }
    }

    /**
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet getRegistryObjectRefs() {
        HashSet refs = new HashSet();

        //refs.addAll(super.getRegistryObjectRefs());
        if (specificationObjectRef != null) {
            refs.add(specificationObjectRef);
        }

        return refs;
    }
    


}
