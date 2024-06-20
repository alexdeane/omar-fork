/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/swing/RegistryMappedModel.java,v 1.2 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.swing;

import java.util.Observable;

import javax.xml.registry.JAXRException;


/**
 * Implements a few commonly used methods in a MappedModel. Uses the strategy
 * design pattern.
 *
 * @author Fabian Ritzmann
 */
public abstract class RegistryMappedModel extends Observable
    implements MappedModel {
    protected String key = null;

    /**
     * Initializes the object with an initial mapping.
     *
     * @param k Key that selects the initial model mapping.
     */
    public RegistryMappedModel(String k) {
        this.key = k;
    }

    /**
     * Sets a new mapping. All observers registered with this model are
     * notified if the mapping changed.
     *
     * @see org.freebxml.omar.client.ui.swing.swing.MappedModel#setKey(String)
     *
     * @param k Key to the new mapping
     */
    public void setKey(String k) {
        if (!k.equals(this.key)) {
            this.key = k;
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Derived classes need to implement the interface method.
     *
     * @see org.freebxml.omar.client.ui.swing.swing.Model#validate()
     */
    public abstract void validate() throws JAXRException;
}
