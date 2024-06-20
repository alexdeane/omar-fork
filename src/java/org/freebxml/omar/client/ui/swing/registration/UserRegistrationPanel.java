/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/registration/UserRegistrationPanel.java,v 1.8 2006/06/21 19:27:20 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.registration;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.xml.registry.JAXRException;
import org.freebxml.omar.client.common.userModel.UserModel;
import org.freebxml.omar.client.ui.swing.I18nPanel;
import org.freebxml.omar.client.ui.swing.JavaUIResourceBundle;

import org.freebxml.omar.client.ui.swing.swing.RegistryDocumentListener;
import org.freebxml.omar.client.ui.swing.swing.RegistryMappedPanel;
import org.freebxml.omar.client.xml.registry.infomodel.UserImpl;


/**
 * Panel for User
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 * @author Fabian Ritzmann
 */
public class UserRegistrationPanel extends I18nPanel {
    private final JPanel panel;
    private final UserModel model;
    private TitledBorder userDetailsBorder;
    private TitledBorder personNameBorder;
    private TitledBorder emailAddressBorder;
    private TitledBorder postalAddressBorder;
    private TitledBorder telephoneNumberBorder;
    private TitledBorder digitalCertificateBorder;
    private JTextField idText;

    /**
     * Extends UserPanel to add fields to get alias and password
     */
    public UserRegistrationPanel(UserModel user) throws JAXRException {
        super();
        this.panel = this;
        this.model = user;

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        userDetailsBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.userDetails"));
        setBorder(userDetailsBorder);

        //userId field
        JLabel idLabel = new JLabel(resourceBundle.getString("label.uniqueIdentifier"),
            SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(idLabel, c);
        add(idLabel);
        
        idText = new JTextField();
        idText.setEditable(true);
        idText.getDocument().addDocumentListener(new IDListener(UserRegistrationPanel.this));
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(idText, c);
        add(idText);
        idText.setText(user.getUser().getKey().getId());
        
        //PersonNamePanel
        PersonNamePanel personNamePanel = new PersonNamePanel(user.getPersonNameModel());
        personNameBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.name"));
        personNamePanel.setBorder(personNameBorder);
        RegistryMappedPanel.setConstraints(personNamePanel, c, gbl, 0, 2, 1,
            0.5, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(personNamePanel);
        
        //EmailAddressPanel
        EmailAddressPanel emailPanel = new EmailAddressPanel(user.getEmailAddressModel());
        emailAddressBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.emailAddress"));
        emailPanel.setBorder(emailAddressBorder);
        RegistryMappedPanel.setConstraints(emailPanel, c, gbl, 1, 2, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(emailPanel);

        //PostalAddressPanel
        PostalAddressPanel addressPanel = new PostalAddressPanel(user.getPostalAddressModel());
        postalAddressBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.postalAddress"));
        addressPanel.setBorder(postalAddressBorder);
        RegistryMappedPanel.setConstraints(addressPanel, c, gbl, 0, 3, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(addressPanel);

        //TelephoneNumberPanel
        TelephoneNumberPanel phonePanel = new TelephoneNumberPanel(user.getTelephoneNumberModel());
        telephoneNumberBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.telephoneNumber"));
        phonePanel.setBorder(telephoneNumberBorder);
        RegistryMappedPanel.setConstraints(phonePanel, c, gbl, 1, 3, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(phonePanel);
        
        CertificateInfoPanel certPanel = new CertificateInfoPanel(user.getUserRegistrationInfo());
        digitalCertificateBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.certificate"));
        certPanel.setBorder(digitalCertificateBorder);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(certPanel, c);
        add(certPanel);
        
    }
    
    class IDListener extends RegistryDocumentListener {
        public IDListener(JPanel panel) {
            super(panel, resourceBundle.getString("message.error.CantSetUniqueId"));
        }

        protected void setText(String text) throws JAXRException {
            UserImpl user = (UserImpl)model.getUser();
            user.getKey().setId(text);
            user.setLid(text);
        }
    }    
    
    
    public UserModel getUserModel() {
        return this.model;
    }

    public JPanel getPanel() {
        return this.panel;
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

        userDetailsBorder.setTitle(resourceBundle.getString("title.userDetails"));
        personNameBorder.setTitle(resourceBundle.getString("title.name"));
        emailAddressBorder.setTitle(resourceBundle.getString("title.emailAddress"));
        postalAddressBorder.setTitle(resourceBundle.getString("title.postalAddress"));
        telephoneNumberBorder.setTitle(resourceBundle.getString("title.telephoneNumber"));
        digitalCertificateBorder.setTitle(resourceBundle.getString("title.certificate"));
    }
    
}
