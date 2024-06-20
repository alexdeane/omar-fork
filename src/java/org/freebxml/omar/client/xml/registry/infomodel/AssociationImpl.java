/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/AssociationImpl.java,v 1.18 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Slot;

import org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;

/**
 * Implements JAXR API interface named Association.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class AssociationImpl extends RegistryObjectImpl implements Association {
    private static ClassificationScheme assocTypeScheme = null;
    private RegistryObjectRef sourceObjectRef = null;
    private RegistryObjectRef targetObjectRef = null;
    private RegistryObjectRef assocTypeRef = null;

    public AssociationImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);
    }

    public AssociationImpl(LifeCycleManagerImpl lcm, AssociationType1 ebAss)
        throws JAXRException {
        super(lcm, ebAss);

        DeclarativeQueryManagerImpl dqm = (DeclarativeQueryManagerImpl) lcm.getRegistryService()
                                                                           .getDeclarativeQueryManager();

        if (assocTypeScheme == null) {
            assocTypeScheme = (ClassificationScheme) bqm.getRegistryObject(BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType,
                    LifeCycleManager.CLASSIFICATION_SCHEME);
        }

        assocTypeRef = new RegistryObjectRef(lcm, ebAss.getAssociationType());

        sourceObjectRef = new RegistryObjectRef(lcm, ebAss.getSourceObject());
        targetObjectRef = new RegistryObjectRef(lcm, ebAss.getTargetObject());
    }

    //??JAXR 2.0
    public RegistryObjectRef getSourceObjectRef() throws JAXRException {
        return sourceObjectRef;
    }

    public RegistryObject getSourceObject() throws JAXRException {
        RegistryObject sourceObject = null;

        if (sourceObjectRef != null) {
            sourceObject = sourceObjectRef.getRegistryObject("RegistryObject");
        }

        return sourceObject;
    }

    /**
     * Internal method to set the sourceObject
     * TODO: Current design is messy. Need to figure out
     * clean way to set sourceObject only if different,
     * add association to RegistryObject only if not already added
     * and remove from previous sourceObject if any.
     */
    void setSourceObjectInternal(RegistryObject sourceObject)
        throws JAXRException {        
        setSourceObjectRef( new RegistryObjectRef(lcm, sourceObject));
    }

    public void setSourceObject(RegistryObject sourceObject)
        throws JAXRException {
        sourceObject.addAssociation(this);
    }

    //??JAXR 2.0
    public void setSourceObjectRef(RegistryObjectRef sourceObjectRef)
        throws JAXRException {
            
        //Only set if different
        if ((this.sourceObjectRef == null) || (!(this.sourceObjectRef.getId().equals(sourceObjectRef.getId())))) {
            this.sourceObjectRef = sourceObjectRef;
            setModified(true);
        }
    }

    //??JAXR 2.0
    public RegistryObjectRef getTargetObjectRef() throws JAXRException {
        return targetObjectRef;
    }

    public RegistryObject getTargetObject() throws JAXRException {
        RegistryObject targetObject = null;

        if (targetObjectRef != null) {
            targetObject = targetObjectRef.getRegistryObject("RegistryObject");
        }

        return targetObject;
    }

    public void setTargetObject(RegistryObject targetObject)
        throws JAXRException {
        targetObjectRef = new RegistryObjectRef(lcm, targetObject);
        setModified(true);
    }

    //??JAXR 2.0
    public void setTargetObjectRef(RegistryObjectRef targetObjectRef)
        throws JAXRException {
            
        //Only set if different
        if ((this.targetObjectRef == null) || (!(this.targetObjectRef.getId().equals(targetObjectRef.getId())))) {
            this.targetObjectRef = targetObjectRef;
            setModified(true);
        }
    }

    public Concept getAssociationType() throws JAXRException {
        Concept assocType = null;

        if (assocTypeRef != null) {
            assocType = (Concept)assocTypeRef.getRegistryObject("ClassificationNode");
        }
        return assocType;
    }
    
    //??JAXR 2.0
    public RegistryObjectRef getAssociationTypeRef() throws JAXRException {
        return assocTypeRef;
    }    

    public void setAssociationType(Concept par1) throws JAXRException {
        if (par1 == null) {
            assocTypeRef = null;
        }
        else {
            assocTypeRef = new RegistryObjectRef(lcm, par1);
        }
        setModified(true);
    }

    //??JAXR 2.0
    public void setAssociationTypeRef(RegistryObjectRef assocTypeRef)
        throws JAXRException {
            
        //Only set if different
        if ((this.assocTypeRef == null) || (!(this.assocTypeRef.getId().equals(assocTypeRef.getId())))) {
            this.assocTypeRef = assocTypeRef;
            setModified(true);
        }
    }

    public boolean isExtramural() throws JAXRException {
        // To check if Association is Extramural requires us to determine
        // owners of the three involved objects: source, target, Association.

        RegistryObjectImpl srcObject = ((RegistryObjectImpl) getSourceObject());
        RegistryObjectImpl targetObject = ((RegistryObjectImpl) getTargetObject());

        if (srcObject == null) {
            return false;
        }

        if (targetObject == null) {
            return false;
        }

        UserImpl assOwner = (UserImpl) getOwner();

        if (assOwner == null) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.no.owner.association"));
        }

        UserImpl sourceOwner = (UserImpl) (srcObject.getOwner());
        UserImpl targetOwner = (UserImpl) (targetObject.getOwner());

        if (assOwner.getId().equals(targetOwner.getId()) &&
                assOwner.getId().equals(sourceOwner.getId())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Determines if the Association has been confirmed by the owner of the sourceObject.
     *
     * Note that ebXML Registry 3.0 no longer support Association confirmation after
     * realizing that it is a bogus idea and instead using 3.0 access control mechanisms
     * to control who is allowed to create an Association with one's objects
     * and under what constrainst. The custom access control policies can do much more than
     * what association confirmation allowed us to do in the past.
     * 
     * However, freebXML Registry supports association confirmation in order to pass JAXR TCK
     * as a implementation specific feature. The implementation uses Slots to store the
     * confirmation status.
     *
     */
    public boolean isConfirmedBySourceOwner() throws JAXRException {
        boolean confirmedBySourceOwner = false;
        
        if (!isExtramural()) {
            confirmedBySourceOwner = true;
        } else {        
            Collection slots = this.getSlots();
            Iterator iter = slots.iterator();
            while (iter.hasNext()) {
                Slot slot = (Slot)iter.next();
                String slotName = slot.getName();
                if (slotName.equals(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER)) {
                    Collection values = slot.getValues();
                    if (values.size() > 0) {
                        String value = (String)((values.toArray())[0]);
                        confirmedBySourceOwner = Boolean.valueOf(value).booleanValue();
                    }
                    break;
                }
            }
        }
        
        return confirmedBySourceOwner;
    }

    /**
     * Determines if the Association has been confirmed by the owner of the targetObject.
     *
     * Note that ebXML Registry 3.0 no longer support Association confirmation after
     * realizing that it is a bogus idea and instead using 3.0 access control mechanisms
     * to control who is allowed to create an Association with one's objects
     * and under what constrainst. The custom access control policies can do much more than
     * what association confirmation allowed us to do in the past.
     * 
     * However, freebXML Registry supports association confirmation in order to pass JAXR TCK
     * as a implementation specific feature. The implementation uses Slots to store the
     * confirmation status.
     *
     */
    public boolean isConfirmedByTargetOwner() throws JAXRException {
        boolean confirmedByTargetOwner = false;
        
        if (!isExtramural()) {
            confirmedByTargetOwner = true;
        } else {        
            Collection slots = this.getSlots();
            Iterator iter = slots.iterator();
            while (iter.hasNext()) {
                Slot slot = (Slot)iter.next();
                String slotName = slot.getName();
                if (slotName.equals(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER)) {
                    Collection values = slot.getValues();
                    if (values.size() > 0) {
                        String value = (String)((values.toArray())[0]);
                        confirmedByTargetOwner = Boolean.valueOf(value).booleanValue();
                    }
                    break;
                }
            }
        }
        
        return confirmedByTargetOwner;
    }

    public boolean isConfirmed() throws JAXRException {
        boolean confirmed = false;

        if (isConfirmedBySourceOwner() && isConfirmedByTargetOwner()) {
            confirmed = true;
        }

        return confirmed;
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            org.oasis.ebxml.registry.bindings.rim.ObjectFactory factory = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
            org.oasis.ebxml.registry.bindings.rim.Association ebOrg = factory.createAssociation();

            setBindingObject(ebOrg);

            return ebOrg;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.AssociationType1 ebAssociation)
        throws JAXRException {
        super.setBindingObject(ebAssociation);

        if (assocTypeRef != null) {
            ebAssociation.setAssociationType(assocTypeRef.getId());
        }

        try {
            org.oasis.ebxml.registry.bindings.rim.ObjectFactory factory = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
            org.oasis.ebxml.registry.bindings.rim.ObjectRef ebSourceObjectRef = factory.createObjectRef();
            ebSourceObjectRef.setId(sourceObjectRef.getId());
            ebAssociation.setSourceObject(sourceObjectRef.getId());
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }

        try {
            org.oasis.ebxml.registry.bindings.rim.ObjectFactory factory = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
            org.oasis.ebxml.registry.bindings.rim.ObjectRef ebTargetObjectRef = factory.createObjectRef();
            ebTargetObjectRef.setId(targetObjectRef.getId());
            ebAssociation.setTargetObject(targetObjectRef.getId());
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    /**
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet getRegistryObjectRefs() {
        HashSet refs = new HashSet();

        //refs.addAll(super.getRegistryObjectRefs());
        if (sourceObjectRef != null) {
            refs.add(sourceObjectRef);
        }

        if (targetObjectRef != null) {
            refs.add(targetObjectRef);
        }

        if (assocTypeRef != null) {
            refs.add(assocTypeRef);
        }

        return refs;
    }
    
    public String toString() {
        String str = super.toString();

        str = "sourceObject:" + sourceObjectRef + " targetObject:" + targetObjectRef + " " + str;

        return str;
    }
}
