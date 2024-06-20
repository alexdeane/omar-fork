/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/registration/EmailAddressPanel.java,v 1.6 2006/04/10 10:59:06 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.registration;

import java.util.Locale;
import org.freebxml.omar.client.common.userModel.EmailAddressModel;
import org.freebxml.omar.client.ui.swing.I18nPanel;
import org.freebxml.omar.client.ui.swing.swing.MappedDocumentListener;
import org.freebxml.omar.client.ui.swing.swing.RegistryComboBoxListener;
import org.freebxml.omar.client.ui.swing.swing.RegistryMappedPanel;
import org.freebxml.omar.client.ui.swing.swing.TextField;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

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
 * @author Fabian Ritzmann
 */
public class EmailAddressPanel extends RegistryMappedPanel {

    private final EmailAddressModel model;
    private final JTextField addressText = new JTextField();
    private JLabel addressLabel;
    private JLabel typeLabel;
    private AddressListener addressListener;
    private EmailTypeListener emailTypeListener;

    public EmailAddressPanel(EmailAddressModel email) {
        super(email, resourceBundle.getString("error.displayEmailAddressFailed"));
        this.model = email;

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        addressLabel = new JLabel(resourceBundle.getString("label.emailAddress"), SwingConstants.LEFT);
        setConstraints(addressLabel, c, gbl, 0, 0, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(addressLabel);

        addressListener = new AddressListener();
        this.addressText.getDocument().addDocumentListener(addressListener);
        setConstraints(addressText, c, gbl, 0, 1, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return addressText;
                }

                public String getText() throws JAXRException {
                    EmailAddress address = getEmailAddressModel().getAddress();

                    if (address != null) {
                        return address.getAddress();
                    }

                    return null;
                }
            });

        typeLabel = new JLabel(resourceBundle.getString("label.addressType"), SwingConstants.LEFT);
        setConstraints(typeLabel, c, gbl, 1, 0, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        //add(typeLabel);

        JComboBox typeCombo = new JComboBox(EmailAddressModel.EMAIL_TYPES);
        emailTypeListener = new EmailTypeListener();
        typeCombo.addActionListener(emailTypeListener);
        typeCombo.setEditable(true);
        setConstraints(typeCombo, c, gbl, 1, 1, 1, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        //add(typeCombo);     Removed due to bug where change in this field changed all other fields in panel and was not being stored.   
    }

    public EmailAddressModel getEmailAddressModel() {
        return this.model;
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
        
        setError(resourceBundle.getString("error.displayEmailAddressFailed"));
        
        addressLabel.setText(resourceBundle.getString("label.emailAddress"));
        typeLabel.setText(resourceBundle.getString("label.addressType"));
        
        addressListener.setError(resourceBundle.getString("error.setEmailAddressFailed"));
    }

    class AddressListener extends MappedDocumentListener {
        AddressListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setEmailAddressFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getEmailAddressModel().setAddress(text);
        }
    }

    class EmailTypeListener extends RegistryComboBoxListener {
        EmailTypeListener() {
            super(getEmailAddressModel(), getRegistryMappedPanel());
        }
    }
    
}
