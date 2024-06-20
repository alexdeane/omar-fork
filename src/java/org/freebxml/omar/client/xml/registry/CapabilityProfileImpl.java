/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/CapabilityProfileImpl.java,v 1.4 2007/03/23 18:38:59 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import javax.xml.registry.CapabilityProfile;
import javax.xml.registry.JAXRException;


/**
 * Implements JAXR API interface named CapabilityProfile.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class CapabilityProfileImpl implements CapabilityProfile {
    /**
     * Gets the JAXR specification version supported by the JAXR provider.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public String getVersion() throws JAXRException {
        return "1.0";
    }

    /**
     * Gets the capability level supported by the JAXR provider.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public int getCapabilityLevel() throws JAXRException {
        return 1;
    }
}
