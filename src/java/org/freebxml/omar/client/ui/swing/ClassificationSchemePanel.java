/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ClassificationSchemePanel.java,v 1.7 2005/03/14 23:33:50 tonygraham Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ClassificationSchemePanel.java,v 1.7 2005/03/14 23:33:50 tonygraham Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;


/**
 * Panel to edit/inspect a Service.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ClassificationSchemePanel extends RegistryObjectPanel {
    JCheckBox externalCheckBox = null;
    String[] valueTypes = { "Embedded Path", "Non-Unique", "Unique" };
    JComboBox valueTypeCombo = null;

    /**
     * Creates new ServicePanel
     */
    public ClassificationSchemePanel() {
        setBorder(BorderFactory.createTitledBorder("ClassificationScheme"));

        externalCheckBox = new JCheckBox("External ClassificationScheme");
        externalCheckBox.setSelected(false);
        externalCheckBox.setEnabled(false);
        c.gridx = 0;
        c.gridy = row + 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(externalCheckBox, c);
        add(externalCheckBox);

        JLabel valueTypeLabel = new JLabel("Value Type:", SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(valueTypeLabel, c);
        add(valueTypeLabel);

        valueTypeCombo = new JComboBox(valueTypes);
        valueTypeCombo.setEditable(false);
        c.gridx = 0;
        c.gridy = row + 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(valueTypeCombo, c);
        add(valueTypeCombo);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, ClassificationScheme.class);

        super.setModel(obj);

        ClassificationScheme classificationScheme = (ClassificationScheme) obj;

        try {
            if (classificationScheme != null) {
                externalCheckBox.setSelected(classificationScheme.isExternal());
            }
            
            int valueType = classificationScheme.getValueType();
            
            if (valueType == ClassificationScheme.VALUE_TYPE_EMBEDDED_PATH) {
                valueTypeCombo.setSelectedIndex(0);
            } else if (valueType == ClassificationScheme.VALUE_TYPE_NON_UNIQUE) {
                valueTypeCombo.setSelectedIndex(1);
            } else if (valueType == ClassificationScheme.VALUE_TYPE_UNIQUE) {
                valueTypeCombo.setSelectedIndex(2);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();
        
        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        ClassificationScheme classificationScheme = (ClassificationScheme) model;
    }

    public void clear() throws JAXRException {
        super.clear();
        externalCheckBox.setSelected(false);
    }
}
