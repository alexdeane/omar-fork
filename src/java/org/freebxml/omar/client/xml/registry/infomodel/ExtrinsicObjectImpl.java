/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/ExtrinsicObjectImpl.java,v 1.28 2007/04/18 19:10:11 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import javax.xml.bind.JAXBException;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtrinsicObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.RepositoryItem;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;


/**
 * Implements JAXR API interface named ExtrinsicObject.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ExtrinsicObjectImpl extends RegistryEntryImpl
implements ExtrinsicObject {
    private static final Log log = LogFactory.getLog(ExtrinsicObjectImpl.class);
    
    protected String mimeType = null;
    protected boolean opaque = false;
    protected DataHandler repositoryItem = null;
    protected VersionInfoType contentVersionInfo = null;

    public ExtrinsicObjectImpl(LifeCycleManagerImpl lcm)
    throws JAXRException {
        super(lcm);
    }

    public ExtrinsicObjectImpl(LifeCycleManagerImpl lcm,
    ExtrinsicObjectType ebExtrinsicObj) throws JAXRException {
        super(lcm, ebExtrinsicObj);

        mimeType = ebExtrinsicObj.getMimeType();
        opaque = ebExtrinsicObj.isIsOpaque();
        contentVersionInfo = ebExtrinsicObj.getContentVersionInfo();

        try {
            HashMap slotsMap = BindingUtility.getInstance().getSlotsFromRegistryObject(ebExtrinsicObj);
            String slotName = BindingUtility.getInstance().CANONICAL_SLOT_EXTRINSIC_OBJECT_REPOSITORYITEM_URL;
            if (slotsMap.containsKey(slotName)) {
                String riURLStr = (String)slotsMap.get(slotName);
                File riFile = new File(riURLStr);
                repositoryItem = new DataHandler(new FileDataSource(riFile));

                //Remove transient slot
                this.removeSlot(slotName);
            }
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    public Concept getObjectType() throws JAXRException {
        Concept objectType = super.getObjectType();

        if (objectType == null) {
            if (objectType == null) {
                objectType = bqm.findConceptByPath(
                "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject/ExtrinsicObject");

                if (objectType != null) {
                    setObjectType(objectType);
                }
            }
        }

        return objectType;
    }

    /**
     * Set the ObjectType of this ExtrinsicObject's repository item.  This
     * needs to be added to the JAXR 2.0 as an ExtrinsicObject method.
     *
     * @param objectTypeThe ObjectType enumeration value as a Concept
     */
    public void setObjectType(Concept objectType) throws JAXRException {
        setObjectTypeInternal(objectType);
    }

    //??JAXR 2.0
    public void setObjectTypeRef(RegistryObjectRef objectTypeRef)
    throws JAXRException {
        this.objectTypeRef = objectTypeRef;
        setModified(true);
    }

    public String getMimeType() throws JAXRException {
        // Return the mimeType if we know it else ask the repositoryItem
        if ((mimeType == null) && (repositoryItem != null)) {
            mimeType = repositoryItem.getContentType();
        }

        return mimeType;
    }

    public void setMimeType(String par1) throws JAXRException {
        mimeType = par1;
        setModified(true);
    }

    public boolean isOpaque() throws JAXRException {
        return opaque;
    }

    public void setOpaque(boolean par1) throws JAXRException {
        opaque = par1;
        setModified(true);
    }

    public String getContentURI() throws JAXRException {
        //?? This will be removed from JAXR API
        return null;
    }

    public javax.activation.DataHandler getRepositoryItem()
    throws UnsupportedCapabilityException, JAXRException {
        if (repositoryItem != null) {
            return repositoryItem;
        } else if (!isNew()) {
            RepositoryItem item = bqm.getRepositoryItem(getId());
            if (item != null) {
                repositoryItem = item.getDataHandler();
            }
        }

        return repositoryItem;
    }

    /**
     * This method is only added for testing purposes and should not be used by clients.
     */
    public javax.activation.DataHandler getRepositoryItemInternal()
    throws UnsupportedCapabilityException, JAXRException {
        return repositoryItem;
    }

    public void setRepositoryItem(javax.activation.DataHandler dataHandler)
    throws javax.xml.registry.UnsupportedCapabilityException,
    javax.xml.registry.JAXRException {
        setRepositoryItemInternal(dataHandler);
        setModified(true);
    }

    public void setRepositoryItemInternal(javax.activation.DataHandler dataHandler)
    throws javax.xml.registry.UnsupportedCapabilityException,
    javax.xml.registry.JAXRException {
        //TODOD: Make caching of repositoryItem use ehcache with file spillover in future.
        this.repositoryItem = dataHandler;
    }

    public void removeRepositoryItem() throws javax.xml.registry.JAXRException {
        //TODO: mark object as dirty and remove RepositoryItem only on save
        // For now, removin repositoryItem from server immediatelly!
        BulkResponse resp = lcm.deleteObjects(Collections.singletonList(getKey()), null,
            BindingUtility.CANONICAL_DELETION_SCOPE_TYPE_ID_DeleteRepositoryItemOnly);
        if (BulkResponse.STATUS_SUCCESS == resp.getStatus()) {
            // This should be defined in JAXR 2.0 spec
            this.mimeType = null;
            this.repositoryItem = null;
        } else {
            Exception e = (Exception)resp.getExceptions().iterator().next();
            throw new JAXRException(i18nUtil.getString("repositoryitem.removefailed",
            new String[] {getId()}), e);
        }
    }

    public boolean isRepositoryItemPresent() throws JAXRException {
        if (repositoryItem != null) {
            return true;
        } else if (!isNew()) {
            RepositoryItem item = bqm.getRepositoryItem(getId());
            if (item != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add to JAXR 2.0??
     */
    public VersionInfoType getContentVersionInfo() throws JAXRException {
        return contentVersionInfo;
    }

    /**
     * Add to JAXR 2.0??
     */
    public String getContentVersionInfoComment() throws JAXRException {
        String contentVersionInfoComment = "";
        if (contentVersionInfo!=null && contentVersionInfo.getComment() != null){
            contentVersionInfoComment = contentVersionInfo.getComment();
        }
        return contentVersionInfoComment;
    }

    /**
     * Add to JAXR 2.0??
     */
    public void setContentVersionInfoComment(String comment) throws JAXRException {
        try {
            if (contentVersionInfo == null) {
                contentVersionInfo = BindingUtility.getInstance().rimFac.createVersionInfoType();
            }
            contentVersionInfo.setComment(comment);
            setModified(true);
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    /**
     * Add to JAXR 2.0??
     */
    public void setContentVersionInfo(VersionInfoType versionInfo)
    throws JAXRException {
        this.contentVersionInfo = versionInfo;
        setModified(true);
    }

    //Added for convenience in use in config.xml for RegistryBrowser. Should not be in JAXR API
    public String getContentVersionName() throws JAXRException {
        String contentVersionName = "";
        if (contentVersionInfo != null) {
            contentVersionName = contentVersionInfo.getVersionName();
        }
        return contentVersionName;
    }

    //Added for convenience in use in config.xml for RegistryBrowser. Should not be in JAXR API
    public String getContentComment() throws JAXRException {
        String contentComment = "";
        if (contentVersionInfo != null) {
            contentComment = contentVersionInfo.getComment();
        }
        return contentComment;
    }

    public Object toBindingObject() throws JAXRException {
        try {
            org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject ebExtrinsicObj =
            bu.rimFac.createExtrinsicObject();
            setBindingObject(ebExtrinsicObj);

            return ebExtrinsicObj;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

    protected void setBindingObject(
    org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType ebExtrinsicObj)
    throws JAXRException {
        super.setBindingObject(ebExtrinsicObj);

        ebExtrinsicObj.setMimeType(mimeType);
        ebExtrinsicObj.setIsOpaque(isOpaque());

        if (objectTypeRef != null) {
            ebExtrinsicObj.setObjectType(objectTypeRef.getId());
        }
        ebExtrinsicObj.setContentVersionInfo(contentVersionInfo);
    }
    
    /**
     * Gets the size in bytes of repositoryItem associated with this objects (if any). 
     *
     * Add to JAXR 2.0
     *
     * @return if object has repositoryItem return its size in bytes.
     *  if object has no repositoryItem return 0
     */
    public long getRepositoryItemSize() throws JAXRException {
        long size = 0;
        
        InputStream is=null;
        try {
            DataHandler dh  = getRepositoryItem();

            if (dh != null) {
                is = dh.getInputStream();

                while (is.read() != -1) {
                    size++;
                }
            }    
        } catch (IOException e) {
            throw new JAXRException(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                log.error(e, e);
            }
        }
        
        return size;
    }
    
}
