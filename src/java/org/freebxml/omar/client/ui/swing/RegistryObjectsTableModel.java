/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/RegistryObjectsTableModel.java,v 1.18 2006/07/29 05:53:37 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.RegistryObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.ui.common.UIUtility;
import org.freebxml.omar.client.ui.common.conf.bindings.Configuration;
import org.freebxml.omar.client.ui.common.conf.bindings.MethodParameter;
import org.freebxml.omar.client.ui.common.conf.bindings.ObjectTypeConfig;
import org.freebxml.omar.client.ui.common.conf.bindings.ObjectTypeConfigType;
import org.freebxml.omar.client.ui.common.conf.bindings.SearchResultsColumnType;
import org.freebxml.omar.client.ui.common.conf.bindings.SearchResultsConfigType;
import org.freebxml.omar.client.xml.registry.infomodel.ConceptImpl;








import org.freebxml.omar.common.BindingUtility;


/**
 * A JTable that lists
 *
 * @author Jim Glennon
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryObjectsTableModel extends AbstractTableModel
implements PropertyChangeListener {
    private static final Log log = LogFactory.getLog(RegistryObjectsTableModel.class);

    protected JavaUIResourceBundle resourceBundle = JavaUIResourceBundle.getInstance();

    ArrayList registryObjects = new ArrayList();
    RegistryBrowser registryBrowser;
    HashMap objectTypeToConfigMap = new HashMap();

    //Current configuration. Changes for each search
    ObjectTypeConfig otCfg;
    SearchResultsConfigType srCfg;
    List srCols;

    // column caches
    String cachedColumnNames[];
    Class cachedColumnClasses[];

    /**
     * Default Constructor
     */
    public RegistryObjectsTableModel() {
        registryBrowser = RegistryBrowser.getInstance();
        loadConfiguration();
    }

    private void loadConfiguration() {
        Configuration cfg = UIUtility.getInstance().getConfiguration();

        List otCfgs = cfg.getObjectTypeConfig();
        Iterator iter = otCfgs.iterator();

        while (iter.hasNext()) {
            ObjectTypeConfigType otCfg = (ObjectTypeConfigType) iter.next();
            String id = otCfg.getId();
            objectTypeToConfigMap.put(id, otCfg);
        }
    }

    /**
     * Get the number of columns in model
     *
     * @return number of columns
     *
     * @see
     */
    public int getColumnCount() {
        if (srCols != null) {
            return srCols.size();
        } else {
            return 0;
        }
    }

    /**
     * Get the width of column 'col', from cfg.
     *
     * @param col The column index.
     * @return int The column width.
     */
    public int getColumnWidth(int col) {
        SearchResultsColumnType srCol = (SearchResultsColumnType) srCols.get(col);
        int width = (srCol.getColumnWidth()).intValue();
        int cols = getColumnCount();
        return width;
    }

    /**
     * Gets the number of rows in the table model.
     *
     */
    public int getRowCount() {
        return registryObjects.size();
    }

    /**
     * Gets the Class for specified column based upon dynamic configuration.
     */
    public Class getColumnClass(int col) {
        if (cachedColumnClasses[col] != null) {
            return cachedColumnClasses[col];
        }

        Class clazz = null;

        try {
            SearchResultsColumnType srCol = (SearchResultsColumnType) srCols.get(col);
            String columnClass = srCol.getColumnClass();
            if (columnClass == null) {
                columnClass = "java.lang.Object";
            }
            clazz = Class.forName(columnClass);
        } catch (ClassNotFoundException e) {
            RegistryBrowser.displayError(e);
        }

        cachedColumnClasses[col] = clazz;
        return clazz;
    }

    /*
     * Gets whether a cell in table is editable or not.
     */
    public boolean isCellEditable(int row, int col) {
        boolean editable = false;

        SearchResultsColumnType srCol = (SearchResultsColumnType) srCols.get(col);
        editable = srCol.isEditable();

        return editable;
    }

    /**
     * Get value at specified cell in table.
     *
     * @param row the row for cell
     * @param col the column for cell
     *
     * @return value for cell
     *
     */
    public Object getValueAt(int row, int col) {
        RegistryObject ro = (RegistryObject) registryObjects.get(row);
        Object value = null;

        try {
            switch (col) {
                case -1:

                    //Special invisible column that contains the actual object
                    value = ro;

                    break;

                default:

                    SearchResultsColumnType srCol = (SearchResultsColumnType) srCols.get(col);
                    String className = otCfg.getClassName();
                    String methodName = srCol.getMethod();
                    Class clazz = Class.forName(className);

                    List params = srCol.getMethodParameter();
                    int numParams = params.size();
                    Class[] parameterTypes = new Class[numParams];
                    Object[] parameterValues = new Object[numParams];
                    Iterator paramsIter = params.iterator();
                    int i = 0;

                    //Setup parameterTypes
                    while (paramsIter.hasNext()) {
                        MethodParameter mp = (MethodParameter) paramsIter.next();
                        String paramTypeName = mp.getType();
                        parameterTypes[i] = Class.forName(paramTypeName);

                        String paramValue = mp.getValue();
                        parameterValues[i] = paramValue;

                        i++;
                    }

                    Method method = clazz.getMethod(methodName, parameterTypes);
                    //System.err.println("row=" + row + " col=" + col + " class=" + clazz + " methodName=" + methodName);

                    //Invoke method to get Value as object. Convert the object to a format suitable for display
                    value = method.invoke(ro, parameterValues);
            }
        } catch (ClassNotFoundException e) {
            RegistryBrowser.displayError(resourceBundle.getString("error.classNotFound"),
            e);
        } catch (NoSuchMethodException e) {
            RegistryBrowser.displayError(resourceBundle.getString("error.noSuchMethod"),
            e);
        } catch (IllegalArgumentException e) {
            RegistryBrowser.displayError(e);
        } catch (IllegalAccessException e) {
            RegistryBrowser.displayError(e);
        } catch (InvocationTargetException e) {
            //Commented because we were getting weird swing NPEs
            //RegistryBrowser.displayError(e.getCause());
        } catch (ExceptionInInitializerError e) {
            RegistryBrowser.displayError(e);
        }

        return value;
    }

    /**
     * Method Declaration.
     *
     *
     * @param col
     *
     * @return
     *
     * @see
     */
    public String getColumnName(int col) {
        if (cachedColumnNames[col] != null) {
            return cachedColumnNames[col];
        }

        String columnName = null;
        String localizedColumnName = null;

        try {
            columnName = ((SearchResultsColumnType) srCols.get(col)).getColumnHeader();
            localizedColumnName = resourceBundle.getString("columnName." +
            columnName.replaceAll(" ", ""));
        } catch (MissingResourceException ex) {
            System.out.println("Missed: " + columnName);

            localizedColumnName = columnName;
        } catch (IndexOutOfBoundsException ex) {
            log.warn(resourceBundle.getString("error.outOfBounds") + ex.getMessage());
        }

        cachedColumnNames[col] = localizedColumnName;
        return localizedColumnName;
    }

    void update(BulkResponse response) {
        Collection registryObjects = new ArrayList();
        try {
            // check for errors
            Collection exceptions = response.getExceptions();
            if (exceptions != null) {
                String errMsg = resourceBundle.getString("error.registryRequest");
                Iterator iter = exceptions.iterator();
                Exception exception = null;
                while (iter.hasNext()) {
                    exception = (Exception) iter.next();
                    RegistryBrowser.displayError(errMsg, exception);
                }
            }

            // check for objects
            // collection may be empty if there were errors
            registryObjects.addAll(response.getCollection());
            //Get the most specific object type that is common to all RegistryObjects
            Concept commonObjectType = UIUtility.getInstance().getCommonObjectType(registryObjects);
            //Dynamically update model configuration basd upon objectType
            updateConfiguration(commonObjectType);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        if (registryObjects.isEmpty()) {
            JOptionPane.showMessageDialog(null,
            resourceBundle.getString("message.noObjects"),
            resourceBundle.getString("title.registryBrowser.java"),
            JOptionPane.INFORMATION_MESSAGE);
        }

        setRegistryObjects(registryObjects);
    }

    /*
     * used by table inside action listener
     */

    /**
     * Method Declaration.
     *
     *
     * @return
     *
     * @see
     */
    ArrayList getRegistryObjects() {
        return registryObjects;
    }

    /**
     * Method Declaration.
     *
     *
     * @param objs
     *
     * @see
     */
    void setRegistryObjects(Collection objs) {
        registryObjects.clear();
        registryObjects.addAll(objs);
        fireTableDataChanged();
    }

    /**
     * Update the objectType specific configuration based upon specified objectType.
     * Dynamically called each time a update is called.
     */
    private void updateConfiguration(Concept objectType)
    throws JAXRException {
        // reset object list
        setRegistryObjects(new ArrayList());

        //Get the most specific ObjectTypeConfig for the most specific commonObjectType
        otCfg = UIUtility.getInstance().getObjectTypeConfig(objectType);
        srCfg = otCfg.getSearchResultsConfig();
        srCols = srCfg.getSearchResultsColumn();

        // reset cached column names and types
        int numCols = getColumnCount();
        cachedColumnNames = new String[numCols];
        cachedColumnClasses = new Class[numCols];

        fireTableStructureChanged();
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
        // reset cached column names
        int numCols = getColumnCount();
        cachedColumnNames = new String[numCols];
    }
}
