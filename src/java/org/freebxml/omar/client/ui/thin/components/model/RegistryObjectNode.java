/*
 * $Id: RegistryObjectNode.java,v 1.3 2006/03/23 23:59:01 geomurr Exp $
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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import javax.faces.context.FacesContext;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.JAXRException;
import org.freebxml.omar.client.xml.registry.infomodel.ClassificationSchemeImpl;
import org.freebxml.omar.client.xml.registry.infomodel.InternationalStringImpl;
import org.freebxml.omar.client.ui.thin.WebUIResourceBundle;
import org.freebxml.omar.client.ui.thin.jsf.ExplorerGraphBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Node is a JavaBean representing a node in a tree control or menu.</p>
 */

public class RegistryObjectNode extends Node {

    private static Log log = LogFactory.getLog(RegistryObjectNode.class);
    private String id = null;
    private boolean hasChild = false;
    private RegistryObject ro = null;
    private Concept registryObjectType = null;
    // ----------------------------------------------------------- Constructors

    public RegistryObjectNode(RegistryObject ro) {
        super();
        if (ro == null) {
            throw new IllegalArgumentException("RegistryObject is null");
        }
        setEnabled(true);     
        this.ro = ro;
    }
    
    public RegistryObjectNode(RegistryObject ro, Concept registryObjectType) {
        this(ro);
        this.registryObjectType = registryObjectType;
    }
    
    public Concept getRegistryObjectType() {
        return registryObjectType;
    }
    
    public RegistryObject getRegistryObject()  {
        return ro;
    }
    
    public void setRegistryObject(RegistryObject ro)  {
        if (ro == null) {
            throw new IllegalArgumentException("RegistryObject is null");
        }
        this.ro = ro;
    }
    
    public String getId() throws JAXRException {
        return ro.getKey().getId();
    }
    
    public String getRegistryObjectPath() throws JAXRException {
        String path = null;
        if (ro instanceof ClassificationScheme) {
            path = "/" + getId();
        } else if (ro instanceof Concept) {
            path = ((Concept)ro).getPath();
        }
        return path;
    }
    
    public String getLabel() {
        String label = null;
        try {
            label = ((InternationalStringImpl)ro.getName()).getClosestValue(FacesContext.getCurrentInstance().getViewRoot().getLocale());
            if (label == null && ro instanceof Concept) {
                label = ((Concept)ro).getValue();
            }
            if (label == null) {
                label = WebUIResourceBundle.getInstance().getString("message.noName", "No Name");
                StringBuffer sb = new StringBuffer("(");
                sb.append(label).append(")");
                label = sb.toString();
            }
            if (ro instanceof ClassificationScheme || ro instanceof Concept) {           
                StringBuffer sb = new StringBuffer(label);
                if (ro instanceof ClassificationScheme) {
                    ClassificationScheme scheme = (ClassificationScheme)ro;
                    sb.append(" (").append(scheme.getChildConceptCount()).append(")");
                } else {
                    Concept concept = (Concept)ro;
                    sb.append(" (").append(concept.getChildConceptCount()).append(")");
                }       
                label = sb.toString();
            }
        }  catch (JAXRException ex) {
            log.error(ex);
        }
        return label;
    }
}
