/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2007 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/thin/ExportBean.java,v 1.2 2007/04/25 20:43:26 psterk Exp $
 * ====================================================================
 */

package org.freebxml.omar.client.ui.thin;

import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.xml.registry.Query;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Slot;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CanonicalConstants;
import org.freebxml.omar.common.RepositoryItem;

/**
 *
 * @author psterk
 */
public class ExportBean {
    
    private static Log log = LogFactory.getLog(ExportBean.class);
    private ScrollerBean scrollerBean = null;
    private Map queryParams = new HashMap();
    private String zipFileName = null;
    private File zipFile = null;
    
    /** 
     * Default constructor. Creates a new instance of ExportBean 
     */
    public ExportBean() {
    }
    
    /**
     * This method is used to get the instance of ExportBean from the session
     * context.
     *
     * @return org.freebxml.omar.client.ui.thin.ExportBean
     * Return the ExportBean instance from the session
     */
    public static ExportBean getInstance() throws Exception {
        ExportBean exportBean = 
            (ExportBean)FacesContext.getCurrentInstance()
                                                      .getExternalContext()
                                                      .getSessionMap()
                                                      .get("exportBean");
        if (exportBean == null) {
            exportBean = new ExportBean();
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getSessionMap()
                        .put("exportBean", exportBean);
        }
        return exportBean;
    }
    
    /**
     * This method is used to export a collection of selected objects from
     * the search results table.
     * 
     * @return java.lang.String
     * Return status string: used by JSF for page navigation
     */
    public String doExport() {
        String status = "failure";
        // Reset the zipFileName class member variable
        zipFileName = null;
        try {
            // Execute a compressed content filter query to get a zip file of content
            Collection filterQueryIds = new ArrayList();
            filterQueryIds.add(BindingUtility.FREEBXML_REGISTRY_FILTER_QUERY_COMPRESSCONTENT);
            queryParams.put("$queryFilterIds", filterQueryIds);
            queryParams.put(CanonicalConstants.CANONICAL_SEARCH_DEPTH_PARAMETER, 
                            SearchPanelBean.getInstance().getSearchDepth());
            Iterator beanItr = getAllSelectedRegistryObjectBeans().iterator();
            List ids = new ArrayList();
            while (beanItr.hasNext()) {
                RegistryObjectBean rob = (RegistryObjectBean)beanItr.next();
                if (rob.getObjectType().equalsIgnoreCase("ExtrinsicObject")) {
                    ids.add(rob.getId());
                }
            }
            // Execute an arbitrary query with selected ids
            String queryId = CanonicalConstants.CANONICAL_QUERY_ArbitraryQuery;
            String sqlString = getExportSQLString(ids);
            queryParams.put(CanonicalConstants.CANONICAL_SLOT_QUERY_ID, queryId);
            queryParams.put("$query", sqlString);
            Query query = RegistryBrowser.getDQM().createQuery(Query.QUERY_TYPE_SQL);
            BulkResponse bResponse = RegistryBrowser.getDQM().executeQuery(query, queryParams);
            Collection registryObjects = bResponse.getCollection();
            // There should just be one EO containing the zip file
            Iterator roItr = registryObjects.iterator();
            if (roItr.hasNext()) {
                Object obj = roItr.next();
                if (obj instanceof ExtrinsicObjectImpl) {
                    ExtrinsicObjectImpl eo = (ExtrinsicObjectImpl)obj;
                    // Get the zip filename and set it to this.zipFileName
                    Slot filenameSlot = eo.getSlot(BindingUtility.FREEBXML_REGISTRY_FILTER_QUERY_COMPRESSCONTENT_FILENAME);
                    if (filenameSlot != null) {
                        Iterator filenameItr = filenameSlot.getValues().iterator();
                        if (filenameItr.hasNext()) {
                            zipFileName = (String)filenameItr.next();
                        }
                    }
                    status = "success";
                } else {
                    String msg = WebUIResourceBundle.getInstance()
                                                    .getString("message.ExpectedExtrinsicObject", 
                                                                new Object[]{obj});
                    RegistryObjectCollectionBean.getInstance().append(msg);
                    status = "showExportPage";
                }
            } else {
                String msg = WebUIResourceBundle.getInstance()
                                                .getString("message.extrinsicObjectWithNoRI");
                RegistryObjectCollectionBean.getInstance().append(msg);
                status = "showExportPage";
            }
        } catch (Throwable t) {
            OutputExceptions.error(log, t);
        } finally {
            try {
                queryParams.clear();
            } catch (Throwable t) {
                OutputExceptions.warn(log, t);
            }
        }
        return status;
    }
    
    /**
     * This method is used to retrieve the query parameters
     *
     * @return java.util.Map
     * A Map of query parameters
     */
    public Map getQueryParams() {
        return this.queryParams;
    }
   
