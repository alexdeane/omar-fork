/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/JBTabbedPane.java,v 1.12 2005/05/06 17:05:18 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.xml.registry.JAXRException;
import org.freebxml.omar.client.ui.swing.graph.JBGraph;
import org.freebxml.omar.client.ui.swing.graph.JBGraphModel;
import org.freebxml.omar.client.ui.swing.graph.JBGraphPanel;
import org.freebxml.omar.common.CommonProperties;

/**
 * The JTabbedPane for RegistryBrowser
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBTabbedPane extends JTabbedPane implements PropertyChangeListener {
    static JavaUIResourceBundle resourceBundle = JavaUIResourceBundle.getInstance();
    DiscoveryPanel discoveryPanel;
    JPanel submissionPanel;
    boolean noUserRegRequired = false;
    
    /**
     * Class Constructor.
     *
     */
    public JBTabbedPane() throws JAXRException {
        noUserRegRequired = Boolean.valueOf(CommonProperties.getInstance().getProperty("omar.common.noUserRegistrationRequired", "false")).booleanValue();
        
        discoveryPanel = new DiscoveryPanel();
        addTab(resourceBundle.getString("tabbedPane.discovery"), discoveryPanel);
        
        submissionPanel = new JPanel();
        submissionPanel.setLayout(new BorderLayout());
        
        JBGraphPanel graphPanel = new JBGraphPanel();
        JBGraph graph = new JBGraph(new JBGraphModel());
        graphPanel.setModel(graph);
        graphPanel.setEnabled(true);
        submissionPanel.add(graphPanel, BorderLayout.CENTER);
        
        if (noUserRegRequired) {
            addTab(resourceBundle.getString("tabbedPane.submission"),
            submissionPanel);
        }
        
        
        setSelectedIndex(0);
        
        //add listener for 'locale' bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(
            RegistryBrowser.PROPERTY_LOCALE, this);
        
        //add listener for 'authenticated' bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(
            RegistryBrowser.PROPERTY_AUTHENTICATED,this);
    }

    public void reloadModel() {
        discoveryPanel.reloadModel();
    }
    
    /**
     * Action for the Find tool.
     */
    public void findAction() {
        discoveryPanel.find();
    }
    
    /**
     * Listens to property changes in the bound property RegistryBrowser.PROPERTY_AUTHENTICATED.
     * Hides certain menuItems when user is unAuthenticated.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_AUTHENTICATED)) {
            boolean authenticated = ((Boolean) ev.getNewValue()).booleanValue();
            
            //Show submission pane only if authenticated
            if (authenticated || noUserRegRequired) {
                addTab(resourceBundle.getString("tabbedPane.submission"),
                submissionPanel);
            } else {
                remove(submissionPanel);
            }
            
            setSelectedIndex(0);
            
            //getRootPane().updateUI();
        } else if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_LOCALE)) {
            processLocaleChange((Locale) ev.getNewValue());
        }
    }
    
    /**
     * Processes a change in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    protected void processLocaleChange(Locale newLocale) {
        resourceBundle = JavaUIResourceBundle.getInstance();
        setLocale(newLocale);
        updateUIText();
    }
    
    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
    protected void updateUIText() {
        setTitleAt(indexOfComponent(discoveryPanel),
        resourceBundle.getString("tabbedPane.discovery"));
        // 'Submission' panel may not be added as a panel at the moment.
        if (indexOfComponent(submissionPanel) != -1) {
            setTitleAt(indexOfComponent(submissionPanel),
            resourceBundle.getString("tabbedPane.submission"));
        }
    }
}
