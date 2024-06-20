/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/swing/RegistryComboBoxListener.java,v 1.3 2006/04/10 10:59:06 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.swing;

import org.freebxml.omar.client.common.MappedModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;


/**
 * Listens on a ComboBox and sets a new key in the MappedModel if a
 * value was selected.
 *
 * @author Fabian Ritzmann
 */
public class RegistryComboBoxListener implements ActionListener {
    private final MappedModel model;
    private final MappedPanel panel;

    /**
     * Initializes the object.
     *
     * @param m A MappedModel on which this controller operates
     * @param p A MappedPanel that displays the MappedModel
     */
    public RegistryComboBoxListener(MappedModel m, MappedPanel p) {
        this.model = m;
        this.panel = p;
    }

    /**
     * Retrieves the selected value from the ComboBox, tells the panel that
     * the mapping is changing and sets the new key in the model.
     *
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        String text = (String) cb.getSelectedItem();
        this.panel.setMappingIsChanging(true);
        this.model.setKey(text);
        this.panel.setMappingIsChanging(false);
    }
}
