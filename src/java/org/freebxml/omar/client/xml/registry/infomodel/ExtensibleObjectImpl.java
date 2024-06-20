/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/ExtensibleObjectImpl.java,v 1.12 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.ExtensibleObject;
import javax.xml.registry.infomodel.Slot;

import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;


/**
 * Implements JAXR API interface named ExtensibleObject.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public abstract class ExtensibleObjectImpl implements ExtensibleObject {
    // I18n support for all infomodel classes
    protected static final JAXRResourceBundle i18nUtil = JAXRResourceBundle.getInstance();
    private TreeMap slots = new TreeMap();
    protected LifeCycleManagerImpl lcm = null;

    //State variable
    private boolean _new = false; //Object is unsaved and has never been committed to registry
    private boolean modified = false; //Object has been modified in memory and not yet committed to registry
    private boolean loaded = false; //Object has not been fully loaded from registry

    //Replace with functions to save memory later??
    protected DeclarativeQueryManagerImpl dqm = null;
    protected BusinessQueryManagerImpl bqm = null;
    
    protected BindingUtility bu = BindingUtility.getInstance();

    
    ExtensibleObjectImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        this.lcm = lcm;
        _new = true;
        
        dqm = (DeclarativeQueryManagerImpl) (lcm.getRegistryService()
                                                .getDeclarativeQueryManager());
        bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                             .getBusinessQueryManager());
        
    }

    ExtensibleObjectImpl(LifeCycleManagerImpl lcm, IdentifiableType ebObject)
        throws JAXRException {
        this(lcm);
        _new = false;

        dqm = (DeclarativeQueryManagerImpl) (lcm.getRegistryService()
                                                .getDeclarativeQueryManager());
        bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                             .getBusinessQueryManager());
        
        List ebSlots = ebObject.getSlot();
        Iterator iter = ebSlots.iterator();

        while (iter.hasNext()) {
            SlotType1 slot = (SlotType1) iter.next();
            internalAddSlot(new SlotImpl(lcm, slot));
        }
    }

    public BusinessQueryManager getBusinessQueryManager()
        throws JAXRException {
        return lcm.getRegistryService().getBusinessQueryManager();
    }

    public DeclarativeQueryManager getDeclarativeQueryManager()
        throws JAXRException {
        return lcm.getRegistryService().getDeclarativeQueryManager();
    }
    
    public LifeCycleManager getLifeCycleManager() throws JAXRException {
        return lcm;
    }    

    /**
     * Implementation private
     */
    public boolean isNew() {
        return _new;
    }

    /**
     * Implementation private
     */
    public void setNew(boolean _new) {
        this._new = _new;
    }

    /**
     * Implementation private
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Implementation private
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    boolean isLoaded() {
        return loaded;
    }

    protected void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    private void internalAddSlot(Slot slot) throws JAXRException {
        if (slot != null) {
            String slotName = slot.getName();

            if (!slots.keySet().contains(slotName)) {
                // CHECK THIS! This will set modified to true!!!
                //Check if slot already been added
                //If already added then there could be a name conflict
                //So remove before re-adding
                if (!slots.entrySet().contains(slot)) {
                    removeSlot(slot.getName());
                }

                ((SlotImpl) slot).setParent(this);
                slots.put(slotName, slot);
            } else {
                Object[] objs = { slotName };
                throw new JAXRException(i18nUtil.getString(
                        "slot.name.duplicate", objs));
            }
        }
    }

    public void addSlot(Slot slot) throws JAXRException {
        internalAddSlot(slot);
        setModified(true);
    }

    public void addSlots(Collection _slots) throws JAXRException {
        //??Issue that if an error is encountered in adding slots
        //than Slots would have been added partially. 
        //Need to compensate for that in case of exception half way thru
        Iterator iter = _slots.iterator();

        while (iter.hasNext()) {
            Object obj = (SlotImpl) iter.next();

            if (!(obj instanceof SlotImpl)) {
                throw new InvalidRequestException(i18nUtil.getString("message.error.expected.slot",new Object[] {obj}));
            }

            SlotImpl slot = (SlotImpl) obj;
            addSlot(slot);
        }
    }

    //??Add to JAXR 2.0
    public void removeSlot(Slot slot) throws JAXRException {
        String slotName = ((SlotImpl)slot).getName();
        removeSlot(slotName);
    }
    
    public void removeSlot(String slotName) throws JAXRException {
        Object removed = slots.remove(slotName);

        if (removed != null) {
            removeSlotInternal((SlotImpl) removed);
        }
    }

    private void removeSlotInternal(SlotImpl slot) throws JAXRException {
        slot.setParent(null);
        setModified(true);
    }

    public void removeSlots(Collection slotNames) throws JAXRException {
        Iterator iter = slotNames.iterator();

        while (iter.hasNext()) {
            String slotName = (String) iter.next();
            removeSlot(slotName);
        }
    }

    //??Add to JAXR 2.0
    public void removeAllSlots() throws JAXRException {
        TreeMap _slots = (TreeMap) slots.clone();
        Iterator iter = _slots.values().iterator();

        while (iter.hasNext()) {
            SlotImpl slot = (SlotImpl) iter.next();

            //Must avoid case where a name change in SLot could prevent
            //normal removeSlot to not work.
            //String slotName = slot.getName();
            //removeSlot(slotName);            
            removeSlotInternal(slot);
        }

        slots = new TreeMap();
    }

    public Slot getSlot(String slotName) throws JAXRException {
        return (Slot) slots.get(slotName);
    }

    public Collection getSlots() throws JAXRException {
        return ((TreeMap) (slots.clone())).values();
    }

    //??Add to JAXR 2.0
    public void setSlots(Collection slots) throws JAXRException {
        removeAllSlots();
        addSlots(slots);
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.IdentifiableType ebObject)
        throws JAXRException {
        Iterator iter = getSlots().iterator();

        while (iter.hasNext()) {
            SlotImpl slot = (SlotImpl) iter.next();

            try {
                org.oasis.ebxml.registry.bindings.rim.ObjectFactory factory = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
                org.oasis.ebxml.registry.bindings.rim.Slot ebSlot = factory.createSlot();
                slot.setBindingObject(ebSlot);
                ebObject.getSlot().add(ebSlot);
            } catch (JAXBException e) {
                throw new JAXRException();
            }
        }
    }

    public HashSet getRIMComposedObjects()
        throws JAXRException {
        HashSet composedObjects = new HashSet();
        composedObjects.addAll(slots.values());
        
        return composedObjects;
    }
    
    public HashSet getComposedObjects() 
        throws JAXRException {
        HashSet composedObjects = new HashSet();
        composedObjects.addAll(slots.values());
        
        return composedObjects;
    }
}
