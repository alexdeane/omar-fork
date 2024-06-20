/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/registration/PersonNamePanel.java,v 1.5 2006/04/10 10:59:06 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.registration;

import java.util.Locale;
import org.freebxml.omar.client.common.userModel.PersonNameModel;
import org.freebxml.omar.client.ui.swing.I18nPanel;
import org.freebxml.omar.client.ui.swing.swing.RegistryDocumentListener;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import org.freebxml.omar.client.ui.swing.swing.RegistryMappedPanel;


/**
 * Panel for PersonName
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 * @author Fabian Ritzmann
 */
public class PersonNamePanel extends I18nPanel {

    private final PersonNameModel model;
    private JLabel firstNameLabel;
    private JLabel middleNameLabel;
    private JLabel lastNameLabel;
    private FirstNameListener firstNameListener;
    private MiddleNameListener middleNameListener;
    private LastNameListener lastNameListener;

    PersonNamePanel(PersonNameModel person) {
        super();
        this.model = person;

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        firstNameLabel = new JLabel(resourceBundle.getString("label.firstName"),
                SwingConstants.LEFT);
        RegistryMappedPanel.setConstraints(firstNameLabel, c, gbl, 0, 0, 1,
            0.0, GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(firstNameLabel);

        JTextField firstNameText = new JTextField();
        firstNameListener = new FirstNameListener();
        firstNameText.getDocument().addDocumentListener(firstNameListener);
        RegistryMappedPanel.setConstraints(firstNameText, c, gbl, 0, 1, 1,
            0.75, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(firstNameText);

        middleNameLabel = new JLabel(resourceBundle.getString("label.middleName"), SwingConstants.LEFT);
        RegistryMappedPanel.setConstraints(middleNameLabel, c, gbl, 1, 0, 1,
            0.0, GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(middleNameLabel);

        JTextField middleNameText = new JTextField();
        middleNameListener = new MiddleNameListener();
        middleNameText.getDocument().addDocumentListener(middleNameListener);
        RegistryMappedPanel.setConstraints(middleNameText, c, gbl, 1, 1, 1,
            0.25, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        add(middleNameText);

        lastNameLabel = new JLabel(resourceBundle.getString("label.lastName"),
                SwingConstants.LEFT);
        RegistryMappedPanel.setConstraints(lastNameLabel, c, gbl, 0, 2, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(lastNameLabel);

        JTextField lastNameText = new JTextField();
        lastNameListener = new LastNameListener();
        lastNameText.getDocument().addDocumentListener(lastNameListener);
        RegistryMappedPanel.setConstraints(lastNameText, c, gbl, 0, 3, 2, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(lastNameText);
    }

    public PersonNameModel getPersonNameModel() {
        return this.model;
    }

    public PersonNamePanel getPersonNamePanel() {
        return this;
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
        
        firstNameListener.setError(resourceBundle.getString("error.setFirstName"));
        middleNameListener.setError(resourceBundle.getString("error.setMiddleName"));
        lastNameListener.setError(resourceBundle.getString("error.setLastName"));

    }
    
    class FirstNameListener extends RegistryDocumentListener {
        public FirstNameListener() {
            super(getPersonNamePanel(), resourceBundle.getString("error.setFirstName"));
        }

        protected void setText(String text) throws JAXRException {
            getPersonNameModel().setFirstName(text);
        }
    }

    class MiddleNameListener extends RegistryDocumentListener {
        public MiddleNameListener() {
            super(getPersonNamePanel(), resourceBundle.getString("error.setMiddleName"));
        }

        protected void setText(String text) throws JAXRException {
            getPersonNameModel().setMiddleName(text);
        }
    }

    class LastNameListener extends RegistryDocumentListener {
        LastNameListener() {
            super(getPersonNamePanel(), resourceBundle.getString("error.setLastName"));
        }

        protected void setText(String text) throws JAXRException {
            getPersonNameModel().setLastName(text);
        }
    }
}
