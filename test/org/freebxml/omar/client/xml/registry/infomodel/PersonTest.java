/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/PersonTest.java,v 1.2 2006/12/07 02:36:23 farrukh_najmi Exp $
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
import java.util.*;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.User;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.TelephoneNumber;

import java.net.*;

/**
 * jUnit Test for Person
 *
 * @author <a href="Mohammed.Fazuluddin@Sun.com">Fazul</a>
 */
public class PersonTest extends ClientTest {

    PersonImpl personImpl;
    public PersonTest(String testName) {
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
        TestSuite suite = new TestSuite(PersonTest.class);
        return suite;
    }
    
    public void testAddTelephoneNumbers() throws Exception {
        TelephoneNumber tn1, tn2, tn3 = null;
        BulkResponse br= null;
        List personsList = new ArrayList();
        
        PersonImpl person = createPerson("TestPersonWithTN");

        tn1 = lcm.createTelephoneNumber();
        tn1.setType("Office Phone");
        
        tn2 = lcm.createTelephoneNumber();
        tn2.setType("Mobile Phone");

        tn3 = lcm.createTelephoneNumber();
        tn3.setType("Fax");
        
        List tels = new ArrayList();
        tels.add(tn1);
        tels.add(tn2);
        tels.add(tn3);
        
        person.setTelephoneNumbers(tels);
        personsList.add(person);
        lcm.saveObjects(personsList);

        PersonImpl retrievePerson = (PersonImpl)dqm.getRegistryObject(person.getKey().getId());
        assertNotNull("person was not saved", retrievePerson);
        
        Collection retList = retrievePerson.getTelephoneNumbers("Fax");
        assertEquals("Count of Telephone Numbers returned from Person should be 1.", 1, retList.size());
    }
             
    public void testUrl() throws JAXRException,Exception{
        
        //Creating a new User
        User user =lcm.createUser();
        
        //Adding URL to User
        URL url =new URL("http://TheCoffeeBreak.com/JaneMDoe.html");
        user.setUrl(url);
             
        PersonName personName = lcm.createPersonName("Fazuluddin","","Mohammed");
        user.setPersonName(personName);

        //Adding PostalAddress to User
        Collection postalAddr = new ArrayList();
        PostalAddress postalAddress = lcm.createPostalAddress("1112","Longford","Bangalore","Karnataka","India","600292","String");
        postalAddr.add(postalAddress);
        user.setPostalAddresses(postalAddr);

        //Adding User Informaion to Collection Object and Saving to Registry
        Collection userObject =new ArrayList();
        userObject.add(user);
        lcm.saveObjects(userObject);
        
        User user1  = (User)bqm.getRegistryObject(user.getKey().getId(), LifeCycleManager.USER);
        
        //Testing with expected result
        assertEquals(url,user1.getUrl());
                    
    }

}
