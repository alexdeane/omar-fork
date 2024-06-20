/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/SlotImpl.java,v 1.15 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.JAXBException;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Slot;
import org.oasis.ebxml.registry.bindings.rim.Value;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;


/**
 * Implements JAXR API interface named Slot.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class SlotImpl implements Slot {
    private String name = null;
    private String slotType = null;
    private String value = null;
    private ArrayList values = new ArrayList();
    private ExtensibleObjectImpl parent = null;
    protected LifeCycleManagerImpl lcm = null;

    public SlotImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        this.lcm = lcm;
    }

    SlotImpl(LifeCycleManagerImpl lcm, SlotType1 ebSlot)
        throws JAXRException {
        this.lcm = lcm;

        name = ebSlot.getName();
        slotType = ebSlot.getSlotType();

        ValueListType valList = ebSlot.getValueList();

        if (valList != null) {
            Iterator valListIt = valList.getValue().iterator();
            while (valListIt.hasNext()) {
                values.add(((Value)valListIt.next()).getValue());
            }
        }
    }

    void setParent(ExtensibleObjectImpl _parent) throws InvalidRequestException {
        if ((_parent != null) && (parent != null) && (_parent != parent)) {
            throw new InvalidRequestException(
                JAXRResourceBundle.getInstance().getString("message.error.add.slot.object.already.added.object",new Object[] {_parent,parent}));
        }

        parent = _parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String par1) throws JAXRException {
        name = par1;

        if (parent != null) {
            parent.setModified(true);
        }
    }

    public String getSlotType() throws JAXRException {
        return slotType;
    }

    public void setSlotType(String par1) throws JAXRException {
        slotType = par1;

        if (parent != null) {
            parent.setModified(true);
        }
    }

    public Collection getValues() throws JAXRException {
        return (ArrayList) (values.clone());
    }

    public void setValues(Collection par1) throws JAXRException {
        values.clear();
        values.addAll(par1);

        if (parent != null) {
            parent.setModified(true);
        }
    }

    void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.SlotType1 ebSlot)
        throws JAXRException {
        ebSlot.setName(name);
        ebSlot.setSlotType(slotType);

        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.ValueList ebValueList = factory.createValueList();
            Iterator valIterator = values.iterator();
            while (valIterator.hasNext()) {
                Value val = factory.createValue((String)valIterator.next());
                ebValueList.getValue().add(val);
            }
            ebSlot.setValueList(ebValueList);
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected Object clone() throws CloneNotSupportedException {
        SlotImpl _clone = null;

        try {
            new SlotImpl(lcm);
            _clone.setName(getName());
            _clone.setSlotType(getSlotType());
            _clone.setValues(getValues());
            _clone.setParent(parent);
        } catch (JAXRException e) {
            //Cannot happen.
            e.printStackTrace();
        }

        return _clone;
    }
    
    public String toString() {
        String str = super.toString();

        try {
            str += " slotName:" + getName() + " values: " + getValues();
        } catch (JAXRException e) {
        }

        return str;
    }
    
}