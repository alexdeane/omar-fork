/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/RegistryImpl.java,v 1.4 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryEntry;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


/**
 * Implements future JAXR API interface named Registry.
 * Add Registry to JAXR 2.0??
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class RegistryImpl extends RegistryEntryImpl
    implements RegistryEntry {
    
    private String replicationSyncLatency="P1D";
    private String catalogingLatency="P1D";
    private String specificationVersion="3.0";
    private String conformanceProfile="registryLite";
    private RegistryObjectRef operator=null;
    
    public RegistryImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public RegistryImpl(LifeCycleManagerImpl lcm,
        RegistryType registry) throws JAXRException {
        super(lcm, registry);
        
        replicationSyncLatency = registry.getReplicationSyncLatency();
        catalogingLatency = registry.getCatalogingLatency();
        specificationVersion = registry.getSpecificationVersion();
        conformanceProfile = registry.getConformanceProfile();
        operator = new RegistryObjectRef(lcm, registry.getOperator());
    }

    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.Registry registry = factory.createRegistry();

            setBindingObject(registry);

            return registry;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.RegistryType registry)
        throws JAXRException {
        super.setBindingObject(registry);
        
        registry.setReplicationSyncLatency(replicationSyncLatency);
        registry.setCatalogingLatency(catalogingLatency);
        registry.setSpecificationVersion(specificationVersion);
        registry.setConformanceProfile(conformanceProfile);
        registry.setOperator(operator.getId());
    }

    public String getReplicationSyncLatency() {
        return replicationSyncLatency;
    }

    public void setReplicationSyncLatency(String replicationSyncLatency) {
        this.replicationSyncLatency = replicationSyncLatency;
    }

    public String getCatalogingLatency() {
        return catalogingLatency;
    }

    public void setCatalogingLatency(String catalogingSyncLatency) {
        this.catalogingLatency = catalogingSyncLatency;
    }

    public String getSpecificationVersion() {
        return specificationVersion;
    }

    public void setSpecificationVersion(String specificationVersion) {
        this.specificationVersion = specificationVersion;
    }

    public String getConformanceProfile() {
        return conformanceProfile;
    }

    public void setConformanceProfile(String conformanceProfile) {
        this.conformanceProfile = conformanceProfile;
    }

    public RegistryObjectRef getOperator() {
        return operator;
    }

    public void setOperator(RegistryObjectRef operator) {
        this.operator = operator;
    }
}
