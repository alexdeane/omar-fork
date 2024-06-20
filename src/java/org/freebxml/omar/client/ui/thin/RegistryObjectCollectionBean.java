/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/thin/RegistryObjectCollectionBean.java,v 1.149 2007/06/13 21:58:03 psterk Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.thin;

import java.io.Serializable;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Method;

import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.User;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.Concept;
import javax.activation.FileDataSource; 
import javax.activation.DataHandler; 
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.infomodel.LocalizedString;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.context.FacesContext;
import javax.faces.component.UIData;
import javax.faces.application.FacesMessage;
import org.freebxml.omar.client.xml.registry.infomodel.AuditableEventImpl;
import org.freebxml.omar.common.CanonicalSchemes;

import org.oasis.ebxml.registry.bindings.rim.impl.NotifyActionImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.ui.common.conf.bindings.ObjectTypeConfig;
import org.freebxml.omar.client.ui.common.conf.bindings.SearchResultsColumnType;
import org.freebxml.omar.client.ui.common.conf.bindings.SearchResultsConfigType;
import org.freebxml.omar.client.ui.common.UIUtility;
import org.freebxml.omar.client.ui.thin.components.model.RegistryObjectNode;
import org.freebxml.omar.client.xml.registry.BusinessLifeCycleManagerImpl;
import  org.freebxml.omar.client.xml.registry.infomodel.ClassificationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryObjectImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ExtensibleObjectImpl;
import org.freebxml.omar.client.xml.registry.infomodel.IdentifiableImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryPackageImpl;
import org.freebxml.omar.client.xml.registry.infomodel.UserImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ConceptImpl;
import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.QueryManagerImpl;
import org.freebxml.omar.client.xml.registry.BulkResponseImpl; 
import org.freebxml.omar.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ExternalLinkImpl;
import org.freebxml.omar.client.xml.registry.infomodel.SpecificationLinkImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ServiceBindingImpl;
import org.freebxml.omar.client.xml.registry.infomodel.PersonImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryImpl;
import org.freebxml.omar.client.xml.registry.infomodel.InternationalStringImpl;
import org.freebxml.omar.client.xml.registry.infomodel.OrganizationImpl;
import org.freebxml.omar.common.AbstractResourceBundle;
import org.freebxml.omar.common.CommonProperties;
import org.freebxml.omar.common.CanonicalConstants;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.exceptions.UnresolvedReferenceException;
import org.freebxml.omar.common.exceptions.ReferencesExistException;
import org.oasis.ebxml.registry.bindings.query.impl.AdhocQueryResponseImpl;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


/**
  *
  * @author  psterk
  */
public class RegistryObjectCollectionBean implements Serializable {
    
    private Log log = LogFactory.getLog(RegistryObjectCollectionBean.class);
    private List registryObjectBeans = null;
    private List relatedSearchResults;  
    private Map registryObjectLookup = new HashMap();
    private Map relatedRegistryObjectLookup = new HashMap();  
    private RegistryObjectBean currentRegistryObject = null;
    private RegistryObjectBean currentComposedRegistryObject = null;
    private String currentComposedType = null;
    private ScrollerBean scrollerBean = null;
    private ScrollerBean pinnedScrollerBean = null;
    private Iterator headersItr;
    private int numRegistryObjects = 0;
    private int headerIndex = 0;
    private StringBuffer rowClasses = new StringBuffer();
    private String newObjectType = "RegistryObject";
    private boolean disableSave = true;
    private int pinnedROBsize = 0;
    private int selectedROBsize = 0;
    private RelationshipBean relationshipBean = null; 
    private Set pinnedRegistryObjectBean = new HashSet();
    private List selectedRegistryObjectBean = new ArrayList();
    private List pivotalRegistryObjectBean = new ArrayList();  
    private String relationship =  null;   
    private String referenceSourceCode = null;
    private String referenceObjectTypeCode =  null;
    private String referenceTargetCode = null;
    private String sourceResult = null;
    private String targetResult = null;
    private String sourceType = null;
    private String referenceRelation = null;
    private String associationRelation = null;
    private String referenceAttribute = null;
    private String fileName = null;
    private RegistryObjectBean passROB = null;
    private boolean newComposedObject = false;
    private String relationshipName = "Slots";
    // by default, versioning is turned off
    private boolean isObjectVersioned = false;
    private Collection pseudoComposedRobsToDelete = null;
    private RegistryObjectNode node = null;
    private String statusTypeConceptId = "";
    private List refAttributeList = null;
    private List assoErroMessageList = null;
    
    /** cached list of SelectItems for events (+status) types */
    private List auditableEventType_SelectItems;
    /** cached list of SelectItems for status types */
    private List statusType_SelectItems = null;
    private String cachedROBId = null;
        
    /** 
      */
    public RegistryObjectCollectionBean() {
    }

    public static RegistryObjectCollectionBean getInstance() {
        RegistryObjectCollectionBean rocBean = 
            (RegistryObjectCollectionBean)FacesContext.getCurrentInstance()
                                                      .getExternalContext()
                                                      .getSessionMap()
                                                      .get("roCollection");
        if (rocBean == null) {
            rocBean = new RegistryObjectCollectionBean();
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getSessionMap()
                        .put("roCollection", rocBean);
        }
        return rocBean;
    }
   
    public void appendRegistryObjects(BulkResponse bResponse) throws Exception {
        this.doDeleteFile();
        List beans = createRegistryObjectBeans(bResponse.getCollection());
        createRegistryObjectBeansLookup(beans);
        setRegistryObjectBeans(beans); 
        try {
            RegistryResponseType resp = ((BulkResponseImpl)bResponse).getRegistryResponse();
            int maxResults = ((AdhocQueryResponseImpl)resp).getTotalResultCount().intValue();
            getScrollerBean().setTotalResultCount(maxResults);
        } catch (Throwable t) {
            String msg = WebUIResourceBundle.getInstance().getString("message.couldNotSetTotalResultCount");
            msg = msg + ". " + WebUIResourceBundle.getInstance().getString("checkLogForDetails");
            OutputExceptions.error(log, msg, t);
        }
    }
    
    public void handleRegistryObjects(BulkResponse bResponse) throws Exception {    
        handleRegistryObjects(bResponse.getCollection());
        try {
            RegistryResponseType resp = ((BulkResponseImpl)bResponse).getRegistryResponse();
            int maxResults = ((AdhocQueryResponseImpl)resp).getTotalResultCount().intValue();
            getScrollerBean().setTotalResultCount(maxResults);
        } catch (Throwable t) {
            String msg = WebUIResourceBundle.getInstance().getString("message.couldNotSetTotalResultCount");
            msg = msg + ". " + WebUIResourceBundle.getInstance().getString("checkLogForDetails");
            OutputExceptions.error(log, msg, t);
        }
    }
    
    public void handleRegistryObjects(Collection registryObjects) throws Exception {
        doClear();
        List beans = createRegistryObjectBeans(registryObjects);
        createRegistryObjectBeansLookup(beans);
        setRegistryObjectBeans(beans);
        getScrollerBean().setTotalResultCount(beans.size());
    }
    
    public void doClear() {
        scrollerBean = null;
        currentRegistryObject = null;
        currentComposedRegistryObject = null;
        clearRegistryObjectBeans();
        relatedSearchResults = null;
        passROB = null;
        node = null;
        relatedRegistryObjectLookup.clear();
        resetRowClasses();
        this.clearRelationObjects();
        this.relationshipBean = null;
        this.doDeleteFile();
        disableSave = true;
        this.assoErroMessageList = null;
    }
    
    private void clearRegistryObjectBeans() {
        if (registryObjectBeans != null) {
            List tempRobList = new ArrayList(registryObjectBeans);
            Iterator robItr = tempRobList.iterator();
            while (robItr.hasNext()) {
                RegistryObjectBean rob = (RegistryObjectBean)robItr.next();
                // Remove all ROBs except pinned ones
                if (!(pinnedRegistryObjectBean != null && pinnedRegistryObjectBean.contains(rob))|| !rob.isPinned()) {
                    registryObjectBeans.remove(rob);
                    try {
                        Object obj = registryObjectLookup.remove(rob.getId());
                        if (obj == null) {
                            registryObjectLookup.remove(rob.getRegistryAssignedId());
                        }
                    } catch (JAXRException ex) {
                        log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotGetIdFromRegistryObject"), ex);
                    }
                }
            }
            if (registryObjectBeans.size() == 0) {
                registryObjectBeans = null;
            }
        }
    }
    
    public ScrollerBean getScrollerBean() {
       if (scrollerBean == null) {
           scrollerBean = new ScrollerBean();
       }
       return scrollerBean;
   } 
    
    public void setScrollerBean(ScrollerBean scrollerBean) {
        this.scrollerBean = scrollerBean;
    }

    public ScrollerBean getPinnedScrollerBean() {
       if (pinnedScrollerBean == null) {
           pinnedScrollerBean = new ScrollerBean();
           pinnedScrollerBean.setTotalResultCount(getNumberOfPinnedRegistryObjectBeans());
       }
       return pinnedScrollerBean;
   }
    
    public void setPinnedScrollerBean(ScrollerBean pinnedScrollerBean) {
        this.pinnedScrollerBean = pinnedScrollerBean;
    }

    public List getRegistryObjectBeans() {
        return registryObjectBeans;
    }
    
    public void setRegistryObjectBeans(List roBeans) {        
        if (registryObjectBeans == null) {
            registryObjectBeans = new ArrayList();
        }
        getRegistryObjectBeans().addAll(roBeans);
    }

    public List getRelatedRegistryObjectBeans() {
        return relatedSearchResults;
    }
    
    public int getNumberRelatedRegistryObjectBeans() {
        List list = getRelatedRegistryObjectBeans();
        if (list == null) {
            return 0;
        } else {
            return list.size();
        }
    }
    
    public String getCurrentComposedRegistryObjectType() {
        return currentComposedType;
    }

    public Map getRelatedRegistryObjectLookup() {
        return relatedRegistryObjectLookup;
    }
    
    public int getNumberOfRelatedRegistryObjectBeans() {
        return relatedSearchResults.size();
    }

    public String setCurrentRegistryObjectBean() {
        Map requestMap = FacesContext.getCurrentInstance()
                                     .getExternalContext()
                                     .getRequestParameterMap();
        String id = (String)requestMap.get("idValue");
        if (id != null && ! id.equals("")) {
            setCurrentRegistryObjectBean(id);
            currentRegistryObject.initRelatedObjects();
        }
        return "";
    }

    /**
     * This method is used to temporarily set the current registry object bean to
     * the related object drilldown object. Since this setting is temporary,
     * this method does not save the current registry object. When the user
     * clicks on a Details Pane Tab, the tab's action listener will reset the
     * current registry object to the saved object.
     */ 
    public void setCurrentRelatedDrilldownRegistryObjectBean(ActionEvent event) {
        setCurrentRelatedDrilldownRegistryObjectBean();
    }
    
    public void setNewRelatedRegistryObjectBean() {
        Map requestMap = FacesContext.getCurrentInstance()
                                     .getExternalContext()
                                     .getRequestParameterMap();
        String id = (String)requestMap.get("drilldownIdValue");

            if (currentComposedRegistryObject == null) {
                try {
                    newComposedObject = true;
                    cleanupComposedROB();
                    doAddCurrentComposedROB();
                    id = currentComposedRegistryObject.getId();
                } catch (Throwable t) {
                    log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotGetId"), t);
                }
            }
        
        disableSave = true;
    }
    
    public void setCurrentRelatedDrilldownRegistryObjectBean() {
        Map requestMap = FacesContext.getCurrentInstance()
                                     .getExternalContext()
                                     .getRequestParameterMap();
        String id = (String)requestMap.get("drilldownIdValue");
        newComposedObject = false;

        if (id == null) {
            if (currentComposedRegistryObject == null) {
                try {
                    newComposedObject = true;
                    cleanupComposedROB();
                    doAddCurrentComposedROB();
                    id = currentComposedRegistryObject.getId();
                } catch (Throwable t) {
                    log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotGetId"), t);
                }
            }
        } else {
            currentComposedRegistryObject = (RegistryObjectBean)registryObjectLookup.get(id);            
            if (currentComposedRegistryObject == null) {
                try {
                    QueryManagerImpl qm = (QueryManagerImpl)RegistryBrowser.getBQM();
                    RegistryObject cRO = qm.getRegistryObject(id);
                    currentComposedRegistryObject =  new RegistryObjectBean(null, 
                                                                      cRO,
                                                                      false);
                    registryObjectLookup.put(id, currentComposedRegistryObject);
                } catch (Throwable t) {
                    log.error(WebUIResourceBundle.getInstance().getString("message.ErrorInRetrievingRegistryObject"), t);
                }
            }
            currentComposedRegistryObject.initRelatedObjects();
            
        }
        disableSave = true;
    }
    
    /* This method is used to set the current registry object for the 
     * details page. The current object is copied to a saved object. This 
     * maintains state for the details page.
     */
    public void setCurrentRegistryObjectBean(ActionEvent event) {
        Map requestMap = FacesContext.getCurrentInstance()
                                     .getExternalContext()
                                     .getRequestParameterMap();
        if (this.assoErroMessageList != null){
                this.assoErroMessageList.clear();
        }
        String id = (String)requestMap.get("idValue");
        if (id != null && ! id.equals("")) {
            //checkAssociationObjectValidity method will check for valid 
            //Association object. 
           if(this.checkAssociationObjectValidity(id, true)){ 
              setCurrentRegistryObjectBean(id);
              currentRegistryObject.initRelatedObjects();
              setRowClasses(currentRegistryObject);
           }
        }
    }

    public List getAssociationErrorMessage(){
        return assoErroMessageList;
    }
    public void setAssociationErrorMessage(String message){
        if (assoErroMessageList == null){
            assoErroMessageList = new ArrayList();
        }
        assoErroMessageList.add(message);
    }
    /**
     * This method is used to build the custom error message in case of dangling
     * references for Association Object.This method prepare a list of  messages
     * which is useful to display error message in multiple lines.
     * @param Registry object  
     * @return void
     */
    private void buildAssociationErrorMessage(RegistryObject ro) {
        String[] errorMessage = new String[5];
        String sourceMessage ="";
        String targetMessage ="";        
        try{
            errorMessage[0] = ro.getKey().getId();
            errorMessage[1] = ro.getObjectType().toString();
            errorMessage[2] = "";
            try{
                if(RegistryBrowser.getBQM().getRegistryObject(((Association)ro)
                        .getSourceObject().getKey().getId().trim()) == null ) {
                        errorMessage[2]=WebUIResourceBundle.getInstance().getString("sourceLabel");
                }else{
                        sourceMessage=WebUIResourceBundle.getInstance()
                        .getString("message.AssociationNotValid2", new Object[]{
                        ((Association)ro).getSourceObject().getKey().getId(),
                        ((Association)ro).getSourceObject().getObjectType().toString(),
                        ((Association)ro).getSourceObject().getName().toString()});
                }
            }catch(Exception ex){
                        errorMessage[2]=WebUIResourceBundle.getInstance().getString("sourceLabel");
            }
            try{
                if (RegistryBrowser.getBQM().getRegistryObject(((Association)ro)
                        .getTargetObject().getKey().getId().trim()) == null ) {
                        errorMessage[2]=WebUIResourceBundle.getInstance().getString("targetLabel");

                 }else{
                        targetMessage=WebUIResourceBundle.getInstance()
                        .getString("message.AssociationNotValid3", new Object[]{
                        ((Association)ro).getTargetObject().getKey().getId(),
                        ((Association)ro).getTargetObject().getObjectType().toString(),
                        ((Association)ro).getTargetObject().getName().toString()});
                 }
            }catch(Exception ex){
                        errorMessage[2]=WebUIResourceBundle.getInstance().getString("targetLabel");
            }
            //build error message list to display.
            if (this.assoErroMessageList != null){
                this.assoErroMessageList.clear();
            }
            this.setAssociationErrorMessage(WebUIResourceBundle.getInstance().getString("message.AssociationNotValid1", errorMessage));
            if(sourceMessage.length() > 0){
                this.setAssociationErrorMessage(sourceMessage);
            }
            if(targetMessage.length() > 0){
                this.setAssociationErrorMessage(targetMessage);
            }
            this.setAssociationErrorMessage(WebUIResourceBundle.getInstance().getString("message.AssociationNotValid4"));
        }catch(Exception ex){}            
         
    }
    /**
     * This method use to check the validity of Association Object by checking 
     * its source/target object still exist.In case an exception occurs like
     * UnresolvedReferenceException it build user friendly message to display on
     * UI and server log. 
     */
    private boolean checkAssociationObjectValidity(String id,boolean isCurrentRegistryObject){
        boolean status = true;
        RegistryObject ro = null;
        try {
            ro = RegistryBrowser.getBQM().getRegistryObject(id.trim());
            if (ro instanceof Association) {
                if (((Association)ro).getSourceObject() !=null && ((Association)ro).getTargetObject() !=null) {
                    status = true;
                    if(RegistryBrowser.getBQM().getRegistryObject(((Association)ro)
                        .getSourceObject().getKey().getId().trim()) == null || 
                        RegistryBrowser.getBQM().getRegistryObject(((Association)ro)
                            .getTargetObject().getKey().getId().trim()) == null ) {
                        throw new NullPointerException();
                    }
                }
            }
        }catch(UnresolvedReferenceException ure){
            try{
                if(isCurrentRegistryObject){
                    this.buildAssociationErrorMessage(ro);
                    this.currentRegistryObject = null;
                    log.warn(ure.getMessage());
                }         
            }catch(Exception ex){}
             status = false;
        }catch(NullPointerException npe){
            try{
                if(isCurrentRegistryObject){
                    this.buildAssociationErrorMessage(ro);
                    this.currentRegistryObject = null;
                    log.warn(npe.getMessage());
                }
            }catch(Exception ex){} 
             status = false;            
        } 
        catch(Exception ex){
            try{
            String msg = WebUIResourceBundle.getInstance()
            .getString("message.AssociationNotValid1", new Object[]{ro.getKey().getId(),
                    ro.getObjectType().toString(), WebUIResourceBundle.getInstance()
            .getString("message.SourceTarget")});
            OutputExceptions.warn(log, msg,msg, ex);
            }catch(Exception e){}
        }
        return status;
    }
    /* This method is called by the PaneComponent to reset the current 
     * registry object to the saved one. This maintains proper state after any
     * related object drilldowns.
     */
    public void resetCurrentComposedRegistryObjectBean() {
        currentComposedRegistryObject = null;
    }
  
    public void resetCurrentComposedRegistryObjectBean(ActionEvent event) {
        resetCurrentComposedRegistryObjectBean();
    }
    
    public Map getRegistryObjectLookup() {
        return registryObjectLookup;
    }
    
    public void setCurrentRegistryObjectBean(String id) {
        currentRegistryObject = (RegistryObjectBean)registryObjectLookup.get(id);
        if (currentRegistryObject == null) {
            try {
                // retrieve the object from database
                RegistryObject ro = (RegistryObject) RegistryBrowser.getDQM().getRegistryObject(id); 
                ArrayList registryObjects = new ArrayList();
                registryObjects.add(ro); 
                createRegistryObjectBeansLookup(createRegistryObjectBeans((Collection)registryObjects));
                currentRegistryObject = (RegistryObjectBean)registryObjectLookup.get(id);
                setRowClasses(currentRegistryObject);
            } 
            catch (Throwable t) {
                log.error(WebUIResourceBundle.getInstance().getString("message.ErrorInRetrievingRegistryObject"), t); 
            }
        }
        currentRegistryObject.setFirstAccess(true);
    }
 
    public RegistryObjectBean getCurrentDrilldownRegistryObjectBean() {
        return currentRegistryObject;
    }
    
