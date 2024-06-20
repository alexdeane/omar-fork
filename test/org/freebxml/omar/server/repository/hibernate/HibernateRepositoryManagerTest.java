/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/repository/hibernate/HibernateRepositoryManagerTest.java,v 1.6 2006/06/07 20:39:43 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.repository.hibernate;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.repository.RepositoryManagerTest;

/**
 * Tests for HibernateRepositoryManager. 
 *
 * IMPORTANT:
 *  This test simply extends the RepositoryItemTest by setting the property to choose
 *  HibernateRepositoryManager.
 *
 * @author Diego Ballve / Digital Artefacts
 */
public class HibernateRepositoryManagerTest extends RepositoryManagerTest {

    static {
        RegistryProperties.getInstance().put("omar.server.repository.RepositoryManagerFactory.repositoryManagerClass",
            "org.freebxml.omar.server.repository.hibernate.HibernateRepositoryManager");
    };
    
    public HibernateRepositoryManagerTest(java.lang.String testName) throws IOException {
        super(testName);
        
        rm = RepositoryManagerFactory.getInstance().getRepositoryManager();        
    }
    
    public static Test suite() throws IOException {
        TestSuite suite = new TestSuite(HibernateRepositoryManagerTest.class);
        //TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new HibernateRepositoryManagerTest("testInsertRI2M"));        
        return suite;
    }
    

    public static void main(String[] args) {
        try {
            junit.textui.TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}
