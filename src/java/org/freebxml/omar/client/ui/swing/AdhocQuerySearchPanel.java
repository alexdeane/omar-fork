/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/AdhocQuerySearchPanel.java,v 1.7 2006/08/24 20:41:48 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.ui.common.UIUtility;
import org.freebxml.omar.client.ui.common.conf.bindings.Configuration;
import org.freebxml.omar.client.ui.common.conf.bindings.InternationalStringType;
import org.freebxml.omar.client.ui.common.conf.bindings.ObjectRefType;
import org.freebxml.omar.client.ui.common.conf.bindings.Parameter;
import org.freebxml.omar.client.ui.common.conf.bindings.Query;
import org.freebxml.omar.client.ui.common.conf.bindings.Slot;
import org.freebxml.omar.client.ui.common.conf.bindings.SlotListType;
import org.freebxml.omar.client.ui.common.conf.bindings.Value;
import org.freebxml.omar.client.ui.common.conf.bindings.ValueListType;
import org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.QueryImpl;
import org.freebxml.omar.client.xml.registry.infomodel.AdhocQueryImpl;
import org.freebxml.omar.client.xml.registry.infomodel.InternationalStringImpl;
import org.freebxml.omar.common.BindingUtility;



/**
 *  The panel that displays UI for specifying parameters to a Ad hoc Query.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class AdhocQuerySearchPanel extends QueryPanel {
    private GridBagConstraints c = new GridBagConstraints();
    private FindParamsPanel findParamsPanel;
    private ArrayList queries = new ArrayList();
    private JComboBox selectQueryCombo;
    /** Label for the selectQueryCombo */
    private JLabel selectQueryLabel;
    private JTextField queryNameText;
    /** Label for the nameTextField */
    private JLabel queryNameLabel;
    private JTextArea queryDescText;
    /** Label for the description TextArea */
    private JLabel queryDescLabel;
    private JPanel querySelectionPanel;
    private JPanel parentOfEntryPanel;
    private ArrayList paramComponents = new ArrayList();
    private Query query;
    private DeclarativeQueryManagerImpl dqm;
    private LifeCycleManagerImpl lcm;
    private HashMap queryToParamEntryPanelMap = new HashMap();
    private HashMap queryToAdhocQueryMap = new HashMap();
    
    private static final Log log = LogFactory.getLog(AdhocQuerySearchPanel.class);

    /**
     * Class Constructor.
     */
    public AdhocQuerySearchPanel(final FindParamsPanel findParamsPanel, Configuration cfg) {
        super(findParamsPanel, cfg);
        
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        
        // create QuerySelection and QueryParams panels
        JPanel querySelectionPanel = createQuerySelectionPanel();
        parentOfEntryPanel = createParentOfEntryPanels();
        
        JScrollPane querySelectionScrollPane = new JScrollPane(querySelectionPanel);
        JScrollPane parentOfEntryScrollPane = new JScrollPane(parentOfEntryPanel);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false,
            querySelectionScrollPane, parentOfEntryScrollPane);
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);

        //add listener for 'locale' bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(
            RegistryBrowser.PROPERTY_LOCALE, this);
    }
    
    public void reloadModel() {
        final SwingWorker worker = new SwingWorker(this) {
            public Object doNonUILogic() {
                try {
                    // This might take a while. Contructor should be running as SwingWorker
                    JAXRClient client = RegistryBrowser.getInstance().getClient();
                    Connection connection = client.getConnection();
                    RegistryService service = connection.getRegistryService();
                    dqm = (org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl)service.getDeclarativeQueryManager();            
                    lcm = (org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl)service.getBusinessLifeCycleManager();            
                    processConfiguration();
                    // model!!!
                } catch (JAXRException e) {
                    throw new UndeclaredThrowableException(e);
                }
                return null;
            }
            public void doUIUpdateLogic() {
                try {
                    selectQueryCombo.setModel(new DefaultComboBoxModel(getQueryNames()));
                    if (queries.size() > 0) {
                        setQuery((org.freebxml.omar.client.ui.common.conf.bindings.Query) queries.get(0));
                    }
                } catch (JAXRException e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        };
        worker.start();
    }
    
    private JPanel createQuerySelectionPanel() {
        JPanel querySelectionPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        querySelectionPanel.setLayout(gbl);
        
        //The selectQueryCombo
        selectQueryLabel =
        new JLabel(resourceBundle.getString("title.selectQuery"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(selectQueryLabel, c);
        querySelectionPanel.add(selectQueryLabel);
        
        //TODO: SwingBoost: localize this:
        selectQueryCombo = new JComboBox(new String[] {"loading queries..."});
        selectQueryCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                RegistryBrowser.setWaitCursor();
                if (ev.getStateChange() == ItemEvent.DESELECTED) {
                    return;
                }
                
                String item = (String) ev.getItem();
                int index = ((JComboBox) ev.getSource()).getSelectedIndex();
                
                Query query =
                (Query) queries.get(index);
                
                try {
                    setQuery(query);
                }
                catch (JAXRException e) {
                    RegistryBrowser.setDefaultCursor();
                    throw new UndeclaredThrowableException(e);
                }
                RegistryBrowser.setDefaultCursor();
            }
        });
        
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(selectQueryCombo, c);
        querySelectionPanel.add(selectQueryCombo);
        
        //The nameTextField
        queryNameLabel =
        new JLabel(resourceBundle.getString("title.name"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(queryNameLabel, c);
        querySelectionPanel.add(queryNameLabel);
        
        queryNameText = new JTextField();
        queryNameText.setEditable(false);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(queryNameText, c);
        querySelectionPanel.add(queryNameText);
        
        //The description TextArea
        queryDescLabel =
        new JLabel(resourceBundle.getString("title.description"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(queryDescLabel, c);
        querySelectionPanel.add(queryDescLabel);
        
        queryDescText = new JTextArea(4, 25);
        queryDescText.setLineWrap(true);
        queryDescText.setEditable(false);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(queryDescText, c);
        querySelectionPanel.add(queryDescText);
        
        return querySelectionPanel;
    }
    
    private void setQuery(Query query) throws JAXRException {
        this.query = query;
        
        String name = getQueryName(query);
        queryNameText.setText(name);
        queryDescText.setText(getQueryDescription(query));
        
        JPanel parameterEntryPanel = getParameterEntryPanel(query);
        parentOfEntryPanel.setVisible(false);
        parentOfEntryPanel.removeAll();
        parentOfEntryPanel.add(parameterEntryPanel);
        parentOfEntryPanel.setVisible(true);
    }
    
    private JPanel getParameterEntryPanel(Query query) {
        JPanel panel = (JPanel)queryToParamEntryPanelMap.get(query);
        if (panel == null) {
            panel = createParameterEntryPanel(query);
            queryToParamEntryPanelMap.put(query, panel);
        }
        
        return panel;
    }
    
    protected void processConfiguration()  {
        List _queries = cfg.getQuery();
        
        Iterator iter = _queries.iterator();
        
        while (iter.hasNext()) {
            Query query = (Query) iter.next();
            AdhocQueryImpl ahq = getAdhocQuery(query);
            
            if (ahq != null) {
                queries.add(query);
            }
        }
    }
    
    private String getQueryDescription(Query query) throws JAXRException {
        String queryDesc = null;
        
        InternationalString desc = getAdhocQuery(query).getDescription();
        queryDesc = getLocalizedValue(desc);
        
        return queryDesc;
    }
    
    private String getQueryName(Query query) throws JAXRException {
        String queryName = null;
        
        InternationalString name = getAdhocQuery(query).getName();
        queryName = getLocalizedValue(name);
        
        return queryName;
    }
    
    private String[] getQueryNames() throws JAXRException {
        String[] queryNames = new String[queries.size()];
        
        int i = 0;
        Iterator iter = queries.iterator();
        
        while (iter.hasNext()) {
            Query query = (Query) iter.next();
            queryNames[i++] = getQueryName(query);
        }
        
        return queryNames;
    }
    
    private AdhocQueryImpl getAdhocQuery(Query query) {
        AdhocQueryImpl adhocQuery = null;
        ObjectRefType queryRef = query.getAdhocQueryRef();
        String queryId = queryRef.getId();
        try {
            Object adhocQueryObj = queryToAdhocQueryMap.get(query);
            if (adhocQueryObj == null) {
                adhocQuery = (AdhocQueryImpl)dqm.getRegistryObject(queryId); //, "AdhocQuery");
                if (adhocQuery == null) {
                    String msg = resourceBundle.getString(
                        "message.error.failedLoadingAdhocQuery.notFound",
                        new String[] {queryId});
                    log.warn(msg);
                    
                    queryToAdhocQueryMap.put(query, Boolean.FALSE);
                } else {
                    queryToAdhocQueryMap.put(query, adhocQuery);
                }                
            } else {            
                if (adhocQueryObj instanceof AdhocQueryImpl) {
                    adhocQuery = (AdhocQueryImpl)adhocQueryObj;
                }
            }                        
        } catch (JAXRException e) {
            String msg = resourceBundle.getString(
                "message.error.failedLoadingAdhocQuery.exception",
                new String[] {queryId, e.getLocalizedMessage()});
            log.warn(msg, e);
            RegistryBrowser.displayError(msg, e);
        }
        
        return adhocQuery;
    }
    
    
    private String getLocalizedValue(InternationalStringType ist) {
        String str = "";
        
        try {
            org.oasis.ebxml.registry.bindings.rim.InternationalStringType istConverted =  
                (org.oasis.ebxml.registry.bindings.rim.InternationalStringType)UIUtility.getInstance().convertToRimBinding(ist);
            org.freebxml.omar.client.xml.registry.infomodel.InternationalStringImpl is =
                new org.freebxml.omar.client.xml.registry.infomodel.InternationalStringImpl(lcm, istConverted);
            str = getLocalizedValue(is);
        } catch (Exception e) {
            String msg = resourceBundle.getString(
                "message.error.failedLoadingAdhocQuery.exception",
                new String[] {"", e.getLocalizedMessage()});
            log.warn(msg, e);
            RegistryBrowser.displayError(msg, e);
        }
        return str;
    }
    
    /*
    private String getLocalizedValue(InternationalStringType i18n) {
        String value = "";
        
        List localizedStrings = i18n.getLocalizedString();
        Iterator iter = localizedStrings.iterator();
        
        //?? This should use InternationalString.getClosestValue()
        while (iter.hasNext()) {
            LocalizedString ls = (LocalizedString) iter.next();
            value = ls.getValue();
        }
        
        return value;
    }
    */
    
    private String getLocalizedValue(InternationalString i18n) throws JAXRException {
        String value = ((InternationalStringImpl)i18n).getClosestValue();
        
        return value;
    }
    
    private JPanel createParentOfEntryPanels() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.
        createTitledBorder(resourceBundle.
        getString("title.adHocQueryParameters")));
        
        BorderLayout bl = new BorderLayout();
        panel.setLayout(bl);
        
        return panel;
    }
    
    private JPanel createParameterEntryPanel(Query query) {
        JPanel parameterEntryPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        parameterEntryPanel.setLayout(gbl);
        
        paramComponents.clear();
        
        List params = query.getParameter();
        Iterator iter = params.iterator();
        int i = 0;
        
        while (iter.hasNext()) {
            Parameter param =
            (Parameter) iter.next();
            String tooltip = getLocalizedValue(param.getDescription());
            JLabel label = new JLabel(getLocalizedValue(param.getName()) + ":",
            SwingConstants.LEADING);
            label.setToolTipText(tooltip);
            c.gridx = 0;
            c.gridy = i;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.LINE_END;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(label, c);
            parameterEntryPanel.add(label);
            
            //Get default value for Parameter if any
            String defaultValue = param.getDefaultValue();
            
            JComponent comp = getComponentForParameter(param, defaultValue);
            
            //comp.setToolTipText(tooltip);
            c.gridx = 1;
            c.gridy = i;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.5;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_END;
            c.insets = new Insets(4, 4, 4, 4);
            gbl.setConstraints(comp, c);
            parameterEntryPanel.add(comp);
            paramComponents.add(comp);
            
            i++;
        }
        
        return parameterEntryPanel;
    }
    
    private JComponent getComponentForParameter(Parameter param, String defaultValue) {
        JComponent comp = null;
        
        String type = param.getDatatype();
        
        if (type.equals("boolean")) {
            boolean val = false;
            
            if (defaultValue.equalsIgnoreCase("true")) {
                val = true;
            }
            
            comp = new JCheckBox("", val);
        } else if (type.equals("taxonomyElement")) {
            String domainId = null;
            
            //Get the domain taxonomy element id
            SlotListType slotList = param.getSlotList();
            List slots = slotList.getSlot();
            Iterator slotsIter = slots.iterator();
            
            while (slotsIter.hasNext()) {
                Slot slot = (Slot) slotsIter.next();
                
                if (slot.getName().equalsIgnoreCase("domain")) {
                    ValueListType valList = slot.getValueList();
                    
                    if (valList != null) {
                        List values = valList.getValue();
                        Iterator valuesIter = values.iterator();
                        domainId = ((Value) valuesIter.next()).getValue();
                    }
                }
            }
            
            RegistryObject ro = null;
            
            try {
                ro = dqm.getRegistryObject(domainId);
            } catch (JAXRException e) {
                log.error(e);
                System.exit(-1);
            }
            
            ConceptsTreeModel domainTreeModel = new ConceptsTreeModel(true, ro);
            comp = new org.freebxml.omar.client.ui.swing.TreeCombo(domainTreeModel);
        } else {
            comp = new JTextField(defaultValue);
            ((JTextField) comp).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    RegistryBrowser.getInstance().findAction();
                }
            });
        }
        
        return comp;
    }
    
    /**
     * Execute the query using parameters defined by the fields in QueryPanel.
     */
    BulkResponse executeQuery() {
        BulkResponse resp = null;
        
        try {
            Connection connection = RegistryBrowser.client.getConnection();
            RegistryService service = connection.getRegistryService();
            
            HashMap parameters = getParameters();
            QueryImpl query = (QueryImpl)dqm.createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL);
            query.setFederated(isFederated());
            
            // make JAXR request
            resp = dqm.executeQuery(query, parameters);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        
        return resp;
    }
    
    private HashMap getParameters() throws JAXRException {
        HashMap params = new HashMap();
        
        params.put(BindingUtility.CANONICAL_SLOT_QUERY_ID, getAdhocQuery(query).getKey().getId());
        
        //Get the value of each parameter from its UI component
        int paramCnt = paramComponents.size();
        
        for (int i=0; i<paramCnt; i++ ) {
            Parameter param =
            (Parameter)query.getParameter().get(i);
            Component comp = (Component) paramComponents.get(i);            
            String paramName = param.getParameterName();
            String paramValue = getParameterValue(i, comp);
            if ((paramValue != null) && (paramValue.length() > 0)) {
                params.put(paramName, paramValue);
            }
        }
        
        
        return params;
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
        super.processLocaleChange(newLocale);
        
        updateUIText();
    }
    
    private String getParameterValue(int paramPosition,
    Component component) {
        String paramValue = null;
        
        try {
            if (component instanceof JTextComponent) {
                paramValue = ((JTextComponent) component).getText();
            } else if (component instanceof AbstractButton) {
                boolean selected = ((AbstractButton) component).isSelected();
                
                if (selected) {
                    paramValue = "1";
                } else {
                    paramValue = "0";
                }
            } else if (component instanceof org.freebxml.omar.client.ui.swing.TreeCombo) {
                Object conceptsTreeNode = ((org.freebxml.omar.client.ui.swing.TreeCombo) component).getSelectedItemsObject();
                Object nodeInfo = ((DefaultMutableTreeNode) conceptsTreeNode).getUserObject();
                
                if (nodeInfo instanceof org.freebxml.omar.client.ui.swing.NodeInfo) {
                    Object obj = ((org.freebxml.omar.client.ui.swing.NodeInfo) nodeInfo).obj;
                    
                    if (obj instanceof ClassificationScheme) {
                        paramValue = null;
                    } else if (obj instanceof Concept) {
                        paramValue = ((Concept) obj).getPath();
                    }
                }
            } else {
                RegistryBrowser.displayError(
                "Internal error: unsupported component class: " +
                component.getClass().getName());
            }
            
        } catch (JAXRException e) {
            log.error(e);
        }
        
        return paramValue;
    }
    
    
    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
    protected void updateUIText() {
        super.updateUIText();
        
        selectQueryLabel.
        setText(resourceBundle.getString("title.selectQuery"));
        queryNameLabel.
        setText(resourceBundle.getString("title.name"));
        queryDescLabel.
        setText(resourceBundle.getString("title.description"));
        ((TitledBorder) parentOfEntryPanel.getBorder()).setTitle(resourceBundle.
        getString("title.adHocQueryParameters"));
    }
    
    public void clear() throws JAXRException {
    }
    
}
