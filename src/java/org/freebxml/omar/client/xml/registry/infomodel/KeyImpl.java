/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/KeyImpl.java,v 1.7 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Key;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.HashCodeUtil;


/**
 * Implements JAXR API interface named Key.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class KeyImpl implements Key, Comparable {
    private String id = org.freebxml.omar.common.Utility.getInstance().createId();
    private boolean registryAssignedId = true;

    private KeyImpl() {
    }

    public KeyImpl(LifeCycleManagerImpl lcm) {
    }

    public String getId() throws JAXRException {
        return id;
    }

    public void setId(String par1) throws JAXRException {
        id = par1;
        registryAssignedId = false;
    }

    public String toString() {
        if (id == null) {
            return super.toString();
        }

        return id;
    }
    
    /** Returns true if the object specified is a RegistryObjectImpl
      * with the same id.
      *
      * @param o
      *                The object to compare to.
      * @return
      *                <code>true</code> if the objects are equal.
      * @todo
      *                Do we need to ensure the object is the same type as this
      *                instance? For example, this instance could be a ServiceImpl
      *                and the object could be an ExternalLinkImpl. Could these have
      *                the same id?
      */
    public boolean equals(Object o) {
        if (compareTo(o) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash( result, id);
        return result;
    }    
    
    /**
     * Compares two registries objects.
     * Consider adding Comparable to RegistryObject in JAXR 2.0??
     *
     * @return 0 (equal) is the id of the objects matches this objects id.
     * Otherwise return -1 (this object is less than arg o).
     */
    public int compareTo(Object o) {
        int result = -1;

        if (o instanceof Key) {
            try {
                Key key = (Key)o;
                String myId = getId();                
                String otherId = key.getId();
                result = myId.compareTo(otherId);
            } catch (JAXRException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    boolean isRegistryAssignedId() {
        return registryAssignedId;
    }
}
