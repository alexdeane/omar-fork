/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/IdentifiableImpl.java,v 1.8 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.io.StringWriter;
import java.lang.reflect.UndeclaredThrowableException;

import javax.xml.bind.Marshaller;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Key;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.RegistryServiceImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.HashCodeUtil;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;


/**
 * Base class for all classes that have id and home attributes.
 * TODO: Add to JAXR 2.0 as a new interface
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public abstract class IdentifiableImpl extends ExtensibleObjectImpl
    implements Comparable {
    private static final Log log = LogFactory.getLog(IdentifiableImpl.class);

    protected Key key = null;
    protected String home = null;
    
    IdentifiableImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);

        //Assign default key
        key = lcm.createKey();
    }
    
    IdentifiableImpl(LifeCycleManagerImpl lcm, IdentifiableType ebObject)
        throws JAXRException {
        // Pass ebObject to superclass so slot-s can be initialized
        super(lcm, ebObject);

        key = new KeyImpl(lcm);
        key.setId(ebObject.getId());
        home = ebObject.getHome();
    }

    public Key getKey() throws JAXRException {
        return key;
    }

    /**
     * Add to JAXR 2.0??
     */
    public String getId() throws JAXRException {
        return key.getId();
    }

    public void setKey(Key key) throws JAXRException {
        this.key = key;
        setModified(true);
    }

    /**
     * Add to JAXR 2.0??
     */
    public String getHome() throws JAXRException {
        return home;
    }

    /**
     * Add to JAXR 2.0??
     */
    public void setHome(String home) throws JAXRException {
        this.home = home;
    }    
    
    public String toXML() throws JAXRException {
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = BindingUtility.getInstance().getJAXBContext()
                                                  .createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(toBindingObject(), sw);

            return sw.toString();
        } catch (javax.xml.bind.JAXBException e) {
            throw new JAXRException(e);
        }
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    abstract public Object toBindingObject() throws JAXRException;

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.IdentifiableType ebObject)
        throws JAXRException {
        // Pass ebObject to superclass so slot-s can be initialized
        super.setBindingObject(ebObject);

        ebObject.setId(key.getId());
        
        if (home != null) {
            ebObject.setHome(home);
        }
    }

    public String toString() {
        String str = super.toString();

        try {
            str = getId() + "," + str;
        } catch (JAXRException e) {
	    log.warn(JAXRResourceBundle.getInstance().getString("ErrorGettingId"),
		     e);
        }

        return str;
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
        try {
            if (key != null) {
                result = HashCodeUtil.hash( result, key.getId());
            }
            result = HashCodeUtil.hash( result, this.getClass());
        } catch (JAXRException e) {
            throw new UndeclaredThrowableException(e);
        }
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

        if (o instanceof IdentifiableImpl) {           
            try {
                //Need class match otherwise RegistryObjectRef and IdentifiableImpl
                //with same id will match when they should not.
                if (o.getClass() == this.getClass()) {
                    String myId = getId();
                    String otherId = ((IdentifiableImpl) o).getKey().getId();
                    result = myId.compareTo(otherId);
                }
            } catch (JAXRException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
