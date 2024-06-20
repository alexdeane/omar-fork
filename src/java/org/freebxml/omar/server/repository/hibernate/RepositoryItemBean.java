/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/repository/hibernate/RepositoryItemBean.java,v 1.5 2004/09/05 23:05:35 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.repository.hibernate;

import java.sql.Blob;
import org.freebxml.omar.server.repository.RepositoryItemKey;

/**
 * A bean to be used to persist a RepositoryItem
 *
 * @author  Diego Ballve / Digital Artefacts
 */
public class RepositoryItemBean {
  
    //TODO: Optimization: Make this implement RepositoryItem directly and we
    //can used the bean cached by hibernate directly
    
    private Blob blobContent;
    private Blob blobSignature;
    private byte binaryContent[];
    private byte binarySignature[];
    private RepositoryItemKey key;
    
    /** Creates a new instance of RepositoryItemBean */
    public RepositoryItemBean() {
    }
    
    /**
     * Getter for property blobContent.
     * @return Value of property blobContent.
     */
    public java.sql.Blob getBlobContent() {
        return blobContent;
    }
    
    /**
     * Setter for property blobContent.
     * @param blobContent New value of property blobContent.
     */
    public void setBlobContent(java.sql.Blob blobContent) {
        this.blobContent = blobContent;
    }
    
    /**
     * Getter for property blobSignature.
     * @return Value of property blobSignature.
     */
    public java.sql.Blob getBlobSignature() {
        return blobSignature;
    }
    
    /**
     * Setter for property blobSignature.
     * @param blobSignature New value of property blobSignature.
     */
    public void setBlobSignature(java.sql.Blob blobSignature) {
        this.blobSignature = blobSignature;
    }
    
    /**
     * Getter for property binaryContent.
     * @return Value of property binaryContent.
     */
    public byte[] getBinaryContent() {
        return this.binaryContent;
    }
    
    /**
     * Setter for property binaryContent.
     * @param binaryContent New value of property binaryContent.
     */
    public void setBinaryContent(byte[] binaryContent) {
        this.binaryContent = binaryContent;
    }
    
    /**
     * Getter for property binarySignature.
     * @return Value of property binarySignature.
     */
    public byte[] getBinarySignature() {
        return this.binarySignature;
    }
    
    /**
     * Setter for property binarySignature.
     * @param binarySignature New value of property binarySignature.
     */
    public void setBinarySignature(byte[] binarySignature) {
        this.binarySignature = binarySignature;
    }

    /**
     * Getter for property key.
     * @return Value of property lid.
     */
    public RepositoryItemKey getKey() {
        return key;
    }
    
    /**
     * Setter for property lid.
     * @param lid New value of property lid.
     */
    public void setKey(RepositoryItemKey key) {
        this.key = key;
    }        
}
