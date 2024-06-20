/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/FindServiceTest.java,v 1.1 2006/03/28 01:08:06 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Service;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import javax.xml.registry.infomodel.Key;

/**
 * Duplicate a TCK test to reproduce and verify bug fix for Sun bug 6392404
 *
 * @author dianne.jiao@sun.com
 */
public class FindServiceTest extends ClientTest {
    
    static String serviceId = "urn:uuid:2d97634e-c8d2-4fef-b57a-d3987dce16bd";
    static String org1Id = "urn:uuid:b89412f8-dc03-46f8-9cc6-c0c938f44349";
    static String org2Id = "urn:uuid:799595b5-dc26-4b48-b437-c9b34e0b3699";
        
    public FindServiceTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(FindServiceTest.class);
        return suite;
    }
    
    /** 
     *
     * Create and save an organization.
     * Create and save a service.
     * Request only service be returned. 
     * Verify that only the service is returned.
     */

    public void testgetRegistryObjectsSpecifiedType() throws Exception {        
        
	Key orgkey = null;
	String myServiceName = "TCK_TEST_SERVICE";
	Collection orgKeys = null;
        boolean pass = false;
        
	try {
	    System.out.println("Create a service and an organization \n");
	    Service service = lcm.createService(myServiceName);
	    Collection myServices = new ArrayList();

	    // create an organization
	    Organization org = (Organization)lcm.createObject(lcm.ORGANIZATION);

	    org.setName(lcm.createInternationalString("CTS_Test_ORG"));
	    // publish the organization
	    Collection orgs = new ArrayList();
	    orgs.add(org);
	    System.out.println("Save the organization and get the key id from getCollection\n");
	    BulkResponse br = lcm.saveOrganizations(orgs); // publish to registry

	    orgKeys = br.getCollection();
	    Iterator iter = orgKeys.iterator();
            while ( iter.hasNext() ) {
		orgkey = (Key) iter.next();
            }
            String orgKeyId = orgkey.getId();
            System.out.println("Saved Organization key id is: " + orgKeyId + "\n");
            System.out.println("Call getRegistryObjects to get all owned objects\n");
            br = bqm.getRegistryObjects();
            System.out.println("Find the saved organization and add the service to it \n");
           // get the org back
           Collection ros = br.getCollection();
           Organization o = null;
           iter = ros.iterator();
           String regKeyId = null;
           while ( iter.hasNext() ) {
               Object obj = iter.next();
               if (obj instanceof Organization) {
                  o = (Organization)obj;
                  if( o.getKey().getId().equals(orgKeyId) ) {
                    System.out.println("Found the organization\n");
                    regKeyId = o.getKey().getId();
                    o.addService(service);
                    myServices.add(service);
                    break;
	    	  }
               }
            }
	    if ( o == null ) {
            	System.out.println("Error: failed to get the Organization with getRegistryObjects \n");
            }
//==
	    System.out.println("save the service to the registry \n");
	    br = lcm.saveServices(myServices);
	    Key servicekey = null;
	    Collection serviceKeys = br.getCollection();
            System.out.println("The number of service keys returned from getCollection is: " + serviceKeys.size() + "\n");
	    iter = serviceKeys.iterator();
	    while ( iter.hasNext() ) {
	    	servicekey = (Key) iter.next();
            }
	    System.out.println("Save the service key returned from saveServices\n");
	    String serviceKeyId = servicekey.getId();
	    System.out.println("Saved Service key id is: " + serviceKeyId +  "\n");
System.out.println("request service objects with getRegistryObjects(LifeCycleManager.SERVICE) \n");
	     br = bqm.getRegistryObjects(LifeCycleManager.SERVICE);
	     // br = bqm.getRegistryObjects();

	    Collection myObjects = br.getCollection();
            System.out.println("Count of objects returned from service request is: " + myObjects.size() + "\n");
	    if ( myObjects.size() == 0 )
            	 System.out.println(" failed - nothing returned from getRegistryObjects");

	    iter = myObjects.iterator();
	    RegistryObject ro = null;

	    while ( iter.hasNext() ) {
	      ro = (RegistryObject)iter.next();
	      if ( ro instanceof Service) {
	    	  System.out.println(" ro is a Service \n");
	    	  if ( ro.getKey().getId().equals(serviceKeyId) ) {
	              System.out.println("Got back my service - Good! \n");
	              pass = true;
	    	  }
	      } else if ( !( ro instanceof Service )) {
	    	  System.out.println(" returned ro not a service! " + ro.toString() + "\n");
              }
            }

	  } catch  (Exception e) {
	    System.out.println("Caught exception: " + e.getMessage());
	    e.printStackTrace();
	  }finally {
	      System.out.println("cleanup at test end \n");
	      //super.cleanUpRegistry(orgKeys, LifeCycleManager.ORGANIZATION);
          }

	assert(pass);

    }

    
    public static void main(String[] args) {
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}
