/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ConceptsTreeModel.java,v 1.9 2006/03/08 17:18:07 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.SwingUtilities;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.registry.JAXRException;

import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.ui.swing.SwingWorker;
import org.freebxml.omar.client.ui.swing.JavaUIResourceBundle;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryObjectImpl;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;


/**
 * A JTable that lists
 *
 * @author Jim Glennon
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ConceptsTreeModel extends DefaultTreeModel {
    RegistryBrowser registryBrowser;
    ConceptsTreeNode rootNode;
    RegistryObject rootTaxonomyElem;
    HashSet hiddenSchemes;
    private Log log = LogFactory.getLog(this.getClass());
    
    public ConceptsTreeModel(boolean updateOnCreate) {
        this(updateOnCreate, null);
    }
    
    public ConceptsTreeModel(boolean updateOnCreate,
    RegistryObject rootTaxonomyElem) {
        super(new DefaultMutableTreeNode());
        registryBrowser = RegistryBrowser.getInstance();

        setRootTaxonomyElem(rootTaxonomyElem);

        // only update if parent component needs to
        if (updateOnCreate == true) {
            update();
        }
    }
    
    public void insertClassificationScheme(ClassificationScheme scheme) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.obj = scheme;
        nodeInfo.loaded = true;
        
        ConceptsTreeNode newNode = new ConceptsTreeNode(nodeInfo);
        
        // insertNodeInto(newNode, rootNode, rootNode.getChildCount());
        rootNode.add(new ConceptsTreeNode(nodeInfo));
        reload(rootNode);
    }
    
    public void insertConcept(Concept concept, DefaultMutableTreeNode parentNode) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.obj = concept;
        nodeInfo.loaded = false;
        
        ConceptsTreeNode newNode = new ConceptsTreeNode(nodeInfo);
        
        // insertNodeInto(newNode, rootNode, rootNode.getChildCount());
        parentNode.add(new ConceptsTreeNode(nodeInfo));
        reload(parentNode);
    }
    
    /**
     * If no root node specified then load all ClassificationSchemes under a
     * dummy root node. If a root node is specified then load it as teh root
     * node. If the children of a node have been loaded then call exapnd on it.
     */
    public void update() {
        JAXRClient client = RegistryBrowser.client;
        
        if (rootTaxonomyElem != null) {
            //Root scheme specified just load it as root node
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.obj = rootTaxonomyElem;
            nodeInfo.loaded = false;
            rootNode = new ConceptsTreeNode(nodeInfo);
            setRoot(rootNode);
            
            String depthStr = ProviderProperties.getInstance()
            .getProperty("jaxr-ebxml.registryBrowser.objectTypeCombo.initialFetchDepth",
            "4");
            int depth = Integer.parseInt(depthStr);
            // use quiet mode and notify after expand is finished
            expandTree(rootNode, depth, true);
            nodeStructureChanged(rootNode);
        } else {
            //NO single root scheme specified so load all schemes
            //under a dummy root node named Concepts
            rootNode = new ConceptsTreeNode("Concepts");
            setRoot(rootNode);
            
            Collection schemes = client.getClassificationSchemes();
            
            // add classification nodes to the tree
            Iterator iter = schemes.iterator();
            
            while (iter.hasNext()) { // update
                
                ClassificationScheme scheme = (ClassificationScheme) iter.next();
                
                if (!hideScheme(scheme)) {
                    NodeInfo nodeInfo = new NodeInfo();
                    nodeInfo.obj = scheme;
                    nodeInfo.loaded = false;
                    
                    ConceptsTreeNode newNode = new ConceptsTreeNode(nodeInfo);
                    
                    // insertNodeInto(newNode, rootNode, rootNode.getChildCount());
                    rootNode.add(new ConceptsTreeNode(nodeInfo));
                }
            }
        }
        
        reload(rootNode);
    }
    
    /**
     * Determines whether to hide this scheme based upon user configuration.
     */
    private boolean hideScheme(ClassificationScheme scheme) {
        boolean hide = false;
        
        try {
            String id = scheme.getKey().getId();
            
            if (getHiddenSchemes().contains(id)) {
                hide = true;
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        
        return hide;
    }
    
    /*
     * Loads the list of schemes to hide from the property file.
     */
    private HashSet getHiddenSchemes() {
        if (hiddenSchemes == null) {
            hiddenSchemes = new HashSet();
            
            String hiddenSchemesStr = ProviderProperties.getInstance()
            .getProperty("jaxr-ebxml.registryBrowser.ConceptsTreeModel.hiddenSchemesList");
            
            if (hiddenSchemesStr != null) {
                StringTokenizer tokenizer = new StringTokenizer(hiddenSchemesStr,
                "|");
                
                while (tokenizer.hasMoreTokens()) {
                    try {
                        String schemeId = tokenizer.nextToken();
                        hiddenSchemes.add(schemeId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return hiddenSchemes;
    }
    
    /**
     * Fetches children of specified node upto specified depth.
     * @param depth a value of 1 means fetch immediate children.
     * A value of N means fetch N levels deep.
     * A value of 0 means do nothing.
     * A value of less than 0 means fetch entire tree.
     */
    public void expandTree(final DefaultMutableTreeNode node, final int depth) {
        // Warning: this method should be executed by a SwingWorker
        expandTree(node, depth, false);
    }
        
    /** 
     * Implementation for expandTree. Option to fire no events.
     */
    private void expandTree(final DefaultMutableTreeNode node, final int depth, boolean quiet) {
        // Warning: this method should be executed by a SwingWorker
        
        Object userObj = node.getUserObject();

        if (!(userObj instanceof NodeInfo)) {
            return;
        }

        final NodeInfo nodeInfo = (NodeInfo) userObj;
        if (nodeInfo.loaded) {
            return;
        }

        nodeInfo.loaded = true;
        Collection childConcepts = null; 
        try {
            childConcepts = getChildConcepts(nodeInfo.obj);
        } catch (JAXRException e) {
            log.error(e);
        }

        if (childConcepts != null) {
            Iterator iter = childConcepts.iterator();
            int newDepth = depth - 1;

            while (iter.hasNext()) { // expandTree()
                NodeInfo newNodeInfo = new NodeInfo();
                Concept childConcept = (Concept) iter.next();
                newNodeInfo.obj = childConcept;
                newNodeInfo.loaded = false;
                ConceptsTreeNode newNode = new ConceptsTreeNode(newNodeInfo);

                // insertNodeInto(newNode, node, node.getChildCount());
                node.add(newNode);

                if (newDepth != 0) {
                    expandTree(newNode, newDepth, quiet);
                }
            }
            if (!quiet) {
                nodeStructureChanged(node);
            }
        }
    }
    
    private String getName(RegistryObject ro) {
        String name = null;
        
        try {
            name = RegistryBrowser.getName(ro);
            
            if ((name == null) || (name.length() == 0)) {
                if (ro instanceof Concept) {
                    name = ((Concept) ro).getValue();
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        
        if (name == null) {
            name = "";
        }
        
        return name;
    }
    
    private Collection getChildConcepts(RegistryObject ro) throws JAXRException {
        Collection childConcepts = null;
        if (ro instanceof ClassificationScheme) {
            ClassificationScheme scheme = (ClassificationScheme) ro;
            childConcepts = scheme.getChildrenConcepts();
        } else if (ro instanceof Concept) {
            Concept concept = (Concept) ro;
            childConcepts = concept.getChildrenConcepts();
        }

        return childConcepts;
    }
    
    /**
     * Getter for property rootTaxonomyElem.
     * @return Value of property rootTaxonomyElem.
     */
    protected RegistryObject getRootTaxonomyElem() {
        return rootTaxonomyElem;
    }
    
    /**
     * Setter for property rootTaxonomyElem.
     * @param rootTaxonomyElem New value of property rootTaxonomyElem.
     */
    public void setRootTaxonomyElem(RegistryObject rootTaxonomyElem) {
        if (!((rootTaxonomyElem == null) ||
        (rootTaxonomyElem instanceof ClassificationScheme) ||
        (rootTaxonomyElem instanceof Concept))) {
            log.error(JavaUIResourceBundle.getInstance().getString("message.OnlyClassificationSchemeConceptOrNullExpected", new Object[]{rootTaxonomyElem}));
        }
        this.rootTaxonomyElem = rootTaxonomyElem;
    }
    
    private class ConceptsTreeNode extends DefaultMutableTreeNode {
        ConceptsTreeNode(Object userObject) {
            super(userObject);
        }
        
        public String toString() {
            String str = super.toString();
            
            Object userObj = getUserObject();
            
            if (userObj instanceof NodeInfo) {
                NodeInfo nodeInfo = (NodeInfo) userObj;
                
                if (nodeInfo.obj instanceof RegistryObjectImpl) {
                    RegistryObjectImpl ro = (RegistryObjectImpl)nodeInfo.obj;
                    try {
                        str = ro.getDisplayName();
                    } catch (JAXRException e) {
                        //Cant happen
                        log.error(e);
                    }
                }
            }
                        
            return str;
        }
    }
}
