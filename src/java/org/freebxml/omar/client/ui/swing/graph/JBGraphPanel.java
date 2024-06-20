/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/graph/JBGraphPanel.java,v 1.5 2005/03/20 17:06:49 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.graph;

import org.freebxml.omar.client.ui.swing.JBEditorDialog;
import org.freebxml.omar.client.ui.swing.JBPanel;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Window;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JScrollPane;

import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryObject;


/**
 * Panel to show RegistryObjects as a Graph
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBGraphPanel extends JBPanel {
    // The width of this component in pixel
    private static final int panelWidth = 1600;

    // The height of this component in pixel
    private static final int panelHeight = 2000;

    public JBGraphPanel() {
        setLayout(new BorderLayout());
    }

    public static void browseObject(Window parent, RegistryObject ro,
        boolean editable) {
        JBGraph graph = new JBGraph(new JBGraphModel());
        graph.addRegistryObject(ro, new Rectangle(200, 200, 50, 50), true);

        JBEditorDialog dialog = JBEditorDialog.showObjectDetails(parent, graph,
                false, editable);
    }

    public static void browseObjects(Window parent, Collection objs,
        boolean editable) {
        JBGraph graph = new JBGraph(new JBGraphModel());

        int x = 200;
        int y = 200;
        Iterator iter = objs.iterator();

        while (iter.hasNext()) {
            RegistryObject ro = (RegistryObject) iter.next();
            x += 100;
            y += 100;
            graph.addRegistryObject(ro, new Rectangle(x, y, 50, 50), true);
        }

        JBGraph.circleLayout(graph);

        JBEditorDialog dialog = JBEditorDialog.showObjectDetails(parent, graph,
                false, editable);
        dialog.setTitle(resourceBundle.getString("menu.browse"));
    }

    public void setModel(Object obj) throws JAXRException {
        if (!(obj instanceof JBGraph)) {
            throw new InvalidRequestException("Expecting a JBGRaph. Got a " +
                obj.getClass().getName());
        }

        super.setModel(obj);

        JBGraph graph = (JBGraph) obj;
        JScrollPane graphSP = new JScrollPane(graph); //(new JGraph());

        add(graph.getToolBar(), BorderLayout.NORTH);

        add(graphSP, BorderLayout.CENTER);
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        ((JBGraph) model).setEditable(editable);
    }

    /**
     * Tells whether this dialog is read-only or editable.
     */
    public boolean isEditable() {
        return ((JBGraph) model).isEditable();
    }
}
