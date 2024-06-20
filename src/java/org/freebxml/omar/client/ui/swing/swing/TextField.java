/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/swing/TextField.java,v 1.2 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.swing;

import javax.swing.JTextField;

import javax.xml.registry.JAXRException;


/**
 * @author Fabian Ritzmann
 */
public interface TextField {
    /**
     * Should return the currently mapped text in an underlying MappedModel.
     * May return null.
     *
     * @return String The value of the text field in the underlying mapped
     * model. May be null.
     * @throws JAXRException Thrown if the underlying value could not be
     * retrieved
     */
    String getText() throws JAXRException;

    /**
     * Should return the underlying JTextField
     *
     * @return JTextField The underlying text field
     */
    JTextField getTextField();
}
