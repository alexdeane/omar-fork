/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/PersonPanel.java,v 1.1 2004/07/30 17:42:45 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.beans.PropertyChangeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.TelephoneNumber;
import org.freebxml.omar.client.xml.registry.infomodel.PersonImpl;


/**
 * Panel for Person
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class PersonPanel extends RegistryObjectPanel {
	TitledBorder personPanelBorder = null;
    PersonNamePanel personNamePanel = null;
	TitledBorder personNamePanelBorder = null;
    PostalAddressPanel addressPanel = null;
	TitledBorder addressPanelBorder = null;
    TelephoneNumberPanel phonePanel = null;
	TitledBorder phonePanelBorder = null;
    EmailAddressPanel emailPanel = null;
	TitledBorder emailPanelBorder = null;
    private int firstChildIndex = 0;

    /**
     * Used for displaying Personobjects
     */
    public PersonPanel() {
        super();

        firstChildIndex = getComponents().length;

        personPanelBorder =
			BorderFactory.createTitledBorder(getPanelName());
        setBorder(personPanelBorder);

        //PersonNamePanel
        personNamePanel = new PersonNamePanel();
        personNamePanelBorder =
			BorderFactory.createTitledBorder(resourceBundle.getString("title.name"));
        personNamePanel.setBorder(personNamePanelBorder);
        c.gridx = 0;
        c.gridy = row + 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(personNamePanel, c);
        add(personNamePanel);

        //EmailAddressPanel
        emailPanel = new EmailAddressPanel();
        emailPanelBorder =
			BorderFactory.createTitledBorder(resourceBundle.getString("title.emailAddress"));
        emailPanel.setBorder(emailPanelBorder);
        c.gridx = 1;
        c.gridy = row + 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(emailPanel, c);
        add(emailPanel);

        //PostalAddressPanel
        addressPanel = new PostalAddressPanel();
        addressPanelBorder =
			BorderFactory.createTitledBorder(resourceBundle.getString("title.postalAddress"));
        addressPanel.setBorder(addressPanelBorder);
        c.gridx = 0;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(addressPanel, c);
        add(addressPanel);

        //TelephoneNumberPanel
        phonePanel = new TelephoneNumberPanel();
        phonePanelBorder =
			BorderFactory.createTitledBorder(resourceBundle.getString("title.telephoneNumber"));
        phonePanel.setBorder(phonePanelBorder);
        c.gridx = 1;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(phonePanel, c);
        add(phonePanel);

        row += 2;

        //add listener for 'locale' bound property
		RegistryBrowser.getInstance().
			addPropertyChangeListener(RegistryBrowser.PROPERTY_LOCALE,
									  this);
    }
    
    protected String getPanelName() {
        return resourceBundle.getString("title.personDetails");
    }

    //Used by UserRegistrationPanel to hide detail not needed.
    protected void hideRegistryObjectAttributes() {
        Component[] children = getComponents();

        for (int i = 0; i < firstChildIndex; i++) {
            this.remove(children[i]);
        }
    }

    public PersonImpl getPerson() throws JAXRException {
        PersonImpl person = null;

        if (model != null) {
            person = (PersonImpl) getModel();
        }

        return person;
    }

    public void setPerson(PersonImpl person) throws JAXRException {
        setModel(person);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, PersonImpl.class);

        super.setModel(obj);

        PersonImpl person = (PersonImpl) obj;

        try {
            if (person != null) {
                PersonName personName = person.getPersonName();

                if (personName == null) {
                    personName = person.getLifeCycleManager().createPersonName("",
                            "", "");
                    person.setPersonName(personName);
                }

                personNamePanel.setModel(personName);

                Collection addrs = person.getPostalAddresses();

                if (addrs != null) {
                    PostalAddress address = null;

                    if (addrs.size() == 0) {
                        address = person.getLifeCycleManager()
                                      .createPostalAddress("", "", "", "", "",
                                "", "");

                        //Fix JAXR API 2.0 ?? person.addPostalAddress();
                        addrs = new ArrayList();
                        addrs.add(address);
                        person.setPostalAddresses(addrs);
                    } else {
                        address = (PostalAddress) (addrs.iterator().next());
                    }

                    addressPanel.setModel(address);
                }

                Collection emails = person.getEmailAddresses();

                if (emails != null) {
                    EmailAddress email = null;

                    if (emails.size() == 0) {
                        email = person.getLifeCycleManager().createEmailAddress("");

                        //Fix JAXR API 2.0 ?? person.addPostalAddress();
                        emails = new ArrayList();
                        emails.add(email);
                        person.setEmailAddresses(emails);
                    } else {
                        email = (EmailAddress) (emails.iterator().next());
                    }

                    emailPanel.setModel(email);
                }

                Collection phones = person.getTelephoneNumbers(null);

                if (phones != null) {
                    TelephoneNumber phone = null;

                    if (phones.size() == 0) {
                        phone = person.getLifeCycleManager()
                                    .createTelephoneNumber();

                        //Fix JAXR API 2.0 ?? person.addPostalAddress();
                        phones = new ArrayList();
                        phones.add(phone);
                        person.setTelephoneNumbers(phones);
                    } else {
                        phone = (TelephoneNumber) (phones.iterator().next());
                    }

                    phonePanel.setModel(phones.iterator().next());
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            personNamePanel.getModel();
            addressPanel.getModel();

            emailPanel.getModel();
            phonePanel.getModel();
            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        PersonImpl person = (PersonImpl) model;

        personNamePanel.validateInput();
        addressPanel.validateInput();

        emailPanel.validateInput();
        phonePanel.validateInput();
    }

    public void clear() throws JAXRException {
        super.clear();

        personNamePanel.clear();
        addressPanel.clear();

        emailPanel.clear();
        phonePanel.clear();
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        personNamePanel.setEditable(editable);
        addressPanel.setEditable(editable);
        emailPanel.setEditable(editable);
        phonePanel.setEditable(editable);
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

		updateUI();
	}

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
	protected void updateUIText() {
		super.updateUIText();

        personPanelBorder.
			setTitle(resourceBundle.getString("title.personDetails"));
        personNamePanelBorder.
			setTitle(resourceBundle.getString("title.name"));
        addressPanelBorder.
			setTitle(resourceBundle.getString("title.postalAddress"));
        phonePanelBorder.
			setTitle(resourceBundle.getString("title.telephoneNumber"));
        emailPanelBorder.
			setTitle(resourceBundle.getString("title.emailAddress"));
	}
}
