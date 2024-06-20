/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ExternalIdentifiersList.java,v 1.4 2005/02/13 03:51:09 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ExternalIdentifiersList.java,v 1.4 2005/02/13 03:51:09 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.Component;
import java.text.MessageFormat;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.ExternalIdentifier;


/**
 * Specialized JList for showing ExternalIdentifiers.
 * Supports drag&drop of ExternalIdentifier objects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ExternalIdentifiersList extends RegistryObjectsList {
    /**
     * Default constructor
     */
    public ExternalIdentifiersList() {
        this(new ExternalIdentifiersListModel());
    }

    /**
     * Constructor
     */
    public ExternalIdentifiersList(ExternalIdentifiersListModel model) {
        super(LifeCycleManager.EXTERNAL_IDENTIFIER, model);

        setCellRenderer(new ExternalIdentifierRenderer());
    }

    class ExternalIdentifierRenderer extends JLabel implements ListCellRenderer {
        public ExternalIdentifierRenderer() {
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
                ExternalIdentifiersListModel model = (ExternalIdentifiersListModel) list.getModel();
                ExternalIdentifier extId = (ExternalIdentifier) (model.elementAt(index));
                ClassificationScheme scheme = extId.getIdentificationScheme();
                String schemeName = RegistryBrowser.getName(scheme);
                String cvalue = extId.getValue();

                Object[] listCellArgs = {schemeName, cvalue};
                MessageFormat form =
                    new MessageFormat(resourceBundle.getString("format.classificationCell"));
          
                setText(form.format(listCellArgs));
                
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }

            return this;
        }
    }
}
