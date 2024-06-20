/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/ServiceBindingImpl.java,v 1.20 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.exceptions.MissingParentReferenceException;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;


/**
 * Implements JAXR API interface named ServiceBinding.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ServiceBindingImpl extends RegistryObjectImpl
    implements ServiceBinding {
    private RegistryObjectRef serviceRef = null;
    private String accessURI = null;
    private RegistryObjectRef targetBindingRef = null;
    private HashSet specLinks = new HashSet();
    private boolean validateURI = true;

    public ServiceBindingImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public ServiceBindingImpl(LifeCycleManagerImpl lcm,
        ServiceBindingType ebBinding) throws JAXRException {
        super(lcm, ebBinding);

        serviceRef = new RegistryObjectRef(lcm, ebBinding.getService());

        accessURI = ebBinding.getAccessURI();

        Object targetBindingObj = ebBinding.getTargetBinding();

        if (targetBindingObj != null) {
            targetBindingRef = new RegistryObjectRef(lcm, targetBindingObj);
        }

        Iterator ebSpecLinks = ebBinding.getSpecificationLink().iterator();

        while (ebSpecLinks.hasNext()) {
            org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType ebSpecLink =
                (org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType) ebSpecLinks.next();
            addSpecificationLink(new SpecificationLinkImpl(lcm, ebSpecLink));
        }
    }

    public String getAccessURI() throws JAXRException {
        return accessURI;
    }

    public void setAccessURI(String uri) throws JAXRException {
        if ((uri != null) && (targetBindingRef != null) && (uri.length() > 0 )) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.targetbinding.notnull.error"));
        }
        validateURI(uri);
        accessURI = uri;
        setModified(true);
    }

    //??JAXR 2.0
    public RegistryObjectRef getTargetBindingRef() throws JAXRException {
        return targetBindingRef;
    }

    public ServiceBinding getTargetBinding() throws JAXRException {
        ServiceBinding targetBinding = null;

        if (targetBindingRef != null) {
            targetBinding = (ServiceBinding) targetBindingRef.getRegistryObject(
                    "ServiceBinding");
        }

        return targetBinding;
    }

    /**
     * Internal method to set the targetBinding
     */
    void setTargetBindingInternal(ServiceBinding targetBinding)
        throws JAXRException {
        targetBindingRef = new RegistryObjectRef(lcm, targetBinding);
        setModified(true);
    }

    public void setTargetBinding(ServiceBinding _binding)
        throws JAXRException {
        if (_binding == null){
            targetBindingRef = null;
        }else{
        if ((_binding != null) && (accessURI != null) && (accessURI.length() > 0)) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.accessURI.notnull.error"));
        }
        if ((targetBindingRef == null) || (!(_binding.getKey().getId().equals(targetBindingRef.getId())))) {
            if (!(_binding instanceof ServiceBindingImpl)) {
                throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.expected.serviceBindingImpl",new Object[] {_binding}));
            }

            setTargetBindingInternal(_binding);
        }
       }
    }

    //??JAXR 2.0
    public RegistryObjectRef getServiceRef() throws JAXRException {
        return serviceRef;
    }

    public Service getService() throws JAXRException {
        Service service = null;

        if (serviceRef != null) {
            service = (Service) serviceRef.getRegistryObject("Service");
        }

        return service;
    }

    /**
     * Internal method to set the service
     */
    void setServiceInternal(Service service) throws JAXRException {
        if (service != null) {
            serviceRef = new RegistryObjectRef(lcm, service);
        } else {
            serviceRef = null;
        }
        setModified(true);
    }

    /**
     * Not to be used by clients. Expected to be used only by ServiceImpl.
     */
    void setService(Service service) throws JAXRException {
        if (serviceRef != null) {
            if (service != null) {
                if (!(service.getKey().getId().equals(serviceRef.getId()))) {
                    if (!(service instanceof ServiceImpl)) {
                        throw new InvalidRequestException(
                            JAXRResourceBundle.getInstance().getString("message.error.expected.ServiceImpl",new Object[] {service}));
                    }
                }
            }
        }

        setServiceInternal(service);
    }

    public void addSpecificationLink(SpecificationLink specLink)
        throws JAXRException {
        specLinks.add(specLink);
        ((SpecificationLinkImpl) specLink).setServiceBinding(this);

        //In version 2.1 there is no parent attribute in SpecificationLink (fixed in 2.5).
        //This means that a SpecificationLink must be saved within a ServiceBinding.
        //Therefor we must force the ServiceBinding to be saved. 
        setModified(true);
    }

    public void addSpecificationLinks(Collection _specLinks)
        throws JAXRException {
        Iterator iter = _specLinks.iterator();

        while (iter.hasNext()) {
            Object obj = (SpecificationLinkImpl) iter.next();

            if (!(obj instanceof SpecificationLinkImpl)) {
                throw new InvalidRequestException(
                    JAXRResourceBundle.getInstance().getString("message.error.expected.SpecificationLinkImpl",new Object[] {obj}));
            }

            SpecificationLinkImpl specLink = (SpecificationLinkImpl) obj;
            addSpecificationLink(specLink);
        }
    }

    public void removeSpecificationLink(SpecificationLink specLink)
        throws JAXRException {
        specLinks.remove(specLink);
        ((SpecificationLinkImpl) specLink).setServiceBinding(null);

        //In version 2.1 there is no parent attribute in SpecificationLink (fixed in 2.5).
        //This means that a SpecificationLink must be saved within a ServiceBinding.
        //Therefor we must force the ServiceBinding to be saved. 
        setModified(true);
    }

    public void removeSpecificationLinks(Collection _specLinks)
        throws JAXRException {
        Iterator iter = _specLinks.iterator();

        while (iter.hasNext()) {
            Object obj = (SpecificationLinkImpl) iter.next();

            if (!(obj instanceof SpecificationLinkImpl)) {
                throw new InvalidRequestException(
                    JAXRResourceBundle.getInstance().getString("message.error.expected.SpecificationLinkImpl",new Object[] {obj}));
            }

            SpecificationLinkImpl specLink = (SpecificationLinkImpl) obj;
            removeSpecificationLink(specLink);
        }
    }

    public Collection getSpecificationLinks() throws JAXRException {
        return (HashSet) (specLinks.clone());
    }

    public boolean getValidateURI() throws JAXRException {
        return validateURI;
    }     

    public void setValidateURI(boolean validateURI) throws JAXRException {
        this.validateURI = validateURI;
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.ServiceBinding ebBinding = factory.createServiceBinding();

            setBindingObject(ebBinding);

            return ebBinding;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.ServiceBindingType ebBinding)
        throws JAXRException {
        super.setBindingObject(ebBinding);

        if (accessURI != null) {
            ebBinding.setAccessURI(accessURI);
        }

        if (targetBindingRef != null) {
            ebBinding.setTargetBinding(targetBindingRef.getId());
        }

        if (serviceRef != null) {
            ebBinding.setService(serviceRef.getId());
        } else {
            throw new MissingParentReferenceException(
                JAXRResourceBundle.getInstance().getString("message.error.missing.service.id", new Object[] {getId()}));
        }

        Iterator iter = getSpecificationLinks().iterator();

        while (iter.hasNext()) {
            SpecificationLinkImpl specLink = (SpecificationLinkImpl) iter.next();
            ebBinding.getSpecificationLink().add(specLink.toBindingObject());
        }
    }

    public HashSet getRIMComposedObjects()
        throws JAXRException {
        return getComposedObjects();
    }
    
    public HashSet getComposedObjects()
        throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();
        
        Collection specLinks = getSpecificationLinks();
        composedObjects.addAll(specLinks);
        
        return composedObjects;
    }

    /**
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet getRegistryObjectRefs() {
        HashSet refs = new HashSet();

        //refs.addAll(super.getRegistryObjectRefs());
        if (targetBindingRef != null) {
            refs.add(targetBindingRef);
        }

        if (serviceRef != null) {
            refs.add(serviceRef);
        }

        return refs;
    }
    
    private void validateURI(String uri) throws InvalidRequestException {        
        
        if (validateURI) {
            // check the http url
            boolean isValid = org.freebxml.omar.common.Utility.getInstance().isValidURI(uri);

            if (!isValid) {
                throw new InvalidRequestException(" "+JAXRResourceBundle.getInstance().getString("message.error.url.not.resolvable",new Object[] {uri}));
            }
        }
    }
    
}
