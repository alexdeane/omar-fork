/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/ConceptImpl.java,v 1.23 2007/04/18 19:10:11 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;


/**
 * Implements JAXR API interface named Concept.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ConceptImpl extends RegistryObjectImpl implements Concept {
    private static final Log log = LogFactory.getLog(ConceptImpl.class);
    private RegistryObjectRef parentRef = null;
    private ArrayList children = new ArrayList();
    private boolean childrenLoaded = false;
    private String value;
    private String path;
    private RegistryObjectRef schemeRef = null;

    public ConceptImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);
        childrenLoaded = true;
    }

    public ConceptImpl(LifeCycleManagerImpl lcm, ClassificationNodeType cnode)
        throws JAXRException {
        super(lcm, cnode);

        value = cnode.getCode();
        path = cnode.getPath();

        String schemeId = getSchemeId();
        if (schemeId != null) {
            // Risk a ClassCastException later but do not force loading
            // ClassificationScheme now since it might trigger endless
            // loop between create ConceptImpl and ClassificationSchemeImpl
            schemeRef = new RegistryObjectRef(lcm, schemeId);
        }

        Object pnode = cnode.getParent();

        if (pnode != null) {
            parentRef = new RegistryObjectRef(lcm, pnode);
        }

        //See if cnode includes children
        List ebChildren = cnode.getClassificationNode();
        
        if (ebChildren.size() > 0) {
            Iterator iter = ebChildren.iterator();
            while (iter.hasNext()) {
                ClassificationNodeType ebChild = (ClassificationNodeType) iter.next();
                ConceptImpl child = new ConceptImpl(lcm, ebChild);
                addChildConcept(child);
            }        
            childrenLoaded = true;
        } else {
            try {
                //Check if optmization flag present indicating there are no children.
                Map slotsMap = bu.getSlotsFromRegistryObject(cnode);
                String childCount = (String)slotsMap.get(bu.CANONICAL_SLOT_NODE_PARENT_CHILD_NODE_COUNT);
                if ((childCount != null) && (childCount.equals("0"))) {
                    childrenLoaded = true;
                }
            } catch (JAXBException e) {
                //No big harm done as this is a optmization flag only.
                log.error(e);
            }
        }
    }
    
    /**
     * Gets the name of this object that is suitable for display in UIs.
     * This is typically the String in closest matching locale for the name of the object.
     * If name is null then return the value attribute value.
     *
     * @return the name suitable for display.
     */
    public String getDisplayName() throws JAXRException {
        String displayName = super.getDisplayName();
        
        if ((displayName == null) || (displayName.length() == 0)) {
            displayName = getValue();
        }
        
        if (displayName == null) {
            displayName = "";
        }
        
        return displayName;
    }        

    public String getValue() throws JAXRException {
        return value;
    }

    public void setValue(String par1) throws JAXRException {
        value = par1;
        setModified(true);
    }

    public void addChildConcept(Concept c) throws JAXRException {
        if (!(children.contains(c))) {
            children.add(c);
            ((ConceptImpl) c).setParentConcept(this);
            //No need to call setModified(true) since RIM does not require parent to remember children
        }
    }

    public void addChildConcepts(Collection par1) throws JAXRException {
        Iterator iter = par1.iterator();

        while (iter.hasNext()) {
            ConceptImpl concept = (ConceptImpl) iter.next();
            addChildConcept(concept);
        }

        //No need to call setModified(true) since RIM does not require parent to remember children
    }

    public void removeChildConcept(Concept par1) throws JAXRException {
        children.remove(par1);

        //No need to call setModified(true) since RIM does not require parent to remember children
    }

    public void removeChildConcepts(Collection par1) throws JAXRException {
        children.removeAll(par1);

        //No need to call setModified(true) since RIM does not require parent to remember children
    }

    public int getChildConceptCount() throws JAXRException {
        return getChildrenConcepts().size();
    }

    public Collection getChildrenConcepts() throws JAXRException {
        if (!childrenLoaded) {
            DeclarativeQueryManager dqm = lcm.getRegistryService()
                                             .getDeclarativeQueryManager();
            String qs = "SELECT * FROM ClassificationNode WHERE parent = '" +
                getKey().getId() + "' ORDER BY CODE";
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, qs);
            children.addAll(dqm.executeQuery(query).getCollection());
            childrenLoaded = true;
        }

        return children;
    }

    /**
     * Gets child concepts in their original order.
     * This should really be behavior of getChildrenConcepts()
     * method and a separate method should allow ORBER BY 
     * to be specified. Keeping it safe and simple for now.
     * 
     * Not yet planned for JAXR 2.0.
     *
     * @return the Set of child concepts in the default order. 
     *   Current implementation returns then in order of creation.
     */
    public Collection getChildrenConceptsUnordered() throws JAXRException {
        if (!childrenLoaded) {
            DeclarativeQueryManager dqm = lcm.getRegistryService()
                                             .getDeclarativeQueryManager();
            String qs = "SELECT * FROM ClassificationNode WHERE parent = '" +
                getKey().getId() + "' ";
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, qs);
            children.addAll(dqm.executeQuery(query).getCollection());
            childrenLoaded = true;
        }

        return children;
    }

    public Collection getDescendantConcepts() throws JAXRException {
        getChildrenConcepts();
        ArrayList descendants = new ArrayList(children);
        Iterator iter = children.iterator();

        while (iter.hasNext()) {
            Concept child = (Concept) iter.next();

            if (child.getChildConceptCount() > 0) {
                descendants.addAll(child.getDescendantConcepts());
            }
        }

        return descendants;
    }

    public Concept getParentConcept() throws JAXRException {
        RegistryObject parentObj = parentRef.getRegistryObject("RegistryObject");

        if (parentObj instanceof Concept) {
            return (Concept) parentObj;
        } else {
            return null;
        }
    }

    //??Add to JAXR API
    public RegistryObject getParent() throws JAXRException {
        RegistryObject parentObj = null;

        if (parentRef != null) {
            parentObj = parentRef.getRegistryObject("RegistryObject");
        }

        return parentObj;
    }

    public void setParentConcept(Concept c) throws JAXRException {
        if ((parentRef == null) || (!(parentRef.getId().equals(c.getKey().getId())))) {
            parentRef = new RegistryObjectRef(lcm, c);                
            c.addChildConcept(this);

            setModified(true);                
        }        
    }

    /**
     * Gets teh id for this Concept's ClassificationScheme.
     **/
    private String getSchemeId() {
        String id = null;

        if (path != null) {
            int end = path.indexOf('/', 1);

            if (end > 1) {
                id = path.substring(1, end);
            }
        }

        return id;
    }

    public ClassificationScheme getClassificationScheme()
        throws JAXRException {
        ClassificationScheme scheme = null;

        if (schemeRef != null) {
            scheme = (ClassificationScheme) schemeRef.getRegistryObject(
                    "ClassificationScheme");
        }

        return scheme;
    }

    public void setClassificationScheme(ClassificationScheme c)
        throws JAXRException {
        if ((parentRef == null) || (!(parentRef.getId().equals(c.getKey().getId())))) {
            parentRef = new RegistryObjectRef(lcm, c);
            schemeRef = parentRef;

            c.addChildConcept(this);

            setModified(true);                
        }
    }

    public String getPath() throws JAXRException {
        return path;
    }

    /**
     * Gets the level for this Concept.
     *
     * @return 0 for a Concept with no parent, 1 for a first level Concept, n otherwise
     *
     */
    public int getLevel() throws JAXRException {
        int level = 0;

        if (parentRef != null) {
            //Count the number of '/' characters in path and subtract 1
            for (int i = 0; i < path.length(); i++) {
                if (path.charAt(i) == '/') {
                    level++;
                }
            }
        }

        return level;
    }

    public Object toBindingObject() throws JAXRException {
        org.freebxml.omar.common.BindingUtility bu = org.freebxml.omar.common.BindingUtility.getInstance();

        try {
            org.oasis.ebxml.registry.bindings.rim.ClassificationNode ebClassificationNode =
                bu.rimFac.createClassificationNode();
            setBindingObject(ebClassificationNode);

            return ebClassificationNode;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    public HashSet getComposedObjects()
        throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();
        composedObjects.addAll(children);     
        composedObjects.addAll(getChildrenConcepts());
        return composedObjects;
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType ebClassificationNode)
        throws JAXRException {
        super.setBindingObject(ebClassificationNode);

        if (parentRef != null) {
            org.freebxml.omar.common.BindingUtility bu = org.freebxml.omar.common.BindingUtility.getInstance();

            try {
                org.oasis.ebxml.registry.bindings.rim.ObjectRef ebParentRef = bu.rimFac.createObjectRef();
                ebParentRef.setId(parentRef.getId());
                ebClassificationNode.setParent(ebParentRef.getId());
            } catch (JAXBException e) {
                throw new JAXRException(e);
            }
        }

        /*
         * Following code was added to support ability to export
         * entire tree (see export action in graphical browser).
         * It is being removed because it was causing major performance problems.
        getChildrenConcepts();
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            ConceptImpl childConcept = (ConceptImpl)iter.next();
            org.oasis.ebxml.registry.bindings.rim.ClassificationNode ebChildNode = childConcept.toBindingObject();
            ebClassificationNode.addClassificationNode(ebChildNode);
        }
        */
        ebClassificationNode.setCode(getValue());
    }

    /**
     * Will be added to JAXR 2.0 API in future.
     * Gets all RegistryObjects that are classified by this Concept.
     **/
    public BulkResponse getClassifiedObjects() throws JAXRException {
        String query = "SELECT ro.* FROM RegistryObject ro WHERE id IN " +
            "(SELECT classifiedObject FROM Classification " +
            "WHERE classificationNode = '" + getKey().getId() + "')";

        Query q = dqm.createQuery(Query.QUERY_TYPE_SQL, query);

        return dqm.executeQuery(q);
    }

    /**
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet getRegistryObjectRefs() {
        HashSet refs = new HashSet();

        //refs.addAll(super.getRegistryObjectRefs());
        if (parentRef != null) {
            refs.add(parentRef);
        }

        if (schemeRef != null) {
            refs.add(schemeRef);
        }

        return refs;
    }

    public String toString() {
        String str = super.toString();

        try {
            str = getValue();
        } catch (Exception e) {
            log.warn(e);
        }

        return str;
    }
}