    /**
     * This method is used to set the name of the zip filename of compressed
     * content.
     * 
     * @param java.lang.String zipFileName
     * The name of the zip file name
     */
    public void setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
    }
    
    /**
     * This method is used to get the name of the zip filename
     *
     * @return java.lang.String
     * 
     */
    public String getZipFileName() {
        return zipFileName;
    }
    
    /*
     * This method is used to write the contents of the zip filename to an 
     * OutputStream. This method is used by the ExportFileDownload.jsp page to
     * present the zip file to the user.
     */
    public void doWriteZipFile(OutputStream os) {        
        try {
            if (zipFileName == null) {
                String msg = WebUIResourceBundle.getInstance().getString("message.nullFileName");
                OutputExceptions.error(log, msg, null);
            } else {
                File tempZipFile = new File(System.getProperty("java.io.tmpdir"), zipFileName);
                if (zipFile == null) {
                    zipFile = tempZipFile;
                    writeToOutputStream(zipFile, os);
                } else {
                    if (zipFile.equals(tempZipFile)) {
                        writeToOutputStream(zipFile, os);
                    } else {
                        try {
                            zipFile.delete();
                        } catch (Throwable t) {
                            // Ignore
                        }
                        zipFile = tempZipFile;
                        writeToOutputStream(zipFile, os);
                    }
                }
            }            
        } catch (Throwable t) {
            OutputExceptions.error(log, t);
        }
    }
    
    private void writeToOutputStream(File zipFile, OutputStream os) {
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zipFile));
            byte [] buffer = new byte [1080];
            int n;
            while ((n = bis.read(buffer)) > -1) {
                os.write(buffer, 0, n);
            }
        } catch (Throwable t) {
            OutputExceptions.error(log, t);
        }
    }
    
    /**
     * This method is used to get the ScrollerBean instance used to store
     * the RegistryObjects selected for export.
     *
     * @return org.freebxml.omar.client.ui.thin.ScrollerBean
     * The ScrollerBean instance
     */
    public ScrollerBean getScrollerBean() {
        if (scrollerBean == null) {
            scrollerBean = new ScrollerBean();
        }    
        return scrollerBean;
    }
    
    /**
     * This method is used to indicate if any RegistryObjectBeans have been
     * selected for export.
     * 
     * @return boolean
     * 'true' if at least one RegistryObjects has been selected. 'false', otherwise
     */
    public boolean isRegistryObjectBeanSelected() {
        int num = getNumberAllSelectedRegistryObjectBeans();
        if (num == 0) {
            return false;
        } else {
            return true;
        }
    } 
    
    /**
     * This method is used to calculate the number of RegistryObjectBeans that 
     * have been selected for export
     *
     * @return int
     * The number of RegistryObjects selected for export
     */
    public int getNumberAllSelectedRegistryObjectBeans() {
        return getAllSelectedRegistryObjectBeans().size();
    }
    
    /**
     * This method is used to get all selected RegistryObjectBeans, including
     * bookmarked objects.
     *
     * @return java.util.Collection
     * A Collection of RegistryObjectBeans
     */
    public Collection getAllSelectedRegistryObjectBeans() {
        Collection allSelected = (Collection)getSelectedRegistryObjectBeans();
        UIData data = RegistryObjectCollectionBean.getInstance().getPinnedScrollerBean().getData();
        int n = (data != null)?data.getRowCount():0;
        if (n > 0) {
            ArrayList robList = (ArrayList)data.getValue(); 
            if (robList != null) {
                for (int i = 0; i < n; i++) {
                    data.setRowIndex(i);
                    RegistryObjectBean rob = ((RegistryObjectBean)robList.get(i));
                    if (rob.isPinned() && 
                        rob.getObjectType().indexOf("ExtrinsicObject") != -1) {
                        // Need to verify RO is an EO                      
                        allSelected.add(rob);
                    }
                }
            }
        }
        return allSelected;
    }
      
    /**
     * This method is used to return the CSS class used to indicate selected
     * members of the export table
     *
     * @return java.lang.String
     * A string containing the CSS class
     */
    public String getRowClasses() {
        return RegistryObjectCollectionBean.getInstance().getRowClasses();
    }
    
    /*
     * This method is used to get the SQL string for querying for selected
     * RegistryObjects for export
     */
    private String getExportSQLString(Collection ids) {
        StringBuffer sb = new StringBuffer("SELECT * FROM ExtrinsicObject WHERE id IN (");
        Iterator idItr = ids.iterator();
        for (int i = 0; idItr.hasNext(); i++) {
            String id = (String)idItr.next();
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("'").append(id).append("'");
        }
        sb.append(")");
        return sb.toString();
    }
  
    /*
     * This method is used to get the RegistryObjectBeans that are selected
     * for export.
     */
    private List getSelectedRegistryObjectBeans() {
        List selectedRobs = new ArrayList();
        UIData data = RegistryObjectCollectionBean.getInstance().getScrollerBean().getData();
        if (data != null) {
            int n = data.getRowCount();
            ArrayList robList = (ArrayList)data.getValue();
            if (robList != null) {
                for (int i = 0; i < n; i++) {
                    data.setRowIndex(i);
                    RegistryObjectBean rob = ((RegistryObjectBean)robList.get(i));
                    if (rob.isSelected() && 
                        rob.getObjectType().indexOf("ExtrinsicObject") != -1) {
                        // Need to verify RO is an EO
                        selectedRobs.add(rob);
                    }
                }
            }
        }
        return selectedRobs;
    }    
    
}
