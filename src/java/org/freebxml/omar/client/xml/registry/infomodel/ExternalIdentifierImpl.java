/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/ExternalIdentifierImpl.java,v 1.17 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.bind.JAXBException;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.RegistryObject;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.exceptions.MissingAttributeException;
import org.freebxml.omar.common.exceptions.MissingParentReferenceException;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;


/**
 * Implements JAXR API interface named ExternalIdentifier.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ExternalIdentifierImpl extends RegistryObjectImpl
    implements ExternalIdentifier {
    private RegistryObjectRef registryObjectRef = null;
    private RegistryObjectRef schemeRef = null;
    private String value = null;

    public ExternalIdentifierImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public ExternalIdentifierImpl(LifeCycleManagerImpl lcm,
        ExternalIdentifierType ebExtIdentifier)
        throws JAXRException {
        super(lcm, ebExtIdentifier);

        registryObjectRef = new RegistryObjectRef(lcm,
                ebExtIdentifier.getRegistryObject());
        schemeRef = new RegistryObjectRef(lcm,
                ebExtIdentifier.getIdentificationScheme());
        value = ebExtIdentifier.getValue();
    }

    //??JAXR 2.0
    public RegistryObjectRef getRegistryObjectRef() throws JAXRException {
        return registryObjectRef;
    }
    
    public RegistryObject getRegistryObject() throws JAXRException {
        RegistryObject registryObject = null;

        if (registryObjectRef != null) {
            registryObject = (RegistryObject) registryObjectRef.getRegistryObject(
                    "RegistryObject");
        }

        return registryObject;
    }
    
    /**
     * Internal method to set the service
     */
    void setRegistryObjectInternal(RegistryObject registryObject) throws JAXRException {
        if (registryObject != null) {
            registryObjectRef = new RegistryObjectRef(lcm, registryObject);
        } else {
            registryObjectRef = null;
        }
        setModified(true);
    }

    /**
     * Sets the RegistryObject parent for this object.
     * Also adds it to parent if not already added.
     *
     * //TODO: JAXR 2.0??
     */
    public void setRegistryObject(RegistryObject registryObject) throws JAXRException {
        if (registryObjectRef != null) {
            if (registryObject != null) {
                if (!(registryObject.getKey().getId().equals(registryObjectRef.getId()))) {
                    if (!(registryObject instanceof RegistryObjectImpl)) {
                        throw new InvalidRequestException(
                            JAXRResourceBundle.getInstance().getString("message.error.expected.regestry.object",new Object[] {registryObject}));
                    }
                }
            }
        }

        setRegistryObjectInternal(registryObject);
        
        
        //In case this was called directly by client, make sure that this classification is added to classified Object
        Collection extIds = registryObject.getExternalIdentifiers();

        if (!extIds.contains(this)) {
            registryObject.addExternalIdentifier(this);
        }
         
    }
    

    public String getValue() throws JAXRException {
        return value;
    }

    public void setValue(String par1) throws JAXRException {
        value = par1;
        setModified(true);
    }

    public ClassificationScheme getIdentificationScheme()
        throws JAXRException {
        ClassificationScheme scheme = null;

        if (schemeRef != null) {
            scheme = (ClassificationScheme) schemeRef.getRegistryObject(
                    "ClassificationScheme");
        }

        return scheme;
    }

    public void setIdentificationScheme(ClassificationScheme scheme)
        throws JAXRException {
        if (scheme == null) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.identificationscheme.null"));
        } else if ((schemeRef == null) || (!(scheme.getKey().getId().equals(schemeRef.getId())))) {
            schemeRef = new RegistryObjectRef(lcm, scheme);
            setModified(true);
        }
    }

    public Object toBindingObject() throws JAXRException {
        try {
            org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier ebExtId = bu.rimFac.createExternalIdentifier();
            setBindingObject(ebExtId);

            return ebExtId;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType ebExtId)
        throws JAXRException {
        super.setBindingObject(ebExtId);

        org.freebxml.omar.common.BindingUtility bu = org.freebxml.omar.common.BindingUtility.getInstance();
        
        if (schemeRef != null) {
                ebExtId.setIdentificationScheme(schemeRef.getId());
        } else {
            throw new MissingAttributeException(this, getId(), "identificationScheme");
        }
        
        if (registryObjectRef != null) {
            ebExtId.setRegistryObject(registryObjectRef.getId());
        } else {
            throw new MissingParentReferenceException(
                JAXRResourceBundle.getInstance().getString("message.error.missing.ExternalIdentifier.object.id",new Object[] {getId()}));
        }
        
        
        ebExtId.setValue(value);
    }

    /**
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet getRegistryObjectRefs() {
        HashSet refs = new HashSet();

        //refs.addAll(super.getRegistryObjectRefs());
        if (schemeRef != null) {
            refs.add(schemeRef);
        }

        return refs;
    }
}
