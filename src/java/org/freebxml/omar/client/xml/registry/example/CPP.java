/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/example/CPP.java,v 1.1 2005/03/21 07:40:40 doballve Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.example;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Slot;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;

/**
 * An example calss for the JAXR infomodel class extension feature.
 * Defines a class for the CPP object type.
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class CPP extends ExtrinsicObjectImpl {
    
    public final static String SLOT_NAME_ROLE = "Role";
    
    /** Creates a new instance of CPP */
    public CPP(LifeCycleManagerImpl lcm)
    throws JAXRException {
        super(lcm);
    }

    /** Creates a new instance of CPP binding to an existing RegistryObject */
    public CPP(LifeCycleManagerImpl lcm, ExtrinsicObjectType eoType)
    throws JAXRException {
        super(lcm, eoType);
    }

    /**
     * Sets the value to property _role. Maps to a Slot defined by
     * 'SLOT_NAME_ROLE'.
     *
     * @param _role String value to be set.
     * @throws JAXRException if any exception occurs.
     */
    public void setRole(String _role) throws JAXRException {
        String name = SLOT_NAME_ROLE;
        Collection values = new ArrayList();
        values.add(_role);
        Slot slot = getSlot(name);
        if (slot == null) {
            slot = getLifeCycleManager().createSlot(name, values, "String");
        } else {
            removeSlot(name);
            slot.setValues(values);
        }
        addSlot(slot);
    }

    /** Gets the value of property _namespace. Maps to a Slot named
    * 'urn:freebxml:slot:xml-schema:namespace'.
    *
    * @return String value of property _namespace.
    * @throws JAXRException if any exception occurs.
    */
    public String getRole() throws JAXRException {
        String name = SLOT_NAME_ROLE;
        Slot slot = getSlot(name);
        if (slot == null) {
            return null;
        } else {
            return (String)slot.getValues().iterator().next();
        }
    }
}