/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $header:$
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.ComponentOrientation;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import javax.swing.JPanel;


/**
 * Panel with I18n capabilities.
 *
 * Implementations should override methods processLocaleChange and updateUIText.
 * Method processLocaleChange should at least include:
 * - super.processLocaleChange(newLocale);
 * - updateUIText();
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 * @author Diego Ballve / Digital Artefacts
 */
public abstract class I18nPanel extends JPanel implements PropertyChangeListener {

    /** Resource bundle */
    protected static JavaUIResourceBundle resourceBundle = JavaUIResourceBundle.getInstance();

    /** Creates new I18nPanel */
    public I18nPanel() {
        super();
        setComponentOrientation(ComponentOrientation.getOrientation(getLocale()));
        
        //add listener for 'locale' bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(
            RegistryBrowser.PROPERTY_LOCALE,this);
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
        setDefaultLocale(newLocale);
        updateUIText();
        applyComponentOrientation(ComponentOrientation.getOrientation(newLocale));
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle. Extending
     * classes should implement this method and call super.updateUIText().
     */
    protected void updateUIText() {
        
    }
}
