/*
 * $Id: Graph.java,v 1.8 2006/10/05 03:53:45 psterk Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *    
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *  
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *  
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

package org.freebxml.omar.client.ui.thin.components.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import javax.xml.registry.JAXRException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.ui.thin.WebUIResourceBundle;

/**
 * <p>Graph is a JavaBean representing a complete graph of {@link Node}s.</p>
 */

public class Graph {

    // ----------------------------------------------------------- Constructors

    // No-args constructor
    public Graph() {
        super();
    }


    // Constructor with specified root
    public Graph(Node root) throws JAXRException {
        setRoot(root);
    }
    
    /**
     * Constructor with Node and non-Select Node Depth
     *
     * @param root
     *   A root Node of this tree
     * @param nonSelectNodeDepth
     *   An int that specifies how many levels down from the root cannot be 
     *   selected by the user.  For example, a '2' means the user cannot 
     *   select nodes from the first two levels.  One reason could be that 
     *   these levels contain descriptive content that cannot be used by the
     *   Registry for querying.  The default is '0'.
     */
    public Graph(Node root, int nonSelectNodeDepth) throws JAXRException {
        setRoot(root);
        this.nonSelectNodeDepth = nonSelectNodeDepth;
    }


    // ------------------------------------------------------------- Properties


    /**
     * The collection of nodes that represent this hierarchy, keyed by name.
     */
    protected HashMap registry = new HashMap();

    // The root node
    private Node root = null;

    private int selectedNodeDepth = 0;
    
    private int nonSelectNodeDepth = 0;
    
    private static Log log = LogFactory.getLog(Graph.class);

    public Node getRoot() {
        return (this.root);
    }


    public void setRoot(Node root) throws JAXRException {
        setSelected(null);
        if (this.root != null) {
            removeNode(this.root);
        }
        if (root != null) {
            addNode(root);
        }
        root.setLast(true);
        this.root = root;
    }

    // support multiple selected nodes
    public Collection getSelected() {
        List selectedNodes = new ArrayList();
        Set keys = registry.keySet();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            String key = (String)itr.next();
            Node node = (Node)registry.get(key);
            if (node.isSelected()) {
                selectedNodes.add(node);
            }
        }
        return selectedNodes;
    }

    public void setSelectedNodeDepth(int selectedNodeDepth) {
        this.selectedNodeDepth = selectedNodeDepth;
    }
    
    public int getSelectedNodeDepth() {
        return selectedNodeDepth;
    }

    public void setSelected(Node selected) throws JAXRException {
        if (canNodeBeSelected(selected) == false) {
            return;
        }
        if (selected == root) {
            return;
        }
        if (selected == null) {
            clearSelectedNodes();
        } else {
            Node selectedNode = (Node)registry.get(selected.getId());
            if (selectedNode != null) {
                // toggle selected node off
                if (selectedNode.isSelected()) {
                    selectedNode.setSelected(false);
                } else { // toggle selected node on
                    selectedNode.setSelected(true);
                    // deselect all parent nodes in the selected node's path
                    Node node = selectedNode;
                    while (true) {
                        node = node.getParent();
                        if (node == null) {
                            break;
                        }
                        if (node.isSelected()) {
                            node.setSelected(false);
                        }
                    }
                    // deselect selected node, if any, in child node hierarchy
                    deselectChildNodes(selectedNode);
                }   
            }
        }
    }


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Find and return the named {@link Node} if it exists; otherwise,
     * return <code>null</code>.  The search expression must start with a
     * slash character ('/'), and the name of each intervening node is
     * separated by a slash.</p>
     *
     * @param path Absolute path to the requested node
     */
    public Node findNode(String path) throws JAXRException {

        if (!path.startsWith("/")) {
            throw new IllegalArgumentException(path);
        }
        Node node = getRoot();
        
        path = path.substring(1);
        while (path.length() > 0) {
            String name = null;
            int slash = path.indexOf("/");
            if (slash < 0) {
                name = path;
                path = "";
            } else {
                name = path.substring(0, slash);
                path = path.substring(slash + 1);
            }
            node = node.findChild(name);
            if (node == null) {
                return (null);
            }
        }
        return (node);

    }


    /**
     * Register the specified node in our registry of the complete tree.
     *
     * @param node The <code>Node</code> to be registered
     *
     * @throws IllegalArgumentException if the name of this node
     *                                  is not unique
     */
    protected void addNode(Node node) throws JAXRException, IllegalArgumentException {

        synchronized (registry) {
            String id = node.getId();
            if (registry.containsKey(id)) {
                /*
                throw new IllegalArgumentException("Name '" + name +
                                                   "' is not unique");
                 */
                log.trace(WebUIResourceBundle.getInstance().getString("message.NodeNameIsNotUniqueItWillBeIgnored", new Object[]{id}));
                return;
            }
            node.setGraph(this);
            registry.put(id, node);
        }

    }


    /**
     * Deregister the specified node, as well as all child nodes of this
     * node, from our registry of the complete tree.  If this node is not
     * present, no action is taken.
     *
     * @param node The <code>Node</code> to be deregistered
     */
    void removeNode(Node node) {

        synchronized (registry) {
            Iterator nodeItr = node.getChildren();
            while (nodeItr.hasNext()) {
                removeNode((Node) nodeItr.next());
            }
            node.setParent(null);
            node.setGraph(null);
            if (node == this.root) {
                this.root = null;
            }
        }

    }


    /**
     * Return <code>Node</code> by looking up the node registry.
     *
     * @param nodename Name of the <code>Node</code> to look up.
     */
    public Node findNodeByName(String nodename) {

        synchronized (registry) {
            return ((Node) registry.get(nodename));
        }
    }
    
    private boolean canNodeBeSelected(Node selectedNode) {
        if (selectedNode != null && selectedNode.getDepth() > nonSelectNodeDepth) {
            return true;
        } else {
            return false;
        }
    }
    
    public void deselectAllChildNodes() {
        root.setSelected(false);
        deselectChildNodes(root);
    }
    
    private void clearSelectedNodes() {
        List selectedNodes = new ArrayList();
        Set keys = registry.keySet();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            String key = (String)itr.next();
            Node node = (Node)registry.get(key);
            if (node.isSelected()) {
                node.setSelected(false);
            }
        }
    }
    
    
    private void deselectChildNodes(Node selectedNode) {
        Iterator childItr = selectedNode.getChildren();
        while (childItr.hasNext()) {
            Node node = (Node)childItr.next();
            if (node.isSelected()) {
                node.setSelected(false);
            }
            deselectChildNodes(node);
        }
    }
    
    
}
