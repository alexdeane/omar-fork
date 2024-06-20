/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/AdminFunctionContext.java,v 1.9 2006/02/08 18:38:38 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.PrintStream;

import java.util.Collection;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.xml.registry.infomodel.RegistryPackage;


public class AdminFunctionContext {
    private static Log log = LogFactory.getLog(AdminFunctionContext.class.getName());
    private static AdminFunctionContext instance; //singleton instance

    private File localDir;
    private RegistryPackage currentRP;
    private boolean debug;
    private String editor;
    private String lastException;
    private PrintStream outStream;
    private Properties properties;
    private Collection registryObjects;
    private JAXRService service;
    private String[] users;
    private boolean verbose;

    /** Creates a new instance of AdminFunctionContext */
    protected AdminFunctionContext() {
    }

    /**
     * Gets the singleton instance of
     * <code>AdminFunctionContext</code>.
     *
     * @return an <code>AdminFunctionContext</code> value
     */
    public synchronized static AdminFunctionContext getInstance() {
        if (instance == null) {
            instance = new AdminFunctionContext();
        }

        return instance;
    }

    /**
     * Gets the current value of currentRP.
     *
     * @return the current currentRP value.
     */
    public RegistryPackage getCurrentRP() {
        return currentRP;
    }

    /**
     * Sets the value of currentRP.
     *
     * @param currentRP the new value
     */
    public void setCurrentRP(RegistryPackage currentRP) {
        this.currentRP = currentRP;
    }

    /**
     * Gets the current value of debug.
     *
     * @return the current debug value.
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Sets the value of debug.
     *
     * @param debug the new value
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Gets the current value of editor.
     *
     * @return the current editor value.
     */
    public String getEditor() {
        return editor;
    }

    /**
     * Sets the value of editor.
     *
     * @param editor the new value
     */
    public void setEditor(String editor) {
        this.editor = editor;
    }

    /**
     * Gets the current value of lastException.
     *
     * @return the current lastException value.
     */
    public String getLastException() {
        return lastException;
    }

    /**
     * Sets the value of lastException.
     *
     * @param lastException the new value
     */
    public void setLastException(String lastException) {
        this.lastException = lastException;
    }

    /**
     * Gets the current value of localDir.
     *
     * @return the current localDir value.
     */
    public File getLocalDir() {
        return localDir;
    }

    /**
     * Sets the value of localDir.
     *
     * @param localDir the new value
     */
    public void setLocalDir(File localDir) {
        this.localDir = localDir;
    }

    /**
     * Gets the current value of outStream.
     *
     * @return the current outStream value.
     */
    public PrintStream getOutStream() {
        return outStream;
    }

    /**
     * Sets the value of outStream.
     *
     * @param outStream the new value
     */
    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }

    /**
     * Gets the current value of properties.
     *
     * @return the current properties value.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the value of properties.
     *
     * @param properties the new value
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Gets the current value of registryObjects.
     *
     * @return the current registryObjects value.
     */
    public Collection getRegistryObjects() {
        return registryObjects;
    }

    /**
     * Sets the value of registryObjects.
     *
     * @param registryObjects the new value
     */
    public void setRegistryObjects(Collection registryObjects) {
        this.registryObjects = registryObjects;
    }

    /**
     * Gets the current value of service.
     *
     * @return the current service value.
     */
    public JAXRService getService() {
        return service;
    }

    /**
     * Sets the value of service.
     *
     * @param service the new value
     */
    public void setService(JAXRService service) {
        this.service = service;
    }

    /**
     * Gets the current value of users.
     *
     * @return the current users value.
     */
    public String[] getUsers() {
        return users;
    }

    /**
     * Sets the value of users.
     *
     * @param users the new value
     */
    public void setUsers(String[] users) {
        this.users = users;
    }

    /**
     * Gets the current value of verbose.
     *
     * @return the current verbose value.
     */
    public boolean getVerbose() {
        return verbose;
    }

    /**
     * Sets the value of verbose.
     *
     * @param verbose the new value
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Verifies that the current settings are correct and consistent.
     *
     * @exception AdminException if an error occurs
     */
    public void verifySettings() throws AdminException {
    }

    public void printMessage(String string) {
        outStream.println(string);
    }

    public void printMessage() {
        outStream.println();
    }
}
