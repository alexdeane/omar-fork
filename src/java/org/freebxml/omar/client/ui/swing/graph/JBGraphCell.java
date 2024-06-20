/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/graph/JBGraphCell.java,v 1.6 2006/09/11 22:23:25 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/graph/JBGraphCell.java,v 1.6 2006/09/11 22:23:25 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing.graph;

import com.jgraph.graph.DefaultGraphCell;
import com.jgraph.graph.DefaultPort;
import com.jgraph.graph.GraphConstants;

import org.freebxml.omar.client.ui.swing.RegistryBrowser;

import java.awt.Color;
import java.awt.Font;

import java.net.URL;

import javax.swing.ImageIcon;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;
import org.freebxml.omar.common.CanonicalConstants;


/**
 * The model for a vertex in the JAXR Browser's Registry Object Graph.
 * Each cell represents a RegistryObject in the graph.
 *
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBGraphCell extends DefaultGraphCell {
    RegistryObject registryObject = null;

    private JBGraphCell() {
    }

    public JBGraphCell(RegistryObject ro, boolean createIcon) {
        super(getLabel(ro));

        //super((ro.getClass().getName()).substring((ro.getClass().getName()).lastIndexOf('.')+1, (ro.getClass().getName()).length()-4));
        String objectType = CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject;

        try {
            objectType = ro.getObjectType().getValue();
        } catch (Exception e) {
            //e.printStackTrace();
        }

        this.registryObject = ro;

        //Rectangle bounds = new Rectangle(50, 50);
        //GraphConstants.setBounds(attributes, bounds);
        GraphConstants.setOpaque(attributes, false);
        GraphConstants.setBorderColor(attributes, Color.black);
        GraphConstants.setAutoSize(attributes, true);

        //GraphConstants.setBorder(attributes, BorderFactory.createRaisedBevelBorder());
        GraphConstants.setFontStyle(attributes, Font.BOLD);

        // Create Ports
        int u = GraphConstants.PERCENT;

        // Floating Center Port (Child 0 is Default)
        DefaultPort port = new DefaultPort("Center");
        add(port);

        if (createIcon) {
            ImageIcon icon = getIcon(objectType);

            if (icon != null) {
                GraphConstants.setIcon(attributes, (ImageIcon) icon);
            }
        }
    }

    private ImageIcon getIcon(String objectType) {
        ImageIcon icon = null;
        String resourceName = "icons/rim/" + objectType + ".gif";
        URL url = this.getClass().getClassLoader().getResource(resourceName);

        if (url != null) {
            icon = new ImageIcon(url);
        } else {
            if (!objectType.equals("ExtrinsicObject")) {
                icon = getIcon("ExtrinsicObject");
            }
        }

        return icon;
    }

    private static String getLabel(RegistryObject ro) {
        String label = "";

        try {
            Connection connection = RegistryBrowser.getInstance().getClient()
                                                   .getConnection();
            RegistryService service = connection.getRegistryService();
            int registryLevel = service.getCapabilityProfile()
                                       .getCapabilityLevel();

            if (ro != null) {
                if (ro instanceof User) {
                    label = RegistryBrowser.getUserName((User) ro, registryLevel);
                } else {
                    label = RegistryBrowser.getName(ro);
                }

                if ((label == null) || (label.length() == 0)) {
                    if (ro instanceof Concept) {
                        label = ((Concept) ro).getValue();
                    } else if (ro instanceof Classification) {
                        label = ((Classification) ro).getValue();
                    }
                }
            }
        } catch (JAXRException e) {
            e.printStackTrace();
        }

        return label;
    }

    RegistryObject getRegistryObject() {
        return registryObject;
    }
}
