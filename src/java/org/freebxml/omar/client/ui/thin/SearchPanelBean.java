/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/thin/SearchPanelBean.java,v 1.85 2007/04/19 16:46:48 psterk Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.thin;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.RegistryEntry;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Slot;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.event.ValueChangeEvent;

import org.freebxml.omar.client.ui.thin.jsf.ClassSchemeGraphBean;
import org.freebxml.omar.client.ui.thin.jsf.ExplorerGraphBean;
import org.freebxml.omar.client.ui.thin.components.components.QueryPanelComponent;
import org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.QueryImpl;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CanonicalSchemes;
import org.freebxml.omar.common.IterativeQueryParams;

import javax.servlet.http.HttpSession;
import org.freebxml.omar.common.CanonicalConstants;
import org.freebxml.omar.common.RepositoryItem;

/**
  *
  * @author  dhilder
  */
public class SearchPanelBean implements Serializable {
    
    private Log log = LogFactory.getLog(SearchPanelBean.class);

    private ClassSchemeGraphBean classSchemeGraphBean = null;
    private ClassSchemeGraphBean classSchemeSelector = null;
    private ExplorerGraphBean explorerGraphBean = null;    
    private String searchPanel = null;
    private String explorePanel = null;    
    private List objectTypes = null;
    private boolean useIterQueries = true;
    private QueryPanelComponent queryComponent;
    private boolean federatedQuery = false;
    private boolean isCompressContent = false;
    private String searchDepth = "0";
    private String maxSearchDepth = "4";
   
    /** 
      */
    public SearchPanelBean() throws Exception {
        try {
            String iterQueryStr = ProviderProperties.getInstance()
                                                      .getProperty("omar.client.browser.useIterativeQueries", "true");
            useIterQueries = Boolean.valueOf(iterQueryStr).booleanValue();
            searchDepth = ProviderProperties.getInstance()
                                            .getProperty("omar.client.thinbrowser.compressContent.defaultDepthLevel", "0");
            maxSearchDepth = ProviderProperties.getInstance()
                                               .getProperty("omar.client.thinbrowser.compressContent.maxDepthLevel", "4");
        }
        catch (Throwable t) {
            log.warn(WebUIResourceBundle.getInstance().getString("message.ExceptionOccurredWhileInitializingSearchPanelBean"), t);
            throw new Exception(WebUIResourceBundle.getInstance().getString("excpInitializingSearchPanel"), t);
        }
    }
    
    public static SearchPanelBean getInstance() throws Exception {
        SearchPanelBean searchBean = 
            (SearchPanelBean)FacesContext.getCurrentInstance()
                                                      .getExternalContext()
                                                      .getSessionMap()
                                                      .get("searchPanel");
        if (searchBean == null) {
            searchBean = new SearchPanelBean();
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getSessionMap()
                        .put("searchPanel", searchBean);
        }
        return searchBean;
    }
   
    public QueryBean getCurrentQuery() {
        QueryBean qb = null;
        try {
            qb = getQueryComponent().getCurrentQuery();
        } catch (Throwable t) {
            String msg = WebUIResourceBundle.getInstance().getString("registrySupport");
            OutputExceptions.error(log, msg, t);        
        }
        return qb;
    }
    
    /*
     * This method is called when "Create a New Object" Task is initiated.
     *
     */
    public Collection getObjectTypes() {
        List types = new ArrayList();
        try {
            String classSchemeId = CanonicalSchemes.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType;
            
            ClassificationScheme scheme = (ClassificationScheme)RegistryBrowser.getBQM()
                    .getRegistryObject(classSchemeId, LifeCycleManager.CLASSIFICATION_SCHEME);
            if (scheme == null) {
                log.warn("Could not get ClassificationScheme: "+ classSchemeId);
            } else {
                Collection concepts = scheme.getChildrenConcepts();
                getQueryComponent().loadTypes(concepts, types, "");
                objectTypes = types;
            }
        } catch (Throwable t) {
            log.error("Could not load ObjectType class scheme", t);
        }
        return types;
    }
    
    private BulkResponse executeQuery(Query query, Map queryParams) 
        throws JAXRException {
        return executeQuery(query, queryParams, new IterativeQueryParams());
    }
    
