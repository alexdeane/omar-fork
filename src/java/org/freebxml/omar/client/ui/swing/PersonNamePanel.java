/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/PersonNamePanel.java,v 1.5 2004/03/16 14:24:15 tonygraham Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/PersonNamePanel.java,v 1.5 2004/03/16 14:24:15 tonygraham Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.beans.PropertyChangeEvent;

import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.PersonName;


/**
 * Panel for PersonName
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class PersonNamePanel extends JBPanel {
    JTextField firstNameText = null;
    JTextField middleNameText = null;
    JTextField lastNameText = null;
	JLabel firstNameLabel = null;
	JLabel middleNameLabel = null;
	JLabel lastNameLabel = null;

    /**
     * Used for displaying objects
     */
    public PersonNamePanel() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        firstNameLabel = new JLabel(resourceBundle.getString("label.firstName"),
									SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(firstNameLabel, c);
        add(firstNameLabel);

        firstNameText = new JTextField();
        firstNameText.setEditable(editable);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.75;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(firstNameText, c);
        add(firstNameText);

        middleNameLabel = new JLabel(resourceBundle.getString("label.middleName"),
									 SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(middleNameLabel, c);
        add(middleNameLabel);

        middleNameText = new JTextField();
        middleNameText.setEditable(editable);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.25;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(middleNameText, c);
        add(middleNameText);

		lastNameLabel = new JLabel(resourceBundle.getString("label.lastName"),
								   SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(lastNameLabel, c);
        add(lastNameLabel);

        lastNameText = new JTextField();
        lastNameText.setEditable(editable);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(lastNameText, c);
        add(lastNameText);
    }

    public PersonName getPersonName() throws JAXRException {
        PersonName personName = null;

        if (model != null) {
            personName = (PersonName) getModel();
        }

        return personName;
    }

    public void setPersonName(PersonName personName) throws JAXRException {
        setModel(personName);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, PersonName.class);

        super.setModel(obj);

        PersonName personName = (PersonName) obj;

        try {
            if (personName != null) {
                firstNameText.setText(personName.getFirstName());
                middleNameText.setText(personName.getMiddleName());
                lastNameText.setText(personName.getLastName());
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            PersonName personName = (PersonName) model;

            personName.setFirstName(firstNameText.getText());
            personName.setMiddleName(middleNameText.getText());
            personName.setLastName(lastNameText.getText());

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();
    }

    public void clear() throws JAXRException {
        super.clear();
        firstNameText.setText("");
        middleNameText.setText("");
        lastNameText.setText("");
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        firstNameText.setEditable(editable);
        middleNameText.setEditable(editable);
        lastNameText.setEditable(editable);
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
		super.processLocaleChange(newLocale);

		updateUIText();
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
	protected void updateUIText() {
		super.updateUIText();

        firstNameLabel.setText(resourceBundle.getString("label.firstName"));
        middleNameLabel.setText(resourceBundle.getString("label.middleName"));
		lastNameLabel.setText(resourceBundle.getString("label.lastName"));
	}
}
