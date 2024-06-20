/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/UserImpl.java,v 1.13 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * Implements JAXR API interface named User.
 * It represents a registered user of the registry.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class UserImpl extends PersonImpl {
    
    public UserImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);
    }
    
    public UserImpl(LifeCycleManagerImpl lcm, UserType ebUser)
    throws JAXRException {
        super(lcm, ebUser);
    }
    
    
    
    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.User ebUser = factory.createUser();
            
            setBindingObject(ebUser);
            
            return ebUser;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }
    
}
