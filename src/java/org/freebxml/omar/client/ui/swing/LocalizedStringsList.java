/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/LocalizedStringsList.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.LocalizedString;


/**
 * Specialized JList for showing LocalizedStrings.
 * Supports drag&drop of Slot objects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 *         <a href="mailto:diego.ballve@republica.fi">Diego Ballve</a>
 */
public class LocalizedStringsList extends RegistryObjectsList {
    /**
     * Default constructor
     */
    public LocalizedStringsList() {
        this(new LocalizedStringsListModel());
    }

    /**
     * Constructor
     */
    public LocalizedStringsList(LocalizedStringsListModel model) {
        super(LifeCycleManager.LOCALIZED_STRING, model);

        setCellRenderer(new LocalizedStringRenderer());
    }

    public static String localizedStringToString(LocalizedString lString)
        throws JAXRException {
        String str = "(" + lString.getLocale() + "): " + lString.getValue();

        return str;
    }

    class LocalizedStringRenderer extends JLabel implements ListCellRenderer {
        public LocalizedStringRenderer() {
            setOpaque(true);

            //setHorizontalAlignment(CENTER);
            //setVerticalAlignment(CENTER);
        }

        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            try {
                LocalizedStringsListModel model = (LocalizedStringsListModel) list.getModel();
                LocalizedString lString = (LocalizedString) (model.elementAt(index));

                setText(LocalizedStringsList.localizedStringToString(lString));
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }

            return this;
        }
    }
}