    public RegistryObjectBean getCurrentRegistryObjectBean() {
        if (currentComposedRegistryObject == null) {
            return currentRegistryObject;
        } else {
            return currentComposedRegistryObject;
        }
        
    }

    public void setCurrentRelatedObjectsData(String relationshipName) {
        try {
            this.relationshipName = relationshipName;
            if (relationshipName.endsWith("es")) {
                int endIndex = relationshipName.length() - 2;
                currentComposedType = relationshipName.substring(0, endIndex);
            } else if (relationshipName.endsWith("s")) {
                int endIndex = relationshipName.length() - 1;
                currentComposedType = relationshipName.substring(0, endIndex);
            } else {
                currentComposedType = relationshipName;
            }
            // skip association types. They will be displayed on the 
            // 'Associations' tab.
            if (-1 != relationshipName.indexOf("Association\n")) {
                return;
            }
            //TODO: get rid of these separate method calls for non-RegistryObject types
            if (relationshipName.equals("Slots")) {
                relatedSearchResults = 
                    getSlotsSearchResultsBeans(getCurrentDrilldownRegistryObjectBean());
            }
            else if (relationshipName.equals("TelephoneNumbers") || relationshipName.equals("TelephoneNumber")) {
                relatedSearchResults = 
                    getTelephoneNumbersSearchResultsBeans(getCurrentDrilldownRegistryObjectBean());
            }         
            else if (relationshipName.equals("PostalAddresses") || relationshipName.equals("PostalAddress")) {
                relatedSearchResults = 
                    getPostalAddressesSearchResultsBeans(getCurrentDrilldownRegistryObjectBean());
            }
            else if (relationshipName.equals("EmailAddresses") || relationshipName.equals("EmailAddress")) {
                relatedSearchResults = 
                    getEmailAddressesSearchResultsBeans(getCurrentDrilldownRegistryObjectBean());
            }
            else if (relationshipName.equals("Associations")) {
                relatedSearchResults = 
                    getAssociationsSearchResultsBeans(getCurrentDrilldownRegistryObjectBean());
            }
            else if (relationshipName.equals("RegistryObjects")) {
                RegistryPackageImpl rpi = (RegistryPackageImpl)getCurrentDrilldownRegistryObjectBean().getRegistryObject();
                Collection registryObjects = rpi.getRegistryObjects();
                relatedSearchResults = createComposedRegistryObjectBeans(registryObjects);
                relatedSearchResults.addAll(getRegistryObjectsToAddToRegistryPackage());
            }
            else if (relationshipName.equals("AffectedObjects")) {
                AuditableEventImpl aei = (AuditableEventImpl)getCurrentDrilldownRegistryObjectBean().getRegistryObject();
                Collection registryObjects  = aei.getAffectedObjects();
                relatedSearchResults = createComposedRegistryObjectBeans(registryObjects);
            }
            else{
                Collection registryObjects = (Collection)getCurrentDrilldownRegistryObjectBean().getObjectTypeRefs(relationshipName); 
                relatedSearchResults = createComposedRegistryObjectBeans(registryObjects);
            }
            createRegistryObjectBeansLookup(relatedSearchResults);
        }
        catch(UnresolvedReferenceException ure){ }
        catch (Throwable t) {
            log.error(WebUIResourceBundle.getInstance().getString("message.AnExceptionOccurredDuringGetCurrentRelatedObjectsData"), t);
        }
    }
    
    private Collection getRegistryObjectsToAddToRegistryPackage() {
        List objs = new ArrayList();
        Iterator itr = registryObjectLookup.values().iterator();
        while (itr.hasNext()) {
            RegistryObjectBean rob = (RegistryObjectBean)itr.next();
            if (rob.isAddRoToRegistryPackage()) {
                rob.setRelatedSelected(true);
                objs.add(rob);
            }
        }
        return objs;
    }
    
    public void createRegistryObjectBeansLookup(List roBeans) {
        if (roBeans != null) {
            Iterator itr = roBeans.iterator();
            while (itr.hasNext()) {
                RegistryObjectBean rob = (RegistryObjectBean)itr.next();               
                try {
                    registryObjectLookup.put(rob.getId(), rob);
                } catch (JAXRException ex) {
                    log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotGetIdForObjectId"), ex);
                }
            }
        }
    }

