/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/FederationImpl.java,v 1.6 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import javax.xml.registry.LifeCycleManager;
import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.FederationType;

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
import org.oasis.ebxml.registry.bindings.rim.ObjectRef;


/**
 * Implements a future JAXR API interface named Federation.
 * TODO: Add this as interface to JAXR 2.0
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class FederationImpl extends RegistryEntryImpl
    implements RegistryEntry {
        
    String replicationSyncLatency = "P1D";
        
    public FederationImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public FederationImpl(LifeCycleManagerImpl lcm,
        FederationType federation) throws JAXRException {
        super(lcm, federation);
        
        replicationSyncLatency = federation.getReplicationSyncLatency();
    }
    
    public void join(RegistryImpl registry) throws JAXRException {
        BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                                                      .getBusinessQueryManager());
        Concept assocType = bqm.findConceptByPath(
                "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_HasFederationMember);
        Association ass = lcm.createAssociation(registry, assocType);
        addAssociation(ass);

        //No need to call setModified(true) since RIM modified object is an Assoociation				
        
    }

    public void join(RegistryObjectRef registryRef) throws JAXRException {
        BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                                                      .getBusinessQueryManager());
        Concept assocType = bqm.findConceptByPath(
                "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_HasFederationMember);
        AssociationImpl ass = (AssociationImpl)lcm.createObject(LifeCycleManager.ASSOCIATION);
        ass.setAssociationType(assocType);
        ass.setTargetObjectRef(registryRef);
        addAssociation(ass);

        //No need to call setModified(true) since RIM modified object is an Assoociation				
        
    }
    
    public void leave(RegistryImpl registry) throws JAXRException {
	HashSet associations = new HashSet(getAssociations());
        Iterator iter = associations.iterator();

        while (iter.hasNext()) {
            Association ass = (Association) iter.next();

            if (ass.getTargetObject().equals(registry)) {
                if (ass.getAssociationType().getKey().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HasFederationMember)) {
		    removeAssociation(ass);
		}
            }
        }

        //No need to call setModified(true) since RIM modified object is an Association				        
    }
    
    public void leave(RegistryObjectRef registryRef) throws JAXRException {
	HashSet associations = new HashSet(getAssociations());
        Iterator iter = associations.iterator();

        while (iter.hasNext()) {
            AssociationImpl ass = (AssociationImpl) iter.next();

            if (ass.getTargetObjectRef().equals(registryRef)) {
                if (ass.getAssociationType().getKey().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HasFederationMember)) {
		    removeAssociation(ass);
		}
            }
        }

        //No need to call setModified(true) since RIM modified object is an Association				        
    }
    
    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.Federation federation = factory.createFederation();

            federation.setReplicationSyncLatency(replicationSyncLatency);
            setBindingObject(federation);

            return federation;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }
    
    public Set getFederationMembers() throws JAXRException {
        Set members = new HashSet();

        Iterator iter = getAssociations().iterator();

        while (iter.hasNext()) {
            AssociationImpl ass = (AssociationImpl) iter.next();

            if (ass.getAssociationTypeRef().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HasFederationMember)) {
                members.add(ass.getTargetObject());
            }
        }

        return members;
    }    

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.FederationType federation)
        throws JAXRException {
        super.setBindingObject(federation);
    }
}
