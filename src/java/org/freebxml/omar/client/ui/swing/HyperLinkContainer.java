/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/HyperLinkContainer.java,v 1.2 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.net.MalformedURLException;


/**
 *
 * @author  najmi
 */
public interface HyperLinkContainer {
    /**
     * Get the URL provided by this object.
     */
    public String getURL();

    /**
     * Get the URL provided by this object.
     */
    public void setURL(String url) throws MalformedURLException;
}
