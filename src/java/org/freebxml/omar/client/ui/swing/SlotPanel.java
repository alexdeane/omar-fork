/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/SlotPanel.java,v 1.5 2004/03/16 14:24:16 tonygraham Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/SlotPanel.java,v 1.5 2004/03/16 14:24:16 tonygraham Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Slot;


/**
 * Panel to edit/inspect a Service.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class SlotPanel extends JBPanel {
    JTextField nameText = null;
    JTextField typeText = null;
    JBList valuesList = null;
    protected GridBagConstraints c = null;
    protected GridBagLayout gbl = null;

    /**
     * Creates new SlotPanel
     */
    public SlotPanel() {
        setBorder(BorderFactory.createTitledBorder("Slot Details"));

        gbl = new GridBagLayout();
        c = new GridBagConstraints();
        setLayout(gbl);

        //The name Text
        JLabel nameLabel = new JLabel("Name:", SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(nameLabel, c);
        add(nameLabel);

        nameText = new JTextField();
        nameText.setEditable(editable);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(nameText, c);
        add(nameText);

        JLabel typeLabel = new JLabel("Type:", SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(typeLabel, c);
        add(typeLabel);

        typeText = new JTextField();
        typeText.setEditable(editable);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(typeText, c);
        add(typeText);

        JLabel idLabel = new JLabel("Values:", SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(idLabel, c);
        add(idLabel);

        valuesList = new JBList("java.lang.StringBuffer");
        valuesList.setEditable(editable);
        valuesList.setModel(new JBListModel());
        valuesList.setVisibleRowCount(5);

        JScrollPane valuesListScrollPane = new JScrollPane(valuesList);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(valuesListScrollPane, c);
        add(valuesListScrollPane);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, Slot.class);

        super.setModel(obj);

        Slot slot = (Slot) obj;

        try {
            if (slot != null) {
                String slotName = slot.getName();

                if (slotName != null) {
                    nameText.setText(slotName);
                }

                String slotType = slot.getSlotType();

                if (slotType != null) {
                    typeText.setText(slotType);
                }

                Collection values = slot.getValues();

                if (values != null) {
                    JBListModel valuesListModel = (JBListModel) valuesList.getModel();
                    Iterator iter = values.iterator();

                    while (iter.hasNext()) {
                        StringBuffer value = new StringBuffer((String) iter.next());
                        valuesListModel.addElement(value);
                    }
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            Slot slot = (Slot) model;

            slot.setName(nameText.getText());
            slot.setSlotType(typeText.getText());

            JBListModel valuesListModel = (JBListModel) valuesList.getModel();

            ArrayList values = new ArrayList();
            Object[] valuesArray = valuesListModel.toArray();

            for (int i = 0; i < valuesArray.length; i++) {
                values.add(valuesArray[i].toString());
                ;
            }

            slot.setValues(values);

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        Slot slot = (Slot) model;

        if (nameText.getText().length() > 128) {
            throw new JAXRException("Error. Slot.name length must be <= 128");
        }

        JBListModel valuesListModel = (JBListModel) valuesList.getModel();

        ArrayList values = new ArrayList();
        Object[] valuesArray = valuesListModel.toArray();

        for (int i = 0; i < valuesArray.length; i++) {
            String value = valuesArray[i].toString();

            if (value.length() > 128) {
                throw new JAXRException(
                    "Error. Slot.value length must be <= 128");
            }
        }
    }

    public void clear() throws JAXRException {
        super.clear();
        nameText.setText("");
        typeText.setText("");
        valuesList.setModel(new JBListModel());
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        nameText.setEditable(editable);
        typeText.setEditable(editable);
        valuesList.setEditable(editable);
    }
}
