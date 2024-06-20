/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/swing/MappedModel.java,v 1.2 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.swing;


/**
 * A model that has multiple entries. Only the selected entry is displayed.
 * A key determines what entry is selected.
 *
 * @author Fabian Ritzmann
 */
public interface MappedModel extends Model {
    /**
     * Sets the key that selects the current mapping for the model.
     *
     * @param key
     */
    void setKey(String key);
}
