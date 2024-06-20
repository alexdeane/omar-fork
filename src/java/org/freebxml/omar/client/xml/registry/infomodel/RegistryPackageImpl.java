/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/RegistryPackageImpl.java,v 1.15 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


/**
 * Implements JAXR API interface named RegistryPackage.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class RegistryPackageImpl extends RegistryEntryImpl
    implements RegistryPackage {
    public RegistryPackageImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public RegistryPackageImpl(LifeCycleManagerImpl lcm,
        RegistryPackageType ebPkg) throws JAXRException {
        super(lcm, ebPkg);
    }

    public void addRegistryObject(RegistryObject registryObject)
        throws JAXRException {
        BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                                                      .getBusinessQueryManager());
        Concept assocType = bqm.findConceptByPath(
                "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_HasMember);
        Association ass = lcm.createAssociation(registryObject, assocType);
        addAssociation(ass);

        //No need to call setModified(true) since RIM modified object is an Assoociation				
    }

    public void addRegistryObjects(Collection registryObjects)
        throws JAXRException {
        Iterator iter = registryObjects.iterator();

        while (iter.hasNext()) {
            RegistryObject registryObject = (RegistryObject) iter.next();
            addRegistryObject(registryObject);
        }

        //No need to call setModified(true) since RIM modified object is an Assoociation				
    }

    /**
     * Remove registryObject from this RegistryPackage by removing the
     * 'HasMember' association between this RegistryPackage and
     * registryObject.
     *
     * @param registryObject a <code>RegistryObject</code> to remove
     * @exception JAXRException if an error occurs
     */
    public void removeRegistryObject(RegistryObject registryObject)
        throws JAXRException {
	HashSet associations = new HashSet(getAssociations());
        Iterator iter = associations.iterator();

        while (iter.hasNext()) {
            Association ass = (Association) iter.next();

            if (ass.getTargetObject().equals(registryObject)) {
                if (ass.getAssociationType().getKey().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HasMember)) {
		    removeAssociation(ass);
		}
            }
        }

        //No need to call setModified(true) since RIM modified object is an Association				
    }

    public void removeRegistryObjects(Collection registryObjects)
        throws JAXRException {
        Iterator iter = registryObjects.iterator();

        while (iter.hasNext()) {
            RegistryObject registryObject = (RegistryObject) iter.next();
            removeRegistryObject(registryObject);
        }

        //No need to call setModified(true) since RIM modified object is an Association				
    }

    public Set getRegistryObjects() throws JAXRException {
        Set members = new HashSet();

        Iterator iter = getAssociations().iterator();

        while (iter.hasNext()) {
            AssociationImpl ass = (AssociationImpl) iter.next();

            if (ass.getAssociationTypeRef().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HasMember)) {
                members.add(ass.getTargetObject());
            }
        }

        return members;
    }

    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.RegistryPackage ebPkg = factory.createRegistryPackage();

            setBindingObject(ebPkg);

            return ebPkg;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.RegistryPackageType ebPkg)
        throws JAXRException {
        super.setBindingObject(ebPkg);
    }
}
