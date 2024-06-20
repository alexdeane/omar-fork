/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/graph/JBGraphModel.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/graph/JBGraphModel.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing.graph;

import com.jgraph.graph.DefaultGraphModel;
import com.jgraph.graph.Edge;


/**
 * Custom model that does not allow self references in graph.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBGraphModel extends DefaultGraphModel {
    // Override Superclass Method
    public boolean acceptsSource(Object edge, Object port) {
        // Source only Valid if not Equal Target
        return (((Edge) edge).getTarget() != port);
    }

    // Override Superclass Method
    public boolean acceptsTarget(Object edge, Object port) {
        // Target only Valid if not Equal Source
        return (((Edge) edge).getSource() != port);
    }
}
