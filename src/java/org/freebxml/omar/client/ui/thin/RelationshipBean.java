/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: 
 * ====================================================================
 */

package org.freebxml.omar.client.ui.thin;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.BulkResponse;

import org.freebxml.omar.client.xml.registry.BusinessLifeCycleManagerImpl;
import org.freebxml.omar.client.ui.common.ReferenceAssociation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Anand
 */

public class RelationshipBean implements Serializable {


    private static final Log log = LogFactory.getLog(RelationshipBean.class);
    private boolean isReferencedPanelRendered = false;
    private boolean isAssociationPanelRendered = false;
    private RegistryObject sourceRegistryObject = null;
    private RegistryObject targetRegistryObject = null;
    private RegistryObject ro1 = null;
    private RegistryObject ro2 = null;
    private ReferenceAssociation refAss = null;
    private String sourceType = null;
    private String objectType = null;
    private boolean isReferencedValid = false;	
    public String sourceRegistryObjectName = null;
    public String targetRegistryObjectName = null;
    private String refAttribute = null;
   
   
    public boolean isReferencedPanelRendered(){
        return isReferencedPanelRendered; 
    } 
    public void setReferencedPanelRendered(boolean isReferencedPanelRendered) {
        this.isReferencedPanelRendered = isReferencedPanelRendered;
    }

    public boolean isAssociationPanelRendered(){
        return isAssociationPanelRendered; 
    } 
    public void setAssociationPanelRendered(boolean isAssociationPanelRendered) {
        this.isAssociationPanelRendered = isAssociationPanelRendered;
    }

    /**
    * Getter method for Source RegistryObject which is used for Relationship 
    * operation between two RegistryObject  
    * @return RegistryObject
    */ 
    public RegistryObject getSourceRegistryObject(){
        return sourceRegistryObject;
    } 

   /**
    * Setter method for Source RegistryObject which is used for Relation 
    * @param RelationObject
    */ 
    public void setSourceRegistryObject(RegistryObject sourceRegistryObject){
        this.sourceRegistryObject = sourceRegistryObject;
    } 

   /**
    * Getter method for target RegistryObject which is used for Relationship 
    * operation between two RegistryObject  
    * @return RegistryObject
    */ 
    public RegistryObject getTargetRegistryObject(){
        return targetRegistryObject;
    } 

   /**
    * Setter method for target RegistryObject which is used for Relationship
    * between two RegistryObject 
    * @param RelationObject
    */ 
    public void setTargetRegistryObject(RegistryObject targetRegistryObject){
        this.targetRegistryObject = targetRegistryObject;
    } 

   /**
    * This method return boolean if Relation between two RegistryObjects 
    * are valid for Reference. 
    * @return RelationObject
    */ 
    public boolean isReferencedValid(){
       return isReferencedValid;
    } 

   /**
    * This method set boolean for Relationship operation between 
    * two RegistryObjects are valid for Reference. 
    * @return RelationObject
    */ 
    public void setIsReferencedValid(boolean isReferencedValid){
       this.isReferencedValid = isReferencedValid;
    } 

    /**
     * Create new instance of ReferenceAssociation Object
     * @return ReferenceAssociation
     */
    private void createReferenceAssociation(){
             refAss = new ReferenceAssociation(sourceRegistryObject,targetRegistryObject);
        }

    /**
     * get instance of ReferenceAssociation Object
     * @return ReferenceAssociation
     */
     private ReferenceAssociation getReferenceAssociation(){
	 return refAss;
     }
     
    public void setFirstRegistryObject(RegistryObject ro1){
        this.ro1 = ro1;
    } 

    public RegistryObject getFirstRegistryObject(){
        return ro1;
    } 
    
    public void setSecondRegistryObject(RegistryObject ro2){
        this.ro2 = ro2;
    } 

    public RegistryObject getSecondRegistryObject(){
        return ro2;
    } 

    
    public String getSourceRegistryObjectName(){
        return sourceRegistryObjectName;
    }
    