    private BulkResponse executeQuery(Query query, 
                                      Map queryParams, 
                                      IterativeQueryParams iqParams) 
        throws JAXRException {
        BulkResponse bulkResponse = null;
        try {
            DeclarativeQueryManagerImpl dqm = RegistryBrowser.getDQM();
            // Use search depth parameter for all queries. Support can be
            // added incrementally on server side for depth-related requests.
            queryParams.put(CanonicalConstants.CANONICAL_SEARCH_DEPTH_PARAMETER, getSearchDepth());
            if (isCompressContent) {
                ExportBean.getInstance().setZipFileName(null);
                Collection filterQueryIds = new ArrayList();
                filterQueryIds.add(BindingUtility.FREEBXML_REGISTRY_FILTER_QUERY_COMPRESSCONTENT);
                queryParams.put("$queryFilterIds", filterQueryIds);
            }
            bulkResponse = dqm.executeQuery(query, queryParams, iqParams);
            Collection exceptions = bulkResponse.getExceptions();                    
            //TO DO: forward exceptions to an error JSP
            if (exceptions != null) {
                Iterator iter = exceptions.iterator();
                Exception exception = null;
                StringBuffer sb = new StringBuffer(WebUIResourceBundle.getInstance().getString("errorExecQuery"));
                while (iter.hasNext()) {
                    exception = (Exception) iter.next();
                }                
                log.error("\n"+exception.getMessage());
                throw new JAXRException(sb.toString());
            } else {
                if (isCompressContent) {
                    handleCompressedContent(bulkResponse);
                }
            }
        } 
        catch (Throwable t) {
            log.error(WebUIResourceBundle.getInstance().getString("message.ErrorDuringRequestProcessing"), t);
            throw new JAXRException(WebUIResourceBundle.getInstance().getString("errorRequestProcessing") + t.getMessage());
        }
        return bulkResponse;
    }

