/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/thin/FileUploadBean.java,v 1.2 2005/04/11 08:23:59 doballve Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.thin;

import java.io.File;
import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletInputStream;
import javax.faces.context.FacesContext;


/**
 *
 * @author Anand
 */

public class FileUploadBean implements Serializable {
    
    private final static long FILECHECKLENGTHBYTES = 30 * 1024; 
    
    private static Log log = LogFactory.getLog(FileUploadBean.class);

    private String fileName = null;
    private long fileSize = 0;
    private File file = null;
    private String contentType =  null;
    private boolean isFileLengthMore = false;
    private String absolutePath = null;
    
    /** Creates a new instance of FileUploadBean */
    public FileUploadBean() {
    }
    
    public String getFileName(){
        return fileName;
    }
    public String getContentType(){
        return contentType;
    }
    
    public long getFileSize(){
        return fileSize;
    } 
    
    public File getFile(){
        return file;
    }
    
    public String getAbsolutePath(){
        return absolutePath;
    }
    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public void setContentType(String contentType){
        this.contentType = contentType;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public void setFile(File file) {
        this.file =  file;
    }
    
    public void setAbsolutePath(String absolutePath){
        this.absolutePath = absolutePath;
    }
    
    public boolean isFileLengthMore(){

        if(file != null && fileSize > 0){
            if (fileSize > this.FILECHECKLENGTHBYTES){
                return true;
            }  
        }
        return false;
    }
   
    public static FileUploadBean getInstance() {
        FileUploadBean fileUploadBean = 
                (FileUploadBean)FacesContext.getCurrentInstance()
                                                          .getExternalContext()
                                                          .getSessionMap()
                                                          .get("fileUploadBean");
        if (fileUploadBean == null) {
            fileUploadBean = new FileUploadBean();
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getSessionMap()
                        .put("fileUploadBean", fileUploadBean);
        }
        return fileUploadBean;
    }
    
    /** 
     * Clear (reset) all the properties in this bean.
     */
    public void doClear() {
        if (log.isDebugEnabled()) {
            log.debug("doClear started");
        }
        doDeleteFile();
        fileName = null;
        fileSize = 0;
        file = null;
        contentType =  null;
        isFileLengthMore = false;
        absolutePath = null;
    }

    /** 
     * Deletes the file pointed by 'file' property.
     */
    public void doDeleteFile() {
        if (log.isDebugEnabled()) {
            log.debug("doDeleteFile started");
        }
        if (file != null) {
            file.delete();
        }
    }

}