    public void setSourceRegistryObjectName(RegistryObject ro){
        try{
        sourceRegistryObjectName = ro.getObjectType().getValue();
            }catch(Exception ex){
            log.error(WebUIResourceBundle.getInstance().getString("message.errorOccuredWhileSettingSourceRegistryObjectNameOperation"),ex);
        }   
    }
    
    public String getTargetRegistryObjectName(){
        return targetRegistryObjectName;
    }
    
    public void setTargetRegistryObjectName(RegistryObject ro){
        try{
            targetRegistryObjectName = ro.getObjectType().getValue();
        }catch(Exception ex){
            log.error(WebUIResourceBundle.getInstance().getString("message.errorOccuredWhileSettingTargetRegistryObjectNameOperation"),ex);
        }   
    }

    

   /**
    * This method return boolean if Relation between two RegistryObjects 
    * are valid for Reference. 
    * @return RelationObject
    */ 
    public boolean checkReferenced(String sourceType,String objectType){
        boolean referenceStatus = false;
        String relationStatus = null;
        this.sourceType = sourceType;
        this.objectType = objectType;
        this.setSourceTargetObject(sourceType);
        
	if(refAss == null){
            this.createReferenceAssociation();		
	}
        relationStatus = this.getReferenceAssociation().getReferenceStatus();

        if (relationStatus.equals("Reference")) {
            referenceStatus=true;
            this.isReferencedValid = true;
        }
        refAss = null;
        return referenceStatus;
    }
    private void switchROs(){
        RegistryObject switchRO = null;
        switchRO =  this.sourceRegistryObject;
        this.sourceRegistryObject = this.targetRegistryObject;
        this.targetRegistryObject = switchRO;
    }

   /**
    * This method return String if Relation between two RegistryObjects 
    * happend sucessfully
    * @return String
    */ 
    public String doApplyReference(){
        String status = "failure";
        List roList = new ArrayList();
        BulkResponse br = null;
        try{    
            BusinessLifeCycleManagerImpl blcm = RegistryBrowser.getInstance().getBLCM();
            this.setSourceTargetObject(sourceType);
	    if(refAss == null){
               this.createReferenceAssociation();		
	    }
            if (refAttribute == null) {
                refAss.setReferenceAttribute(this.objectType);
            } else {
                refAss.setReferenceAttribute(this.refAttribute);
            }
            refAss.setReferenceAttributeOnSourceObject();
            roList.add(sourceRegistryObject);
            roList.add(targetRegistryObject);
            br  = blcm.saveObjects(roList);
            if (br.getStatus() == 0){
                status = "relationSuccessful";
            }
        }catch(Exception ex){
            log.error(WebUIResourceBundle.getInstance().getString("message.errorOccuredWhileDoingDoSaveRefrenceOperation"),ex);
        }   
        return status;
    }
    
    public void setSourceTargetObject(String sourceType){
        if(sourceType.equals("source")){
            sourceRegistryObject = this.getFirstRegistryObject();
            targetRegistryObject = this.getSecondRegistryObject();
            this.setSourceRegistryObjectName(sourceRegistryObject);
            this.setTargetRegistryObjectName(targetRegistryObject);
        }
        else {
            sourceRegistryObject = this.getSecondRegistryObject();
            targetRegistryObject = this.getFirstRegistryObject();
            this.setSourceRegistryObjectName(sourceRegistryObject);
            this.setTargetRegistryObjectName(targetRegistryObject);
        }
    }
    
    public List getRefAttributes(String sourceType,String targetType) {
        String refAttributes[] = null;
        String relationStatus = null;
        List refList = null;

        this.sourceType = sourceType;
        this.objectType = targetType;
        this.setSourceTargetObject(sourceType);
        
	if(refAss == null){
            this.createReferenceAssociation();		
	}
        relationStatus = this.getReferenceAssociation().getReferenceStatus();

        if (relationStatus.equals("Reference")) {
            this.isReferencedValid = true;
            refAttributes = refAss.getReferenceAttributes();
        }
        refAss = null;
        if (refAttributes != null) {
            refList = java.util.Arrays.asList(refAttributes);
        }
        
        return refList;
    }

    public void setRefAttribute(String refAttribute) {
        this.refAttribute = refAttribute;
    }

 }