    /*
     * This method is used to get the name of the zip file from the containing
     * ExtrinsicObject
     */
    private void handleCompressedContent(BulkResponse bulkResponse) 
        throws JAXRException {
        try {
            Iterator itr = bulkResponse.getCollection().iterator();
            if (itr.hasNext()) {
                RegistryObject ro = (RegistryObject)itr.next();
                if (ro != null) {
                    Slot fileNameSlot = ro.getSlot(BindingUtility.FREEBXML_REGISTRY_FILTER_QUERY_COMPRESSCONTENT_FILENAME);
                    if (fileNameSlot != null) {
                        Iterator slotItr = fileNameSlot.getValues().iterator();
                        if (slotItr.hasNext()) {
                            String filename = (String)slotItr.next();
                            ExportBean.getInstance().setZipFileName(filename);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            throw new JAXRException(t);
        }
    }
            
    public List getQuerySelectItems() {

        List querySelectItems = (List)FacesContext.getCurrentInstance()
                                                .getExternalContext()
                                                .getRequestMap()
                                                .get("querySelectItems");
        if (querySelectItems == null) {
            try{
                querySelectItems = new ArrayList();
                Iterator beanItr = getQueryComponent().getQueries().values().iterator();
                while (beanItr.hasNext()) {
                    QueryBean bean = (QueryBean)beanItr.next();
                    querySelectItems.add(new SelectItem(bean.getName()));
                }
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRequestMap()
                        .put("querySelectItems", querySelectItems);
            } catch (Throwable t) {                
                String msg = WebUIResourceBundle.getInstance().getString("registrySupport");
                OutputExceptions.error(log, msg, t);
            }
        }
        return querySelectItems;
    }
    
    public void querySelectionChanged(ValueChangeEvent event) throws Exception {
        String queryName = (String)event.getNewValue();
        // iterate through queries and find query with name matching 'queryName'
        Map queries = getQueryComponent().getQueries();
        if (queryName != null) {
            Iterator itr = queries.values().iterator();
            String queryId = null;
            while (itr.hasNext()) {
                QueryBean bean = (QueryBean)itr.next();
                if (bean.getName().equals(queryName)) {
                    queryId = bean.getQueryId();
                    getQueryComponent().setCurrentQuery(bean);
                    if (getQueryComponent().getPanelRegistry().get(queryId) == null) {
                        getQueryComponent().createQueryPanel(queryId);
                    }
                    break;
                }
            }
            getQueryComponent().renderQueryPanel(queryId);
            doClear();
        }
    }

    // TODO: is this method needed?
    public String getQueryPanelPage() throws Exception {
        String queryPanelPage = getQueryComponent().getCurrentQuery().getQueryId() + ".jsp";
        queryPanelPage = queryPanelPage.substring(queryPanelPage.lastIndexOf(":") + 1);
        return queryPanelPage;
    }
    
    public String doSearch() {
        String status = "unknown";
        RegistryBrowser.getInstance().setSessionExpired(false);
        try {
            Map parameters = getQueryComponent().getCurrentQuery().getQueryParameters();
            Query query = RegistryBrowser.getDQM().createQuery(Query.QUERY_TYPE_SQL);
            ((QueryImpl)query).setFederated(federatedQuery);
            int maxResults = RegistryObjectCollectionBean.getInstance()
                                                         .getScrollerBean()
                                                         .getNumberOfSearchResults();
            BulkResponse br = null;
            if (!RegistryBrowser.getInstance().isExploreRendered()) {
                getExplorerGraphBean().loadRegistryObjects(
                        RegistryObjectCollectionBean.getInstance().getNode());
            } else if (useIterQueries) {
                // The max results returned is the number of rows in the RegistryObjects
                // table times the number of pages that are displayed. Currently, 10
                // pages are displayed.  If the user scrolls past the tenth page, a new
                // iterative query is executed
                maxResults = maxResults * 10;
                IterativeQueryParams iqParams = new IterativeQueryParams(0, maxResults);
                br = executeQuery(query, parameters, iqParams);
                RegistryObjectCollectionBean.getInstance().handleRegistryObjects(br);
            } else {
                br = executeQuery(query, parameters);
                RegistryObjectCollectionBean.getInstance().handleRegistryObjects(br);
            }
            if (isCompressContent) {
                status = "compressContent";
            } else {
                status = "searchSuccessful";
            }
        } catch(JAXRException jaxre) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                               WebUIResourceBundle.getInstance().
                                                   getString("message.AnExceptionOccurredDuringTheSearch"), 
                                                   null));
            status = "searchFailed";
            String msg = WebUIResourceBundle.getInstance().getString("registrySupport");
            OutputExceptions.error(log, msg, jaxre);            
        } catch (Throwable t) {
            log.error(WebUIResourceBundle.getInstance().getString("message.AnExceptionOccurredDuringTheSearch"), t);
            status = "searchFailed";
            t.printStackTrace();
            
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                               WebUIResourceBundle.getInstance().
                                                   getString("message.AnExceptionOccurredDuringTheSearch"), 
                                                   null));
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                               WebUIResourceBundle.getInstance().
                                                   getString("checkLogForDetails"), 
                                                   null));
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "< " + t + " >", null));
        }
        return status;
    }   
    
    public void doSearchAppendResultsToResultSet(IterativeQueryParams iqParams) {
        try {
            Map parameters = getQueryComponent().getCurrentQuery().getQueryParameters();
            Query query = RegistryBrowser.getDQM().createQuery(Query.QUERY_TYPE_SQL);
            BulkResponse br = null;
            if (useIterQueries) {
                br = executeQuery(query, parameters, iqParams);
            } else {
                br = executeQuery(query, parameters);
            }
            RegistryObjectCollectionBean.getInstance().appendRegistryObjects(br);
        }
        catch (Throwable t) {
            log.error(WebUIResourceBundle.getInstance().getString("message.AnExceptionOccurredDuringTheSearch"), t);
            t.printStackTrace();
        }
    }
 
    
    public Locale getLocale() {
        UserPreferencesBean userPreferencesBean =
            (UserPreferencesBean)FacesContext.getCurrentInstance()
            .getExternalContext().getSessionMap().get("userPreferencesBean");
        return userPreferencesBean.getContentLocale();
    }

    public String getCharset() {
        UserPreferencesBean userPreferencesBean =
            (UserPreferencesBean)FacesContext.getCurrentInstance()
            .getExternalContext().getSessionMap().get("userPreferencesBean");
        return userPreferencesBean.getCharset();
    }
   
    public String doClear() throws Exception {
        RegistryBrowser.getInstance().setSessionExpired(false);
        if (classSchemeGraphBean != null) {
            classSchemeGraphBean.clearSelectedNodes();
        }
        Map params = getQueryComponent().getCurrentQuery().getParameters();
        Iterator keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String)keys.next();
            ParameterBean paramBean = (ParameterBean)params.get(key);
            paramBean.setBooleanValue(null);
            paramBean.setTextValue(null);
            paramBean.setListValue(null);
        }
        RegistryObjectCollectionBean.getInstance().doClear();
        return "showSearchResultsPage";
    }
    
    public String getInactivityLength() {
        HttpSession session = 
            (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        int inactiveInterval = session.getMaxInactiveInterval();
        // convert seconds to minutes
        inactiveInterval = inactiveInterval / 60;
        return String.valueOf(inactiveInterval);
    }
    
    public ClassSchemeGraphBean getClassSchemeGraphBean() throws Exception {
        if (classSchemeGraphBean == null) {
            classSchemeGraphBean = new ClassSchemeGraphBean(this);
        }
        return classSchemeGraphBean;
    }
    
     public void setClassSchemeGraphBean(ClassSchemeGraphBean classSchemeGraphBean) 
        throws Exception {
        this.classSchemeGraphBean = classSchemeGraphBean;
    }
     
    public ExplorerGraphBean getExplorerGraphBean() throws Exception {
        if (explorerGraphBean == null) {
            explorerGraphBean = new ExplorerGraphBean();
        }
        return explorerGraphBean;
    }
  
    public void setSearchResults(List list){}
    
    public List getRegistryEntryStatusCodes() {
        ArrayList list = new ArrayList();

        list.add(new SelectItem((new Integer(RegistryEntry.STATUS_APPROVED)), 
                                    CanonicalSchemes.CANONICAL_STATUS_TYPE_CODE_Approved));
        list.add(new SelectItem((new Integer(RegistryEntry.STATUS_DEPRECATED)), 
                                    CanonicalSchemes.CANONICAL_STATUS_TYPE_CODE_Deprecated));
        list.add(new SelectItem((new Integer(RegistryEntry.STATUS_WITHDRAWN)), 
                                    CanonicalSchemes.CANONICAL_STATUS_TYPE_CODE_Withdrawn));
        list.add(new SelectItem((new Integer(RegistryEntry.STATUS_SUBMITTED)), 
                                    CanonicalSchemes.CANONICAL_STATUS_TYPE_CODE_Submitted));
        return list;
    }

    public ClassSchemeGraphBean getClassSchemeSelector() throws Exception {
        if (classSchemeSelector == null) {
            classSchemeSelector = new ClassSchemeGraphBean(this);
        }
        return classSchemeSelector;
    }
    
    public void setClassSchemeSelector(ClassSchemeGraphBean classSchemeSelector) 
        throws Exception {
        this.classSchemeSelector = classSchemeSelector;
    }
    
    public void clearClassSchemeSelector() {
        classSchemeSelector = null;
    }
   
    public boolean getMenuDisplayTree() throws Exception {
        return getQueryComponent().getMenuDisplayTree();
    }



    /**
     * Getter for property queryComponent.
     * @return Value of property queryComponent.
     */
    public QueryPanelComponent getQueryComponent() throws Exception {
        if (null == queryComponent) {
            try {
                queryComponent = new QueryPanelComponent();
                queryComponent.init();
            } catch (Throwable t) {                
                String msg = WebUIResourceBundle.getInstance().getString("registrySupport");
                OutputExceptions.error(log, msg, t);
            }
        }
        return this.queryComponent;
    }

    /**
     * Setter for property queryComponent.
     * @param queryComponent New value of property queryComponent.
     */
    public void setQueryComponent(QueryPanelComponent queryComponent) {
        this.queryComponent = queryComponent;
    }
    
    public boolean isFederatedQuery() {
        return federatedQuery;
    }        
    
    public void setFederatedQuery(boolean federatedQuery) {
        this.federatedQuery = federatedQuery;
    }
    
    /**
     * This method is used to get a boolean indicating that compressed content 
     * is requested. This method is used by the SearchPanel.jsp page
     *
     * @return boolean
     * Return 'true' if compressed content is request.
     */
    public boolean isCompressContent() {
        return isCompressContent;
    }
    
    /**
     * This method is used to set a boolean indicating that compressed content
     * is requested.
     *
     * @param boolean isCompressContent
     * Pass 'true' if compressed content is requested.
     */
    public void setCompressContent(boolean isCompressContent) {
        this.isCompressContent = isCompressContent;
    }
    
    /**
     * This method gets the search depth that is used when 
     * compressing content
     * 
     * @return java.lang.String
     * The search depth
     */
    public String getSearchDepth() {
        return searchDepth;
    }
    
    /**
     * This method sets the search depth that is used when 
     * compressing content
     * 
     * @param searchDepth
     * A java.lang.String search depth
     */
    public void setSearchDepth(String searchDepth) {
        this.searchDepth = searchDepth;
    }
    
    /**
     * This method gets the SelectItems used by search depth drop down list
     *
     * @return java.util.List
     * A List of SelectItems
     */
    public List getSearchDepthItems() {
        List items = new ArrayList();
        int maxSearchDepthInt = Integer.parseInt(maxSearchDepth);
        for (int i = 0; i < maxSearchDepthInt; i++) {
            String depthStr = String.valueOf(i);
            items.add(new SelectItem(depthStr, depthStr+" "+"Levels"));
        }
        items.add(new SelectItem("-1", "All Levels"));
        return items;
    }

}
