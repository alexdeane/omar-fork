/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/ClientRequestContext.java,v 1.2 2007/03/23 18:38:59 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.freebxml.omar.common.CommonRequestContext;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;

/**
 * Implements the RequestContext interface for JAXR Provider client.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ClientRequestContext extends CommonRequestContext {

    //Objects originally specified in request
    private Set originalObjects = new HashSet();

    private Set composedObjects = new HashSet();
    private Set processedObjects = new HashSet();
    private Set candidateSubmitObjects = new HashSet();
    private Map submitObjectsMap = new HashMap();
    private Map slotsMap = null;
    
    
    public ClientRequestContext(String contextId, RegistryRequestType request) {
        super(contextId, request);       
    }

    public Set getOriginalObjects() {
        return originalObjects;
    }

    public void setOriginalObjects(Set originalObjects) {
        this.originalObjects = originalObjects;
    }

    public Set getComposedObjects() {
        return composedObjects;
    }

    public void setComposedObjects(Set composedObjects) {
        this.composedObjects = composedObjects;
    }

    public Set getProcessedObjects() {
        return processedObjects;
    }

    public void setProcessedObjects(Set processedObjects) {
        this.processedObjects = processedObjects;
    }

    public Set getCandidateSubmitObjects() {
        return candidateSubmitObjects;
    }

    public void setCandidateSubmitObjects(Set candidateSubmitObjects) {
        this.candidateSubmitObjects = candidateSubmitObjects;
    }

    public Map getSubmitObjectsMap() {
        return submitObjectsMap;
    }

    public void setSubmitObjectsMap(Map submitObjectsMap) {
        this.submitObjectsMap = submitObjectsMap;
    }

    public Map getSlotsMap() {
        return slotsMap;
    }

    public void setSlotsMap(Map slotsMap) {
        this.slotsMap = slotsMap;
    }
    
}
