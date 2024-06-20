/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/EmailAddressPanel.java,v 1.8 2004/11/03 17:38:22 tonygraham Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/EmailAddressPanel.java,v 1.8 2004/11/03 17:38:22 tonygraham Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.beans.PropertyChangeEvent;

import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.EmailAddress;


/**
 * Panel for EmailAddress
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class EmailAddressPanel extends JBPanel {
    JTextField addressText = null;
    String[] emailTypes = { resourceBundle.getString("type.officeEmail"),
			    resourceBundle.getString("type.homeEmail") };
    JComboBox typeCombo = null;
	JLabel addressLabel = null;
	JLabel typeLabel = null;

    /**
     * Used for displaying objects
     */
    public EmailAddressPanel() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

		addressLabel = new JLabel(resourceBundle.getString("label.emailAddress"),
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
        gbl.setConstraints(addressLabel, c);
        add(addressLabel);

        addressText = new JTextField();
        addressText.setEditable(editable);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(addressText, c);
        add(addressText);

		typeLabel = new JLabel(resourceBundle.getString("label.addressType"),
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
        gbl.setConstraints(typeLabel, c);
        add(typeLabel);

        typeCombo = new JComboBox(emailTypes);
        typeCombo.setEditable(true);
        typeCombo.setEnabled(editable);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(typeCombo, c);
        add(typeCombo);
    }

    public EmailAddress getEmailAddress() throws JAXRException {
        EmailAddress email = null;

        if (model != null) {
            email = (EmailAddress) getModel();
        }

        return email;
    }

    public void setEmailAddress(EmailAddress email) throws JAXRException {
        setModel(email);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, EmailAddress.class);

        super.setModel(obj);

        EmailAddress email = (EmailAddress) obj;

        try {
            if (email != null) {
                addressText.setText(email.getAddress());
                typeCombo.setSelectedItem((String) email.getType());
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public void setEmailAddress(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, EmailAddress.class);

        super.setModel(obj);

        EmailAddress email = (EmailAddress) obj;

        try {
            if (email != null) {
                addressText.setText(email.getAddress());
                typeCombo.setSelectedItem((String) email.getType());
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            EmailAddress emailAddress = (EmailAddress) model;

            emailAddress.setAddress(addressText.getText());

            emailAddress.setType((String) typeCombo.getSelectedItem());

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        EmailAddress emailAddress = (EmailAddress) model;
    }

    public void clear() throws JAXRException {
        super.clear();
        addressText.setText("");
        typeCombo.setSelectedIndex(0);
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        addressText.setEditable(editable);
        typeCombo.setEnabled(editable);
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

		addressLabel.setText(resourceBundle.getString("label.emailAddress"));
		typeLabel.setText(resourceBundle.getString("label.addressType"));
		emailTypes = new String[] { resourceBundle.getString("type.officeEmail"),
					    resourceBundle.getString("type.homeEmail") };
	}
}
