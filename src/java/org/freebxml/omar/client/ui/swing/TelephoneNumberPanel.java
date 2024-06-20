/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/TelephoneNumberPanel.java,v 1.8 2005/02/05 14:03:38 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/TelephoneNumberPanel.java,v 1.8 2005/02/05 14:03:38 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.beans.PropertyChangeEvent;

import java.util.Locale;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.TelephoneNumber;


/**
 * Panel for TelephoneNumber
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class TelephoneNumberPanel extends JBPanel {
    JTextField countryCodeText = null;
    JTextField areaCodeText = null;
    JTextField numberText = null;
    JTextField extensionText = null;
    JTextField urlText = null;
	JLabel areaCodeLabel = null;
	JLabel countryCodeLabel = null;
	JLabel extensionLabel = null;
	JLabel numberLabel = null;
	JLabel typeLabel = null;
	JLabel urlLabel = null;
    String[] phoneTypes = { resourceBundle.getString("type.officePhone"),
			    resourceBundle.getString("type.homePhone"),
			    resourceBundle.getString("type.mobilePhone"),
			    resourceBundle.getString("type.beeper"),
			    resourceBundle.getString("type.fax") };

    JComboBox typeCombo = null;
    JTextField postalSchemeText = null;

    /**
     * Used for displaying objects
     */
    public TelephoneNumberPanel() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        countryCodeLabel =
			new JLabel(resourceBundle.getString("label.countryCode"),
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
        gbl.setConstraints(countryCodeLabel, c);
        add(countryCodeLabel);

        countryCodeText = new JTextField();
        countryCodeText.setEditable(editable);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(countryCodeText, c);
        add(countryCodeText);

        areaCodeLabel =
			new JLabel(resourceBundle.getString("label.areaCode"),
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
        gbl.setConstraints(areaCodeLabel, c);
        add(areaCodeLabel);

        areaCodeText = new JTextField();
        areaCodeText.setEditable(editable);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(areaCodeText, c);
        add(areaCodeText);

        numberLabel = new JLabel(resourceBundle.getString("label.number"),
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
        gbl.setConstraints(numberLabel, c);
        add(numberLabel);

        numberText = new JTextField();
        numberText.setEditable(editable);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(numberText, c);
        add(numberText);

        extensionLabel = new JLabel(resourceBundle.getString("label.extension"),
									SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(extensionLabel, c);
        add(extensionLabel);

        extensionText = new JTextField();
        extensionText.setEditable(editable);
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(extensionText, c);
        add(extensionText);

        urlLabel = new JLabel(resourceBundle.getString("label.url"),
							  SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(urlLabel, c);
        //add(urlLabel);

        urlText = new JTextField();
        urlText.setEditable(editable);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(urlText, c);
        //add(urlText);

        typeLabel = new JLabel(resourceBundle.getString("label.phoneType"),
							   SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(typeLabel, c);
        add(typeLabel);

		//        typeCombo = new JComboBox(phoneTypes);
        typeCombo = new JComboBox();
        typeCombo.setModel(getPhoneTypesCBModel());
        typeCombo.setEditable(true);
        c.gridx = 0;
        c.gridy = 7;
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

    private ComboBoxModel getPhoneTypesCBModel() {
        javax.swing.DefaultComboBoxModel cbm =
            new javax.swing.DefaultComboBoxModel(phoneTypes);

        return cbm;
    }

    public TelephoneNumber getTelephoneNumber() throws JAXRException {
        TelephoneNumber telephoneNumber = null;

        if (model != null) {
            telephoneNumber = (TelephoneNumber) getModel();
        }

        return telephoneNumber;
    }

    public void setTelephoneNumber(TelephoneNumber phone)
        throws JAXRException {
        setModel(phone);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, TelephoneNumber.class);

        super.setModel(obj);

        TelephoneNumber phone = (TelephoneNumber) obj;

        try {
            if (phone != null) {
                countryCodeText.setText(phone.getCountryCode());
                areaCodeText.setText(phone.getAreaCode());
                numberText.setText(phone.getNumber());
                extensionText.setText(phone.getExtension());
                urlText.setText(phone.getUrl());
                typeCombo.setSelectedItem((String) phone.getType());
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            TelephoneNumber telephoneNumber = (TelephoneNumber) model;

            telephoneNumber.setCountryCode(countryCodeText.getText());
            telephoneNumber.setAreaCode(areaCodeText.getText());
            telephoneNumber.setNumber(numberText.getText());
            telephoneNumber.setExtension(extensionText.getText());
            telephoneNumber.setUrl(urlText.getText());
            telephoneNumber.setType((String) typeCombo.getSelectedItem());
            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        TelephoneNumber telephoneNumber = (TelephoneNumber) model;
    }

    public void clear() throws JAXRException {
        super.clear();
        countryCodeText.setText("");
        areaCodeText.setText("");
        numberText.setText("");
        extensionText.setText("");
        urlText.setText("");
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        countryCodeText.setEditable(editable);
        areaCodeText.setEditable(editable);
        areaCodeText.setEditable(editable);
        numberText.setEditable(editable);
        extensionText.setEditable(editable);
        urlText.setEditable(editable);
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

        areaCodeLabel.setText(resourceBundle.getString("label.areaCode"));
        countryCodeLabel.setText(resourceBundle.getString("label.countryCode"));
        extensionLabel.setText(resourceBundle.getString("label.extension"));
        numberLabel.setText(resourceBundle.getString("label.number"));
        urlLabel.setText(resourceBundle.getString("label.url"));
        typeLabel.setText(resourceBundle.getString("label.phoneType"));

	phoneTypes = new String[] { resourceBundle.getString("type.officePhone"),
				    resourceBundle.getString("type.homePhone"),
				    resourceBundle.getString("type.mobilePhone"),
				    resourceBundle.getString("type.beeper"),
				    resourceBundle.getString("type.fax") };
	}
}
