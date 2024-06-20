/*
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.Connection;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.RegistryEntry;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.TelephoneNumber;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.UUIDFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * jUnit Test for LifeCycleManager
 *
 * @author Farrukh Najmi
 */
public class SecureSessionPerformanceTest extends ClientTest {
    
    private static final Log log = LogFactory.getLog(SecureSessionPerformanceTest.class);
    
    public SecureSessionPerformanceTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(SecureSessionPerformanceTest.class);
        return suite;
    }
    
    public void testImlicitSave() throws Exception {
        //Create the pkg that is the main object to save explicitly
        String createSecureSession = 
            ProviderProperties.getInstance()
                              .getProperty("jaxr-ebxml.security.createSecureSession");    
        log.info("jaxr-ebxml.security.createSecureSession: "+
            createSecureSession);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            
            //Create RegistryPackage
            ArrayList saveObjects = new ArrayList();
            RegistryPackage pkg = lcm.createRegistryPackage("SecureSessionPerformanceTest.pkg"+i);
            saveObjects.add(pkg);
            
            //Save RegistryPackage
            BulkResponse br = lcm.saveObjects(saveObjects);      
            assertTrue("Package creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
            
            //Delete RegistryPackage
            ArrayList deleteObjects = new ArrayList();            
            deleteObjects.add(pkg.getKey());
            br = lcm.deleteObjects(deleteObjects);
            assertTrue("Package deletion failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
        long endTime = System.currentTimeMillis();
        log.info("Time to create and delete 10 objects (millisecs): "+
            (endTime - startTime));
    }
        
    public static void main(String[] args) {
	System.out.println("Get into the program...\n");
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}
