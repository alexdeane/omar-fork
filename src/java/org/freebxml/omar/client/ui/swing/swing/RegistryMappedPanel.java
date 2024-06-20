/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/swing/RegistryMappedPanel.java,v 1.4 2006/04/10 10:59:06 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.swing;

import org.freebxml.omar.client.common.RegistryMappedModel;
import org.freebxml.omar.client.ui.swing.I18nPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.text.Document;


/**
 * Implements several methods commonly used by panels that have an
 * underlying MappedModel.
 *
 * @author Fabian Ritzmann
 */
public abstract class RegistryMappedPanel extends I18nPanel implements Observer,
    MappedPanel {
    private boolean mappingIsChanging = false;
    private final List textFields = new ArrayList();
    private String errorMessage;

    /**
     * Sets initial values. Registers the panel as an observer with the
     * underlying model.
     *
     * @param model The underlying MappedModel
     * @param updateError Prepended to exception text if model update
     * triggered an exception
     */
    protected RegistryMappedPanel(RegistryMappedModel model, String updateError) {
        this.errorMessage = updateError;
        model.addObserver(this);
    }

    /**
     * Returns a reference to this object.
     *
     * @return RegistryMappedPanel A reference to this object
     */
    public final RegistryMappedPanel getRegistryMappedPanel() {
        return this;
    }

    /**
     * Iterates through the text fields associated with this panel,
     * clears the field and sets the new text to be displayed. Displays
     * an error dialog if an exception occurs.
     *
     * For this to work it is necessary that all text fields implement
     * the TextField interface and are added with addTextField.
     *
     * @see java.util.Observer#update(Observable, Object)
     *
     * @param o ignored
     * @param arg ignored
     */
    public void update(Observable o, Object arg) {
        TextField field = null;
        Document doc = null;
        Iterator i = this.textFields.iterator();

        while (i.hasNext()) {
            field = (TextField) i.next();

            try {
                doc = field.getTextField().getDocument();
                doc.remove(0, doc.getLength());

                String text = field.getText();

                if (text != null) {
                    doc.insertString(0, text, null);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    this.errorMessage + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Sets a flag that the mapping is changing.
     *
     * @see org.freebxml.omar.client.ui.swing.swing.ChangeableType#setChangingType(boolean)
     */
    public void setMappingIsChanging(boolean changing) {
        this.mappingIsChanging = changing;
    }

    /**
     * Returns the value of a flag.
     *
     * @see org.freebxml.omar.client.ui.swing.swing.ChangeableType#isChangingType()
     */
    public boolean mappingIsChanging() {
        return this.mappingIsChanging;
    }

    /**
     * Adds the text field to an internal list. All text fields in the list
     * are updated when the underlying model reports a change.
     */
    public void addTextField(TextField field) {
        add(field.getTextField());
        this.textFields.add(field);
    }

    /**
     * Utility method that sets constraints on a layout.
     *
     * @param component Component that is affected
     * @param c Container for constraints
     * @param gbl Container for layout
     * @param gridx value to set on constraints
     * @param gridy value to set on constraints
     * @param gridwidth value to set on constrains
     * @param weightx value to set on constraints
     * @param fill value to set on constrains
     * @param anchor value to set on constraints
     */
    public static void setConstraints(JComponent component,
        GridBagConstraints c, GridBagLayout gbl, int gridx, int gridy,
        int gridwidth, double weightx, int fill, int anchor) {
        c.gridx = gridx;
        c.gridy = gridy;
        c.gridwidth = gridwidth;
        c.gridheight = 1;
        c.weightx = weightx;
        c.weighty = 0.0;
        c.fill = fill;
        c.anchor = anchor;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(component, c);
    }

    /**
     * Setter for error message prefix, to be used with updateUIText.
     *
     * @param error String that is prepended to error message if
     * setText throws an exception
     */
    public void setError(String error) {
        this.errorMessage = error;
    }
}
