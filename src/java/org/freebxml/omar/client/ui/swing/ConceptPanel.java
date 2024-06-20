/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ConceptPanel.java,v 1.7 2004/05/03 21:44:19 tonygraham Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ConceptPanel.java,v 1.7 2004/05/03 21:44:19 tonygraham Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;

import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;


/**
 * Panel to edit/inspect a Service.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ConceptPanel extends RegistryObjectPanel {
	TitledBorder panelBorder = null;
	JButton showSchemeButton = null;
    JTextField schemeText = null;
    JTextField parentText = null;
    JTextField valueText = null;
    JTextField pathText = null;

    /**
     * Creates new ConceptPanel
     */
    public ConceptPanel() {
        super();

        panelBorder =
			BorderFactory.createTitledBorder(resourceBundle.getString("title.concept"));
        setBorder(panelBorder);

        //Select ClassificationScheme or Concept
        showSchemeButton =
			new JButton(resourceBundle.getString("button.selectExistingConcept"));
        showSchemeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    getSelectedSchemeOrConcept();
                }
            });
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(showSchemeButton, c);
        add(showSchemeButton);

        JLabel schemeLabel = new JLabel("ClassificationScheme:",
                SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(schemeLabel, c);
        add(schemeLabel);

        schemeText = new JTextField();
        schemeText.setEditable(false);
        c.gridx = 0;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(schemeText, c);
        add(schemeText);

        JLabel parentLabel = new JLabel("Parent id:", SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = row + 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(parentLabel, c);
        add(parentLabel);

        parentText = new JTextField();
        parentText.setEditable(false);
        c.gridx = 1;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(parentText, c);
        add(parentText);

        JLabel pathLabel = new JLabel("Path:", SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(pathLabel, c);
        add(pathLabel);

        pathText = new JTextField();
        pathText.setEditable(false);
        c.gridx = 0;
        c.gridy = row + 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(pathText, c);
        add(pathText);

        JLabel valueLabel = new JLabel("Value:", SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = row + 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(valueLabel, c);
        add(valueLabel);

        valueText = new JTextField();
        valueText.setEditable(editable);
        c.gridx = 1;
        c.gridy = row + 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(valueText, c);
        add(valueText);
    }

    private void getSelectedSchemeOrConcept() {
        ConceptsTreeDialog dialog = ConceptsTreeDialog.showSchemes(this, true,
                true);

        if (dialog.getStatus() == JBDialog.OK_STATUS) {
            ArrayList selectedObjects = dialog.getSelectedConcepts();

            int selectionCount = selectedObjects.size();

            if (selectionCount == 0) {
                RegistryBrowser.displayError(
                    "Must select a Concept in ClassificationScheme dialog");
            } else {
                if (selectionCount > 1) {
                    RegistryBrowser.displayError(
                        "Only one Concept selection is allowed in ClassificationScheme dialog. Using last selection");
                }

                Object obj = selectedObjects.get(selectionCount - 1);

                try {
                    String conceptNameStr = null;
                    ClassificationScheme scheme = null;

                    if (obj instanceof Concept) {
                        Concept concept = (Concept) obj;
                        setModel(concept);
                    }
                } catch (JAXRException e) {
                    RegistryBrowser.displayError(e);
                }
            }
        }
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, Concept.class);

        super.setModel(obj);

        Concept concept = (Concept) obj;

        try {
            if (concept != null) {
                ClassificationScheme scheme = concept.getClassificationScheme();
                RegistryObject parent = concept.getParent();
                String path = concept.getPath();
                String value = concept.getValue();

                if (scheme != null) {
                    String schemeName = RegistryBrowser.getName(scheme);

                    if ((schemeName == null) || (schemeName.length() == 0)) {
                        schemeName = scheme.getKey().getId();
                    }

                    schemeText.setText(schemeName);
                }

                if (parent != null) {
                    parentText.setText(parent.getKey().getId());
                }

                if (path != null) {
                    pathText.setText(path);
                }

                if (value != null) {
                    valueText.setText(value);
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            Concept concept = (Concept) model;
            concept.setValue(valueText.getText());

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();
    }

    public void clear() throws JAXRException {
        super.clear();
        schemeText.setText("");
        parentText.setText("");
        pathText.setText("");
        valueText.setText("");
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        valueText.setEditable(editable);
    }

	/**
	 * Processes a change in the bound property
	 * RegistryBrowser.PROPERTY_LOCALE.
	 */
	protected void processLocaleChange(Locale newLocale) {
		super.processLocaleChange(newLocale);

		updateUIText();
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
	protected void updateUIText() {
		super.updateUIText();

        panelBorder.setTitle(resourceBundle.getString("title.concept"));
        showSchemeButton.setText(resourceBundle.getString("button.selectExistingConcept"));
	}
}
