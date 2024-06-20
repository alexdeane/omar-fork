/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/SlotsList.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/SlotsList.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.Component;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Slot;


/**
 * Specialized JList for showing Slots.
 * Supports drag&drop of Slot objects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class SlotsList extends RegistryObjectsList {
    /**
     * Default constructor
     */
    public SlotsList() {
        this(new SlotsListModel());
    }

    /**
     * Constructor
     */
    public SlotsList(SlotsListModel model) {
        super(LifeCycleManager.SLOT, model);

        setCellRenderer(new SlotRenderer());
    }

    public static String slotToString(Slot slot) throws JAXRException {
        String str = "name=" + slot.getName() + " type=" + slot.getSlotType() +
            " values=";

        Collection values = slot.getValues();

        if (values == null) {
            str += values;
        } else {
            Iterator iter = values.iterator();

            while (iter.hasNext()) {
                String value = (String) iter.next();
                str += value;

                if (iter.hasNext()) {
                    str += ", ";
                }
            }
        }

        return str;
    }

    class SlotRenderer extends JLabel implements ListCellRenderer {
        public SlotRenderer() {
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
                SlotsListModel model = (SlotsListModel) list.getModel();
                Slot slot = (Slot) (model.elementAt(index));

                setText(SlotsList.slotToString(slot));
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }

            return this;
        }
    }
}
