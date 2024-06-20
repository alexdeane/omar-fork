/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ConceptsList.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ConceptsList.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;


/**
 * Specialized JList for showing Concepts.
 * Supports drag&drop of Concept objects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ConceptsList extends RegistryObjectsList {
    /**
    * Constructor
    */
    public ConceptsList() {
        this(new ConceptsListModel());
    }

    /**
    * Constructor
    */
    public ConceptsList(ConceptsListModel model) {
        super(LifeCycleManager.CONCEPT, model);

        setCellRenderer(new ConceptRenderer());
    }

    class ConceptRenderer extends JLabel implements ListCellRenderer {
        public ConceptRenderer() {
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
                ConceptsListModel model = (ConceptsListModel) list.getModel();
                Concept concept = (Concept) (model.elementAt(index));
                ClassificationScheme scheme = concept.getClassificationScheme();
                String schemeName = RegistryBrowser.getName(scheme);

                String keyName = RegistryBrowser.getName(concept);
                String keyValue = concept.getValue();
                String str = schemeName + ":" + keyName + ":" + keyValue;

                setText(str);
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }

            return this;
        }
    }
}
