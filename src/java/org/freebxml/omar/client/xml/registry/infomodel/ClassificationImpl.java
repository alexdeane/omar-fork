/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/ClassificationImpl.java,v 1.14 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.bind.JAXBException;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryObject;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.exceptions.MissingParentReferenceException;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;

/**
 * Implements JAXR API interface named Classification.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ClassificationImpl extends RegistryObjectImpl
    implements Classification {
    private RegistryObjectRef conceptRef = null;
    private RegistryObjectRef schemeRef = null;
    private String value = null;
    private RegistryObjectRef classifiedObjectRef = null;

    public ClassificationImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public ClassificationImpl(LifeCycleManagerImpl lcm,
        ClassificationType ebClass, RegistryObject classifiedObject)
        throws JAXRException {
        super(lcm, ebClass);

        Object schemeObj = ebClass.getClassificationScheme();

        if (schemeObj != null) {
            schemeRef = new RegistryObjectRef(lcm, schemeObj);
        }

        if (classifiedObject != null) {
            classifiedObjectRef = new RegistryObjectRef(lcm, classifiedObject);
        }

        Object cnodeObj = ebClass.getClassificationNode();

        if (cnodeObj != null) {
            conceptRef = new RegistryObjectRef(lcm, cnodeObj);
        }

        value = ebClass.getNodeRepresentation();

        // clean modified flag since object freshly loaded from registry
        setModified(false);
    }

    public Concept getConcept() throws JAXRException {
        Concept concept = null;

        if (conceptRef != null) {
            concept = (Concept) (conceptRef.getRegistryObject(
                    "ClassificationNode"));
        }

        return concept;
    }

    public void setConcept(Concept concept) throws JAXRException {
        //If Class scheme is  External type in that case the concept will be null
        //so the conceptRef should be set to the null also. 
        if (concept == null) {
            conceptRef = null;
        } else {
            ClassificationScheme scheme = concept.getClassificationScheme();
            if (scheme == null) {
                throw new InvalidRequestException(
                 JAXRResourceBundle.getInstance().getString("message.error.no.classScheme.ancestore"));
            }
            conceptRef = new RegistryObjectRef(lcm, concept);
        }          
        setModified(true);   
    }

    public ClassificationScheme getClassificationScheme()
        throws JAXRException {
        ClassificationScheme scheme = null;

        if (schemeRef == null) {
            Concept concept = getConcept();

            if (concept != null) {
                scheme = concept.getClassificationScheme();
            }
        } else {
            scheme = (ClassificationScheme) (schemeRef.getRegistryObject(
                    "ClassificationScheme"));
        }

        return scheme;
    }

    public void setClassificationScheme(ClassificationScheme scheme)
        throws JAXRException {
        schemeRef = new RegistryObjectRef(lcm, scheme);
        setModified(true);
    }

    public InternationalString getName() throws JAXRException {
        InternationalString name = super.getName();

        //Clone name from Concept if none defined
        if (name.getLocalizedStrings().size() == 0) {
            if (!isExternal()) {
                Concept concept = getConcept();

                if (concept != null) {
                    name = (InternationalStringImpl) ((InternationalStringImpl) (concept.getName())).clone();
                }
            }
        }

        return name;
    }

    public String getValue() throws JAXRException {
        String val = null;

        if (isExternal()) {
            val = value;
        } else {
            Concept concept = getConcept();

            if (concept != null) {
                val = concept.getValue();
            }
        }

        return val;
    }

    public void setValue(String par1) throws JAXRException {
        value = par1;
        setModified(true);
    }

    public RegistryObject getClassifiedObject() throws JAXRException {
        RegistryObject classifiedObject = null;

        if (classifiedObjectRef != null) {
            classifiedObject = (RegistryObject) (classifiedObjectRef.getRegistryObject(
                    "RegistryObject"));
        }

        return classifiedObject;
    }

    public void setClassifiedObject(RegistryObject ro)
        throws JAXRException {
        if (ro == null) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.classified.object.null"));
        }

        classifiedObjectRef = new RegistryObjectRef(lcm, ro);
        setModified(true);

        //In case this was called directly by client, make sure that this classification is added to classified Object
        Collection classifications = ro.getClassifications();

        if (!classifications.contains(this)) {
            ro.addClassification(this);
        }
    }

    public boolean isExternal() throws JAXRException {
        boolean external = false;

        if (conceptRef == null) {
            external = true;
        }

        return external;
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            org.oasis.ebxml.registry.bindings.rim.ObjectFactory factory = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
            org.oasis.ebxml.registry.bindings.rim.Classification ebClassification =
                factory.createClassification();
            setBindingObject(ebClassification);

            return ebClassification;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.ClassificationType ebClassification)
        throws JAXRException {
        super.setBindingObject(ebClassification);

        if (schemeRef != null) {
            ebClassification.setClassificationScheme(schemeRef.getId());
        }

        if (conceptRef != null) {
            ebClassification.setClassificationNode(conceptRef.getId());
        }

        if (classifiedObjectRef != null) {
            ebClassification.setClassifiedObject(classifiedObjectRef.getId());
        } else {
            throw new MissingParentReferenceException(
                JAXRResourceBundle.getInstance().getString("message.error.missing.classified.object.id",new Object[] {getId()}));
        }

        if (value != null) {
            ebClassification.setNodeRepresentation(value);
        }
    }

    /**
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet getRegistryObjectRefs() {
        HashSet refs = new HashSet();

        //refs.addAll(super.getRegistryObjectRefs());
        if (conceptRef != null) {
            refs.add(conceptRef);
        }

        if (schemeRef != null) {
            refs.add(schemeRef);
        }

        if (classifiedObjectRef != null) {
            refs.add(classifiedObjectRef);
        }

        return refs;
    }
}
