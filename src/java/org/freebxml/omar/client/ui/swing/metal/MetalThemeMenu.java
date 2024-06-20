/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/metal/MetalThemeMenu.java,v 1.5 2007/08/15 23:49:17 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.metal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.ui.swing.RegistryBrowser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.text.MessageFormat;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;


/**
 * A JMenu for (Metal) Theme related choices.
 *
 * //TODO: add + and - commands for font size.
 * //TODO: Get theme display names from property file.
 *
 * @author  Diego Ballve / Republica Corp
 */
public class MetalThemeMenu extends JMenu implements ActionListener, PropertyChangeListener {
    /** Base name for ThemeNames resource bundles.  Do not localize. */
    public static final String BASE_NAME = "org.freebxml.omar.client.ui.swing.metal.ThemeNames";
    private ResourceBundle themeNames;
    private static final Log log = LogFactory.getLog(MetalThemeMenu.class);

    /** An array of currently available themes. Used to construct the menu. */
    private MetalTheme[] themes;

    /**
     * Constucts a JMenu named 'name' with a a RadioButton for each
     * object in 'themeArray'.
     *
     * @param name the visible name for the Menu.
     * @param themeArray the array of themes to put in the menu.
     */
    public MetalThemeMenu(String name, MetalTheme[] themeArray) {
        super(name);
        themeNames = ResourceBundle.getBundle(BASE_NAME, Locale.getDefault());
        themes = themeArray;

        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem defaultItem = null;

        for (int i = 0; i < themes.length; i++) {
            String themeName;

            try {
                themeName = themeNames.getString(themes[i].getName().replaceAll("\\s",""));
            } catch (MissingResourceException mre) {
                themeName = themes[i].getName();

                Object[] noResourceArgs = { themes[i].getName(), getLocale() };
                MessageFormat form = new MessageFormat(themeNames.getString(
                            "message.error.noResource"));
                log.error(form.format(noResourceArgs));
            }

            JRadioButtonMenuItem item = new JRadioButtonMenuItem(themeName);
            group.add(item);
            add(item);
            item.setActionCommand(i + "");
            item.addActionListener(this);

            // Theme name without spaces is the key for looking up localized item text.
            item.setName(themes[i].getName().replaceAll(" ", ""));

            if (i == 0) {
                item.setSelected(true);
                defaultItem = item;
            }
        }

        //add listener for 'locale' bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(RegistryBrowser.PROPERTY_LOCALE,
            this);
        
        defaultItem.doClick();
    }

    /**
     * Listens to property changes in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_LOCALE)) {
            themeNames = ResourceBundle.getBundle(BASE_NAME, (Locale) ev.getNewValue());

            setLocale((Locale) ev.getNewValue());
            updateText();
        }
    }

    /**
     * Updates the UI strings based on the current locale.
     */
    protected void updateText() {
        for (int i = 0; i < getItemCount(); i++) {
            JMenuItem item = getItem(i);

            // item is null if there is a JSeparator at that position.
            if (item != null) {
                String itemName = item.getName();
                String themeName;

                try {
                    themeName = themeNames.getString(itemName);
                } catch (MissingResourceException mre) {
                    themeName = itemName;

                    Object[] noResourceArgs = { itemName, getLocale() };
                    MessageFormat form = new MessageFormat(themeNames.getString(
                                "message.error.noResource"));
                    log.error(form.format(noResourceArgs));
                }

                item.setText(themeName);
            }
        }
    }

    /**
     * Listener for events from this menu.
     */
    public void actionPerformed(ActionEvent e) {
        String numStr = e.getActionCommand();
        MetalTheme selectedTheme = themes[Integer.parseInt(numStr)];
        MetalLookAndFeel.setCurrentTheme(selectedTheme);

        try {
            // I18N: Do not localize next statement.
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception ex) {
            log.error(themeNames.getString("message.error.failedLoadingMetal"), ex);
        }
    }
}
