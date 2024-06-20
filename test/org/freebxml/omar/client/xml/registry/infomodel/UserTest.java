/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/UserTest.java,v 1.5 2005/01/31 20:32:36 farrukh_najmi Exp $
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.User;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl;

/**
 * jUnit Test for User
 *
 * @author <a href="mailto:wannes.sels@cronos.be">Wannes Sels</a>
 */
public class UserTest extends ClientTest {

    public UserTest(String testName) {
        super(testName);
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

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(UserTest.class);
        return suite;
    }

    public void testGetOrganization() throws Exception {                    
        // create a User
        User user = createUser("Farid");
        
        // create the default organization
        Organization org  = createOrganization("ScienceTeam");
        Collection users = new ArrayList();
        users.add(user);
        org.addUsers(users);
        Collection orgs = new ArrayList();
        orgs.add(org);
        BulkResponse br = lcm.saveOrganizations(orgs); // publish to registry

        // Find the published organization
        Organization pubOrg = (Organization)bqm.getRegistryObject(org.getKey().getId(), LifeCycleManager.ORGANIZATION);
        assertNotNull("org not found after saving it.", pubOrg);
        
        //debug.add("Get the Users from this organization \n");
        users.clear();
        users.addAll(pubOrg.getUsers());
        assertNotNull("org.getUsers() returned null.", users);        
        assertTrue("org.getUsers() returned no users.", (users.size() > 0));
        
        if (!(users.contains((user)))) {
            System.err.println("users=" + users + " looking for=" + user);
        }
        assertTrue("Did not find my user in org.getUsers() after saving org.", (users.contains((user))));
        
        //debug.add("Now get the organization from the User \n");
        Organization testOrg = user.getOrganization();
        assertEquals("user.getOrganization() a different org.", testOrg, pubOrg);
        
        users.clear();
        users.addAll(testOrg.getUsers());
        assertNotNull("testOrg.getUsers() returned null.", users);        
        assertTrue("testOrg.getUsers() returned no users.", (users.size() > 0));        
        assertTrue("Did not find my user in testOrg.getUsers() after saving org.", (users.contains((user))));
    }
    
    public void testQueryEmailAddressFromUser() throws Exception {

        //based on demo data

        String FarrukhId = "urn:freebxml:registry:predefinedusers:farrukh";
        LifeCycleManager lcm = getLCM();
        DeclarativeQueryManagerImpl dqm = (DeclarativeQueryManagerImpl)getDQM();

        //get the user
        User u = (User)dqm.getRegistryObject(FarrukhId);
        Assert.assertNotNull("User was not found when queried by id", u);

        //get the email addresses
        Collection EmailAddresses = u.getEmailAddresses();
        Assert.assertNotNull("User must not have null for EmailAddresses",EmailAddresses);

        Assert.assertEquals ("User does not have expected number of Email Adresses",2,EmailAddresses.size());

    }
}