/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/cms/ContentCatalogingServiceImplTest.java,v 1.6 2005/11/21 04:28:21 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.persistence.PersistenceManager;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.freebxml.omar.server.repository.RepositoryManager;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * JUnit TestCase for ContentCatalogingServiceImpl.  Uses the test Cataloging
 * service loaded with DemoDB as the service that the CMSManager invokes.
 */
public class ContentCatalogingServiceImplTest extends ServerTest {
    protected static final String CPPA_CATALOGING_SERVICE_ID = "urn:freebxml:registry:demoDB:test:cms:ContentCatalogingService:cppaCataloging";

    protected static final String CPPA_CATALOGING_OBJECT_TYPE = "urn:uuid:eb11b777-eb16-455e-8837-8c98aae3c0db";
    protected static final String CATALOGING_CONTROL_FILE_FOR_CPPA_CATALOGING_ASSOC_ID =
        "urn:uuid:92418a05-eb1b-4bb5-8579-dc9accb1469a";
    protected static final String CATALOGING_CONTROL_FILE_FOR_CPPA_CATALOGING_EO_ID =
        "urn:uuid:50ea1df2-5bb8-44c0-8d70-a1e18d84001e";
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                                    .getRepositoryManager();
    protected ContentManagementService catalogingServiceImpl = new ContentCatalogingServiceImpl();
    protected static CMSTestUtility cmsTestUtility = CMSTestUtility.getInstance();
    protected UserType registryOperator;
    protected ServiceType cppaCatalogingService;
    protected InvocationController cppaCatalogingIC;
    protected ServerRequestContext context;

    /**
     * Constructor for ContentCatalogingServiceImplTest
     *
     * @param name
     */
    public ContentCatalogingServiceImplTest(String name) {
        super(name);

        try {
            context = new ServerRequestContext("ContentCatalogingServiceImplTest:ContentCatalogingServiceImplTest", null);
            registryOperator = (UserType) qm.getRegistryObject(context, AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR);

            cppaCatalogingService = (ServiceType) qm.getRegistryObject(context, CPPA_CATALOGING_SERVICE_ID);
            cppaCatalogingIC = new InvocationController(CATALOGING_CONTROL_FILE_FOR_CPPA_CATALOGING_ASSOC_ID,
                    CATALOGING_CONTROL_FILE_FOR_CPPA_CATALOGING_EO_ID);

        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            fail("Couldn't initialize objects:\n" + baos);
        }
    }

    public void testInvoke_cppaCataloging() throws Exception {
        RegistryObjectType cppaCatalogingValidEO = cmsTestUtility.createExtrinsicObject("CPPACataloging EO",
                CPPA_CATALOGING_OBJECT_TYPE);
        RepositoryItem cppaCatalogingValidRI = cmsTestUtility.createCPPRepositoryItem(cppaCatalogingValidEO.getId());
        
        try {
            context = new ServerRequestContext("ContentCatalogingServiceImplTest.testInvoke_cppaCataloging", null);
            context.setUser(registryOperator);

            ArrayList eoList = new ArrayList();
            eoList.add(cppaCatalogingValidEO);
            context.getRepositoryItemsMap().put(cppaCatalogingValidEO.getId(),
                cppaCatalogingValidRI);
            pm.insert(context, eoList);
            rm.insert(context, cppaCatalogingValidRI);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }

        cppaCatalogingValidEO = qm.getRegistryObject(context, cppaCatalogingValidEO.getId());
        context.commit();
        
        assertNotNull("ExtrinsicObject from registry should not be null", cppaCatalogingValidEO);

        ServiceOutput output = catalogingServiceImpl.invoke(context, new ServiceInput(
                    cppaCatalogingValidEO, cppaCatalogingValidRI),
                    cppaCatalogingService, cppaCatalogingIC, registryOperator);

        assertTrue("ServiceOutput value should be a RequestContext.",
            output.getOutput() instanceof ServerRequestContext);
    }

    public static Test suite() {
        return new TestSuite(ContentCatalogingServiceImplTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
