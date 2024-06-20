/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/OrganizationPanel.java,v 1.6 2004/03/16 14:24:15 tonygraham Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/OrganizationPanel.java,v 1.6 2004/03/16 14:24:15 tonygraham Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;


/**
 * Panel used in a popup dialog that holds the information
 * about an organization such as its registry object and
 * registry entry information
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class OrganizationPanel extends RegistryObjectPanel {
    RegistryObjectPanel roPanel = null;
    JBList phonesList = null;
    JBList addrsList = null;
    JTextField primContactText = null;
    JBEditorDialog postalAddressDialog = null;
    JBEditorDialog primaryContactDialog = null;

    /**
     * Creates new OrganizationPanel
     */
    public OrganizationPanel() {
        super();

        setBorder(BorderFactory.createTitledBorder("Organization Details"));

        JLabel addrLabel = new JLabel("Postal Address:", SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(addrLabel, c);
        add(addrLabel);

        addrsList = new JBList(LifeCycleManager.POSTAL_ADDRESS,
                new JBListModel());
        addrsList.setVisibleRowCount(3);
        c.gridx = 0;
        c.gridy = row + 1;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(addrsList, c);
        add(addrsList);

        //Telephone Number
        JLabel phonesLabel = new JLabel("Telephone Numbers:",
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
        gbl.setConstraints(phonesLabel, c);
        add(phonesLabel);

        phonesList = new JBList(LifeCycleManager.TELEPHONE_NUMBER,
                new JBListModel());
        phonesList.setVisibleRowCount(2);

        JScrollPane phonesListScrollPane = new JScrollPane(phonesList);
        c.gridx = 0;
        c.gridy = row + 3;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(phonesListScrollPane, c);
        add(phonesListScrollPane);

        //Primary contact
        JLabel primContactLabel = new JLabel("Primary Contact:",
                SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(primContactLabel, c);
        add(primContactLabel);

        primContactText = new JTextField();
        primContactText.setEditable(false);
        c.gridx = 0;
        c.gridy = row + 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(primContactText, c);
        add(primContactText);

        JButton primContactButton = new JButton("Contact Details...");
        primContactButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    try {
                        getPrimaryContactDialog().setVisible(true);
                    } catch (JAXRException e) {
                        RegistryBrowser.displayError(e);
                    }
                }
            });
        c.gridx = 1;
        c.gridy = row + 5;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(primContactButton, c);
        add(primContactButton);
    }

    private JBEditorDialog getPostalAddressDialog() throws JAXRException {
        if (postalAddressDialog == null) {
            Window window = SwingUtilities.getWindowAncestor(this);

            if (window instanceof JFrame) {
                postalAddressDialog = new JBEditorDialog((JFrame) window, false);
            } else if (window instanceof JDialog) {
                postalAddressDialog = new JBEditorDialog((JDialog) window, false);
            }

            postalAddressDialog.setLocation(50, 50);

            if (model != null) {
                postalAddressDialog.setModel(getOrganization().getPostalAddress());
            }

            postalAddressDialog.setEditable(editable);
        }

        return postalAddressDialog;
    }

    private JBEditorDialog getPrimaryContactDialog() throws JAXRException {
        if (primaryContactDialog == null) {
            Window window = SwingUtilities.getWindowAncestor(this);

            if (window instanceof JFrame) {
                primaryContactDialog = new JBEditorDialog((JFrame) window, false);
            } else if (window instanceof JDialog) {
                primaryContactDialog = new JBEditorDialog((JDialog) window,
                        false);
            }

            primaryContactDialog.setLocation(50, 50);

            //if (model != null) {
            primaryContactDialog.setModel(getOrganization().getPrimaryContact());

            //}
            primaryContactDialog.setEditable(editable);
        }

        return primaryContactDialog;
    }

    Organization getOrganization() throws JAXRException {
        Organization organization = null;

        if (model != null) {
            organization = (Organization) getModel();
        }

        return organization;
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, Organization.class);

        super.setModel(obj);

        Organization org = (Organization) obj;

        try {
            int registryLevel = RegistryBrowser.client.getCapabilityProfile()
                                                      .getCapabilityLevel();

            DefaultListModel phonesModel = (DefaultListModel) phonesList.getModel();
            DefaultListModel addrsModel = (DefaultListModel) addrsList.getModel();

            if (org != null) {
                PostalAddress addr = org.getPostalAddress();
                String addrStr = "";

                if (addr != null) {
                    addrsModel.addElement(addr);
                }

                Collection phones = org.getTelephoneNumbers(null);
                Iterator iter = phones.iterator();

                while (iter.hasNext()) {
                    TelephoneNumber phone = (TelephoneNumber) iter.next();
                    phonesModel.addElement(phone);
                }

                User primaryContact = org.getPrimaryContact();
                String userName = RegistryBrowser.getUserName(primaryContact,
                        registryLevel);
                primContactText.setText(userName);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        Organization org = (Organization) model;

        JBListModel phonesModel = (JBListModel) phonesList.getModel();
        org.setTelephoneNumbers(phonesModel.getModels());

        /*
        try {
           concept.setValue(valueText.getText());

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }
        catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        */
    }

    public void clear() throws JAXRException {
        DefaultListModel phonesModel = (DefaultListModel) phonesList.getModel();
        phonesModel.clear();

        DefaultListModel addrsModel = (DefaultListModel) addrsList.getModel();
        addrsModel.clear();
        primContactText.setText("");
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        phonesList.setEditable(editable);
        addrsList.setEnabled(editable);
    }
}
