/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/repository/filesystem/FileSystemRepositoryManagerTest.java,v 1.2 2004/09/02 04:06:31 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.repository.filesystem;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.repository.RepositoryManagerTest;

/**
 * Tests for FileSystemRepositoryManager. 
 *
 * IMPORTANT:
 *  This test simply extends the RepositoryItemTest by setting the property to choose
 *  HibernateRepositoryManager.
 *
 * @author Diego Ballve / Digital Artefacts
 */
public class FileSystemRepositoryManagerTest extends RepositoryManagerTest {

    static {
        RegistryProperties.getInstance().put("omar.server.repository.RepositoryManagerFactory.repositoryManagerClass",
            "org.freebxml.omar.server.repository.filesystem.FileSystemRepositoryManager");
    };
    
    public FileSystemRepositoryManagerTest(java.lang.String testName)
	throws IOException {
        super(testName);
        
        rm = RepositoryManagerFactory.getInstance().getRepositoryManager();        
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(FileSystemRepositoryManagerTest.class);
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
