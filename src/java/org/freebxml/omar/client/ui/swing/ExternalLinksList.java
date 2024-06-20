/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ExternalLinksList.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ExternalLinksList.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.ExternalLink;


/**
 * Specialized JList for showing ExternalLinks.
 * Supports drag&drop of ExternalLink objects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ExternalLinksList extends RegistryObjectsList {
    /**
     * Default constructor
     */
    public ExternalLinksList() {
        this(new ExternalLinksListModel());
    }

    /**
     * Constructor
     */
    public ExternalLinksList(ExternalLinksListModel model) {
        super(LifeCycleManager.EXTERNAL_LINK, model);

        setCellRenderer(new ExternalLinkRenderer());
    }

    class ExternalLinkRenderer extends JLabel implements ListCellRenderer {
        public ExternalLinkRenderer() {
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
                ExternalLinksListModel model = (ExternalLinksListModel) list.getModel();
                ExternalLink link = (ExternalLink) (model.elementAt(index));

                String externalURI = link.getExternalURI();

                String str = externalURI;
                String desc = RegistryBrowser.getDescription(link);

                if ((desc != null) && (desc.length() > 0)) {
                    str += (" ( " + desc + " )");
                }

                setText(str);
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }

            return this;
        }
    }
}
