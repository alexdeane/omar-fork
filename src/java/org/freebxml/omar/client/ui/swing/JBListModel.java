/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/JBListModel.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/JBListModel.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListModel;

import javax.xml.registry.JAXRException;


/**
 * Base class for all xxListModel classes
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBListModel extends DefaultListModel {
    /**
     * Constructor
     */
    public JBListModel() {
        super();
    }

    public ArrayList getModels() throws JAXRException {
        ArrayList models = new ArrayList();

        Object[] objs = toArray();

        for (int i = 0; i < objs.length; i++) {
            models.add(objs[i]);
        }

        return models;
    }

    public void setModels(ArrayList models) throws JAXRException {
        clear();

        if (models != null) {
            Iterator iter = models.iterator();

            while (iter.hasNext()) {
                addElement(iter.next());
            }
        }

        fireContentsChanged(this, 0, size());
    }
}
