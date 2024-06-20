/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ClassificationsList.java,v 1.6 2005/02/13 03:51:09 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ClassificationsList.java,v 1.6 2005/02/13 03:51:09 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import java.text.MessageFormat;

import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;


/**
 * Specialized JList for showing Classifications.
 * Supports drag&drop of Classification objects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ClassificationsList extends RegistryObjectsList {
    /**
    * Constructor
    */
    public ClassificationsList() {
        this(new ClassificationsListModel());
    }

    /**
    * Constructor
    */
    public ClassificationsList(ClassificationsListModel model) {
        super(LifeCycleManager.CLASSIFICATION, model);

        setCellRenderer(new ClassificationRenderer());
    }

    class ClassificationRenderer extends JLabel implements ListCellRenderer {
        public ClassificationRenderer() {
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
                ClassificationsListModel model = (ClassificationsListModel) list.getModel();
                Classification cl = (Classification) (model.elementAt(index));
                ClassificationScheme scheme = cl.getClassificationScheme();                
                String schemeName = RegistryBrowser.getName(scheme);
                String cvalue = cl.getValue();

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
