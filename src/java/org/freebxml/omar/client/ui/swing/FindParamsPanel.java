/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/FindParamsPanel.java,v 1.12 2006/06/16 03:26:24 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;


import javax.swing.JRadioButton;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.ui.common.UIUtility;
import org.freebxml.omar.client.ui.common.conf.bindings.Configuration;




/**
 * A form to allow user enter all parameters for a find or search in
 * the registry. Parent of BusinesQueryPanel and AdhocQueryPanel.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class FindParamsPanel extends JPanel implements PropertyChangeListener {
    private JavaUIResourceBundle resourceBundle = JavaUIResourceBundle.getInstance();
    private GridBagConstraints c = new GridBagConstraints();
    
    //parent for eparam entry panel for both Business and Adhoc queries
    private JPanel paramEntryParentPanel;
    private BusinessQueryPanel businessPanel;
    private AdhocQuerySearchPanel adhocPanel;
    private CardLayout cardLayout;
    private boolean businessQuery = true;
    private DiscoveryPanel discoveryPanel;
    
    /** The 'Action' panel */
    JPanel actionPanel;
    
    /** The Clear Form button */
    JButton findButton;
    
    /** The Clear Form button */
    JButton clearButton;
    
    JCheckBox federatedCheckBox;
    
    JRadioButton businessButton;
    
    JRadioButton adhocButton;
    
    private Log log = LogFactory.getLog(this.getClass());
    
    /**
     * Class Constructor.
     */
    public FindParamsPanel(DiscoveryPanel discoveryPanel) throws JAXRException {
        this.discoveryPanel = discoveryPanel;
       
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        
        //The searchTypeSelectionPanel at the top of the panel
        JPanel searchTypePanel = createSearchTypePanel();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(searchTypePanel, c);
        add(searchTypePanel);
        
        //The federated CheckBox
        federatedCheckBox = new JCheckBox(resourceBundle.getString("label.federatedQuery"));
        federatedCheckBox.setSelected(false);
        federatedCheckBox.setEnabled(true);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(federatedCheckBox, c);
        add(federatedCheckBox);
        
        //The actionPanel
        JPanel actionPanel = createActionPanel();
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(searchTypePanel, c);
        add(actionPanel);
        
        //Next is the panel that is the parent of both adhocQueryPanel and
        //businessQueryPanel
        paramEntryParentPanel = createParamEntryParentPanel();
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(paramEntryParentPanel, c);
        add(paramEntryParentPanel);
        
        //RegistryBrowser.getInstance().getConceptsTreeDialog().addPropertyChangeListener(this);
        //add listener for 'locale' bound property
        RegistryBrowser.getInstance().
        addPropertyChangeListener(RegistryBrowser.PROPERTY_LOCALE,
        this);
    }
    
    private JPanel createActionPanel() {
        actionPanel = new JPanel();
        
        //The Find button
        findButton = new JButton(resourceBundle.getString("button.search"));
        actionPanel.add(findButton);
        findButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                discoveryPanel.find();
            }
        });
        
        //The Clear Form button
        clearButton = new JButton(resourceBundle.getString("button.clearForm"));
        actionPanel.add(clearButton);
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    discoveryPanel.clear();
                } catch (JAXRException e) {
                    RegistryBrowser.displayError(e);
                }
            }
        });
        
        return actionPanel;
    }
    
    private JPanel createSearchTypePanel() {
        //It just has two radio buttons that Selects from Business and Adhoc Query
        JPanel searchTypePanel = new JPanel();
        
        businessButton = new JRadioButton(resourceBundle.getString("button.businessQuery"));
        businessButton.setMnemonic(KeyEvent.VK_B);
        businessButton.setSelected(true);
        businessButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(paramEntryParentPanel, "businessPanel");
                businessQuery = true;
            }
        });

        adhocButton = new JRadioButton(resourceBundle.getString("button.adHocQuery"));
        adhocButton.setMnemonic(KeyEvent.VK_A);
        adhocButton.setSelected(false);
        adhocButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(paramEntryParentPanel, "adhocPanel");
                businessQuery = false;
            }
        });
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(businessButton);
        buttonGroup.add(adhocButton);
        
        searchTypePanel.add(businessButton);
        searchTypePanel.add(adhocButton);
        
        return searchTypePanel;
    }

    private JPanel createParamEntryParentPanel() throws JAXRException {
        JPanel paramEntryParentPanel = new JPanel();
        cardLayout = new CardLayout();
        paramEntryParentPanel.setLayout(cardLayout);
        
        Configuration cfg = UIUtility.getInstance().getConfiguration();
        
        //The panel used for business query based searches
        businessPanel = new BusinessQueryPanel(this, cfg);
        paramEntryParentPanel.add(businessPanel, "businessPanel");
        
        //The panel used for ad hoc query based searches
        adhocPanel = new AdhocQuerySearchPanel(this, cfg);
        paramEntryParentPanel.add(adhocPanel, "adhocPanel");
        
        return paramEntryParentPanel;
    }
    
    public void reloadModel() {
        adhocPanel.reloadModel();
        businessPanel.reloadModel();        
    }
    
    boolean isFederated() {
        return federatedCheckBox.isSelected();
    }
    
    /**
     * Execute the query using parameters defined by the fields in
     * currently selected QueryPanel.
     */
    BulkResponse executeQuery() {
        BulkResponse resp = null;
        
        if (businessQuery) {
            resp = businessPanel.executeQuery();
        } else {
            resp = adhocPanel.executeQuery();
        }
        
        return resp;
    }
    
    /**
     * Invoke the find action. Delegates to parent.
     */
    void find() {
        discoveryPanel.find();
    }
    
    /**
     * Clears or resets the UI. Delegates to childern.
     */
    public void clear() throws JAXRException {
        businessPanel.clear();
        adhocPanel.clear();
    }
    
    /**
     * Listens to property changes in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_LOCALE)) {
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
        findButton.setText(resourceBundle.getString("button.search"));
        clearButton.setText(resourceBundle.getString("button.clearForm"));
        businessButton.setText(resourceBundle.getString("button.businessQuery"));
        adhocButton.setText(resourceBundle.getString("button.adHocQuery"));
    }
}
