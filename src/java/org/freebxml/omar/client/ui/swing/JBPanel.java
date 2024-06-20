/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/JBPanel.java,v 1.9 2005/07/31 17:12:18 doballve Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import javax.xml.registry.JAXRException;

/**
 * A specialization of JPanel for JAXR Browser designed to displays RIM objects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 * @version
 */
public abstract class JBPanel extends I18nPanel {

    protected Object model;
    protected boolean editable = false;
    
    /** Creates new JBPanel */
    public JBPanel() {
        super();
    }
    
    public Object getModel() throws JAXRException {
        if (model != null) {
            validateInput();
        }
        
        return model;
    }
    
    public void setModel(Object model) throws JAXRException {
        clear();
        this.model = model;
    }
    
    public void clear() throws JAXRException {
    }
    
    protected void validateInput() throws JAXRException {
    }
    
    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
    /**
     * Tells whether this dialog is read-only or editable.
     */
    public boolean isEditable() {
        return editable;
    }
}
