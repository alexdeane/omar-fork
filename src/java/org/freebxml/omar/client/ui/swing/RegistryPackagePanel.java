/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/RegistryPackagePanel.java,v 1.5 2005/01/02 23:27:13 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/RegistryPackagePanel.java,v 1.5 2005/01/02 23:27:13 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.beans.PropertyChangeEvent;

import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

/**
 * Panel to edit/inspect a RegistryPackage.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryPackagePanel extends RegistryObjectPanel {
	TitledBorder registryPackagePanelBorder = null;

    /**
     * Creates new RegistryPackagePanel
     */
    public RegistryPackagePanel() {
        super();

        registryPackagePanelBorder =
			BorderFactory.
			createTitledBorder(resourceBundle.getString("title.registryPackageDetails"));
        setBorder(registryPackagePanelBorder);
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

        registryPackagePanelBorder.
			setTitle(resourceBundle.getString("title.registryPackageDetails"));
	}
}
