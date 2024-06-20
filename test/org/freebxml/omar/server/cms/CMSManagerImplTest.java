/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/cms/CMSManagerImplTest.java,v 1.9 2005/11/21 04:28:21 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import java.net.URL;
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
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * JUnit TestCase for CMSManagerImpl.  Uses the Canonical XML Content
 * Cataloging Service as the service that the CMSManager invokes.
 */
public class CMSManagerImplTest extends ServerTest {
    protected static final String VALIDATION_TEST_ALWAYS_SUCCEED_CLASSIFICATION_NODE_ID =
        "urn:uuid:b0b80d53-331e-4c96-b4ba-a24236462337";
    protected static final String VALIDATION_TEST_THROW_INVALID_CONTENT_EXCEPTION_CLASSIFICATION_NODE_ID =
        "urn:uuid:e2953fd7-5537-4298-aa22-be8cae70dbcc";
    protected static final String VALIDATION_TEST_CPPA_VALIDATION_CLASSIFICATION_NODE_ID =
        "urn:uuid:7e2a7ba4-61c4-4f9d-9262-c2a7a7860d69";
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                                    .getRepositoryManager();
    protected static CMSTestUtility cmsTestUtility = CMSTestUtility.getInstance();
    protected ServerRequestContext context;
    protected CMSManagerImpl manager = new CMSManagerImpl();
    protected URL riURL = getClass().getResource("/resources/CPP1.xml");
    protected UserType registryOperator;

    /**
     * Constructor for CMSManagerImplTest
     *
     * @param name
     */
    public CMSManagerImplTest(String name) {
        super(name);

        try {
            ServerRequestContext context = new ServerRequestContext("CMSManagerImplTest:CMSManagerImplTest", null);
            registryOperator = (UserType) qm.getRegistryObject(context, AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR);
        } catch (Exception e) {
            fail("Could not get RegistryObject for 'RegistryOperator'.");
        }
    }

    /**
     * Creates a new <code>RequestContext</code> for each test.
     */
    protected void setUp() {
        try {
            context = new ServerRequestContext("CMSManagerImplTest.setUp", null);
            context.setUser(registryOperator);
        } catch (Exception e) {
            fail("Couldn't initialise RequestContext.");
        }
    }
    
    /**
     * Invokes the manager on an empty context.  Should succeed.
     *
     * @exception Exception if an error occurs
     */
    public void testInvokeServices_EmptyContext() throws Exception {
        try {
            manager.invokeServices(context);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }

    /**
     * Invokes the manager on a context with one ExtrinsicObject
     * without a corresponding RepositoryItem.  The absence of the
     * RepositoryItem is an error for the Canonical XML Content
     * Cataloging Service.
     *
     * @exception Exception if an error occurs
     */
    public void testInvokeServices_EO_NoRI() throws Exception {
        try {
            String desc = "CMSManagerImplTest.testInvokeServices_EO_NoRI()";

            // Add an ExtrinsicObject to context.
            ExtrinsicObjectType eo = cmsTestUtility.createExtrinsicObject(desc,
                    bu.CPP_CLASSIFICATION_NODE_ID);
            context.getTopLevelObjectsMap().put(eo.getId(), eo);

            manager.invokeServices(context);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }

    public void testInvokeServices_EO_RI() throws Exception {
        try {
            String desc = "CMSManagerImplTest.testInvokeServices_EO_RI()";

            // Add an ExtrinsicObject + RepositoryItem pair to context.
            ExtrinsicObjectType eo = cmsTestUtility.createExtrinsicObject(desc,
                    bu.CPP_CLASSIFICATION_NODE_ID);
            context.getTopLevelObjectsMap().put(eo.getId(), eo);
            context.getRepositoryItemsMap().put(eo.getId(),
                cmsTestUtility.createCPPRepositoryItem(eo.getId()));

            manager.invokeServices(context);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }

    /**
     * Invokes the manager on a context containing multiple
     * ExtrinsicObjects each with a RepositoryItem.  A CMS system that
     * can handle one ExtrinsicObject+RepositoryItem pair won't
     * necessarily be able to handle more than one in a context.
     *
     * @exception Exception if an error occurs
     */
    public void testInvokeServices_2EO_2RI() throws Exception {
        try {
            String desc = "CMSManagerImplTest.testInvokeServices_2EO_2RI()";

            ArrayList eoList = new ArrayList();

            // Add an ExtrinsicObject + RepositoryItem pair to context.
            ExtrinsicObjectType eo = cmsTestUtility.createExtrinsicObject(desc,
                    bu.CPP_CLASSIFICATION_NODE_ID);
            context.getTopLevelObjectsMap().put(eo.getId(), eo);
            eoList.add(eo);

            RepositoryItem ri1 = cmsTestUtility.createCPPRepositoryItem(eo.getId());
            context.getRepositoryItemsMap().put(eo.getId(), ri1);
            eo.setMimeType(ri1.getDataHandler().getContentType());

            // Add another ExtrinsicObject + RepositoryItem pair to context.
            eo = cmsTestUtility.createExtrinsicObject(desc,
                    bu.CPP_CLASSIFICATION_NODE_ID);
            eoList.add(eo);
            context.getTopLevelObjectsMap().put(eo.getId(), eo);

            RepositoryItem ri2 = cmsTestUtility.createCPPRepositoryItem(eo.getId());
            context.getRepositoryItemsMap().put(eo.getId(), ri2);
            eo.setMimeType(ri2.getDataHandler().getContentType());

            pm.insert(context, eoList);
            rm.insert(context, ri1);
            rm.insert(context, ri2);
            manager.invokeServices(context);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }

    /**
     * Invokes the manager on a context containing multiple
     * ExtrinsicObjects each with a RepositoryItem.  A CMS system that
     * can handle one ExtrinsicObject+RepositoryItem pair won't
     * necessarily be able to handle more than one in a context.
     *
     * @exception Exception if an error occurs
     */
    public void testInvokeServices_validationTestThrowInvalidContentException()
        throws Exception {
        try {
            String desc = "CMSManagerImplTest.testInvokeServices_validationTestThrowInvalidContentException()";

            ArrayList eoList = new ArrayList();

            // Add an ExtrinsicObject + RepositoryItem pair to context.
            ExtrinsicObjectType eo = cmsTestUtility.createExtrinsicObject(desc,
                    bu.CPP_CLASSIFICATION_NODE_ID);
            context.getTopLevelObjectsMap().put(eo.getId(), eo);
            eoList.add(eo);

            RepositoryItem ri1 = cmsTestUtility.createCPPRepositoryItem(eo.getId());
            context.getRepositoryItemsMap().put(eo.getId(), ri1);
            eo.setMimeType(ri1.getDataHandler().getContentType());

            // Add another ExtrinsicObject + RepositoryItem pair to context.
            eo = cmsTestUtility.createExtrinsicObject(desc,
                    bu.CPP_CLASSIFICATION_NODE_ID);
            eoList.add(eo);
            context.getTopLevelObjectsMap().put(eo.getId(), eo);

            RepositoryItem ri2 = cmsTestUtility.createCPPRepositoryItem(eo.getId());
            context.getRepositoryItemsMap().put(eo.getId(), ri2);
            eo.setMimeType(ri2.getDataHandler().getContentType());

            pm.insert(context, eoList);
            rm.insert(context, ri1);
            rm.insert(context, ri2);
            manager.invokeServices(context);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }

    public static Test suite() {
        return new TestSuite(CMSManagerImplTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
