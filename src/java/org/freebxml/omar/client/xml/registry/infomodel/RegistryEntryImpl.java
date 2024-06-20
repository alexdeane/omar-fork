/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/RegistryEntryImpl.java,v 1.12 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryEntry;
import javax.xml.registry.infomodel.Versionable;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;


/**
 * Implements JAXR API interface named RegistryEntry.
 * The RegistryEntry interface will likely be removed from JAXR 2.0??
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public abstract class RegistryEntryImpl extends RegistryObjectImpl
    implements RegistryEntry, Versionable {
    private int stability = RegistryEntry.STABILITY_DYNAMIC;
    private Date expiration = null;

    public RegistryEntryImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public RegistryEntryImpl(LifeCycleManagerImpl lcm,
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectType ebObject)
        throws JAXRException {
        super(lcm, ebObject);

        /*
        Calendar cal = ebObject.getExpiration();

        if (cal != null) {
            expiration = new Date(cal.getTimeInMillis());
        }
         */
    }

    public int getStability() throws JAXRException {
        return stability;
    }

    public void setStability(int stability) throws JAXRException {
        this.stability = stability;
        setModified(true);
    }

    public Date getExpiration() throws JAXRException {
        return expiration;
    }

    public void setExpiration(Date par1) throws JAXRException {
        expiration = par1;
        setModified(true);
    }

    public int getMajorVersion() throws JAXRException {
        return 1;
    }

    public void setMajorVersion(int par1) throws JAXRException {
    }

    public int getMinorVersion() throws JAXRException {
        return 1;
    }

    public void setMinorVersion(int par1) throws JAXRException {
    }

    public String getUserVersion() throws JAXRException {
        //userInfo is obsolete but can be mapped to RegistryObject.comment
        String comment=null;
        VersionInfoType versionInfo = getVersionInfo();
        if (versionInfo != null) {
            comment = versionInfo.getComment();
        }
        return comment;
    }

    public void setUserVersion(String comment) throws JAXRException {
        //userInfo is obsolete but can be mapped to RegistryObject.comment
        try {
            VersionInfoType versionInfo = getVersionInfo();
            if (versionInfo == null) {
                versionInfo = BindingUtility.getInstance().rimFac.createVersionInfoType();
            }

            versionInfo.setComment(comment);
            setVersionInfo(versionInfo);
        } catch (javax.xml.bind.JAXBException e) {
            throw new JAXRException(e);
        }

    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectType ebRegistryEntry)
        throws JAXRException {
        super.setBindingObject(ebRegistryEntry);

        /*
        if (expiration != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(expiration);
            ebRegistryEntry.setExpiration(calendar);
        }
        */
    }
}
