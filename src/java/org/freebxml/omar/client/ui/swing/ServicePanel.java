/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ServicePanel.java,v 1.6 2005/01/02 23:27:13 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ServicePanel.java,v 1.6 2005/01/02 23:27:13 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.beans.PropertyChangeEvent;

import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;


/**
 * Panel to edit/inspect a Service.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ServicePanel extends RegistryObjectPanel {
	TitledBorder servicePanelBorder = null;

    /**
     * Creates new ServicePanel
     */
    public ServicePanel() {
        super();

        servicePanelBorder =
			javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("title.serviceDetails"));
        setBorder(servicePanelBorder);
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

        servicePanelBorder.
			setTitle(resourceBundle.getString("title.serviceDetails"));
	}
}
