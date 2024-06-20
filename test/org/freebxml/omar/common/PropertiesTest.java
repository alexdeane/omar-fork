/*
 * ====================================================================
 * 
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2002-2004 freebxml.org.  All rights reserved.
 * 
 * ====================================================================
 */
package org.freebxml.omar.common;

import junit.framework.TestCase;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.server.common.RegistryProperties;

/**
 * @author Diego Ballve
 */
public class PropertiesTest extends TestCase {

    /**
     * Constructor for PropertiesTest.
     * @param name
     */
    public PropertiesTest(String name) {
        super(name);
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        //this.endorsedDirs = System.getProperty("java.endorsed.dirs");
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCommonGetInstance() {
        CommonProperties props = CommonProperties.getInstance();
        assertNotNull(props);
    }

    public void testProviderGetInstance() {
        ProviderProperties props = ProviderProperties.getInstance();
        assertNotNull(props);
    }

    public void testRegistryGetInstance() {
        RegistryProperties props = RegistryProperties.getInstance();
        assertNotNull(props);
    }

    public void testCommonReloadProperties() {
        CommonProperties props = CommonProperties.getInstance();
        assertNotNull(props);
        props.reloadProperties();
        props.getProperty("omar.home");
    }

    public void testProviderReloadProperties() {
        ProviderProperties props = ProviderProperties.getInstance();
        assertNotNull(props);
        props.reloadProperties();
        props.getProperty("jaxr-ebxml.home");
    }

    public void testRegistryReloadProperties() {
        RegistryProperties props = RegistryProperties.getInstance();
        assertNotNull(props);
        props.reloadProperties();
        props.getProperty("omar.home");
    }
    
}
