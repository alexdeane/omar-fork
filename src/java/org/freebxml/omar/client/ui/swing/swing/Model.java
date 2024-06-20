/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/swing/Model.java,v 1.2 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.swing;

import javax.xml.registry.JAXRException;


/**
 * A model that can be validated.
 *
 * @author Fabian Ritzmann
 */
public interface Model {
    /**
     * Validates the model.
     *
     * The method may alter the state of the object, e.g. remove empty
     * elements.
     *
     * @throws JAXRException Thrown if the model is invalid or
     * validation encountered an internal error
     */
    void validate() throws JAXRException;
}
