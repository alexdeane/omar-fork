/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/ExternalLinkImpl.java,v 1.18 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;


/**
 * Implements JAXR API interface named ExternalLink.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ExternalLinkImpl extends RegistryObjectImpl implements ExternalLink {
    private String externalURI = null;
    private boolean validateURI = true;
    
    public ExternalLinkImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);
    }
    
    public ExternalLinkImpl(LifeCycleManagerImpl lcm, ExternalLinkType ebExtLink)
    throws JAXRException {
        super(lcm, ebExtLink);
        
        externalURI = ebExtLink.getExternalURI();
    }
    
    public Collection getLinkedObjects() throws JAXRException {
        Set linkedObjects = new HashSet();

        Iterator iter = getAssociations().iterator();

        while (iter.hasNext()) {
            AssociationImpl ass = (AssociationImpl) iter.next();

            if (ass.getAssociationTypeRef().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_ExternallyLinks)) {
                linkedObjects.add(ass.getTargetObject());
            }
        }

        return linkedObjects;
    }
    
    public HashSet getComposedObjects() throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();
        composedObjects.addAll(getLinkedObjects());
        return composedObjects;
    }
    
    public String getExternalURI() throws JAXRException {
        return externalURI;
    }
    
    public void setExternalURI(String uri) throws JAXRException {
        validateURI(uri);
        externalURI = uri;
        setModified(true);
    }
    
    public boolean getValidateURI() throws JAXRException {
        return validateURI;
    }
    
    public void setValidateURI(boolean validateURI) throws JAXRException {
        this.validateURI = validateURI;
    }
    
    public Concept getObjectType() throws JAXRException {
        Concept objectType = super.getObjectType();
        
        if (objectType == null) {
            if (objectType == null) {
                objectType = bqm.findConceptByPath(
                "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject/ExternalLink");
                
                if (objectType != null) {
                    setObjectType(objectType);
                }
            }
        }

        return objectType;
    }

    public void setObjectType(Concept objectType) throws JAXRException {
        setObjectTypeInternal(objectType);
    }

    public Object toBindingObject() throws JAXRException {
        try {
            org.oasis.ebxml.registry.bindings.rim.ExternalLink ebExtLink = bu.rimFac.createExternalLink();
            setBindingObject(ebExtLink);
            
            return ebExtLink;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }
    
    protected void setBindingObject(
    org.oasis.ebxml.registry.bindings.rim.ExternalLinkType ebExtLink)
    throws JAXRException {
        super.setBindingObject(ebExtLink);
        
        ebExtLink.setExternalURI(externalURI);
        if (objectTypeRef != null) {
            ebExtLink.setObjectType(objectTypeRef.getId());
        }
        
    }
    
    private void validateURI(String uri) throws InvalidRequestException {
        
        if (validateURI) {
            // check the http url
            boolean isValid = org.freebxml.omar.common.Utility.getInstance().isValidURI(uri);
            
            if (!isValid) {
                throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.url.not.resolvable",new Object[] {uri}));
            }
        }
    }

    public boolean isExternalURIPresent() throws JAXRException {
        return (externalURI != null && externalURI.length() > 0);
    }

}
