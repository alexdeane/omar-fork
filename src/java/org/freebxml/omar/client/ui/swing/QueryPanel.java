/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/QueryPanel.java,v 1.9 2006/06/16 03:26:24 farrukh_najmi Exp $
 * ====================================================================
 */

package org.freebxml.omar.client.ui.swing;

import java.awt.ComponentOrientation;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import org.freebxml.omar.client.ui.common.conf.bindings.Configuration;
import org.freebxml.omar.client.xml.registry.util.QueryUtil;

/**
 * Base class for all Query related panels.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public abstract class QueryPanel extends JPanel
implements PropertyChangeListener {
    
    protected JavaUIResourceBundle resourceBundle = JavaUIResourceBundle.getInstance();
    
    private TitledBorder border;
    private FindParamsPanel findParamsPanel;
    protected Configuration cfg;
    protected QueryUtil qu = QueryUtil.getInstance();
    
    /**
     * Class Constructor.
     */
    public QueryPanel(final FindParamsPanel findParamsPanel, Configuration cfg) {
        this.cfg = cfg;
        this.findParamsPanel = findParamsPanel;
        border = BorderFactory.createTitledBorder(resourceBundle.getString("title.searchCriteria"));
        this.setBorder(border);        
    }
    
    protected abstract void processConfiguration();
    
    /**
     * Clears or resets the UI.
     */
    public abstract void clear() throws JAXRException;
    
    /**
     * Invoke the find action. Delegates to parent.
     */
    void find() {
        findParamsPanel.find();
    }
    
    boolean isFederated() {
        return findParamsPanel.isFederated();
    }    
    
    /**
     * Execute the query using parameters defined by the fields in QueryPanel.
     */
    abstract BulkResponse executeQuery();
    
    public abstract void propertyChange(PropertyChangeEvent ev);
    
    /**
     * Processes a change in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    protected void processLocaleChange(Locale newLocale) {
        resourceBundle = JavaUIResourceBundle.getInstance();
        
        setLocale(newLocale);
        applyComponentOrientation(ComponentOrientation.getOrientation(newLocale));
    }
    
    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
    protected void updateUIText() {
        border.setTitle(resourceBundle.getString("title.searchCriteria"));
    }
}