    private List createComposedRegistryObjectBeans(Collection registryObjects)
        throws ClassNotFoundException, NoSuchMethodException,
        ExceptionInInitializerError, Exception {
        if (registryObjects == null) {
            return null;
        }
        int numObjects = registryObjects.size();
        List list = new ArrayList(registryObjects);
                  
        Iterator roItr = registryObjects.iterator();
        if (log.isDebugEnabled()) {
            log.debug("Query results: ");
        }
 
        int numCols = 5;
        // Replace ObjectType with Id. TODO - formalize this convention
        List roBeans = new ArrayList(numObjects);
        for (int i = 0; roItr.hasNext(); i++) {                  
            RegistryObject ro = (RegistryObject)roItr.next();
            String header = null;
            Object columnValue = null;
            List srvbHeader = new ArrayList(numCols);
            List searchResultValueBeans = new ArrayList(numCols);      
           
            header = WebUIResourceBundle.getInstance().getString("Details");
            columnValue = ro.getKey().getId(); 
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            header = WebUIResourceBundle.getInstance().getString("ObjectType");
            columnValue = ro.getObjectType().getValue();
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            header = WebUIResourceBundle.getInstance().getString("Name");
            columnValue = getLocalizedNameString(ro);

            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));         
 
            
            header = WebUIResourceBundle.getInstance().getString("Description");
            columnValue = getLocalizedDescriptionString(ro);
            if(columnValue == null) {
                if(ro instanceof ClassificationImpl) {
                    Concept concept = ((ClassificationImpl)ro).getConcept();
                    if(concept != null)
		        columnValue = concept.getPath();                    
                }
	    }
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            RegistryObjectBean srb =  new RegistryObjectBean(searchResultValueBeans,
                                                             ro,
                                                             false);
            roBeans.add(srb);
        }        
        return roBeans;
    }
    
    private List getSlotsSearchResultsBeans(RegistryObjectBean roBean) 
        throws ClassNotFoundException, NoSuchMethodException,
        ExceptionInInitializerError, Exception {
        Collection slots = roBean.getSlots();
        if (slots == null) {
            return null;
        }
        int numSlotObjects = slots.size();
        List list = new ArrayList(numSlotObjects);
                  
        Iterator roItr = slots.iterator();
        if (log.isDebugEnabled()) {
            log.debug("Query results: ");
        }
 
        String objectType = "Slot";
        int numCols = 5;
        // Replace ObjectType with Id. TODO - formalize this convention
        List roBeans = new ArrayList(numSlotObjects);
        for (int i = 0; roItr.hasNext(); i++) {                  
            Slot slot = (Slot)roItr.next();
            String header = null;
            Object columnValue = null;
            List srvbHeader = new ArrayList(numCols);
            List searchResultValueBeans = new ArrayList(numCols);      
           
            header = WebUIResourceBundle.getInstance().getString("Details");
            columnValue = roBean.getId()+"."+slot.hashCode();
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            header = WebUIResourceBundle.getInstance().getString("Name");
            columnValue = (Object)slot.getName();
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));         
 
            header = WebUIResourceBundle.getInstance().getString("Slot Type");
            columnValue = (Object)slot.getSlotType();
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            header = WebUIResourceBundle.getInstance().getString("Values");
            columnValue = (Object)slot.getValues();
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            RegistryObjectBean srb =  new RegistryObjectBean(searchResultValueBeans, 
                                                         roBean.getRegistryObject(),
                                                         objectType,
                                                         (Object)slot, 
                                                         false);  
            roBeans.add(srb);
        }        
        return roBeans;
    }

    private List getTelephoneNumbersSearchResultsBeans(RegistryObjectBean roBean)
        throws ClassNotFoundException, NoSuchMethodException,
        ExceptionInInitializerError, Exception {
        Collection telephoneNumbers = roBean.getTelephoneNumbers();
        if (telephoneNumbers == null) {
            return null;
        }
        int numTelephoneNumberObjects = telephoneNumbers.size();
        List list = new ArrayList(numTelephoneNumberObjects);

        Iterator roItr = telephoneNumbers.iterator();
        if (log.isDebugEnabled()) {
            log.debug("Query results: ");
        }

        String objectType = "TelephoneNumber";
        int numCols = 2;
        // Replace ObjectType with Id. TODO - formalize this convention
        List roBeans = new ArrayList(numTelephoneNumberObjects);
        for (int i = 0; roItr.hasNext(); i++) {
            TelephoneNumber telephoneNumber = (TelephoneNumber)roItr.next();
            String header = null;
            Object columnValue = null;
            List srvbHeader = new ArrayList(numCols);
            List searchResultValueBeans = new ArrayList(numCols);
           
            header = WebUIResourceBundle.getInstance().getString("Details");
            columnValue = roBean.getId()+"."+telephoneNumber.hashCode();      
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            header = WebUIResourceBundle.getInstance().getString("Country Code");
            columnValue = telephoneNumber.getCountryCode();      
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
           
            header = WebUIResourceBundle.getInstance().getString("Number");
            columnValue = "("+telephoneNumber.getAreaCode()+") "+telephoneNumber.getNumber();      
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            header = WebUIResourceBundle.getInstance().getString("Extension");
            columnValue = telephoneNumber.getExtension();
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));

            RegistryObjectBean srb =  new RegistryObjectBean(searchResultValueBeans, 
                                                         roBean.getRegistryObject(),
                                                         objectType,
							 (Object)telephoneNumber,
                                                         false);              
            roBeans.add(srb);
        }
        return roBeans;
    }
    
    private List getPostalAddressesSearchResultsBeans(RegistryObjectBean roBean)
        throws ClassNotFoundException, NoSuchMethodException,
        ExceptionInInitializerError, Exception {
        Collection postalAddresses = roBean.getPostalAddresses();
        if (postalAddresses == null) {
            return null;
        }
        int numPostalAddressObjects = postalAddresses.size();
        List list = new ArrayList(numPostalAddressObjects);

        Iterator roItr = postalAddresses.iterator();
        if (log.isDebugEnabled()) {
            log.debug("Query results: ");
        }

        String objectType = "PostalAddress";
        int numCols = 2;
        // Replace ObjectType with Id. TODO - formalize this convention
        List roBeans = new ArrayList(numPostalAddressObjects);
        for (int i = 0; roItr.hasNext(); i++) {
            PostalAddress postalAddress = (PostalAddress)roItr.next();
            String header = null;
            Object columnValue = null;
            List srvbHeader = new ArrayList(numCols);
            List searchResultValueBeans = new ArrayList(numCols);
           
            header = WebUIResourceBundle.getInstance().getString("Details");
            columnValue = roBean.getId()+"."+postalAddress.hashCode();      
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            header = WebUIResourceBundle.getInstance().getString("Street Number");
            columnValue = postalAddress.getStreetNumber();      
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
           
            header = WebUIResourceBundle.getInstance().getString("Street");
            columnValue = postalAddress.getStreet();      
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            header = WebUIResourceBundle.getInstance().getString("City");
            columnValue = postalAddress.getCity();
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));

            RegistryObjectBean srb =  new RegistryObjectBean(searchResultValueBeans, 
                                                         roBean.getRegistryObject(),
                                                         objectType,
                                                         (Object)postalAddress,
                                                         false);              
            roBeans.add(srb);
        }
        return roBeans;
    }
    
    private List getEmailAddressesSearchResultsBeans(RegistryObjectBean roBean)
        throws ClassNotFoundException, NoSuchMethodException,
        ExceptionInInitializerError, Exception {
        Collection emailAddresses = roBean.getEmailAddresses();
        if (emailAddresses == null) {
            return null;
        }
        int numEmailAddressObjects = emailAddresses.size();
        List list = new ArrayList(numEmailAddressObjects);

        Iterator roItr = emailAddresses.iterator();
        if (log.isDebugEnabled()) {
            log.debug("Query results: ");
        }

        String objectType = "EmailAddress";
        int numCols = 2;
        // Replace ObjectType with Id. TODO - formalize this convention
        List roBeans = new ArrayList(numEmailAddressObjects);
        for (int i = 0; roItr.hasNext(); i++) {
            EmailAddress emailAddress = (EmailAddress)roItr.next();
            String header = null;
            Object columnValue = null;
            List srvbHeader = new ArrayList(numCols);
            List searchResultValueBeans = new ArrayList(numCols);
           
            header = WebUIResourceBundle.getInstance().getString("Details");
            columnValue = roBean.getId()+"."+emailAddress.hashCode();      
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            header = WebUIResourceBundle.getInstance().getString("Email Address");
            columnValue = emailAddress.getAddress();      
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
           
            header = WebUIResourceBundle.getInstance().getString("Type");
            columnValue = emailAddress.getType();      
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            header = "";
            columnValue = "";
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));

            RegistryObjectBean srb =  new RegistryObjectBean(searchResultValueBeans, 
                                                         roBean.getRegistryObject(),
                                                         objectType,
                                                         (Object)emailAddress, 
                                                         false);              
            roBeans.add(srb);
        }
        return roBeans;
    }
    
    private List getAssociationsSearchResultsBeans(RegistryObjectBean roBean)
        throws ClassNotFoundException, NoSuchMethodException,
        ExceptionInInitializerError, Exception {
        // The objectTypeRefs map is normally populated by a call to ro.getComposedObjects().
        // However, the getComposedObjects() method retrieves all composed objects for the 
        // inspected RO. In terms of Associations, only those where the RO is the source object 
        // are returned. In order to get all Associations, including those where the RO is the
        // target of an Association, you must call RegistryObjectImpl.getAllAssociations().
        Collection associations = ((RegistryObjectImpl)roBean.getRegistryObject()).getAllAssociations();
        // check if association is valid to list in association tab of
        // details panel.
        if (associations != null){
            associations = this.filterValidAssociation(associations);
        }
        if (associations == null) {
            return null;
        }
        int numAssociations = associations.size();
        List list = new ArrayList(numAssociations);

        Iterator roItr = associations.iterator();
        if (log.isDebugEnabled()) {
            log.debug("Query results: ");
        }

        String objectType = "Association";
        int numCols = 2;
        // Replace ObjectType with Id. TODO - formalize this convention
        List roBeans = new ArrayList(numAssociations);
        for (int i = 0; roItr.hasNext(); i++) {
            Association association = (Association)roItr.next();
            String header = null;
            Object columnValue = null;
            List srvbHeader = new ArrayList(numCols);
            List searchResultValueBeans = new ArrayList(numCols);
           
            header = WebUIResourceBundle.getInstance().getString("Details");
            columnValue = association.getKey().getId();      
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            header = WebUIResourceBundle.getInstance().getString("Source Object");
            columnValue = association.getSourceObject().getKey().getId();      
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
            
            header = WebUIResourceBundle.getInstance().getString("Target Object");
            columnValue = association.getTargetObject().getKey().getId();      
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));
           
            header = WebUIResourceBundle.getInstance().getString("Type");
            columnValue = association.getAssociationType();
            searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));

            RegistryObjectBean srb =  new RegistryObjectBean(searchResultValueBeans, 
                                                         roBean.getRegistryObject(),
                                                         objectType,
                                                         (Object)association, 
                                                         false);              
            roBeans.add(srb);
        }
        return roBeans;
    }
    /**
     * This method calls checkAssociationObjectValidity method to validate the 
     * Association Object in case Association Object no longer valid it 
     * removes the Association Object from the list so in the Association tab 
     * of details panel will not list this.
     *   
     */
    private Collection filterValidAssociation(Collection collection){
        Iterator itr = collection.iterator();
        try {
            while(itr.hasNext()){
                if(!this.checkAssociationObjectValidity(((Association)itr.next()).getKey().getId(), false)) {
                    itr.remove();
                }
            }
            if (collection.isEmpty()){
                return null;
            }
        }catch(Exception ex){} 
        return collection;
    }
    
    public String getHeader() {
        String header = "";
        RegistryObjectBean bean = (RegistryObjectBean)registryObjectBeans.get(0);
        if (bean != null) {
            if (headersItr == null) {
                headersItr = (bean.getHeaders()).iterator();
            }
            if (headersItr.hasNext()) {
                header = (String)headersItr.next();
                if (! headersItr.hasNext()) {
                    headersItr = null;
                }
            }
        }
        return header; 
    }

    public String getPinnedHeader() {
        String header = "";
        RegistryObjectBean bean = (RegistryObjectBean)getPinnedRegistryObjectBeans().get(0);
        if (bean != null) {
            if (headersItr == null) {
                headersItr = (bean.getHeaders()).iterator();
            }
            if (headersItr.hasNext()) {
                header = (String)headersItr.next();
                if (! headersItr.hasNext()) {
                    headersItr = null;
                }
            }
        }
        return header; 
    }

    public String getRelatedObjectHeader() {
        String header = "";
        if (relatedSearchResults != null && relatedSearchResults.size() > 0) {
            RegistryObjectBean bean = (RegistryObjectBean)relatedSearchResults.get(0);
            if (bean != null) {
                if (headersItr == null) {
                    headersItr = (bean.getHeaders()).iterator();
                }
            }
            if (headersItr.hasNext()) {
                header = (String)headersItr.next();              
                headerIndex++;
                if (! headersItr.hasNext() || headerIndex == 4) {
                    headersItr = null;
                    headerIndex = 0;
                }
            }
        }
        return header; 
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
    
    public List createRegistryObjectBeans(Collection registryObjects) 
        throws ClassNotFoundException, NoSuchMethodException,
        ExceptionInInitializerError, Exception {
        return createRegistryObjectBeans(registryObjects, true);
    }
            
    public List createRegistryObjectBeans(Collection registryObjects, 
                                          boolean enableIterateAdjustment) 
        throws ClassNotFoundException, NoSuchMethodException,
        ExceptionInInitializerError, Exception {
        int numRegistryObjects = registryObjects.size();
        if (numRegistryObjects == 0) {
            return new ArrayList();
        }
        List roBeans = new ArrayList(numRegistryObjects);
                  
        Iterator roItr = registryObjects.iterator();
        if (log.isDebugEnabled()) {
            log.debug("Query results: ");
        }
        Collection allROs = registryObjects;
        
        Concept commonObjectType = UIUtility.getInstance().
            getCommonObjectType(allROs);
        ObjectTypeConfig otCfg = UIUtility.getInstance().
            getObjectTypeConfig(commonObjectType);         
        SearchResultsConfigType srCfg = otCfg.getSearchResultsConfig();
        
        List srCols = srCfg.getSearchResultsColumn();
        int numCols = srCols.size();
        // Replace ObjectType with Id. TODO - formalize this convention
        for (int i = 0; roItr.hasNext(); i++) {
                  
            RegistryObject registryObject = (RegistryObject)roItr.next();
            if (log.isDebugEnabled()) {
                log.debug("Name: "+registryObject.getName());
                log.debug("Description: "+registryObject.getDescription());
            }
            String header = null;
            String className = otCfg.getClassName();
            if (registryObject instanceof javax.xml.registry.infomodel.ExternalLink) {
                className = "org.freebxml.omar.client.xml.registry.infomodel.ExternalLinkImpl";
            }
            List searchResultValueBeans = new ArrayList(numCols+1);  
            header = WebUIResourceBundle.getInstance().getString("details", "Details");
            SearchResultValueBean srvb = 
                new SearchResultValueBean(header, registryObject.getKey().getId());
            searchResultValueBeans.add(srvb);
            /*
            if (passROB !=null && (passROB.getId()).equals(registryObject.getKey().getId())) {
                registryObject = passROB.getRegistryObject();
            }
            */
            List srvbHeader = new ArrayList(numCols+1);
            // Replace data with link to Id. TODO - formalize this convention
            for (int j = 0; j < numCols; j++) {
                SearchResultsColumnType srColType = (SearchResultsColumnType) 
                    srCols.get(j);
                header = srColType.getColumnHeader();
                header = WebUIResourceBundle.getInstance().getString(header, header);
                Object columnValue = UIUtility.getInstance().
                    getColumnValue(srColType, className, registryObject, getLocale(), getCharset());
                srvb = new SearchResultValueBean(header, columnValue);
         
                searchResultValueBeans.add(srvb);
            }
            RegistryObjectBean srb =  new RegistryObjectBean(searchResultValueBeans, 
                                                         registryObject,
                                                         enableIterateAdjustment,
                                                         otCfg);
            roBeans.add(srb);
        }                 
        return roBeans;
    }
    
    public List getPinnedRegistryObjectBeans() {
        List pinnedROs = null;
        if (pinnedRegistryObjectBean != null) {
            Iterator robItr = pinnedRegistryObjectBean.iterator();
            while (robItr.hasNext()) {
                RegistryObjectBean rob = (RegistryObjectBean)robItr.next();
               if (pinnedROs == null) {
                    pinnedROs = new ArrayList();
                }
                pinnedROs.add(rob);
            }
        }
        return pinnedROs;
    }

    public List getAssociationTypes() throws JAXRException {
        ArrayList list = new ArrayList();
        Collection concepts = ((BusinessQueryManagerImpl)UIUtility.getInstance().getBusinessQueryManager()).findConceptsByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/%");
        Iterator conceptsItr = concepts.iterator();
        while (conceptsItr.hasNext()) {
            Concept concept = (Concept)conceptsItr.next();
            list.add(new SelectItem(concept.getKey().getId(), concept.getValue()));
        }
        return list;
    }

    public List getAuditableEventTypes() {
        ArrayList list = new ArrayList();
        list.add(new SelectItem((new Integer(AuditableEvent.EVENT_TYPE_CREATED)),
				WebUIResourceBundle.getInstance().getString("Created")));
        list.add(new SelectItem((new Integer(AuditableEvent.EVENT_TYPE_DELETED)),
                                WebUIResourceBundle.getInstance().getString("Deleted")));
        list.add(new SelectItem((new Integer(AuditableEvent.EVENT_TYPE_DEPRECATED)),
                                WebUIResourceBundle.getInstance().getString("Deprecated")));
        list.add(new SelectItem((new Integer(AuditableEvent.EVENT_TYPE_UPDATED)),
                                WebUIResourceBundle.getInstance().getString("Updated")));
        list.add(new SelectItem((new Integer(AuditableEvent.EVENT_TYPE_VERSIONED)),
                                WebUIResourceBundle.getInstance().getString("Versioned")));
        list.add(new SelectItem((new Integer(AuditableEvent.EVENT_TYPE_UNDEPRECATED)),
                                WebUIResourceBundle.getInstance().getString("Undeprecated")));
        list.add(new SelectItem((new Integer(AuditableEventImpl.EVENT_TYPE_APPROVED)),
                                WebUIResourceBundle.getInstance().getString("Approved")));
        list.add(new SelectItem((new Integer(AuditableEventImpl.EVENT_TYPE_DOWNLOADED)),
                                WebUIResourceBundle.getInstance().getString("Downloaded")));
        list.add(new SelectItem((new Integer(AuditableEventImpl.EVENT_TYPE_RELOCATED)),
                                WebUIResourceBundle.getInstance().getString("Relocated")));
        return list;
    }

    /** Getter for List of SelectItems for event types (and status types). */
    public List getAuditableEventType_SelectItems() {
        if (auditableEventType_SelectItems == null) {
            auditableEventType_SelectItems = new ArrayList(loadAuditableEventType_SelectItems());
        }
        
        return auditableEventType_SelectItems;
    }

    /** Initialize event types */
    private List loadAuditableEventType_SelectItems() {
        List list = new ArrayList();

        // load event types
        try {
            ArrayList eventTypeList = new ArrayList();
            Collection concepts = RegistryBrowser.getBQM().findConceptsByPath(
                    "/" + CanonicalSchemes.CANONICAL_CLASSIFICATION_SCHEME_LID_EventType + "/%");
            for (Iterator it = concepts.iterator(); it.hasNext(); ) {
                ConceptImpl concept = (ConceptImpl)it.next();
                eventTypeList.add(new SelectItem(concept.getLid(),
                                                 WebUIResourceBundle.getInstance().getString(concept.getValue())));    
            }
            list.addAll(eventTypeList);
        } catch (Exception e) {
            log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotGetClassificationScheme",
                    new Object[] {CanonicalSchemes.CANONICAL_CLASSIFICATION_SCHEME_LID_EventType}), e);
        }

        // load status types
        try {
            ArrayList statusTypeList = new ArrayList();
            Collection concepts = RegistryBrowser.getBQM().findConceptsByPath(
                    "/" + CanonicalSchemes.CANONICAL_CLASSIFICATION_SCHEME_LID_StatusType + "/%");
            for (Iterator it = concepts.iterator(); it.hasNext(); ) {
                ConceptImpl concept = (ConceptImpl)it.next();
                statusTypeList.add(new SelectItem(concept.getLid(), "setStatus: " + concept.getValue()));        
            }
            list.addAll(statusTypeList);
        } catch (Exception e) {
            log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotGetClassificationScheme",
                    new Object[] {CanonicalSchemes.CANONICAL_CLASSIFICATION_SCHEME_LID_StatusType}), e);
        }
        
        return list;
    }
     
    public List getQueryTypes() {
        ArrayList list = new ArrayList();
        list.add(new SelectItem((new Integer(Query.QUERY_TYPE_SQL)), "SQL Query"));
        list.add(new SelectItem((new Integer(Query.QUERY_TYPE_XQUERY)), "XQuery"));
        list.add(new SelectItem((new Integer(Query.QUERY_TYPE_EBXML_FILTER_QUERY)), "ebXML Filter Query"));
        return list;
    }


    public List getClassifications() throws JAXRException {
        ArrayList list = new ArrayList();
        Collection classifications = 
            (Collection)getCurrentRegistryObjectBean().getFields().get("classifications");        
        Iterator iter = classifications.iterator();
        while (iter.hasNext()) {
            Classification anItem = (Classification)iter.next();
            String cstr = anItem.getName()+":"+
                          anItem.getValue();
            list.add(new SelectItem(cstr));
        } 
        return list;
    }

    public List getExternalLinks() throws JAXRException {
        ArrayList list = new ArrayList();
        Collection externalLinks = 
            (Collection)getCurrentRegistryObjectBean().getFields().get("externalLinks");
        Iterator iter = externalLinks.iterator();
        while (iter.hasNext()) {
            ExternalLink anItem = (ExternalLink)iter.next();
            list.add(new SelectItem(anItem.getExternalURI()+"("+anItem.getName()+")"));
        }
        return list;
    }

    public List getExternalIdentifiers() throws JAXRException {
        ArrayList list = new ArrayList();
        Collection externalIdentifiers =
            (Collection)getCurrentRegistryObjectBean().getFields().get("externalIdentifiers");
        Iterator iter = externalIdentifiers.iterator();
        while (iter.hasNext()) {
            ExternalIdentifier anItem = (ExternalIdentifier)iter.next();
            list.add(new SelectItem(anItem.getValue()));
        }
        return list;
    }

    public List getSlots() throws JAXRException {
        ArrayList list = new ArrayList();
        Collection slots =
            (Collection)getCurrentRegistryObjectBean().getFields().get("slots");
        Iterator iter = slots.iterator();
        while (iter.hasNext()) {
            Slot anItem = (Slot)iter.next();
            String cstr = "name="+anItem.getName()+
                          " type="+anItem.getSlotType()+
                          " values="+anItem.getValues();
            list.add(new SelectItem(cstr));
        }
        return list;
    }
    
    public List getActions() throws JAXRException {
        ArrayList list = new ArrayList();
        Collection actions =
            (Collection)getCurrentRegistryObjectBean().getFields().get("action");
        Iterator iter = actions.iterator();
        while (iter.hasNext()) {
            Object object = iter.next();
            System.err.println(object.toString());
            NotifyActionImpl anItem = (NotifyActionImpl)object;
            String cstr = "endPoint="+anItem.getEndPoint()+
                          " notificationOption="+anItem.getNotificationOption();
            list.add(new SelectItem(cstr));
        }
        return list;
    }
    
    /**
     * This method handles ActionEvents triggered by a click on the result
     * set scroller links.
     * 
     * @param event
     *  The ActionEvent that encapsulates the click action of the scoller link
     */
    public void processScrollEvent(ActionEvent event) {
        Map requestMap = FacesContext.getCurrentInstance()
                                     .getExternalContext()
                                     .getRequestParameterMap();
        String id = (String)requestMap.get("idValue");
        // Ignore drill down events. Look for the idValue to determine if this
        // action is a drill down
        // Also, check for Relate requests where the relationshipBean is not null
        if ((id == null || id.equals("")) && relationshipBean == null) {
            // Clear current RegistryObjectBean to clear the Details page
            currentRegistryObject = null;
            // Delegate remainder of event processing
            getScrollerBean().processScrollEvent(event);
            resetRowClasses();
        }
    }

    /**
     * This method is to get the number of members in a search result.
     */
    public int getNumberOfRegistryObjectBeans() {
        if (registryObjectBeans == null) {
            numRegistryObjects = 0;
        } else {
            numRegistryObjects = registryObjectBeans.size();
        }
        return numRegistryObjects;
    }

     /**
     * This method is to get the number of members in a search result.
     */
    public int getNumberOfPinnedRegistryObjectBeans() {
        return pinnedRegistryObjectBean.size();
    }

    public int getNumberOfSearchResultValueBeans() {
        int numSRVBs = 0;
        if (registryObjectBeans != null) {
            if (registryObjectBeans.size() > 0) {
                RegistryObjectBean rob = (RegistryObjectBean)registryObjectBeans.iterator()
                                                                                 .next();
                numSRVBs = rob.getSearchResultValueBeans().size();
            }
        }
        return numSRVBs;
    }

    public int getNumberOfSearchPinnedValueBeans() {
        int numSPVBs = 0;
        List pinnedROs = getPinnedRegistryObjectBeans();
        if (pinnedROs != null) {
            if (pinnedROs.size() > 0) {
                RegistryObjectBean rob = (RegistryObjectBean)pinnedROs.iterator()
                                                                       .next();
                numSPVBs = rob.getSearchResultValueBeans().size();
            }
        }
        return numSPVBs;
    }

    public void setRowClasses(RegistryObjectBean rob){
        try {
            rowClasses = new StringBuffer();
            for (int i = scrollerBean.getCurrentRow(); i < scrollerBean.getNextRow();i++) {
                RegistryObjectBean ro = (RegistryObjectBean)registryObjectBeans.get(i);
                if (ro.getId().equals(rob.getId())) {
                    rowClasses.append("list-row-selected,");
                } else {
                    rowClasses.append("list-row,");
                }
            }
        }catch(Exception e){
            
        }
    }

    public String resetRowClasses() {
        rowClasses = new StringBuffer();
        rowClasses.append("list-row,");
        return rowClasses.toString();
    }

    public String getRowClasses() {
        rowClasses.append("list-row,");
        return rowClasses.toString();
    }
    
   /**
    * This method creates a RegistryObject type
    * dynamically. To add new Related ROB's.
    * 
    * @param none
    * @return String
    */    
    public String doAddCurrentComposedROB() {        
        String status = "failure";
        try {    
            LifeCycleManagerImpl lcm = (LifeCycleManagerImpl)RegistryBrowser.getBLCM();
            Class clazz = lcm.getClass();
            String type = getCurrentComposedRegistryObjectType();    
            String methodName = "createObject";
            Method m = null;  
            // Create new composed RO using the LCM; create and store the ROB
            Class argClass[] = new Class[1];
            argClass[0] = type.getClass();
            m = clazz.getMethod(methodName, argClass);
            Object args[] = new Object[1];
            args[0] = type;
            Object ro = m.invoke(lcm, args);
            if (ro instanceof RegistryObject) {
                VersionInfoType vit = BindingUtility.getInstance().rimFac.createVersionInfoType();
                ((RegistryObjectImpl)ro).setVersionInfo(vit);
            }
            RegistryObjectBean rob = null;
            List searchResultValueBeans = new ArrayList(4);
            searchResultValueBeans.add(new SearchResultValueBean("", ""));           
            searchResultValueBeans.add(new SearchResultValueBean("", ""));            
            searchResultValueBeans.add(new SearchResultValueBean("", ""));            
            searchResultValueBeans.add(new SearchResultValueBean("", ""));
            if (ro instanceof TelephoneNumber) {
                rob = new RegistryObjectBean(searchResultValueBeans, 
                                                   currentRegistryObject.getRegistryObject(),
                                                   "TelephoneNumber",
                                                   (TelephoneNumber)ro,
                                                   false);
            } else if (ro instanceof PostalAddress) {
                rob = new RegistryObjectBean(searchResultValueBeans, 
                                                   currentRegistryObject.getRegistryObject(),
                                                   "PostalAddress",
                                                   (PostalAddress)ro,
                                                   false);
            } else if (ro instanceof EmailAddress) {
                rob = new RegistryObjectBean(searchResultValueBeans, 
                                                   currentRegistryObject.getRegistryObject(),
                                                   "EmailAddress",
                                                   (EmailAddress)ro,
                                                   false);
            } else if (ro instanceof Slot) {
                List valueList = new ArrayList();
                valueList.add(new String(""));
                ((Slot)ro).setValues(valueList);
                rob = new RegistryObjectBean(searchResultValueBeans, 
                                                   currentRegistryObject.getRegistryObject(),
                                                   "Slot",
                                                   (Slot)ro,
                                                   false);
            } else if (ro instanceof Concept) {
                if (currentRegistryObject.getRegistryObject().getObjectType().getValue().equalsIgnoreCase("ClassificationScheme")) {
                    ClassificationScheme cs = (ClassificationScheme)currentRegistryObject.getRegistryObject();
                    ((ConceptImpl)ro).setClassificationScheme(cs);
                } else {
                    Concept cn = (Concept)currentRegistryObject.getRegistryObject();
                    ClassificationScheme cs = (ClassificationScheme)cn.getClassificationScheme();
                    if (null != cs) {
                        ((ConceptImpl)ro).setClassificationScheme(cn.getClassificationScheme());
                    }
                    ((ConceptImpl)ro).setParentConcept(cn);                  
                }
                rob = new RegistryObjectBean(searchResultValueBeans, (RegistryObject)ro);
            } else if (ro instanceof RegistryObject) {
                rob = new RegistryObjectBean(searchResultValueBeans, (RegistryObject)ro);
            }
            rob.setNew(true);
            currentComposedRegistryObject = rob;
            registryObjectLookup.put(rob.getId(), rob);
            // Return status so JSF runtime can do page navigation
            status = "showDetailsPage"; 
        } catch (Throwable t) {
            log.warn(WebUIResourceBundle.getInstance().getString("message.UnableToCreateComposedObject"), t);
            append(WebUIResourceBundle.getInstance().getString("createCOError") + " " +
                   t.getLocalizedMessage()); 
        }
        return status;
    }
    
    public String doAdd() {
        String status = "failure";
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
        } else {
            try {
                doClear();
                // Get object type from the drop down list
                // use the newObjectType reference
                // Create the new RO using lcm
                String newType = getNonQualifiedObjectType();
                RegistryObject ro = createNewRegistryObject(newType);
                if (ro instanceof ExtrinsicObjectImpl) {
                    Concept type = ((BusinessQueryManagerImpl)RegistryBrowser.getBQM())
                                                                                .findConceptByPath(newObjectType);
                    ((ExtrinsicObjectImpl)ro).setObjectType(type);
                }
                // Create the SRVBs
                // Create the ROB using the RO and SRVBs
                // Register the ROB
                List ros = new ArrayList(1);
                ros.add(ro);
                handleRegistryObjects(ros);
                // Set the last ROB in the collection to the currentRegisryObject
                Iterator robItr = registryObjectBeans.iterator();
                while (robItr.hasNext()) {
                    RegistryObjectBean rob = (RegistryObjectBean)robItr.next();
                    if (! robItr.hasNext()) {
                        currentRegistryObject = rob;
                        currentRegistryObject.setNew(true);
                    }
                }
                // Initialize composed objects of the current ROB
                currentRegistryObject.initRelatedObjects();
                setRowClasses(currentRegistryObject);
                status = "addSuccessful";
            } catch (Throwable t) {
                log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotCreateNewRegistryObject"), t);
                append(WebUIResourceBundle.getInstance().getString("createROError") + " " +
                       t.getLocalizedMessage()); 
            }
        }
        return status;
    }
    
    private String getNonQualifiedObjectType() {
        String type = null;
        if (newObjectType.indexOf("ExtrinsicObject") != -1) {
            type = "ExtrinsicObject";
        } else {
            int lastSlashIndex = newObjectType.lastIndexOf('/');
            type = newObjectType.substring(lastSlashIndex + 1, newObjectType.length());
        }
        return type;
    }
    
    public String getNewObjectType() {
       return newObjectType;
    }
    
    public void setNewObjectType(String newObjectType) {
        this.newObjectType = newObjectType;
    }
 
    private RegistryObject createNewRegistryObject(String type) {
        RegistryObject ro = null;
        try {
            LifeCycleManagerImpl lcm = (LifeCycleManagerImpl)RegistryBrowser.getBLCM();
            ro = (RegistryObject)lcm.createObject(type);
            VersionInfoType vit = BindingUtility.getInstance().rimFac.createVersionInfoType();
            ((RegistryObjectImpl)ro).setVersionInfo(vit);
            if (type.equals("Association")) {
                Concept concept = RegistryBrowser.getBQM().
                    findConceptByPath("/" + CanonicalSchemes.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType
                        + "/" + CanonicalSchemes.CANONICAL_ASSOCIATION_TYPE_CODE_AccessControlPolicyFor);
                ((Association)ro).setAssociationType(concept);
            } else if (ro instanceof User){
                User user = (User)ro;
                List tns = new ArrayList();
                tns.add(lcm.createTelephoneNumber());
                user.setTelephoneNumbers(tns);
                List pas = new ArrayList();
                pas.add(lcm.createPostalAddress("", "", "", "", "", "", ""));
                user.setPostalAddresses(pas);
                List ems = new ArrayList();
                ems.add(lcm.createEmailAddress(""));
                user.setEmailAddresses(ems);
            }
        } catch (Throwable t) {           
            log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotCreateNewRegistryObject"), t);
        }
        return ro;
    }
    
   /**
   * This method saves on existing RelatedObject for a RegistryObject
   * 
   * @param none
   * @return String
   */
    public String doSaveOnCurrentComposedROB() {
        disableSave = true;
        String status = "failure";
        String result = "";
        boolean skipFinally = false;
        try {
            if (this.currentComposedRegistryObject.getRegistryObject() instanceof SpecificationLinkImpl) {
                if(this.currentComposedRegistryObject.getspecificationObject() == null){
                    if(this.currentComposedRegistryObject.getIsInvalidSpecObj()){
                        status = "failure";
                        append(WebUIResourceBundle.getInstance().
                        getString("invalidSpecificationObject"));
                        skipFinally = true;
                        return status;
                    }else{
                        status = "failure";
                        append(WebUIResourceBundle.getInstance().
                        getString("nullSpecificationObject")); 
                        skipFinally = true;
                        return status;
                    }
                }
            }
            if (this.currentComposedRegistryObject.getRegistryObject() instanceof ServiceBindingImpl) {
               this.changeAccessURIandTargetBinding(this.currentComposedRegistryObject);
               if(this.currentComposedRegistryObject.getRegistryObjectErrorMessage() != null){
                    status = "failure";
                    skipFinally = true; 
                    append(this.currentComposedRegistryObject.getRegistryObjectErrorMessage());
                    this.currentComposedRegistryObject.setRegistryObjectErrorMessage(null);
                    return status;
                }
               if(this.currentComposedRegistryObject.getIsInvalidTargetBinding()) {
                    status = "failure";
                    skipFinally = true; 
                    append(WebUIResourceBundle.getInstance().
                        getString("invalidTargetBindingObject"));
                    this.currentComposedRegistryObject.setIsInvalidTargetBinding(false);
                    return status;
               }
            }  
            if (this.currentComposedRegistryObject.getRegistryObject() instanceof ExternalLinkImpl) {
                this.currentComposedRegistryObject.changeExternalURI();
                if(this.currentComposedRegistryObject.getRegistryObjectErrorMessage() != null){
                    status = "failure";
                    skipFinally = true; 
                    append(this.currentComposedRegistryObject.getRegistryObjectErrorMessage());
                    this.currentComposedRegistryObject.setRegistryObjectErrorMessage(null);
                    return status;
                }
            }
            if (this.currentComposedRegistryObject.getRegistryObject() 
                instanceof ExternalIdentifier) {
                String tmpstatus = this.checkExternalIdentifier(null,this.
                        currentComposedRegistryObject.getRegistryObject());
                if (tmpstatus != null){
                    skipFinally = true;
                    return tmpstatus;
                }
            }            
            if (this.currentComposedRegistryObject.getRegistryObject() instanceof RegistryImpl) {
                this.currentComposedRegistryObject.changeOperatorForRegistry();
                if(this.currentComposedRegistryObject.getRegistryObjectErrorMessage() != null){
                    status = "failure";
                    skipFinally = true; 
                    append(this.currentComposedRegistryObject.getRegistryObjectErrorMessage());
                    this.currentComposedRegistryObject.setRegistryObjectErrorMessage(null);
                    return status;
                }
            }                         
            String currentComposedROUUID = currentComposedRegistryObject.getId();
            String currentComposedROType = currentComposedRegistryObject.getObjectType();
            // Check to see if the composed RO is already a member of the 
            // drilldown RO's composed object collection.  If not, call 
            // appropriate addXXX method
            boolean isNew = isComposedObjectNew();
            if (isNew) {
                setNewComposedROB();
            } else {
                resetComposedROB();
            }
            currentRegistryObject.resetRelatedObjects();
            currentRegistryObject.initRelatedObjects();
            // passROB = currentRegistryObject;
            result = WebUIResourceBundle.getInstance()
                                           .getString("publishSuccessful");            
            
            RegistryObject ro = currentComposedRegistryObject.getRegistryObject();
            handleSavesToDrilldownObject(ro);
            resetCurrentComposedRegistryObjectBean();
            setCurrentRelatedObjectsData(relationshipName);
            /*
             * If ro is a pseudo composed object, such as ClassificationNode,
             * flag it as dirty, but not its parent
             */
            if (currentComposedROType.equalsIgnoreCase("ClassificationNode")) {   
                setPseudoComposedObjectAsDirty(currentComposedROUUID);
            } else {
                currentRegistryObject.setDirty(true);
            }         
            currentRegistryObject.setSelected(true);
            status = "saveSuccessful";
        } catch (Throwable t) {
            status = "failure";
            result = WebUIResourceBundle.getInstance()
                                           .getString("errorSavingCO") + ": " +
                                            t.getLocalizedMessage();
            log.error(result, t);
            append(WebUIResourceBundle.getInstance().getString("saveCOError") +
                   t.getLocalizedMessage()); 
        } finally {
            if(!skipFinally){ 
                disableSave = false;
                //setting newComposedObject to true so as to force cleaning of Object
                newComposedObject = true;
                cleanupComposedROB();
            }
        }
        append(result);
        return status;
    }
    
    private void setPseudoComposedObjectAsDirty(String uuid) {
        Iterator itr = relatedSearchResults.iterator();
        while (itr.hasNext()) {
            RegistryObjectBean rob = (RegistryObjectBean)itr.next();
            try {
                if (rob.getId().equalsIgnoreCase(uuid)) {
                    rob.setDirty(true);
                    break;
                }
            } catch (JAXRException ex) {
                log.error(ex);
            }
        }
    }
    
    public String doCancelSaveOnCurrentComposedROB() {
        String status = "showMessagePage";
        try {
            cleanupComposedROB();
            append(WebUIResourceBundle.getInstance().getString("createModifyCOCancel"));
        } catch (Throwable t) {
                log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotRemoveComposedObject"), t);            
        }
        return status;
    }
    
    private void cleanupComposedROB() {
        if (currentComposedRegistryObject != null && newComposedObject) {
            try {
                registryObjectLookup.remove(currentComposedRegistryObject.getId());
                if (relatedSearchResults != null) {
                    relatedSearchResults.remove(currentComposedRegistryObject.getId());
                }
                invokeMethodOnRegistryObject("remove");
            } catch (Exception ex) {
                log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotRemoveComposedObject"), ex);
            }
        }
        resetCurrentComposedRegistryObjectBean();
    }
    
   /**
   * This method saves new RelatedObject for a RegistryObject
   * 
   * @param none
   * @return String
   */
    private void setNewComposedROB() throws Exception {
        invokeMethodOnRegistryObject("add");
    }
    
    /*
     * This method removes the current composed object on the drilldown
     * RegistryObject and then adds a new composed object instance to it
     */
    private void resetComposedROB() throws Exception {
        invokeMethodOnRegistryObject("remove");
        invokeMethodOnRegistryObject("add");
    }
    
    private void invokeMethodOnRegistryObject(String methodPrefix) 
        throws NoSuchMethodException, Exception {
        RegistryObject drilldownRO = currentRegistryObject.getRegistryObject();
        Object currentRO = null;
        if (currentComposedRegistryObject.getNonRegistryObject() == null) {
            currentRO = currentComposedRegistryObject.getRegistryObject();
        } else {
            currentRO = currentComposedRegistryObject.getNonRegistryObject();
        }
        String objectType = currentRO.getClass().getName();
        Class clazz = drilldownRO.getClass();
        Method m = null;
        String methodName = methodPrefix + objectType.substring(objectType.lastIndexOf(".") + 1);
        Class argsClass[] = new Class[1];
        String argClassName = currentRO.getClass().getName();
        if (argClassName.endsWith("Impl")) {
            ClassLoader loader = drilldownRO.getClass().getClassLoader();
            int classNamelastIndex = argClassName.lastIndexOf(".") + 1;
            argClassName = argClassName.substring(classNamelastIndex);
            argClassName = argClassName.substring(0, argClassName.length()-4);
            argsClass[0] = loader.loadClass("javax.xml.registry.infomodel."+argClassName);
            methodName = methodPrefix + argClassName;
            if ((currentRegistryObject.getObjectType().equalsIgnoreCase("ClassificationScheme") ||
                currentRegistryObject.getObjectType().equalsIgnoreCase("ClassificationNode"))  &&
                methodName.equalsIgnoreCase("addConcept")) {
                methodName = "addChildConcept";
            }
            if ((currentRegistryObject.getObjectType().equalsIgnoreCase("ClassificationScheme") ||
                currentRegistryObject.getObjectType().equalsIgnoreCase("ClassificationNode"))  &&
                methodName.equalsIgnoreCase("removeConcept")) {
                methodName = "removeChildConcept";
            }
            if (currentRegistryObject.getObjectType().equalsIgnoreCase("Organization") &&
                methodName.equalsIgnoreCase("addOrganization")) {
                methodName = "addChildOrganization";
            }
            if (currentRegistryObject.getObjectType().equalsIgnoreCase("Organization") &&
                methodName.equalsIgnoreCase("removeOrganization")) {
                methodName = "removeChildOrganization";
            }
        }
        try {
            m = clazz.getMethod(methodName, argsClass);
        } catch (NoSuchMethodException ex) {
            try {
                m = RegistryObjectImpl.class.getMethod(methodName, argsClass);
            } catch (NoSuchMethodException ex2) {
                try {
                    m = IdentifiableImpl.class.getMethod(methodName, argsClass);
                } catch (NoSuchMethodException ex3) {
                    try {
                        m = ExtensibleObjectImpl.class.getMethod(methodName, argsClass);
                    } catch (NoSuchMethodException ex4) {    
                        throw ex4;
                    }
                }
            }
        }
        Object args[] = new Object[1];
        args[0] = currentRO;
        m.invoke(drilldownRO, args);
    }
    
   /**
   * Checks if composed object being saved already exists else it is new.
   * 
   * @param none
   * @return boolean
   */
    private boolean isComposedObjectNew(){
        boolean isNew = true;
        try {
            Object composedObject = currentComposedRegistryObject.getNonRegistryObject();
            if (composedObject == null) {
                composedObject = currentComposedRegistryObject.getRegistryObject();
            }
            HashSet composedObjects = ((RegistryObjectImpl)currentRegistryObject.
                                                           getRegistryObject()).
                                                           getComposedObjects();

            Iterator iter = composedObjects.iterator();
            while (!currentComposedRegistryObject.isNew() && iter.hasNext()) {
                Object existingComposedObject = iter.next();
                if (existingComposedObject == composedObject) {
                    isNew = false;
                    break;
                }
            }
            
        } catch (Exception je) {
            log.error(WebUIResourceBundle.getInstance().getString("message.ErrorDeterminingIfComposedObjectIsNew"), je);
        }
        return isNew;
    }

   /**
   * Saves Current Registry Object being displayed.
   * 
   * @param none
   * @return boolean
   */
    public String doApplyOnCurrentROB() {
        String status = "failure";
        String croID = null;
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
            RegistryBrowser.getInstance().
                    setPublishOperationMessage(WebUIResourceBundle.
                    getInstance().getString("applyButtonText"));                
        } else {
            try {
                List robs = new ArrayList();
                if (currentRegistryObject == null) {
                    append(WebUIResourceBundle.getInstance().getString("noObjectsSelected"));
                } else if (!this.checkErrorMessageForCRO()) {
                    croID = currentRegistryObject.getId();
                    robs.add(currentRegistryObject);
                    internalDoApply(robs);
                    if (currentRegistryObject != null && !currentRegistryObject.isNew()) {
                        refreshSearchExplore();
                    }
                    append(WebUIResourceBundle.getInstance().
                                                getString("applySuccessful"));
                    status = "publishSuccessful";
                } else {
                    status = "saveSuccessful";
                }
                this.reinitializeSearchPanel(croID);
            }catch(InvalidRequestException ire){
                log.error(ire.getLocalizedMessage());
                append(ire.getLocalizedMessage());
            }  
            catch (Exception ex) {
                this.doDeleteFile();
                log.error(WebUIResourceBundle.getInstance().getString("message.FailedToSaveThisObject"), ex);
                append(WebUIResourceBundle.getInstance().getString("message.FailedToApplyThisObject") + " "+
                ex.getLocalizedMessage()); 
            }
        }
        return status;
    }
    
    /*
     * This method ensures that if a user has clicked Apply when the details
     * page is selected, that any changes to details page are persisted
     */
    private void checkApplyToDetailsPage() {
        if (currentRegistryObject.getCurrentDetailsPaneId() == null ||
            currentRegistryObject.getCurrentDetailsPaneId() == "detailsPanel") {
            currentRegistryObject.setSaveChangesToDrilldownObject(true);
        }
    }

    private void internalDoApply(Collection robs) throws Exception {
        handleRemovalOfRepositoryItems(robs);
        handleRegistryPackageMembers(robs);        
        checkApplyToDetailsPage();
        handleSavesToDrilldownObject();
        BulkResponse br = applyObjects(robs);
        if (br.getStatus()==0){
            handlePseudoComposedObjects();
        } else {
            handleExceptions(br);
        }
    }
    
    /*
     * This method is used to ensure that the drilldown objects setModified
     * state is 'false' unless the user has explicitly saved the drilldown obj
     */
    private void handleSavesToDrilldownObject() {
        if (! currentRegistryObject.isSaveChangesToDrilldownObject()) {
            RegistryObject ro = currentRegistryObject.getRegistryObject();
            if (ro instanceof ExtensibleObjectImpl) {
                ((ExtensibleObjectImpl)ro).setModified(false);
            }
        } else {
            // reset state
            currentRegistryObject.setSaveChangesToDrilldownObject(false);
        }
    }
    
    private void handleExceptions(BulkResponse br) throws JAXRException {
        String errorMessages = "";
        try {
            Iterator itr = br.getExceptions().iterator();
            StringBuffer sb = new StringBuffer();
            while (itr.hasNext()) {
                Throwable t = (Throwable)itr.next();
                sb.append(t.getMessage()).append(" ");
            }
            errorMessages = sb.toString();
        } catch (Throwable t) {
            errorMessages = WebUIResourceBundle.getInstance().getString("noErrorMessage");
        }
        throw new JAXRException(errorMessages);
    }
    
    private void handleRemovalOfRepositoryItems(Collection robs) throws JAXRException {
        Iterator robItr = robs.iterator();
        while (robItr.hasNext()) {
            RegistryObjectBean rob = (RegistryObjectBean)robItr.next();
            if (rob.isRepositoryItemRemoved()){
                RegistryObject ro = rob.getRegistryObject();
                if (ro instanceof ExtrinsicObject) {
                    ExtrinsicObjectImpl eo = (ExtrinsicObjectImpl) ro;
                    eo.removeRepositoryItem();
                    currentRegistryObject.setRepositoryItemRemoved(false);
                }
            }
        }
    }
    
    
    public void handleRegistryPackageMembers(Collection robs) 
        throws JAXRException {
        List tempList = new ArrayList();
        tempList.addAll(robs);
        Iterator robItr = tempList.iterator();
        while (robItr.hasNext()) {
            RegistryObjectBean rob = (RegistryObjectBean)robItr.next();
            RegistryObject ro = rob.getRegistryObject();
            if (ro instanceof RegistryPackageImpl) {
                RegistryPackageImpl rp = (RegistryPackageImpl)ro;
                // Handle removal of Members
                Iterator itr = rp.getRegistryObjects().iterator();
                while (itr.hasNext()) {
                    RegistryObject mro = (RegistryObject)itr.next();
                    RegistryObjectBean mrob = 
                        (RegistryObjectBean)registryObjectLookup.get(mro.getKey().getId());
                    if (mrob != null && mrob.isRemoveRoFromRegistryPackage()) {
                        rp.removeRegistryObject(mro);
                        mrob.setRemoveRoFromRegistryPackage(false);
                        mrob.setAddRoToRegistryPackage(false);
                        mrob.setRelatedSelected(false);
                        robs.remove(rob);
                    }
                }
                //Handle addition of Members
                List relatedList = getRelatedRegistryObjectBeans();
                if (relatedList != null) {
                    Iterator addItr = relatedList.iterator();
                    while (addItr.hasNext()) {
                        RegistryObjectBean mrob = (RegistryObjectBean)addItr.next();
                        if (mrob.isAddRoToRegistryPackage()) {
                            rp.addRegistryObject(mrob.getRegistryObject());
                            mrob.setAddRoToRegistryPackage(false);
                        }
                    }
                }
            }
        }
    }
        
    private void handlePseudoComposedObjects() throws InvalidRequestException,
            JAXRException,Exception {
        if (pseudoComposedRobsToDelete != null) {
            Iterator itr = pseudoComposedRobsToDelete.iterator();
            while (itr.hasNext()) {
                RegistryObjectBean rob = (RegistryObjectBean)itr.next();
                List deleteRO = new ArrayList();
                deleteRO.add((rob.getRegistryObject().getKey()));
                if(!RegistryBrowser.getBQM().getRegistryObjects(deleteRO).getCollection().isEmpty()) {
                    String status = doDeleteObject(rob);
                    if (status.equalsIgnoreCase("failure")) {
                        pseudoComposedRobsToDelete.clear();
                        throw new InvalidRequestException(WebUIResourceBundle.getInstance()
                                                                             .getString("message.CouldNotDeleteComposedObject"));
                    }
                }
            }
            pseudoComposedRobsToDelete.clear();
        }
    }
    
    public String doSaveOnCurrentROB() {
        String status = "failure";
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
            RegistryBrowser.getInstance().
                    setPublishOperationMessage(WebUIResourceBundle.
                    getInstance().getString("applyButtonText"));                
        } else {
            if (this.checkErrorMessageForCRO()){
                return "saveSuccessful";
            }
            currentRegistryObject.setSelected(true);
            currentRegistryObject.setDirty(true);
            // If the user has explicitly saved the drilldown object, flag this
            // condition
            if (currentRegistryObject.getCurrentDetailsPaneId() == null ||
                currentRegistryObject.getCurrentDetailsPaneId() == "detailsPanel") {
                currentRegistryObject.setSaveChangesToDrilldownObject(true);
            }
            status = "saveSuccessful";
        }
        return status;
    }
    
    public String doCancelOnCurrentROB() {
        String status = "failure";
        try {
            doClear();
            append(WebUIResourceBundle.getInstance().getString("publishCanceled"));
            status = "publishCancel";
        } catch (Throwable t) {
            append(WebUIResourceBundle.getInstance().getString("cancelROError") + " " +
                t.getLocalizedMessage());
        }
        return status;
    }
    
    private BulkResponse applyObjects(Collection robList) throws Exception {
        Iterator itr = robList.iterator();
        List roList = new ArrayList();
        while (itr.hasNext()) {
            RegistryObject ro = ((RegistryObjectBean)itr.next()).getRegistryObject();
            roList.add(ro);
        }
        HashMap slotsMap = new HashMap();
        slotsMap.put(AbstractResourceBundle.LOCALE, "en_US");
        LifeCycleManager lcm = RegistryBrowser.getBLCM();
        if (isObjectVersioned()) {
            slotsMap.put(CanonicalConstants.CANONICAL_SLOT_LCM_DONT_VERSION, 
                "false");
            slotsMap.put(CanonicalConstants.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, 
                "false");
        } else {
            slotsMap.put(CanonicalConstants.CANONICAL_SLOT_LCM_DONT_VERSION, 
                "true");
            slotsMap.put(CanonicalConstants.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, 
                "true");
        }
        BulkResponse br = ((LifeCycleManagerImpl)lcm).saveObjects(roList, slotsMap);
        return br;
    }
    
   
   /**
   * Approve created Registry Object.  Returns Failure/Success string for display.
   * 
   * @param none
   * @return String
   */
    public String doApproveOnCurrentROB() {
        String status = "failure";
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
            RegistryBrowser.getInstance().
                    setPublishOperationMessage(WebUIResourceBundle.
                    getInstance().getString("approveButtonText"));               
        } else {
            List roList = new ArrayList();
            RegistryObject ro = currentRegistryObject.getRegistryObject();
            try {
                roList.add(ro.getKey());
                LifeCycleManager lcm = ro.getLifeCycleManager();     
                ((LifeCycleManagerImpl)lcm).approveObjects(roList);
                status = "publishSuccessful";
                refreshSearchExplore();
            } catch (Exception ex) {
                log.error(WebUIResourceBundle.getInstance().getString("message.FailedToApproveThisObject"), ex);
                append(WebUIResourceBundle.getInstance().getString("approveROError") + " "+
                       ex.getLocalizedMessage()); 
            }
        }
        return status;
    }
    
   /**
   * Deprecate existing Registry Object.  Returns Failure/Success string for display.
   * 
   * @param none
   * @return String
   */
    public String doDeprecateOnCurrentROB() {
        String status = "failure";
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
            RegistryBrowser.getInstance().
                    setPublishOperationMessage(WebUIResourceBundle.
                    getInstance().getString("deprecateButtonText"));    
        } else {
            List roList = new ArrayList();
            RegistryObject ro = currentRegistryObject.getRegistryObject();
            try {
                roList.add(ro.getKey());            
                LifeCycleManager lcm = ro.getLifeCycleManager();
                lcm.deprecateObjects(roList);
                status = "publishSuccessful";            
                refreshSearchExplore();
            } catch (Exception je){
                log.error(WebUIResourceBundle.getInstance().getString("message.ErrorInDeprecateRegistryObject"), je);
                append(WebUIResourceBundle.getInstance().getString("deprecateROError") + " " +
                       je.getLocalizedMessage()); 
            }
        }
        return status;
    }

   /**
   * UnDeprecate deprecated Registry Object.  Returns Failure/Success string for display.
   * 
   * @param none
   * @return String
   */
    public String doUndeprecateOnCurrentROB() {
        String status = "failure";
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
            RegistryBrowser.getInstance().
                    setPublishOperationMessage(WebUIResourceBundle.
                    getInstance().getString("undeprecateButtonText"));    
        } else {
            List roList = new ArrayList();
            RegistryObject ro = currentRegistryObject.getRegistryObject();
            try {
                roList.add(ro.getKey());
                LifeCycleManager lcm = ro.getLifeCycleManager();
                lcm.unDeprecateObjects(roList);
                status = "publishSuccessful";
                refreshSearchExplore();
            } catch (Exception je){
                log.error(WebUIResourceBundle.getInstance().getString("message.ErrorInUndeprecatingRegistryObject"), je);
                append(WebUIResourceBundle.getInstance().getString("undeprecateROError") + " " +
                       je.getLocalizedMessage()); 
            }
        }
        return status;
    }

   /**
   * Delete existing Registry Object.  Returns Failure/Success string for display.
   * 
   * @param none
   * @return String
   */
    public String doDeleteOnCurrentROB() {
        String status = "failure";
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
            RegistryBrowser.getInstance().
                    setPublishOperationMessage(WebUIResourceBundle.
                    getInstance().getString("deleteButtonText"));    
        } else {
            List roList = new ArrayList();
            RegistryObject ro = currentRegistryObject.getRegistryObject();
            try {
                roList.add(ro.getKey());
                LifeCycleManager lcm = ro.getLifeCycleManager();
                lcm.deleteObjects(roList);
                status = "publishSuccessful";
                refreshSearchExplore();
            } catch (Exception je){
                log.error(WebUIResourceBundle.getInstance().getString("message.ErrorInDeleteExistingRegistryObject"), je);
                append(WebUIResourceBundle.getInstance().getString("deleteROError") + " " +
                       je.getLocalizedMessage());
            }
        }
        return status;
    }

   /**
   * Delete selected composed Objects.  Returns Failure/Success string for display.
   * 
   * @param none
   * @return String
   */
    public String doDeleteOnCurrentComposedROB() {
        String status = "failure";
        int totalCount = 0;
        int successCount = 0;
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
        } else {
            getCurrentRegistryObjectBean().setFormUpdateIgnored(false);
            HashSet robsToDelete = new HashSet();
           if (relatedSearchResults != null && relatedSearchResults.size() > 0) { 
            Iterator itr = relatedSearchResults.iterator();
	     RegistryObject ro = currentRegistryObject.getRegistryObject();

            while (itr.hasNext()) {
                RegistryObjectBean rob = (RegistryObjectBean)itr.next();
                if (rob.isRelatedSelected()) {
                    totalCount++;      
                    Method m = null;
                    String method = null;
                    Class[] args = new Class[1];
                    Class clazz = null;
                    Object[] composedObject = null;
                    String parentClassName = null;
                    String parentType = null;
                    String composedClassName = null;
                    String composedType = null;
                    try {
                        // Use reflection to remove the composed object
                        //Class clazz = javax.xml.registry.infomodel.RegistryObject.class;
                        parentType = currentRegistryObject.getObjectType();
                        if (parentType.equals("ClassificationNode")) {
                            parentType = "Concept";
                        }
                        parentClassName = "javax.xml.registry.infomodel." + parentType;
                        clazz = this.getClass().getClassLoader().loadClass(parentClassName);

                        // Get Composed object to remove
                        Object nonRO = rob.getNonRegistryObject();                 
                        composedObject = new Object[1];
                        if (nonRO != null) {
                            composedObject[0] = nonRO;
                        } else {
                            composedObject[0] = rob.getRegistryObject();
                        }
                        // Get class of composed object
                        composedType = rob.getObjectType();
                        if (composedType.equals("ClassificationNode")) {
                            composedType = "Concept";
                        }

                        composedClassName = "javax.xml.registry.infomodel." + composedType;
                        args[0] = this.getClass().getClassLoader().loadClass(composedClassName);
                        method = "remove"+composedType;

                        if ((parentType.equalsIgnoreCase("ClassificationScheme") ||
                            parentType.equalsIgnoreCase("Concept")) &&
                            method.equalsIgnoreCase("removeConcept")) {
                            method = "removeChildConcept";
                        }
                        if (parentType.equalsIgnoreCase("Organization") &&
                            method.equalsIgnoreCase("removeOrganization")) {
                            method = "removeChildOrganization";
                        }
                        boolean canDelete = true;
                        if (parentType.equalsIgnoreCase("Organization") &&
                            method.equalsIgnoreCase("removeUser")) {
                            User primaryContact = ((OrganizationImpl)currentRegistryObject.getRegistryObject()).getPrimaryContact();
                            if ((primaryContact.getKey().getId()).equals(rob.getRegistryObject().getKey().getId())) {
                                canDelete = false;
                                append(WebUIResourceBundle.getInstance().getString("message.cannotDeletePrimaryContact"));
                            }
                        }
                        if (composedType.equalsIgnoreCase("Association") && 
                                method.equalsIgnoreCase("removeAssociation")) {
                            if(nonRO instanceof Association){
                                if(!rob.getRegistryObject().getKey().getId().equals(((Association)nonRO).getSourceObject().getKey().getId())) {
                                    canDelete = false;
                                    append(WebUIResourceBundle.getInstance().getString("message.cannotDeleteTargetAssociation"));
                                    status = "targetAssociaton";
                                }
                            } 
                        }
                        
                        if (canDelete) {
                            m = clazz.getMethod(method, args);
                            m.invoke(ro, composedObject);
                            if (composedType.equalsIgnoreCase("User")) {
                                currentRegistryObject.removeRelatedObject(composedObject[0]);
                            } else {
                                prepareToDelete(rob, robsToDelete, composedObject);                                
                            }
                            handleSavesToDrilldownObject(composedObject[0]);
                            itr.remove();
                            successCount++;
                            status = "publishSuccessful";
                        }
                    } catch (Throwable t) {
                        try {
                            // Try JAXR provider classes
                            parentClassName = "org.freebxml.omar.client.xml.registry.infomodel." +
                                parentType + "Impl";
                            clazz = this.getClass().getClassLoader().loadClass(parentClassName);
                            Object nonRO = rob.getNonRegistryObject();                 
                            composedObject = new Object[1];
                            if (nonRO != null) {
                                composedObject[0] = nonRO;
                            } else {
                                composedObject[0] = rob.getRegistryObject();
                            }
                            // Get class of composed object
                            composedType = rob.getObjectType();
                        
                            composedClassName = "javax.xml.registry.infomodel." + composedType;
                            args[0] = this.getClass().getClassLoader().loadClass(composedClassName);
                            method = "remove"+composedType;
                            boolean canDelete = true;
                            if (composedType.equalsIgnoreCase("Association") && 
                                method.equalsIgnoreCase("removeAssociation")) {
                                if(nonRO instanceof Association){
                                    if(!rob.getRegistryObject().getKey().getId().equals(((Association)nonRO).getSourceObject().getKey().getId())) {
                                        canDelete = false;
                                        append(WebUIResourceBundle.getInstance().getString("message.cannotDeleteTargetAssociation"));
                                        status = "targetAssociaton";
                                    }
                                } 
                            }
                            if(canDelete) {
                                m = clazz.getMethod(method, args);
                                m.invoke(ro, composedObject);
                                prepareToDelete(rob, robsToDelete, composedObject);
                                handleSavesToDrilldownObject(composedObject[0]);
                                itr.remove();
                                successCount++;
                                status = "publishSuccessful";
                            }
                        } catch (Throwable t3) {
                            try {
                                parentClassName = "javax.xml.registry.infomodel.ExtensibleObject";
                                clazz = this.getClass().getClassLoader().loadClass(parentClassName);
                                args[0] = String.class;
                                m = clazz.getMethod(method, args);
                                Object objArgs[] = new Object[1];
                                objArgs[0] = ((Slot)composedObject[0]).getName();
                                m.invoke(ro, objArgs); 
                                prepareToDelete(rob, robsToDelete, composedObject);
                                handleSavesToDrilldownObject(composedObject[0]);
                                itr.remove();
                                successCount++;
                                status = "publishSuccessful";
                            } catch (Throwable t2) {                                    
                                    log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotDeleteComposedObject"), t2);
                            }
                        }
                    }
                }
            }
            } 
            storePseduoComposedObjects(robsToDelete);
            if (robsToDelete.size() > 0) {
                relatedSearchResults.removeAll(robsToDelete);
            }
        }
        //No Objects were selected for deletion 
        if(totalCount == 0) {
	    status = "showSearchPanel";
            append(WebUIResourceBundle.getInstance().getString("message.noObjectsDeleted"));
        } else if(totalCount != 0 && status.equals("targetAssociaton")){
            status="showSearchPanel";
        }        
        else {
            //All objects successfully deleted, or partial failure
            if(status.equals("publishSuccessful")) {
                append(WebUIResourceBundle.getInstance().getString("message.objectsSuccessfulyDeleted",
                                                   new Object[]{new Integer(successCount)}));
                if(successCount < totalCount) {
                    append(WebUIResourceBundle.getInstance().getString("message.someObjectsFailedDeletion", 
                                                   new Object[]{new Integer(totalCount - successCount)}));
                }
            }
            //All objects failed deletion
            else {
                append(WebUIResourceBundle.getInstance().getString("message.someObjectsFailedDeletion",
                                                   new Object[]{new Integer(totalCount - successCount)}));
            }
        }
        return status;
    }
    
    private void handleSavesToDrilldownObject(Object ro) {
        if (! (ro instanceof ConceptImpl) &&                                        
            ! (ro instanceof ExternalLinkImpl)) {
            currentRegistryObject.setSaveChangesToDrilldownObject(true);
        }
    }
    
    private void storePseduoComposedObjects(Collection robsToDelete) {
        Iterator itr = robsToDelete.iterator();
        while (itr.hasNext()) {
            RegistryObjectBean rob = (RegistryObjectBean)itr.next();
            RegistryObject ro = rob.getRegistryObject();
            if (ro instanceof Concept || ro instanceof Organization ||
                ro instanceof ExternalLink || ro instanceof PersonImpl) {
                if (pseudoComposedRobsToDelete == null) {
                    pseudoComposedRobsToDelete = new ArrayList();
                }
                this.pseudoComposedRobsToDelete.add(rob);
            }
        }
    }

    
    private void prepareToDelete(RegistryObjectBean rob, 
                                 Collection robsToDelete,
                                 Object[] composedObject) 
        throws JAXRException {
        currentRegistryObject.setDirty(true);
        currentRegistryObject.setSelected(true);
        currentRegistryObject.removeRelatedObject(composedObject[0]);
        Object nonRO = rob.getNonRegistryObject();
        if (nonRO == null) {
            robsToDelete.add(rob);
        }
    }

   /**
   * Delete existing Registry Object.  Returns Failure/Success string for display.
   * 
   * @param none
   * @return String
   */
    public String doDeleteObject(RegistryObjectBean deleteROB) {
        String status = "failure";
        List roList = new ArrayList(); 
        RegistryObject ro = deleteROB.getRegistryObject();
        try {
            roList.add(ro.getKey());
            LifeCycleManager lcm = ro.getLifeCycleManager();
            lcm.deleteObjects(roList);
            status = "publishSuccessful";
            refreshSearchExplore();
            
        }catch(ReferencesExistException ree){
                log.error(WebUIResourceBundle.getInstance()
                .getString("message.FailedToDeleteThisObject")+ ":" +
                        ree.getLocalizedMessage());
                append(WebUIResourceBundle.getInstance()
                .getString("lcmDeleteError") + " " +ree.getLocalizedMessage());
        } 
        catch (Exception je){
            log.error("Error in Delete Existing RegistryObject ", je);
            append(WebUIResourceBundle.getInstance().getString("deleteROError") + " " +
                   je.getLocalizedMessage());
        }
        return status;
    }
    
    /**
     * Getter for list of selected RegitryObjectBean for LCM Operations.
     * @return List of selected ROBs.
     */
      private List getSelectedRegistryObjectBeans() {
        List selectedRobs = new ArrayList();
        UIData data = getScrollerBean().getData();
        if (data != null) {
        int n = data.getRowCount();
        ArrayList rob = (ArrayList)data.getValue();    
        for (int i = 0; i < n; i++) {
            data.setRowIndex(i);
            if (((RegistryObjectBean)rob.get(i)).isSelected()) {
                // save selected ROB in a Collection
                // this Collection will be passed to an LCM method
                ((RegistryObjectBean)rob.get(i)).setSelected(false);
                selectedRobs.add(rob.get(i));
            }
        }
        }
        return selectedRobs;
    }    

    /**
     * This method is used to get RegistryObjectBeans that are selected in the
     * results table plus any bookmarked beans
     *
     * @return java.util.Collection
     * A Collection of selected RegistryObjectBeans
     */
    public Collection getAllSelectedRegistryObjectBeans() {
          Collection allSelected = (Collection)getSelectedRegistryObjectBeans();
          UIData data = getPinnedScrollerBean().getData();
          int n = (data != null)?data.getRowCount():0;
          if (n > 0) {
              ArrayList rob = (ArrayList)data.getValue();                
              for (int i = 0; i < n; i++) {
                  data.setRowIndex(i);
                  if (((RegistryObjectBean)rob.get(i)).isPinned()) {
                      // save selected ROB in a Collection                      
                      allSelected.add(rob.clone());
                  }
              }
          }
          return allSelected;
      }
      
    /**
     * Getter for list of selected RegitryObjectBean for LCM Operations.
     * @return List of selected ROBs.
     */
      public List getSelectedPinnedRegistryObjects() {
        List selectedRobs = new ArrayList();
        UIData data = getPinnedScrollerBean().getData();
        int n = (data != null)?data.getRowCount():0;
        if (n > 0) {
            ArrayList rob = (ArrayList)data.getValue();                
            for (int i = 0; i < n; i++) {
                data.setRowIndex(i);
                if (((RegistryObjectBean)rob.get(i)).isPinned()) {
                    // save selected ROB in a Collection
                    // this Collection will be passed to an LCM method
                    ((RegistryObjectBean)rob.get(i)).setSelected(false);
                    ((RegistryObjectBean)rob.get(i)).setPinned(false);
                    selectedRobs.add(rob.get(i));
                }
            }
        }
        return selectedRobs;
    }

    /**
     * This method create a List a of RegistryObject   
     * from  
     * 
     * @param none
     * @return List
     */
   
    public List getSelectedRegistryObjects(){
        List registryObjects = new ArrayList();
        Iterator iter = getSelectedRegistryObjectBeans().iterator();
        while (iter.hasNext()) {
            RegistryObject roObject = (RegistryObject)(((RegistryObjectBean)iter.next()).getRegistryObject());
            registryObjects.add(roObject);
        }
        return registryObjects;
    }  
    
    /**
     * <p>Append an informational message to the set of messages that will
     * be rendered when this view is redisplayed.</p>
     *
     * @param message Message text to be added
     */
    public void append(String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        Iterator iter = context.getMessages();
        if (iter == null || !iter.hasNext()) {
            String statusLabel = WebUIResourceBundle.getInstance().
                                                        getString("statusLabel");
            message = statusLabel + ": " + message;
        }
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                           message, null));
    }
    
    /**
     * <p>Retrive an session map From FaceContext to which intern used to achive 
     * the SearchPanelBean Instance which is used perform doSerch operation that   
     * will be rendered with refresh values when this view is redisplayed.</p>
     */
     private void refreshSearchPanel(){
        Map sessionMap = (Map)FacesContext.getCurrentInstance()
                                          .getExternalContext()
                                          .getSessionMap();
            SearchPanelBean bean = (SearchPanelBean)sessionMap.get("searchPanel");
        if (bean != null) {
            bean.doSearch();
        }
    } 

    /**
     * <p>Retrive an session map From FaceContext to which intern used to achive 
     * the SearchPanelBean Instance which is used perform doSearch operation that   
     * will be rendered with refresh values when this view is redisplayed.</p>
     */
     private void refreshExplorerPanel(){
        try {
            Map sessionMap = (Map)FacesContext.getCurrentInstance()
                                              .getExternalContext()
                                              .getSessionMap();
            SearchPanelBean bean = (SearchPanelBean)sessionMap.get("searchPanel");
            if (bean != null) {
                bean.getExplorerGraphBean().loadRegistryObjects(node);
            }
        } 
        catch (Throwable t) {
            log.error(WebUIResourceBundle.getInstance().getString("message.AnExceptionOccurredDuringTheSearch"), t);
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

    } 

    public boolean isUserAllowedToPublish() {
        boolean allowedToPublish = false;
        boolean noRegRequired = 
            Boolean.valueOf(CommonProperties.getInstance()
                .getProperty("omar.common.noUserRegistrationRequired", "false")).booleanValue();
        if (noRegRequired) {
            allowedToPublish = true;
        } else {
            allowedToPublish = RegistryBrowser.getInstance().isAuthenticated();
        }
        return allowedToPublish;
    }
    
    public String doApply() {
        String status = "failure";
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
            RegistryBrowser.getInstance().
                    setPublishOperationMessage(WebUIResourceBundle.
                    getInstance().getString("saveButtonText"));                
        } else {
            List roList = new ArrayList();
            BulkResponse br= null;
            try {
                Collection robs = getSelectedRegistryObjectBeans();
                if (robs.size() > 0) {
                    internalDoApply(robs);
                    append(WebUIResourceBundle.getInstance().
                                                getString("applySuccessful"));
                    status="publishSuccessful";
                }
            } catch(Exception ex) {
                log.error(WebUIResourceBundle.getInstance().getString("message.FailedToApplyThisObject"), ex);
                append(WebUIResourceBundle.getInstance().getString("lcmSaveError") + " " +
                       ex.getLocalizedMessage());
            }
        } 
        return status;
    }
        
    /**
    * This method return the Status of Approve RegistryObjects 
    * 
    * @param none
    * @return String
    */
    public String doApprove() {
        String status = "failure";
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
            RegistryBrowser.getInstance().
                    setPublishOperationMessage(WebUIResourceBundle.
                    getInstance().getString("approveButtonText"));               
        } else {
        List roList = new ArrayList();
        BulkResponse br= null;
        try {
            BusinessLifeCycleManagerImpl blcm = RegistryBrowser.getInstance().getBLCM();
            Iterator iter = getSelectedRegistryObjects().iterator();
            while (iter.hasNext()) {
                RegistryObject registryObject = (RegistryObject)iter.next();
                roList.add(registryObject.getKey());
            }

            iter = getSelectedPinnedRegistryObjects().iterator();
            while (iter.hasNext()) {
                RegistryObjectBean registryObjectBean = (RegistryObjectBean)iter.next();
                roList.add(registryObjectBean.getRegistryObject().getKey());
            }
            
            br = blcm.approveObjects(roList);
            if (br.getStatus()==0){
                this.refreshSearchPanel();
            }
            status = WebUIResourceBundle.getInstance().
                                            getString("approveSuccessful");
            append(status);
            status = "publishSuccessful";
        } catch(Exception ex) {
            log.error(WebUIResourceBundle.getInstance().getString("message.FailedToApproveThisObject"), ex);
            append(WebUIResourceBundle.getInstance().getString("lcmApproveError") + " " +
                   ex.getLocalizedMessage()); 
        }   
      } 
        return status;
    }

    /**
    * This method return the Status of Deprecate RegistryObjects 
    * 
    * @param none
    * @return String
    */
    public String doDeprecate() {
        String status = "failure";
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
            RegistryBrowser.getInstance().
                    setPublishOperationMessage(WebUIResourceBundle.
                    getInstance().getString("deprecateButtonText"));    
        } else {
            List roList = new ArrayList();
            BulkResponse br = null;
            try {
                BusinessLifeCycleManagerImpl blcm = RegistryBrowser.getInstance().getBLCM();
                Iterator iter = getSelectedRegistryObjects().iterator();
                while (iter.hasNext()) {
                    RegistryObject registryObject = (RegistryObject)iter.next();
                    roList.add(registryObject.getKey());
                }

                iter = getSelectedPinnedRegistryObjects().iterator();
                while (iter.hasNext()) {
                    RegistryObjectBean registryObjectBean = (RegistryObjectBean)iter.next();
                    roList.add(registryObjectBean.getRegistryObject().getKey());
                }

                br  = blcm.deprecateObjects(roList);
                if (br.getStatus()==0){
                    this.refreshSearchPanel();
                }

                status = WebUIResourceBundle.getInstance()
                                                   .getString("deprecateSuccessful");
                append(status);
                status="publishSuccessful";
            } catch(Exception ex) {
                log.error(WebUIResourceBundle.getInstance().getString("message.FailedToDeprecateThisObject"), ex);
                append(WebUIResourceBundle.getInstance().getString("lcmDeprecateError") + " " +
                       ex.getLocalizedMessage()); 
            }   
        }
        return status;
    }

    /**
    * This method return the Status of Undeprecate RegistryObjects 
    * 
    * @param none
    * @return String
    */
    public String doUndeprecate() {
    String status = "failure";
    if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
            RegistryBrowser.getInstance().
                    setPublishOperationMessage(WebUIResourceBundle.
                    getInstance().getString("undeprecateButtonText"));    
    } else {
        List roList = new ArrayList();
        BulkResponse br = null;
        try {
            BusinessLifeCycleManagerImpl blcm = RegistryBrowser.getInstance().getBLCM();
            Iterator iter = getSelectedRegistryObjects().iterator();
            while (iter.hasNext()) {
                RegistryObject registryObject = (RegistryObject)iter.next();
                roList.add(registryObject.getKey());
            }

            iter = getSelectedPinnedRegistryObjects().iterator();
            while (iter.hasNext()) {
                RegistryObjectBean registryObjectBean = (RegistryObjectBean)iter.next();
                roList.add(registryObjectBean.getRegistryObject().getKey());
            }

            br = blcm.unDeprecateObjects(roList);
            if (br.getStatus()==0){
                this.refreshSearchPanel();
            }
            status = WebUIResourceBundle.getInstance()
                                           .getString("undeprecateSuccessful");
            append(status);
            status = "publishSuccessful";
        } catch(Exception ex) {
            log.error(WebUIResourceBundle.getInstance().getString("message.FailedToUndeprecateThisObject"), ex);
            append(WebUIResourceBundle.getInstance().getString("lcmUndeprecateError") + " " +
                   ex.getLocalizedMessage());
        }
    }
    return status;
    }
    
    
    /**
    * This method return the Status of Delete RegistryObjects 
    * 
    * @param none
    * @return String
    */
    public String doDelete() {
        String status = "failure";
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
            RegistryBrowser.getInstance().
                    setPublishOperationMessage(WebUIResourceBundle.
                    getInstance().getString("deleteButtonText"));    
        } else {
        List roList = new ArrayList();
        List pinnedDeleteObjects = new ArrayList();
        BulkResponse br = null;
        int size = 0;
        try {
            BusinessLifeCycleManagerImpl blcm = 
                    RegistryBrowser.getInstance().getBLCM();
            Iterator iter = getSelectedRegistryObjectBeans().iterator();
            while (iter.hasNext()) {
                RegistryObjectBean bean = (RegistryObjectBean)iter.next();
                bean.setPinned(false);
                bean.setSelected(false);
                RegistryObject registryObject = bean.getRegistryObject();
                roList.add(registryObject.getKey());
            }

            iter = getSelectedPinnedRegistryObjects().iterator();
            while (iter.hasNext()) {
                RegistryObjectBean registryObjectBean = (RegistryObjectBean)iter.next();
                pinnedDeleteObjects.add(registryObjectBean);
                registryObjectBean.setPinned(false);
                registryObjectBean.setSelected(false);
                roList.add(registryObjectBean.getRegistryObject().getKey());
            }
            
            br = blcm.deleteObjects(roList, null,
                    RegistryBrowser.getInstance().getDeletionScopeCode());
            if (br.getStatus()==0){
                List pinnedObjects = getPinnedRegistryObjectBeans();
                iter = pinnedDeleteObjects.iterator();
                while (iter.hasNext()) {
                    pinnedRegistryObjectBean.remove(((RegistryObjectBean)iter.next()));
                }
                this.refreshSearchExplore();
            }
            status = WebUIResourceBundle.getInstance()
                                           .getString("deleteSuccessful");
            append(status);
            status = "publishSuccessful";
        }catch(ReferencesExistException ree){
                log.error(WebUIResourceBundle.getInstance()
                .getString("message.FailedToDeleteThisObject")+ ":" +
                        ree.getLocalizedMessage());
                append(WebUIResourceBundle.getInstance()
                .getString("lcmDeleteError") + " " +ree.getLocalizedMessage());
        } 
        catch(Exception ex) {
                log.error(WebUIResourceBundle.getInstance().getString("message.FailedToDeleteThisObject"), ex);
                append(WebUIResourceBundle.getInstance().getString("lcmDeleteError") + " " +
                       ex.getLocalizedMessage());
            }
        }    
        return status;
    }
   

    public List getValueTypes() {
        ArrayList list = new ArrayList();
        list.add(new SelectItem((new Integer(ClassificationScheme.VALUE_TYPE_UNIQUE)),
                                 WebUIResourceBundle.getInstance().getString("unique")));
        list.add(new SelectItem((new Integer(ClassificationScheme.VALUE_TYPE_EMBEDDED_PATH)),
                 WebUIResourceBundle.getInstance().getString("embeddedPath")));
        list.add(new SelectItem((new Integer(ClassificationScheme.VALUE_TYPE_NON_UNIQUE)),
                 WebUIResourceBundle.getInstance().getString("nonUnique")));
        return list;
    }
    
   /**
   * Returns boolean value for hiding portions of jsp for display.
   * 
   * @param none
   * @return boolean
   */
    public boolean isSaveCurrentComposedRO() {
        return disableSave;
    }

   /**
   * Allows Display of all required fields to be filled.
   * 
   * @param none
   */
    public void resetToEditMode() {
        disableSave = true;
    }

   /**
   * RegistryObject name is passed to get proper field name used as one of lcm
   * constants.
   * 
   * @param String
   * @return String
   */
    public String getLifeCycleManagerType(String type) {
        char typeChars[] = type.toCharArray();
        StringBuffer strBuf = new StringBuffer();
        for (int i=0; i < typeChars.length; i++) {
            if (i!=0 && Character.isUpperCase(typeChars[i])){
                strBuf.append("_");
            }
            strBuf.append(typeChars[i]);
        }
        return ((strBuf.toString()).toUpperCase());
    }

   /**
   * Helps Publish Search Page for adding new RO.
   * 
   * @param none
   * @return String
   */
    public String doPublishAdd() {
        RegistryBrowser.getInstance().setSessionExpired(false);
        try {
            SearchPanelBean searchBean = SearchPanelBean.getInstance();
            searchBean.doClear();
        } catch (Exception ex) {
            log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotClearSearchPanelBean"), ex);
        }
//        append(PropertyResourceBundle.getInstance().getString("createNewRO"));
//        append(PropertyResourceBundle.getInstance().getString("createNewAssociation"));
        return "showPublishPage";
    }

    /**
     * Prepares this bean to be used by RegistrationWizard for creating a new user account.
     */
    public String doRegister() {
        String status= "error";
        try {
            SearchPanelBean.getInstance().doClear();
            User user = RegistryBrowser.getBLCM().createUser();
            // Create the SRVBs
            // Create the ROB using the RO and SRVBs
            // Register the ROB           
            PersonName pn = (PersonName)user.getLifeCycleManager().createObject("PersonName");
            user.setPersonName(pn);
            EmailAddress ea = (EmailAddress)user.getLifeCycleManager().createObject("EmailAddress");
            ((UserImpl)user).addEmailAddress(ea);
            PostalAddress pa = (PostalAddress)user.getLifeCycleManager().createObject("PostalAddress");       
            ((UserImpl)user).addPostalAddress(pa);
            TelephoneNumber tn = (TelephoneNumber)user.getLifeCycleManager().createObject("TelephoneNumber");       
            ((UserImpl)user).addTelephoneNumber(tn);
            List ros = new ArrayList(1);
            ros.add(user);
            handleRegistryObjects(ros);
            currentRegistryObject = (RegistryObjectBean)registryObjectBeans.iterator().next();
            currentRegistryObject.initRelatedObjects();
            status = "showRegisterPage";
        } catch (Throwable t) {
            append(WebUIResourceBundle.getInstance().getString("errorInRegistration"));
        }
        return status;
    }
   
 
   /**
   * Returns collection of RO of which new RO can be created
   * 
   * @param none
   * @return collection
   */    
    public Collection getSubmittableRegistryObjects(){
        SearchPanelBean searchPanel = 
            (SearchPanelBean)FacesContext.getCurrentInstance()
                                                      .getExternalContext()
                                                      .getSessionMap()
                                                      .get("searchPanel");
        try {
            if (searchPanel == null) {
                searchPanel = SearchPanelBean.getInstance();
            }
        } catch(Exception e) {
            log.error(WebUIResourceBundle.getInstance().getString("message.FailedToCreateNewSearchPanelObject"), e);
        }
        Collection objectList = searchPanel.getObjectTypes();
        Collection actualList = new ArrayList();
        Collection removeList = getNonSubmittableRegistryObjects();
        Iterator iter = objectList.iterator();
        while (iter.hasNext()){
            SelectItem item = (SelectItem)iter.next();
            String itemValue = (String)item.getValue();
            int ind = itemValue.lastIndexOf("/");
            if (ind != -1) {
                itemValue = itemValue.substring(ind+1);
            }
            if (!removeList.contains(itemValue)) {
                actualList.add(item);
            }
        }
        return actualList;
    }

   /**
   * Returns list of object type not included for creating new RO list.
   * 
   * @param none
   * @return collection
   */ 
    public Collection getNonSubmittableRegistryObjects(){
        List removeList = new ArrayList();
        removeList.add("AuditableEvent");
        removeList.add("AuditTrail");
        removeList.add("Association");
        removeList.add("Classification");
        removeList.add("ExternalIdentifier");
        removeList.add("RegistryObject");
        removeList.add("Notification");
        removeList.add("Subscription");
        removeList.add("SpecificationLink");        
        removeList.add("ServiceBinding");
        return removeList;
    }
     
    
      /**
       * Create a list of selected RegitryObjectBean for Relationship  Operations.
       * @return List of selected ROBs.
       */
       private void  createSelectedROBList() {
             List rob = this.getSelectedRegistryObjectBeans();    
             List pRob = this.getSelectedPinnedRegistryObjects();
             this.selectedRegistryObjectBean.clear();
             this.selectedRegistryObjectBean.addAll(rob);
             this.selectedRegistryObjectBean.addAll(pRob);
             this.selectedROBsize = this.selectedRegistryObjectBean.size();
       }

     /**
      *This will return the no of Selected Objects
      * 
      * @return int 
      */ 
      public int getSelectedROBSize(){
          int selectedROBsize = 0;
          Iterator robItr = registryObjectBeans.iterator();
          while (robItr.hasNext()) {
              RegistryObjectBean rob = (RegistryObjectBean)robItr.next();
              if (rob.isSelected()) {
                  selectedROBsize++;
              }
          }
          return selectedROBsize;
      }
  
    
      public List getSelectedRegistryObjectBean() {
         return selectedRegistryObjectBean;
      }
    
      public void getSelectedRegistryObjectBean(List selectedRegistryObjectBean) {
         if (selectedRegistryObjectBean != this.selectedRegistryObjectBean) {
             this.selectedRegistryObjectBean = selectedRegistryObjectBean;
         }
      }
 

    public Collection getPhoneTypes() {
        Collection list = new ArrayList();
        String[] types = UIUtility.getInstance().getPhoneTypes();
        for (int i = 0; i < types.length; i++) {
            list.add(new SelectItem(types[i], WebUIResourceBundle.getInstance().getString("type." + types[i], types[i])));
        }
        return list;   
    }

    public Collection getEmailTypes() {
        Collection list = new ArrayList();
        String[] types = UIUtility.getInstance().getEmailTypes();
        for (int i = 0; i < types.length; i++) {
            list.add(new SelectItem(types[i], WebUIResourceBundle.getInstance().getString("type." + types[i], types[i])));
        }
        return list;   
    }
 
   /**
    * This method return the Status after setting the value in Registry Object 
    * 
    * @param none
    * @return String
    */
    public String doSetClassSchemeOrNode(){
        String status = "failure";
        String node = null;
        String classScheme = null;
        String temp = null;
        String value = null;
        ClassificationScheme classificationScheme = null;
        Concept concept = null;
        String message = null;
        try{
            Collection nodes = SearchPanelBean.getInstance().getClassSchemeSelector().getSelectedNodes();
            if (nodes.size() < 1) {
                append(WebUIResourceBundle.getInstance().getString("selectClassSchemeOrNode"));
            } else {
                // Get first selected node
                // TODO: add attribute to ClassSchemeGraph bean to enforce single 
                // node selection. In this case, user should only be able to 
                // select a single ClassificationNode
                RegistryObjectNode ron = (RegistryObjectNode)nodes.iterator().next();
                RegistryObject registryObject = ron.getRegistryObject();
                if (registryObject instanceof ClassificationScheme) {
                    classificationScheme = (ClassificationScheme)registryObject;
                    concept = classificationScheme.getObjectType();
                } else {
                    concept = (Concept)registryObject;
                    classificationScheme = concept.getClassificationScheme();
                }
                value = concept.getValue();
                RegistryObject ro = getCurrentRegistryObjectBean().getRegistryObject();
                if (ro instanceof Classification) {
                   boolean external = classificationScheme.isExternal();  
                   //If the node value is null and the Class scheme in not External 
                   //type it will return the control to the error page along 
                   //with the message.     
                   if(value == null && !external) {
                        append(WebUIResourceBundle.getInstance().getString("selectExternalCSorCN"));
                        status = "showErrorMessage";
                        return status;
                   }         
                   ((Classification)ro).setClassificationScheme(classificationScheme);
                   if (external){ 
                       concept = null;
                       ((Classification)ro).setConcept(concept);
                       ((Classification)ro).setValue(null);
                   } else {
                       ((Classification)ro).setConcept(concept); 
                       ((Classification)ro).setValue(value);
                   }
                }
                if (ro instanceof ExternalIdentifier){
                    ((ExternalIdentifier)ro).setIdentificationScheme(classificationScheme);
                    if(value!=null){
                        ((ExternalIdentifier)ro).setValue(value);
                    }else{
                        ((ExternalIdentifier)ro).setValue("");
                    }
                }
                if (registryObject instanceof ClassificationScheme) {
                    append(WebUIResourceBundle.getInstance().getString("selectNewCSSuccessful"));
                } else {
                    append(WebUIResourceBundle.getInstance().getString("selectNewCNSuccessful"));
                }
                SearchPanelBean.getInstance().clearClassSchemeSelector();
                status = "showMessagePage";
            }

        } catch(Exception ex) {
            log.error(WebUIResourceBundle.getInstance().getString("message.ExceptionOccuredWhileSetingClassSchemeOrNode"),ex);
            append(WebUIResourceBundle.getInstance().getString("errorSettingCSorCN") + " " +
                    ex.getLocalizedMessage());
        }
        return status;
    }
     
    public String doSetClassNode(){
        String status = "failure";
        String node = null;
        try{
            Collection nodes = SearchPanelBean.getInstance().getClassSchemeSelector().getSelectedNodes();
            if (nodes.size() < 1) {
                append(WebUIResourceBundle.getInstance().getString("selectClassificationConcept"));
            } else {
                // Get first selected node
                // TODO: add attribute to ClassSchemeGraph bean to enforce single 
                // node selection. In this case, user should only be able to 
                // select a single ClassificationNode
                RegistryObjectNode ron = (RegistryObjectNode)nodes.iterator().next();
                Concept concept = (Concept)ron.getRegistryObject();
                RegistryObject ro = 
                        this.getCurrentRegistryObjectBean().getRegistryObject();

                if (ro instanceof ExtrinsicObjectImpl)  {
                    ((ExtrinsicObjectImpl)ro).setObjectType(concept);
                } else if (ro instanceof ExternalLinkImpl) {
                    ((ExternalLinkImpl)ro).setObjectType(concept);
                }
                append(WebUIResourceBundle.getInstance().getString("selectNewConceptSuccessful"));
            }
            SearchPanelBean.getInstance().clearClassSchemeSelector();
            status = "showMessagePage";
        } catch(Exception ex) {
            log.error(WebUIResourceBundle.getInstance().getString("message.ExceptionOccuredWhileSettingClassNode"),ex);
            append(WebUIResourceBundle.getInstance().getString("errorSettingCN") + " " +
                    ex.getLocalizedMessage());
        }
        return status;
    }


    /**
    * Getter method for source Reference on Refrence panel
    * @ return String   
    */ 
    public String getReferenceSourceCode() {
        return referenceSourceCode;
    }

    /** 
    * Setter method for source Reference on Refrence panel
    * @param String   
    */ 
    public void setReferenceSourceCode(String referenceSourceCode) {
        this.referenceSourceCode = referenceSourceCode;
    }

    /**
    * This method used to get the list of selected Registry Objects for 
    * source.
    * @return List.  
    */
    public List getReferenceScopeCodes() {
        ArrayList pinnedDisplayList = new ArrayList();
        try{
            for(int i = 0; i < pivotalRegistryObjectBean.size(); i++ ) {
                InternationalString referenceName = ((RegistryObjectBean)pivotalRegistryObjectBean.get(i)).getRegistryObject().getName();

                if (referenceName.getValue() != null) {
                    pinnedDisplayList.add(new SelectItem(((RegistryObjectBean)pivotalRegistryObjectBean.get(i)).getId(),
                    referenceName +
                     "(" + ((RegistryObjectBean)pivotalRegistryObjectBean.get(i)).getObjectType() + ")"));
                } else {
                    pinnedDisplayList.add(new SelectItem(((RegistryObjectBean)pivotalRegistryObjectBean.get(i)).getId(),
                    ((RegistryObjectBean)pivotalRegistryObjectBean.get(i)).getId() +
                     "(" + ((RegistryObjectBean)pivotalRegistryObjectBean.get(i)).getObjectType() + ")"));
                }

            }
        }catch(Exception ex){
            log.error(ex);
        }
        return pinnedDisplayList;
    }

    
    /**
    * This method used to get the list of Reference Attribute 
    * @return List.  
    */
    public List getReferenceAttributeCodes() {
        ArrayList pinnedDisplayList = new ArrayList();
        for (int i=0; i < refAttributeList.size(); i++) {
            pinnedDisplayList.add(new SelectItem((String)refAttributeList.get(i),
                                                 UIUtility.getInstance().initCapString((String)refAttributeList.get(i))));
        }
        return pinnedDisplayList;
    }
    
    
    
    public void changeSelectedSourceType(ValueChangeEvent event) {

        if (null != event.getNewValue()){
            String value = (String)event.getNewValue();
            try{
                if(value.equals(((RegistryObjectBean)pivotalRegistryObjectBean.get(0)).getId())){
                    setReferenceSourceCode(((RegistryObjectBean)pivotalRegistryObjectBean.get(0)).getId());
                }else{
                    setReferenceSourceCode(((RegistryObjectBean)pivotalRegistryObjectBean.get(1)).getId());
                } 
            }catch(Exception ex){
                log.error(ex);
            }                
            this.doInitializeReference();
        }
    }    

    
   /**
    * Getter method for Target Reference on reference panel 
    * @return String 
    */
    public String getReferenceTargetCode() {
        return referenceTargetCode;
    }

    /**
    * Setter method for Target Reference on Reference Panel.   
    * @param Reference Target code
    */
    public void setReferenceTargetCode(String referenceTargetCode) {
        this.referenceTargetCode = referenceTargetCode;
    }
    
     /**
     * Getter method for to determine which kind of Relationship can
     * be establish between two Registry Object(Reference or Association) 
     * @return String
     */	    
     public String getRelationshipType(){
         return relationship;
     }

     /**
     * Setter method for setting the Relationship type between two 
     * Registry Object(Reference or Association) 
     * @param String
     */	    
     public void setRelationshipType(String newValue) {
         relationship = newValue;
         if(referenceRelation != null){
            if (referenceRelation.equals("Reference")){
                relationship = referenceRelation;
                referenceRelation = null;
             }
         }
         if(this.associationRelation != null){
            if (this.associationRelation.equals("Association")){
                relationship = associationRelation;
                this.associationRelation = null;
            }
         }
     }
   /**
    * Event method for setting the Relationship type between two 
    * Registry Object(Reference or Association) 
    * @param ValueChangeEvent
    */	    
    public void changeReferenceAssociationType(ValueChangeEvent event) {
        if (null != event.getNewValue()){
            String value = (String)event.getNewValue();
            if (value.equals("Association")){ 
                this.setRelationshipType("Association");
                this.initAssocation();
                this.referenceRelation=null;  
            }
        }
    }    


     
     /**
     * Getter method will return the current instance of RelationshipBean 
     * @return RelationshipBean
     */	    
     public RelationshipBean getRelationshipBean() {
             return relationshipBean;
     }
     
     /**
     * Getter method will set the current instance of RelationshipBean 
     * @return RelationshipBean
     */	    
     public void setRelationshipBean(RelationshipBean relationshipBean) {
         this.relationshipBean = relationshipBean;
     }


    /**
    * Getter method for Object Type Reference on Refrence panel
    * @ return String   
    */ 
    public String getReferenceObjectTypeCode() {
        return referenceObjectTypeCode;
    }

    /**
    * Setter method for Object Type Reference on Refrence panel
    * @param String   
    */ 
    public void setReferenceObjectTypeCode(String referenceObjectTypeCode) {
        this.referenceObjectTypeCode = referenceObjectTypeCode;
    }

     /**
     * This method will create a new instance of RelationshipBean if 
     * RelationshipBean instance is null.
     */	    
     private void createNewRelationshipBean(){
         relationshipBean = new RelationshipBean();
     }   

    /**
    * This method pins Registry Objects for Relate operations
    * 
    * @param none
    * @return String
    */
    public String doBookmark(){
        String status = "failure";
        try{
            List selectedObjects = getSelectedRegistryObjectBeans();
            Iterator selectedItr = selectedObjects.iterator();            
            while (selectedItr.hasNext()) {
                RegistryObjectBean selRO = (RegistryObjectBean)selectedItr.next();
                Iterator pinnedItr = pinnedRegistryObjectBean.iterator();
                boolean present = false;
                while (pinnedItr.hasNext()) {
                    RegistryObjectBean pinRO = (RegistryObjectBean)pinnedItr.next();
                    if ((pinRO.getId()).equals(selRO.getId())) {
                        present = true;
                    }
                }
                if (!present) {
                    pinnedRegistryObjectBean.add(selRO);
                }
            }
            refreshSearchExplore();
            status = "showSearchResultsPage";
        } catch(Exception ex) {
            log.error(WebUIResourceBundle.getInstance().getString("message.FailedDuringRelateOperation"), ex);
            RegistryBrowser.getInstance().setErrorMessage(WebUIResourceBundle.getInstance().getString("errorRelateLcm")); 
        }
        return status;
    }

    /**
    * This method removes pinning of RegistryObjects 
    * 
    * @param none
    * @return String
    */
    public String doRemoveBookmark(){
        String status = "failure";
        try{
            Collection selectedROs = getSelectedPinnedRegistryObjects();
            Iterator selectedItr = selectedROs.iterator();
            while (selectedItr.hasNext()) {
                RegistryObjectBean selRO = (RegistryObjectBean)selectedItr.next();
                Iterator pinnedItr = pinnedRegistryObjectBean.iterator();
                while (pinnedItr.hasNext()) {
                    RegistryObjectBean pinRO = (RegistryObjectBean)pinnedItr.next();
                    if ((pinRO.getId()).equals(selRO.getId())) {
                        pinnedRegistryObjectBean.remove(pinRO);
                        break;
                    }
                }
            }
            refreshSearchExplore();
            status = "showSearchResultsPage";
        } catch(Exception ex) {
            log.error(WebUIResourceBundle.getInstance().getString("message.FailedDuringRelateOperation"), ex);
            RegistryBrowser.getInstance().setErrorMessage(WebUIResourceBundle.getInstance().getString("errorRelateLcm")); 
        }
        return status;
    }

    public void refreshSearchExplore() {
        if (registryObjectBeans != null && registryObjectBeans.size() > 0) {
            if (node ==null) {
                refreshSearchPanel();
            } else {
                refreshExplorerPanel();
            }
        }
    }

    /**
    * This method return the Status of Relate RegistryObjects 
    * 
    * @param none
    * @return String
    */
    public String doRelate(){
        String status = "failure";
        try{
            List sROB = getSelectedRegistryObjectBeans();
            List pROB = getSelectedPinnedRegistryObjects();
            if ((sROB.size() + pROB.size()) != 2) {
                append(WebUIResourceBundle.getInstance().getString("selectTwoObjectsForRelate"));
                status = "showSearchResultsPage";
            } else {
                List tempLists = new ArrayList();
                tempLists.addAll(sROB);
                tempLists.addAll(pROB);
                RegistryObjectBean rob1 = (RegistryObjectBean)tempLists.get(0);
                RegistryObjectBean rob2 = (RegistryObjectBean)tempLists.get(1);
                if ((rob1.getId()).equals(rob2.getId())) {
                    append(WebUIResourceBundle.getInstance().getString("selectAnotherRO"));
                    status = "showSearchResultsPage";
                } else {
                    this.currentRegistryObject = null;
                    this.relationshipBean = null;
                    this.createNewRelationshipBean();
                    this.createSelectedROBList();
                    pivotalRegistryObjectBean.clear();
                    pivotalRegistryObjectBean.addAll(sROB);
                    pivotalRegistryObjectBean.addAll(pROB);
                    setRegistryObjectInRelationshipBean(pivotalRegistryObjectBean);
                    this.doInitializeReference();	
                    this.relationshipBean.setReferencedPanelRendered(true);
                    this.selectedRegistryObjectBean.clear();
                    status = "showSearchResultsPage";
                }
            }
        } catch(Exception ex) {
            status = "failure";
            log.error(WebUIResourceBundle.getInstance().getString("message.FailedDuringRelateOperation"), ex);
            RegistryBrowser.getInstance().setErrorMessage(WebUIResourceBundle.getInstance().getString("errorRelateLcm")); 
        }
       
        return status;
    }

     /*
     * This  method will use to set the two  required RegistryObject in 
     * in RelationBean for RelationOperation.  
     * @param list for  
     */	    
     private void setRegistryObjectInRelationshipBean(List registryObjectList){
        if(relationshipBean.getFirstRegistryObject() == null) {
            relationshipBean.setFirstRegistryObject(((RegistryObjectBean)
                pivotalRegistryObjectBean.get(0)).getRegistryObject());
        }
        if (relationshipBean.getFirstRegistryObject() != null &&
        relationshipBean.getSecondRegistryObject() == null) {
            relationshipBean.setSecondRegistryObject(((RegistryObjectBean)
                pivotalRegistryObjectBean.get(1)).getRegistryObject());
        }
  }  

    /**
    * This method do Intialization for Relationship Operation and also 
    * determine what kind of Relation(Reference and Assocation) should be establish 
    * between two Registry Objects.  
    * 
    * @param none
    * @return String
    */
    public String doInitializeReference(){
        String status = "failure";   
        String referenceType = null;
        InternationalString referenceName = null;
        boolean isReferenced = false; 

        try {
            if (pivotalRegistryObjectBean.size() == 2) {
                if (referenceSourceCode != null) {
                    int index = 0;
                    if (referenceSourceCode.equals(((RegistryObjectBean)pivotalRegistryObjectBean.get(0)).getId())) {
                        index = 1;
                        sourceType="source";
                    } else {
                        sourceType="target";
                    }
                    referenceType = ((RegistryObjectBean)pivotalRegistryObjectBean.get(index)).getObjectType();
                    referenceAttribute=((RegistryObjectBean)pivotalRegistryObjectBean.get(index)).getId();
                    referenceName = ((RegistryObjectBean)pivotalRegistryObjectBean.get(index)).getRegistryObject().getName();

                    if (referenceName.getValue() != null) {
                        setReferenceTargetCode(referenceName + "(" + referenceType + ")");
                    } else {
                        setReferenceTargetCode(referenceAttribute + "(" + referenceType + ")");
                    }

                } else {
                    referenceType = ((RegistryObjectBean)pivotalRegistryObjectBean.get(0)).getObjectType();
                    referenceAttribute=((RegistryObjectBean)pivotalRegistryObjectBean.get(1)).getId();
                    referenceName = ((RegistryObjectBean)pivotalRegistryObjectBean.get(1)).getRegistryObject().getName();

                    setReferenceSourceCode((((RegistryObjectBean)pivotalRegistryObjectBean.get(0)).getId()));
                    if (referenceName.getValue() != null) {
                        setReferenceTargetCode( referenceName + "(" + ((RegistryObjectBean)pivotalRegistryObjectBean.get(1)).getObjectType() + ")");
                    } else {
                        setReferenceTargetCode( referenceAttribute + "(" + ((RegistryObjectBean)pivotalRegistryObjectBean.get(1)).getObjectType() + ")");
                    }
                    sourceType="source";
                }

                isReferenced = this.relationshipBean.checkReferenced(sourceType,referenceType);
                                    
                if (isReferenced) {
                    String user1 = ((RegistryObjectImpl)((RegistryObjectBean)pivotalRegistryObjectBean.get(0)).getRegistryObject()).getOwner().getKey().getId();
                    String user2 = ((RegistryObjectImpl)((RegistryObjectBean)pivotalRegistryObjectBean.get(1)).getRegistryObject()).getOwner().getKey().getId();
                    String currentUser  = RegistryBrowser.getDQM().getCallersUser().getKey().getId();

                    isReferenced = (currentUser.equals(user1) && currentUser.equals(user2));
                }
                
                if (isReferenced) {
                    referenceRelation = "Reference";
                    associationRelation = null;

                    setRelationshipType("Reference");
                    refAttributeList = this.relationshipBean.getRefAttributes(sourceType,referenceType);
                }
                if (!isReferenced) {
                    associationRelation="Association";
                    referenceRelation = null;

                    setRelationshipType("Association");
                    relationshipBean.setIsReferencedValid(false);
                    initAssocation();
                    append(WebUIResourceBundle.getInstance().getString("onlyAssociationSourceTarget",
                                new Object[]{this.relationshipBean.getSourceRegistryObjectName(),
                                            this.relationshipBean.getTargetRegistryObjectName()}));
                    clearRelationObjects();
                }
            status="relationSuccessful";
            }
        } catch(Exception ex) {
            log.error(WebUIResourceBundle.getInstance().getString("message.FailedDuringReferenceOperation"), ex);
        }
        return status;
    }

    public String doApplyReference(){
        String status = "failure";   
        try {
            relationshipBean.setRefAttribute(referenceObjectTypeCode);
            status = this.relationshipBean.doApplyReference();

            if(status.equals("relationSuccessful")) {
                this.append("Reference Successful");
                this.relationshipBean.setReferencedPanelRendered(false);
                this.refreshSearchPanel();
                this.clearRelationObjects();
            }
        } catch(Exception ex) {
            log.error(WebUIResourceBundle.getInstance().getString("message.FailedDuringReferenceOperation"), ex);
            RegistryBrowser.getInstance().setErrorMessage(WebUIResourceBundle.getInstance().getString("errorReferenceLcm"));   
        }
        return status;
    }

    /**
    * This method do Intialization for Relationship Operation and also 
    * determine what kind of Relation(Reference and Assocation) should be establish 
    * between two Registry Objects.  
    * 
    * @param none
    * @return String
    */
    private String initAssocation(){
        ArrayList associationList = new ArrayList();
        String sourceType = null;
        String status = "failure";
        try{
            BusinessLifeCycleManagerImpl blcm = 
                RegistryBrowser.getInstance().getBLCM();
            Association assoc = 
                (Association)blcm.createObject(LifeCycleManager.ASSOCIATION);
            VersionInfoType vit = BindingUtility.getInstance().rimFac.createVersionInfoType();
            ((RegistryObjectImpl)assoc).setVersionInfo(vit);
            if(pivotalRegistryObjectBean.size() == 2) {  
                if(this.referenceSourceCode != null){
                if(this.referenceSourceCode.equals(((RegistryObjectBean)pivotalRegistryObjectBean.get(0)).getId())){
                    sourceType="source";
                } else {
                    sourceType="target";
                }
                }else{
                    sourceType="source";
                }
            }
            
            Concept concept = RegistryBrowser.getBQM().findConceptByPath("/" + CanonicalSchemes.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/AccessControlPolicyFor");
            assoc.setSourceObject(this.relationshipBean.getSourceRegistryObject());
            assoc.setTargetObject(this.relationshipBean.getTargetRegistryObject());
            assoc.setAssociationType(concept); 
            RegistryObjectBean rob = new RegistryObjectBean(null, assoc);
            rob.initRelatedObjects();
            currentRegistryObject = rob;
            this.clearRelationObjects();
            status = "relationSuccessful"; 
        } catch(Exception ex){
            log.error(WebUIResourceBundle.getInstance().getString("message.FailedDuringCreationOfAssociationUIInitOperation"), ex);
            RegistryBrowser.getInstance().setErrorMessage(WebUIResourceBundle.getInstance().getString("errorAssociationLcm"));         
        } 
        return status;
    }

   /**
    * This method will clear do the clear operation for Relationship
    * related object. 
    */	
    private void clearRelationObjects(){
        this.selectedROBsize = 0;
        this.selectedRegistryObjectBean.clear();
    }
    
    public String doRemoveRepositoryItem() {
        String status = "failure";
        String result = "";
        if (this.currentRegistryObject != null) {
            RegistryObject ro = 
               this.getCurrentRegistryObjectBean().getRegistryObject();
            if (ro instanceof ExtrinsicObject) {
                ExtrinsicObjectImpl eo = (ExtrinsicObjectImpl) ro;
                try {
                    this.currentRegistryObject.setRepositoryItemRemoved(true);
                    this.currentRegistryObject.setSelected(true);
                    this.currentRegistryObject.setDirty(true);
                    status = "publishSuccessful";
                } catch (Exception ex) {
                    log.error(WebUIResourceBundle.getInstance().getString("message.FailedDuringRemoveOperationOfRepositoryItemOfExtrinsicObject"), ex);
                    RegistryBrowser.getInstance().setErrorMessage(WebUIResourceBundle.getInstance().getString("errorRemoveRepositoryItem"));         

                }
            }
        }
        return status;
    }
    
    public String getFileName(){
        return fileName; 
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }
    
    public void doDeleteFile(){
        if(this.getFileName() != null){
            File uploadFile = null;
            uploadFile = new File(System.getProperty("java.io.tmpdir"),
                this.getFileName()); 
            uploadFile.delete();
            this.fileName = null;
        }
    }
    
    /**
    * This method will upload file in registry for Extrensic Objects 
    * 
    */	
    public String doUpload(File file, String contentType) {
       String status = "failure";
       RegistryObject ro = getCurrentRegistryObjectBean().getRegistryObject();
       if (ro instanceof ExtrinsicObject) {
           if (file == null) {
               append(WebUIResourceBundle.getInstance().getString("messgeFileUpload"));
               log.error(WebUIResourceBundle.getInstance().getString("message.NullFileObjectPassedToDoUploadMethod"));
           } else {
               try {
                   DataHandler handler = new DataHandler(new FileDataSource(file));
                   ((ExtrinsicObject)ro).setRepositoryItem(handler);
                   if (contentType == null) {
                       log.error(WebUIResourceBundle.getInstance().getString("message.NullContentTypePassedToDoUploadMethod"));
                   } else {
                       ((ExtrinsicObject)ro).setMimeType(contentType);
                   }
                   status = "success";
               } catch (JAXRException ex) {
                   String errMsg = WebUIResourceBundle.getInstance().getString("errorUploadingFile");
                   append(errMsg + " " + ex.getMessage());
                   log.error(errMsg + " " + ex.getMessage());
               }
           }
       }
       return status;
   } 
    
    public boolean isObjectVersioned() {
        return isObjectVersioned;
    }
    
    public void setObjectVersioned(boolean isObjectVersioned) {
        this.isObjectVersioned = isObjectVersioned;
    }
    
    public String getDirtyObjectsMessage() {
        String message = null;
        FacesContext context = FacesContext.getCurrentInstance();
        Iterator iter = context.getMessages();
        if ((iter == null || !iter.hasNext()) && registryObjectBeans != null) {
            Iterator itr = registryObjectBeans.iterator();
            StringBuffer sb = null;
            while (itr.hasNext()) {
                RegistryObjectBean rob = (RegistryObjectBean)itr.next();
                if (rob.isDirty() || isPseudoComposedObjectDirty(rob)) {
                    if (sb == null) {
                        sb = new StringBuffer(WebUIResourceBundle.getInstance().getString("modifiedObjects"));                             
                        sb.append("<br>");
                    } 
                    RegistryObject ro = rob.getRegistryObject();
                    try {
                        sb.append(" ");
                        sb.append(WebUIResourceBundle.getInstance().getString("objectType"));
                        sb.append(": ");
                        sb.append(ro.getObjectType().getValue());
                    } catch (JAXRException ex) {
                        sb.append("RegistryObject");
                    }
                    try {
                        sb.append(", ");
                        sb.append(WebUIResourceBundle.getInstance().getString("name"));
                        sb.append(": ");
                        String nameValue = getLocalizedNameString(ro);
                        sb.append(nameValue);
                    } catch (JAXRException ex) {
                    }
                    sb.append("<br>");
                }
            }      
            if (sb == null) {
                message = "";
            } else {
                message = sb.toString();
            }
        }
        return message;
    }
 
    private String getLocalizedNameString(RegistryObject ro) throws JAXRException {
        String nameValue = null;
        LocalizedString lsName = ((InternationalStringImpl)ro.getName())
                                                             .getClosestLocalizedString(getLocale(), getCharset());

        if (lsName != null) {
            nameValue = lsName.getValue();
        }
        if (nameValue == null) {
            nameValue ="";
        }
        return nameValue;
    }
    
    private String getLocalizedDescriptionString(RegistryObject ro) throws JAXRException {
        String descValue = null;
        LocalizedString lsDesc = ((InternationalStringImpl)ro.getDescription())
                                                             .getClosestLocalizedString(getLocale(), getCharset());

        if (lsDesc != null) {
            descValue = lsDesc.getValue();
        }
        if (descValue == null) {
            descValue ="";
        }
        return descValue;
    }

    private boolean isPseudoComposedObjectDirty(RegistryObjectBean bean) {
        boolean isDirty = false;
        // Check all dirty ROBs for pseudo composed objects (PCO)s. 
        // If parent id of PCO matches id of this rob passed, return 'true'
        Iterator itr = registryObjectLookup.keySet().iterator();
        while (itr.hasNext()) {
            String key = (String)itr.next();
            RegistryObjectBean rob = (RegistryObjectBean)registryObjectLookup.get(key);
            RegistryObject ro = rob.getRegistryObject();
            if (ro instanceof Concept &&
                rob.isDirty()) {
                Concept concept = (Concept)ro;
                try {
                    if (concept.getParent() != null) {
                        String parentId = concept.getParent().getKey().getId();
                        if (parentId.equalsIgnoreCase(bean.getId())) {
                            isDirty = true;
                        }
                    }
                } catch (JAXRException ex) {
                    log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotGetId"));
                }
            }
        }
        return isDirty;
    }
   
    public void setNode(RegistryObjectNode node) {
        this.node = node;
    }

    public RegistryObjectNode getNode() {
        return node;
    }

    private void changeAccessURIandTargetBinding(RegistryObjectBean rob){
        if(rob.getAccessURI() != null && rob.getAccessURI().length() > 0){
            if(rob.getTargetBindingForSerBinding() != null && 
                    rob.getTargetBindingForSerBinding().length() > 0) {
                    rob.changeAccessURI();
                    rob.changeTargetBindingForSerBinding();
            } else {
                    rob.changeTargetBindingForSerBinding();
                    rob.changeAccessURI();
            }
        } else {
            rob.changeAccessURI();
            rob.changeTargetBindingForSerBinding();
        }
        
    } 
    
    private boolean checkErrorMessageForCRO(){ 
        boolean status = false;
        if (this.currentRegistryObject.getRegistryObject() 
        instanceof SpecificationLinkImpl) {
            if(this.currentRegistryObject.getspecificationObject() == null){
                if(this.currentRegistryObject.getIsInvalidSpecObj()){
                    status = true;
                    append(WebUIResourceBundle.getInstance().
                    getString("invalidSpecificationObject"));
                    return status;
                }else{
                    status = true;
                    append(WebUIResourceBundle.getInstance().
                        getString("nullSpecificationObject")); 
                    return status;
                }
            }
        }
        if (this.currentRegistryObject.getRegistryObject() instanceof ServiceBindingImpl) {
            this.changeAccessURIandTargetBinding(this.currentRegistryObject);
                if(this.currentRegistryObject.getRegistryObjectErrorMessage() != null){
                    status = true;
                    append(this.currentRegistryObject.getRegistryObjectErrorMessage());
                    this.currentRegistryObject.setRegistryObjectErrorMessage(null);
                    return status;
                }
                if(this.currentRegistryObject.getIsInvalidTargetBinding()) {
                    status = true;
                    append(WebUIResourceBundle.getInstance().
                    getString("invalidTargetBindingObject"));
                    this.currentRegistryObject.setIsInvalidTargetBinding(false);
                    return status;
                }    
        }
        if (this.currentRegistryObject.getRegistryObject() instanceof ExternalLinkImpl) {
            this.currentRegistryObject.changeExternalURI();
            if(this.currentRegistryObject.getRegistryObjectErrorMessage() != null){
                status = true;
                append(this.currentRegistryObject.getRegistryObjectErrorMessage());
                this.currentRegistryObject.setRegistryObjectErrorMessage(null);
                return status;
            }
        }
        if (this.currentRegistryObject.getRegistryObject() 
            instanceof ExternalIdentifier) {
            if(this.checkExternalIdentifier(null,this
            .currentRegistryObject.getRegistryObject()) != null){
                status = true;
                return status;
            }
        }                        
        if (this.currentRegistryObject.getRegistryObject() instanceof RegistryImpl) {
            this.currentRegistryObject.changeOperatorForRegistry();
            if(this.currentRegistryObject.getRegistryObjectErrorMessage() != null){
                status = true;
                append(this.currentRegistryObject.getRegistryObjectErrorMessage());
                this.currentRegistryObject.setRegistryObjectErrorMessage(null);
                return status;
            }
            if(this.currentRegistryObject.getIsInvalidRegistryOperator()) {
                    status = true;
                    append(WebUIResourceBundle.getInstance().
                    getString("invalidRegistryOperator"));
                    this.currentRegistryObject.setIsInvalidTargetBinding(false);
                    return status;
                } 
        }        
      return status;  
    }
    
    public void cacheCurrentROId() throws JAXRException {
        cachedROBId = getCurrentDrilldownRegistryObjectBean().getId();
    }
    
    public String doAddRoToRegistryPackage() {
        String status = "failure";
        String result = null;
        try {
            String memberObjectId = getCurrentDrilldownRegistryObjectBean().getMemberObjectId();
            getCurrentDrilldownRegistryObjectBean().setMemberObjectId(null);
            if (memberObjectId == null || memberObjectId.equals("")) {
                result = WebUIResourceBundle.getInstance()
                               .getString("objectIdIsNull");
            } else {                
                RegistryPackageImpl regPkg = null;
                RegistryObject drilldownRO = getCurrentDrilldownRegistryObjectBean().getRegistryObject();
                if (!(drilldownRO instanceof RegistryPackageImpl)) {
                    if (cachedROBId != null) {
                        RegistryObjectBean rob = (RegistryObjectBean)getRegistryObjectLookup().get(cachedROBId);
                        if (rob != null) {
                            drilldownRO = rob.getRegistryObject();
                            currentRegistryObject = rob;
                        }
                    }
                }
                if (drilldownRO instanceof RegistryPackageImpl) {
                    regPkg = (RegistryPackageImpl)drilldownRO;  
                    RegistryObject ro = 
                        ((BusinessQueryManagerImpl)RegistryBrowser.getBQM())
                                                                  .getRegistryObject(memberObjectId);
                    if (ro == null) {
                        result = WebUIResourceBundle.getInstance()
                                                    .getString("objectIdIsInvalid");
                    } else if (isRoAlreadyAMember(regPkg, memberObjectId)) {
                        result = WebUIResourceBundle.getInstance()
                                                    .getString("objectAlreadyMember");
                    } else {      
                        List searchResultValueBeans = new ArrayList(4);
                        String header = WebUIResourceBundle.getInstance().getString("Details");
                        String columnValue = ro.getKey().getId(); 
                        searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));

                        header = WebUIResourceBundle.getInstance().getString("ObjectType");
                        columnValue = ro.getObjectType().getValue();
                        searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));

                        header = WebUIResourceBundle.getInstance().getString("Name");

                        columnValue = getLocalizedNameString(ro);
                        searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));         


                        header = WebUIResourceBundle.getInstance().getString("Description");
                        columnValue = getLocalizedDescriptionString(ro);
                        searchResultValueBeans.add(new SearchResultValueBean(header, columnValue));

                        currentComposedRegistryObject = new RegistryObjectBean(searchResultValueBeans,
                                                                               ro,
                                                                               false);
                        RegistryObjectBean rob = (RegistryObjectBean)registryObjectLookup.get(memberObjectId);
                        if (rob == null) {
                            registryObjectLookup.put(memberObjectId, currentComposedRegistryObject);
                        }
                    }
                    relatedSearchResults.add(currentComposedRegistryObject);                                    
                    currentComposedRegistryObject.setRelatedSelected(true);
                    currentComposedRegistryObject.setAddRoToRegistryPackage(true);
                    currentRegistryObject.setDirty(true);
                    currentRegistryObject.setSelected(true);
                    result = WebUIResourceBundle.getInstance()
                                                .getString("addSuccessful");  
                    status = "saveSuccessful";
                } else {
                    result = WebUIResourceBundle.getInstance()
                                                .getString("incorrectDrilldownObject",
                                                           new Object[] {"RegistryPackage"});
                }
            }
        } catch (Throwable t) {
            String message = WebUIResourceBundle.getInstance()
                               .getString("couldNotAddRegistryObjectToPackage");
            log.error(message, t);
            result = message + " " + t.getMessage();
        }
        append(result);
        return status;
    }
    
    private boolean isRoAlreadyAMember(RegistryPackageImpl regPkg, String memberObjectId) 
        throws JAXRException {
        boolean isRoAlreadyAMember = false;
        Collection ros = getRegistryObjectsToAddToRegistryPackage();
        if (ros != null) {
            Iterator itr = ros.iterator();
            while (itr.hasNext()) {
                RegistryObjectBean rob = (RegistryObjectBean)itr.next();
                String id = rob.getRegistryObject().getKey().getId();
                if (id.equalsIgnoreCase(memberObjectId)) {
                    isRoAlreadyAMember = true;
                    break;
                }
            }
        }
        return isRoAlreadyAMember;
    }
 
    public String doCancelAddRoToRegistryPackage() {
        getCurrentDrilldownRegistryObjectBean().setMemberObjectId(null);
        String resultString = "showMessagePage";
        String message = WebUIResourceBundle.getInstance()
                                            .getString("createModifyCOCancel");
        FacesContext.getCurrentInstance()
                     .addMessage(null, 
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                     message, null));      
        return resultString;
    }
    
    public String doRemoveRoFromRegistryPackage() {
        String resultString = "failure";
        if (! isUserAllowedToPublish()) {
            resultString = RegistryBrowser.getInstance().getAuthenticationStatus();
        } else {
            RegistryObjectBean rob = getCurrentRegistryObjectBean();
            if (rob == null) {
                return resultString;
            }
            rob.setFormUpdateIgnored(false);
            Iterator itr = new ArrayList(relatedSearchResults).iterator();
            try {
                RegistryPackageImpl registryPackage = (RegistryPackageImpl)currentRegistryObject.getRegistryObject();            
                boolean isAtLeastOneObjectSelected = false;
                while (itr.hasNext()) {
                    rob = (RegistryObjectBean)itr.next();
                    if (rob.isRelatedSelected()) {  
                        // Set a flag to schedule the removal of the RO from the RP
                        // TODO: set the flag in the RegistryPackageImpl class
                        // Removal done when lcm.saveObjects(...) called
                        isAtLeastOneObjectSelected = true;
                        rob.setRemoveRoFromRegistryPackage(true);
                        rob.setAddRoToRegistryPackage(false);
                        currentRegistryObject.setDirty(true);
                        currentRegistryObject.setSelected(true);
                        relatedSearchResults.remove(rob);
                        String id = rob.getRegistryObject().getKey().getId();
                        RegistryObjectBean lookupRob = (RegistryObjectBean)registryObjectLookup.get(id);
                        if (lookupRob == null) {
                            registryObjectLookup.put(id, rob);
                        }
                    }
                }
                if (!isAtLeastOneObjectSelected) {
                    String message = WebUIResourceBundle.getInstance()
                               .getString("noObjectsSelected");
                    FacesContext.getCurrentInstance()
                            .addMessage(null, 
                                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                         message, null));
                }
                resultString = "saveSuccessful";
            } catch (Throwable t) {
                String message = WebUIResourceBundle.getInstance()
                               .getString("couldNotDeleteRoFromRp");
                log.error(message, t);
                message = message + " " + t.getMessage();
                FacesContext.getCurrentInstance()
                            .addMessage(null, 
                                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                         message, null));
            }
        }
        return resultString;
    }
    public String getReadWritePermission(){
        return (String)FacesContext.getCurrentInstance()
                                       .getExternalContext()
                                       .getRequestParameterMap()
                                       .get("readWritePermissionParam");
        
    }

    public String doCancelClassSchemeOrNode(){
        String status = "showMessagePage";
        try{
            SearchPanelBean.getInstance().clearClassSchemeSelector();
            append(WebUIResourceBundle.getInstance().
                    getString("canceledClassificationSchemeNodeSelection"));
        } catch (Throwable t) {
            log.warn(WebUIResourceBundle.getInstance()
            .getString("message.CouldNotRemoveClassificationSchemeSelectedNode")
            , t);            
        }
        return status;
    }     

    /** Getter for List of SelectItems for status types. */
    public List getStatusType_SelectItems() {
        if (statusType_SelectItems == null) {
            statusType_SelectItems = new ArrayList(loadStatusType_SelectItems());
        }
        
        return statusType_SelectItems;
    }

    /** Initialize status types */
    private List loadStatusType_SelectItems() {
        // init items list with label item
        List itemsList = new ArrayList();
        itemsList.add(0, new SelectItem("", WebUIResourceBundle.getInstance().getString("selectStatusType")));
        try {
            ClassificationScheme statusTypeScheme =
            (ClassificationScheme)RegistryBrowser.getBQM().getRegistryObject(
             CanonicalConstants.CANONICAL_CLASSIFICATION_SCHEME_ID_StatusType, LifeCycleManager.CLASSIFICATION_SCHEME);

            for (Iterator it =
                 statusTypeScheme.getChildrenConcepts().iterator(); it.hasNext(); ) {
                ConceptImpl concept = (ConceptImpl)it.next();
                createStatusTypeSelectItems(itemsList, concept, 0);
            }
        } catch(Exception ex){
            log.error(WebUIResourceBundle.getInstance().getString("message.FailedToRetrieveStatusTypeConcepts"), ex);
            RegistryBrowser.getInstance().setErrorMessage(WebUIResourceBundle.getInstance().getString("errorRetrivalOfStausTypes"));
        }
        return itemsList;
    }

    private void createStatusTypeSelectItems(List itemsList, ConceptImpl statusTypeConcept, int level) throws JAXRException {
        // concepts can be nested. Indent the select options
        final String INDENT_STRING = ".";
        String indentPrefix = "";
        for (int i = 0; i < level; i++) {
            indentPrefix += INDENT_STRING;
        }

        // add item for the concept
        itemsList.add(new SelectItem(statusTypeConcept.getId(),
				     indentPrefix + getLocalizedNameString(statusTypeConcept)));

        // add items for its children
        for (Iterator it = statusTypeConcept.getChildrenConcepts().iterator(); it.hasNext(); ) {
            ConceptImpl childConcept = (ConceptImpl)it.next();
            createStatusTypeSelectItems(itemsList, childConcept, level+ 1);
        }
    }    
    
   /**
   * Set selected status on Registry Object.  Returns Failure/Success string for display.
   * 
   * @param none
   * @return String
   */
    public String doStatusOnCurrentROB() {
        String status = "failure";
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
        } else {
            List roList = new ArrayList();
            BulkResponse br = null;
            try {
                RegistryObject ro = (RegistryObject)currentRegistryObject.getRegistryObject();
                roList.add(ro.getKey());

                LifeCycleManagerImpl lcm =  (LifeCycleManagerImpl)ro.getLifeCycleManager();
                br = lcm.setStatusOnObjects(roList, getCurrentRegistryObjectBean().getStatusTypeConcept());

                if (br.getStatus()==0) {
                    this.refreshSearchPanel();
                }
                status = "publishSuccessful";
            } catch (Exception je) {
                log.error(WebUIResourceBundle.getInstance().getString("message.ErrorInSettingStatusOfRegistryObject"), je);
                append(WebUIResourceBundle.getInstance().getString("setStatusROError") + " " +
                       je.getLocalizedMessage()); 
            }
        }
        return status;
    }

    /**
    * This method set the selected Status to selected RegistryObjects 
    * 
    * @param none
    * @return String
    */
    public String doSetStatus() {
        String status = "failure";
        if (! isUserAllowedToPublish()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
        } else if (statusTypeConceptId == null || statusTypeConceptId.equalsIgnoreCase("")) {
            String msg = WebUIResourceBundle.getInstance().getString("message.invalidStatus");
            append(msg);
            // Use this status to navigate back to the Search Results page rather
            // than showing the Error Page: fewer user clicks.
            status = "showSearchPanel";
        } else {
            List roList = new ArrayList();
            BulkResponse br = null;
            try {
                BusinessLifeCycleManagerImpl blcm = RegistryBrowser.getInstance().getBLCM();
                Iterator iter = getSelectedRegistryObjects().iterator();
                while (iter.hasNext()) {
                    RegistryObject registryObject = (RegistryObject)iter.next();
                    roList.add(registryObject.getKey());
                }

                iter = getSelectedPinnedRegistryObjects().iterator();
                while (iter.hasNext()) {
                    RegistryObjectBean registryObjectBean = (RegistryObjectBean)iter.next();
                    roList.add(registryObjectBean.getRegistryObject().getKey());
                }

                br = blcm.setStatusOnObjects(roList, statusTypeConceptId);
                if (br.getStatus()==0){
                    statusTypeConceptId = "";
                    this.refreshSearchPanel();
                }
                status = WebUIResourceBundle.getInstance()
                                               .getString("statusSet");
                append(status);
                status = "publishSuccessful";
            } catch(Exception ex) {
                log.error(WebUIResourceBundle.getInstance().getString("message.ErrorInSettingStatusOfRegistryObject"), ex);
                append(WebUIResourceBundle.getInstance().getString("setStatusROError") + " " +
                       ex.getLocalizedMessage()); 
            }
        }
        return status;
    }

    public String getStatusTypeConcept() throws JAXRException {
        return statusTypeConceptId;
    }

    public void setStatusTypeConcept(String statusTypeConceptId){
        this.statusTypeConceptId = statusTypeConceptId;
    }

    private String checkExternalIdentifier(String status,RegistryObject ro) {
        StringBuffer eiFields = new StringBuffer();
        ExternalIdentifier tempEI = (ExternalIdentifier)ro;
        try{
            if(tempEI.getIdentificationScheme() == null ) {
                status ="failure";
                eiFields.append(WebUIResourceBundle.getInstance().
                getString("classificationScheme"));
            }

            if(tempEI.getValue() == null || tempEI.getValue().length() <= 0) {
                status ="failure";
                if(eiFields.toString().length() != 0){
                    eiFields.append(","+WebUIResourceBundle.getInstance().
                        getString("classificationValueLabel"));
                }else{
                    eiFields.append(WebUIResourceBundle.getInstance().
                    getString("classificationValueLabel"));
                }
            }

            if(eiFields.toString().length() != 0){
                append(WebUIResourceBundle.getInstance().
                getString("message.CouldNotCreateExternalIdentifier")+eiFields.toString());
                return status;
            }
        } catch (Throwable t) {
            status = "failure";
            log.error(WebUIResourceBundle.getInstance().getString("message.FaildToValidateExternalIdentifierFields"), t);
            append(WebUIResourceBundle.getInstance().getString("validationEIROError") +
                     t.getLocalizedMessage()); 
          }
        return status;
    }    
      private void reinitializeSearchPanel(String croID) {
             Map sessionMap = (Map)FacesContext.getCurrentInstance()
                                          .getExternalContext()
                                          .getSessionMap();
            SearchPanelBean bean = (SearchPanelBean)sessionMap.get("searchPanel");
            try {
                if(croID != null && croID.equals(CanonicalConstants.
                        CANONICAL_CLASSIFICATION_SCHEME_ID_StatusType)) {
                        bean.getInstance().setQueryComponent(null);
                }
            } catch(Throwable t) {
                log.error(WebUIResourceBundle.getInstance().getString("message.exceptionOccurredReinitializingStatusSearchPanel"), t);
                t.printStackTrace();
            
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                               WebUIResourceBundle.getInstance().
                                                   getString("message.exceptionOccurredReinitializingStatusSearchPanel"), 
                                                   null));
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                               WebUIResourceBundle.getInstance().
                                                   getString("checkLogForDetails"), 
                                                   null));
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "< " + t + " >", null));
            }
     }

      public void setCurrentRegistryObjectBeanStatusTypeConcept(String statusTypeConceptId){
        if (this.currentRegistryObject != null) {
            this.currentRegistryObject.setStatusTypeConcept(statusTypeConceptId);
        }
      }
      
      public String getCurrentRegistryObjectBeanStatusTypeConcept(){
        if (this.currentRegistryObject != null) {
            try{
                return this.currentRegistryObject.getStatusTypeConcept();
            }catch(Exception ex){}
        }
          return "";
      }
}
