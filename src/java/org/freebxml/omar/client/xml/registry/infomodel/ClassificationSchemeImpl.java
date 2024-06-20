/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/ClassificationSchemeImpl.java,v 1.26 2007/04/18 19:10:11 farrukh_najmi Exp $
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
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;

/**
 * Implements JAXR API interface named ClassificationScheme.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ClassificationSchemeImpl extends RegistryEntryImpl
    implements ClassificationScheme {

    private static final Log log = LogFactory.getLog(ClassificationSchemeImpl.class);

    private boolean external = false;
    private int valueType = ClassificationScheme.VALUE_TYPE_UNIQUE; //??No default defined by spec
    private ArrayList children = new ArrayList();
    private boolean childrenLoaded = false;

    public ClassificationSchemeImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
        childrenLoaded = true;
    }

    /**
     * This constructor is to do a type-safe cast from a Concept with no parent to a scheme.
     * This has history in UDDI and we are not sure what value it has in ebXML.
     * We are implementing it as it is required by the JAXR API.
     * Needs evaluation for relevance in JAXR 2.0??
     **/
    public ClassificationSchemeImpl(LifeCycleManagerImpl lcm, Concept concept)
        throws JAXRException {
        super(lcm);

        if (concept.getParent() != null) {
            throw new InvalidRequestException(
                 JAXRResourceBundle.getInstance().getString("message.error.cannot.create.concept.parent.classScheme"));
        }

        setName(concept.getName());
        setDescription(concept.getDescription());
        addClassifications(concept.getClassifications());
        addExternalIdentifiers(concept.getExternalIdentifiers());

        //??incomplete
    }

    public ClassificationSchemeImpl(LifeCycleManagerImpl lcm,
        org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType ebScheme)
        throws JAXRException {
        super(lcm, ebScheme);
        external = !(ebScheme.isIsInternal());
        
        //See if ebScheme includes children
        List ebChildren = ebScheme.getClassificationNode();
        
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
                Map slotsMap = bu.getSlotsFromRegistryObject(ebScheme);
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

    //??This shoudl be added to JAXR 2.0 API.
    public void setExternal(boolean external) throws JAXRException {
        if (this.external != external) {
            this.external = external;
            setModified(true);  
        }
    }

    public void addChildConcept(Concept c) throws JAXRException {
        if (!(children.contains(c))) {
            children.add(c);
            ((ConceptImpl) c).setClassificationScheme(this);
            setExternal(false);
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

    public boolean isExternal() throws JAXRException {
        Collection _children = getChildrenConcepts();
        boolean _external = external;
        if (_children.size() == 0) {
            _external = true;
        } else {
            _external = false;
        }
        setExternal(_external);
        return external;
    }

    public void setValueType(int param) throws javax.xml.registry.JAXRException {
        this.valueType = param;
        setModified(true);
    }

    public int getValueType() throws javax.xml.registry.JAXRException {
        return valueType;
    }

    public Object toBindingObject() throws JAXRException {
        try {
            org.oasis.ebxml.registry.bindings.rim.ClassificationScheme ebScheme = bu.rimFac.createClassificationScheme();
            setBindingObject(ebScheme);

            return ebScheme;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    public HashSet getComposedObjects()
        throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();        
        composedObjects.addAll(getChildrenConcepts());
        
        return composedObjects;
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType ebClassificationScheme)
        throws JAXRException {
        super.setBindingObject(ebClassificationScheme);

        ebClassificationScheme.setIsInternal(!isExternal());

        switch (this.getValueType()) {
        case VALUE_TYPE_EMBEDDED_PATH:
            ebClassificationScheme.setNodeType(BindingUtility.CANONICAL_NODE_TYPE_ID_EmbeddedPath);
            break;
        case VALUE_TYPE_NON_UNIQUE:
            ebClassificationScheme.setNodeType(BindingUtility.CANONICAL_NODE_TYPE_ID_NonUniqueCode);
            break;
        case VALUE_TYPE_UNIQUE:
            ebClassificationScheme.setNodeType(BindingUtility.CANONICAL_NODE_TYPE_ID_UniqueCode);
            break;
        }
    }
}
