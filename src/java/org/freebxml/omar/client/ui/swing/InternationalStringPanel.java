/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/InternationalStringPanel.java,v 1.6 2005/05/03 17:43:51 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.beans.PropertyChangeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.InternationalString;


/**
 * Panel to edit/inspect an InternationalString.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 *         <a href="mailto:diego.ballve@republica.fi">Diego Ballve</a>
 */
public class InternationalStringPanel extends JBPanel {
    LocalizedStringsList localizedStringsList = null;
    protected GridBagConstraints c = null;
    protected GridBagLayout gbl = null;
	TitledBorder internationalStringPanelBorder = null;
	JLabel localizedStringsLabel = null;

    /**
     * Creates new LocalizedStringsPanel
     */
    public InternationalStringPanel() {
		super();

        internationalStringPanelBorder =
			BorderFactory.createTitledBorder(resourceBundle.getString("title.internationalStringDetails"));
        setBorder(internationalStringPanelBorder);

        gbl = new GridBagLayout();
        c = new GridBagConstraints();
        setLayout(gbl);

        //LocalizedStrings
        localizedStringsLabel =
			new JLabel(resourceBundle.getString("label.localizedStrings"),
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
        gbl.setConstraints(localizedStringsLabel, c);
        add(localizedStringsLabel);

        localizedStringsList = new LocalizedStringsList();
        localizedStringsList.setVisibleRowCount(3);

        JScrollPane localizedStringsListScrollPane =
			new JScrollPane(localizedStringsList);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(localizedStringsListScrollPane, c);
        add(localizedStringsListScrollPane);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, InternationalString.class);

        super.setModel(obj);

        InternationalString iString = (InternationalString) obj;

        try {
            LocalizedStringsListModel localizedStringsListModel = (LocalizedStringsListModel) localizedStringsList.getModel();

            if (iString != null) {
                Collection c = iString.getLocalizedStrings();
                localizedStringsListModel.setModels(new ArrayList(c));
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            InternationalString iString = (InternationalString) model;

            LocalizedStringsListModel localizedStringsListModel = (LocalizedStringsListModel) localizedStringsList.getModel();
            iString.addLocalizedStrings(Arrays.asList(
                    localizedStringsListModel.toArray()));

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();
    }

    public void clear() throws JAXRException {
        super.clear();

        // TO DO: clear localizedStringsList?
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
        internationalStringPanelBorder.
			setTitle(resourceBundle.getString("title.internationalStringDetails"));
        localizedStringsLabel.
			setText(resourceBundle.getString("label.localizedStrings"));
	}
}
