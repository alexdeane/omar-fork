/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/common/MappedModel.java,v 1.1 2006/04/10 10:59:07 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.common;


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
