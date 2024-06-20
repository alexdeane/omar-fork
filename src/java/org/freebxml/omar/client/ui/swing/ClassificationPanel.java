/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ClassificationPanel.java,v 1.7 2005/05/03 17:43:49 dougb62 Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ClassificationPanel.java,v 1.7 2005/05/03 17:43:49 dougb62 Exp $
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
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;


/**
 * Panel to edit/inspect a Service.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ClassificationPanel extends RegistryObjectPanel {
    JTextField schemeText = null;
    JTextField conceptText = null;
    JTextField valueText = null;
	TitledBorder classificationPanelBorder = null;
	JLabel schemeLabel = null;
	JButton showSchemeButton = null;
	JLabel conceptLabel = null;
	JLabel valueLabel = null;

    /**
     * Creates new ServicePanel
     */
    public ClassificationPanel() {
        super();

        classificationPanelBorder =
			BorderFactory.createTitledBorder(resourceBundle.getString("title.classificationDetails"));
		setBorder(classificationPanelBorder);

        //ClassificationScheme
        schemeLabel = new JLabel(resourceBundle.getString("label.classificationScheme"),
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

        //Select ClassificationScheme or Concept
        showSchemeButton =
			new JButton(resourceBundle.getString("button.selectClassificationScheme"));
        showSchemeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    getSelectedSchemeOrConcept();
                }
            });
        c.gridx = 1;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(showSchemeButton, c);
        add(showSchemeButton);

        //Concept
        conceptLabel = new JLabel(resourceBundle.getString("label.concept"),
								  SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(conceptLabel, c);
        add(conceptLabel);

        conceptText = new JTextField();
        conceptText.setEditable(false);
        c.gridx = 0;
        c.gridy = row + 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(conceptText, c);
        add(conceptText);

        //Value
        valueLabel = new JLabel(resourceBundle.getString("label.value"),
								SwingConstants.TRAILING);
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
        valueText.setEditable(false);
        c.gridx = 1;
        c.gridy = row + 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(valueText, c);
        add(valueText);

        //add listener for 'locale' bound property
		RegistryBrowser.getInstance().
			addPropertyChangeListener(RegistryBrowser.PROPERTY_LOCALE,
									  this);
    }

    private void getSelectedSchemeOrConcept() {
        ConceptsTreeDialog dialog = ConceptsTreeDialog.showSchemes(this, true,
                true);

        if (dialog.getStatus() == JBDialog.OK_STATUS) {
            ArrayList selectedObjects = dialog.getSelectedObjects();

            int selectionCount = selectedObjects.size();

            if (selectionCount == 0) {
                RegistryBrowser.displayError(
                    "Must select a ClassificationScheme or Concept in ClassificationScheme dialog");
            } else {
                if (selectionCount > 1) {
                    RegistryBrowser.displayError(
                        "Only one selection allowed in ClassificationScheme dialog. Using last selection");
                }

                Classification classification = (Classification) model;
                Object obj = selectedObjects.get(selectionCount - 1);

                try {
                    ClassificationScheme scheme = null;

                    if (obj instanceof Concept) {
                        Concept concept = (Concept) obj;
                        classification.setConcept(concept);

                        valueText.setEditable(false);

                        conceptText.setText(concept.getKey().getId());
                        valueText.setText(concept.getValue());

                        scheme = concept.getClassificationScheme();
                    } else if (obj instanceof ClassificationScheme) {
                        scheme = (ClassificationScheme) obj;
                        classification.setClassificationScheme(scheme);

                        valueText.setEditable(true);
                    }

                    String schemeName = RegistryBrowser.getName(scheme);

                    if ((schemeName != null) && (schemeName.length() > 0)) {
                        schemeText.setText(schemeName);
                    } else {
                        schemeText.setText(scheme.getKey().getId());
                    }
                } catch (JAXRException e) {
                    RegistryBrowser.displayError(e);
                }
            }
        }
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, Classification.class);

        super.setModel(obj);

        Classification classification = (Classification) obj;

        try {
            if (classification != null) {
                ClassificationScheme scheme = classification.getClassificationScheme();
                Concept concept = classification.getConcept();

                if (concept != null) {
                    if (scheme == null) {
                        scheme = concept.getClassificationScheme();
                    }

                    conceptText.setText(concept.getKey().getId());
                    valueText.setText(concept.getValue());
                } else {
                    conceptText.setText("");
                    valueText.setText(classification.getValue());
                }

                if (scheme != null) {
                    schemeText.setText(RegistryBrowser.getName(scheme));
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            Classification classification = (Classification) model;

            String value = valueText.getText();

            if ((value != null) && (value.length() > 0)) {
                classification.setValue(value);
            }

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        Classification classification = (Classification) model;
        String value = valueText.getText();

        if ((value == null) || (value.length() == 0)) {
            Concept concept = classification.getConcept();

            if (concept == null) {
                throw new JAXRException(
                    "Error. An Classification must have a ClassificationScheme or Concept specified. If ClassificationScheme is specified instead of Concept then a Value must also be provided.");
            }
        }
    }

    public void clear() throws JAXRException {
        super.clear();
        schemeText.setText("");
        conceptText.setText("");
        valueText.setText("");
    }

    /**
     * Listens to property changes in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_LOCALE)) {
			processLocaleChange((Locale) ev.getNewValue());
		}
	}

	/**
	 * Processes a change in the bound property
	 * RegistryBrowser.PROPERTY_LOCALE.
	 */
	protected void processLocaleChange(Locale newLocale) {
		resourceBundle = JavaUIResourceBundle.getInstance();

		setLocale(newLocale);

		updateUIText();
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
	protected void updateUIText() {
        classificationPanelBorder.
			setTitle(resourceBundle.getString("title.classificationDetails"));

        schemeLabel.setText(resourceBundle.getString("label.classificationScheme"));

        showSchemeButton.setText(resourceBundle.getString("button.selectClassificationScheme"));

        conceptLabel.setText(resourceBundle.getString("label.concept"));
        valueLabel.setText(resourceBundle.getString("label.value"));
	}
}
