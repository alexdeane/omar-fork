/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 - 2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/VersioningTest.java,v 1.5 2007/05/24 14:29:17 anand_mishra Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.RegistryObject;
import org.freebxml.omar.client.xml.registry.infomodel.OrganizationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ServiceImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ClassificationImpl;
import org.freebxml.omar.client.common.ClientTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author anand
 */
public class VersioningTest extends ClientTest {
    
    public static void main(String[] args) {
        try {
            junit.textui.TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
            + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(VersioningTest.class);
        return suite;
    }
    
    /** Creates a new instance of VersioningTests */
    public VersioningTest(String testName) {
        super(testName);
    }
    
    static String serviceId = "urn:Foo:service:MyCoffeeService2";
    static String orgId = "urn:Foo:organization:MyCoffeeOrganization2";
    static String orgId1 = "urn:Foo:organization:MyJavaOrganization";
    static String schemeId = "urn:freebxml:registry:demo:schemes:iso-ch:3166:1999";
    static String classificationId = "urn:Foo:classification:geo:china";

    
    public void testCreateOrgWithService() throws Exception {
        internalTestCreateOrgWithService();
    }
    
    /**
     * Internal method so it can be called repeatedly.
     * Test bug found where ObjectNotFoundException was thrown in
     * AuthorizationServiceImpl when an org with related service
     * was published and then versioned.
     */
    public void internalTestCreateOrgWithService() throws Exception {
        try{
            // pre test clean-up
            deleteIfExist(serviceId, lcm.SERVICE);
            deleteIfExist(orgId, lcm.ORGANIZATION);
            //create service object.
            Service service = (Service)lcm.createService("ServiceTest_Service1");
            service.setKey(lcm.createKey(serviceId));
            ((ServiceImpl)service).setLid(serviceId);
            System.err.println("Adding service with id: " + serviceId);

            ArrayList objects = new ArrayList();
            objects.add(service);

            BulkResponse response = getLCM().saveObjects(objects);
            assertResponseSuccess("Error during save", response);

            //Create Organization
            Organization org = (Organization)lcm.createOrganization("Java Coffee co.");
            org.setKey(lcm.createKey(orgId));
            ((OrganizationImpl)org).setLid(orgId);
            System.out.println("Organization URN is " + orgId);

            //Find service and add to organization.
            Service service1 = (Service) bqm.getRegistryObject(serviceId);
            System.out.println("Service URN is " + serviceId);
            System.err.println("Service found :  " + serviceId);

            // Create services collection, add service to it,
            //  then add services to organization

            Collection services = new ArrayList();
            services.add(service1);
            org.addServices(services);
            System.err.println("Added service to organization");

            // Add organization and submit to registry
            // Retrieve key if successful

            Collection orgs = new ArrayList();
            orgs.add(org);
            // Saving organization
            System.err.println("Saving Organization");
            response = lcm.saveObjects(orgs);
            assertResponseSuccess("Error during save", response);
        }finally{
            // post test clean-up
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, 
                    "SELECT * FROM Service WHERE lid = '" + serviceId + "'");
            BulkResponse br = dqm.executeQuery(query);
            Collection allVersions = br.getCollection();
            Iterator itr = allVersions.iterator();
            while(itr.hasNext()){
                RegistryObject ro = (RegistryObject)itr.next();
                if (!ro.getAssociations().isEmpty()){
                   Iterator itr1 = ro.getAssociations().iterator();
                   while(itr1.hasNext()){
                       deleteIfExist(((RegistryObject)itr1.next()).getKey().getId(),
                               lcm.ASSOCIATION);
                   }
                }
                deleteIfExist(ro.getKey().getId(),lcm.SERVICE);
            }
            deleteIfExist(orgId, lcm.ORGANIZATION);
         }        
    }
    
    public void testComposedObjectDoesNotVersion() throws Exception {
        try{
            //pre test clean 
            deleteIfExist(orgId1, lcm.ORGANIZATION);
            //create Org object.
            Organization org = (Organization)lcm.createOrganization("Org");
            org.setKey(lcm.createKey(orgId1));
            ((OrganizationImpl)org).setLid(orgId1);
            System.err.println("Adding service with id: " + org);

            ArrayList objects = new ArrayList();
            objects.add(org);

            BulkResponse response = getLCM().saveObjects(objects);
            assertResponseSuccess("Error during save", response);

            //Find organization.
            Organization org1 = (Organization) bqm.getRegistryObject(orgId1);

            //Find ClassicationScheme.
            ClassificationScheme cScheme = (ClassificationScheme)bqm.getRegistryObject(schemeId);
            assertNotNull("Did not find scheme", cScheme);

            // Set classification scheme to ISO 3166
            // Create and add classification to org
            Classification classification = lcm.createClassification(cScheme, "China", "CN");
            classification.setKey(lcm.createKey(classificationId));
            ((ClassificationImpl)classification).setLid(classificationId);
            org1.addClassification(classification);

            Collection orgs = new ArrayList();
            orgs.add(org1);        
            BulkResponse response1 = lcm.saveObjects(orgs);
            assertResponseSuccess("Error during save", response1);        

            //Now create a new version of org by simply changing its name
            org1.setName(lcm.createInternationalString((new Date()).toString()));
            orgs.clear();
            orgs.add(org1);        
            response1 = lcm.saveObjects(orgs);
            assertResponseSuccess("Error during save", response1);

            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, 
                    "SELECT * FROM Organization WHERE lid = '" + orgId1 + "'");
            BulkResponse br = dqm.executeQuery(query);
            assertResponseSuccess("Error during query", br);

            Collection allVersions = br.getCollection();
            assertTrue("Failed to find orgs", (allVersions.size() > 0));

            Organization latestVersion = (Organization) allVersions.toArray()[allVersions.size()-1];
            Collection classifications = latestVersion.getClassifications();
            assertTrue("# of classifications not correct", (classifications.size() == 1));

            ClassificationImpl classification1 = (ClassificationImpl)classifications.toArray()[0];
            this.assertTrue("Composed object MUST NOT be versioned but was versioned.", "1.1".equals(classification1.getVersionInfo().getVersionName()));        
        }finally{
            // post test clean-up
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, 
                    "SELECT * FROM Organization WHERE lid = '" + orgId1 + "'");
            BulkResponse br = dqm.executeQuery(query);
            Collection allVersions = br.getCollection();
            Iterator itr = allVersions.iterator();
            while(itr.hasNext()){
                RegistryObject ro = (RegistryObject)itr.next();
                if (!ro.getAssociations().isEmpty()){
                   Iterator itr1 = ro.getAssociations().iterator();
                   while(itr1.hasNext()){
                       deleteIfExist(((RegistryObject)itr1.next()).getKey().getId(),
                               lcm.ASSOCIATION);
                   }
                }
                deleteIfExist(ro.getKey().getId(),lcm.ORGANIZATION);
            }        
        }
    }        
}
