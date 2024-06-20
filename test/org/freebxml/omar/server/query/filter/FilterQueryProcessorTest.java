/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/query/filter/FilterQueryProcessorTest.java,v 1.4 2006/11/04 08:04:39 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.query.filter;

import java.io.File;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.common.IterativeQueryParams;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.query.FilterQueryType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;

/**
 * Test the Filter QUery feature.
 *
 * @author Nikola Stojanovic
 * @author Farrukh Najmi
 *
 */
public class FilterQueryProcessorTest extends ServerTest {
    
    RRFilterQueryProcessor qp = RRFilterQueryProcessor.getInstance();
    org.oasis.ebxml.registry.bindings.query.ResponseOption responseOption = null;
    
    public FilterQueryProcessorTest(java.lang.String testName) {
        super(testName);
        
        try {
            responseOption =
            BindingUtility.getInstance().queryFac.createResponseOption();
            responseOption.setReturnComposedObjects(true);
            responseOption.setReturnType(org.oasis.ebxml.registry.bindings.query.ReturnType.LEAF_CLASS);
        }
        catch (javax.xml.bind.JAXBException e) {
            e.printStackTrace();
        }
    }
    
    //Test a RegistryObjectQuery.
    public void testRegistryObjectQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testRegistryObjectQuery",
                "misc/samples/query/filter/RegistryObjectQuery.xml");
    }

    //Test a RegistryObjectQuery -- with AND.
    public void testRegistryObjectQueryAND() throws Exception {
        doQuery("FilterQueryProcessorTest.testRegistryObjectQueryAND",
                "misc/samples/query/filter/RegistryObjectQueryAND.xml");
    }
    
    //Test a RegistryObjectQuery -- with OR.
    public void testRegistryObjectQueryOR() throws Exception {
        doQuery("FilterQueryProcessorTest.testRegistryObjectQueryOR",
                "misc/samples/query/filter/RegistryObjectQueryOR.xml");
    }
    
    // Test a ClassificationNodeQuery.
    public void testClassificationNodeQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testClassificationNodeQuery", 
                "misc/samples/query/filter/ClassificationNodeQuery.xml");
    }
    
     // Test a ClassificationSchemeQuery.
    public void testClassificationSchemeQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testClassificationSchemeQuery", 
                "misc/samples/query/filter/ClassificationSchemeQuery.xml");
    }
    
    // Test a AssociationQuery.
    public void testAssociationQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testAssociationQuery", 
                "misc/samples/query/filter/AssociationQuery.xml");
    }
    
    // Test a ClassificationQuery.
    public void testClassificationQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testClassificationQuery", 
            "misc/samples/query/filter/ClassificationQuery.xml");
    }
    
    // Test a ExternalIdentifierQuery.
    public void testExternalIdentifierQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testExternalIdentifierQuery", 
            "misc/samples/query/filter/ExternalIdentifierQuery.xml");
    }
    
    // Test a AuditableEventQuery.
    public void testAuditableEventQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testAuditableEventQuery", 
            "misc/samples/query/filter/AuditableEventQuery.xml");
    }
    
    // Test a PersonQuery.
    public void testPersonQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testPersonQuery", 
            "misc/samples/query/filter/PersonQuery.xml");
    }
    
    // Test a UserQuery.
    public void testUserQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testUserQuery", 
            "misc/samples/query/filter/UserQuery.xml");
    }
    
    // Test a ServiceQuery.
    public void testServiceQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testServiceQuery", 
            "misc/samples/query/filter/ServiceQuery.xml");
    }
    
    // Test a ServiceBindingQuery.
    public void testServiceBindingQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testServiceBindingQuery", 
            "misc/samples/query/filter/ServiceBindingQuery.xml");
    }    
    // Test a SpecificationLinkQuery.
    public void testSpecificationLinkQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testSpecificationLinkQuery", 
            "misc/samples/query/filter/SpecificationLinkQuery.xml");
    }
    
    // Test a RegistryQuery.
    public void testRegistryQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testRegistryQuery", 
            "misc/samples/query/filter/RegistryQuery.xml");
    }
    
    /**
     * Submits a FilterQuery from specified file.
     */
    private void doQuery(String contextId, String file) throws Exception {
        ServerRequestContext context = null;
        try {
            context = new ServerRequestContext(contextId, null);
            IterativeQueryParams paramHolder = new IterativeQueryParams(0, -1);
            Unmarshaller unmarshaller = bu.getJAXBContext().createUnmarshaller();
            FilterQueryType query = (FilterQueryType)unmarshaller.unmarshal(new File(file));
            
            Marshaller marshaller = bu.getJAXBContext().createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
            marshaller.marshal(query, System.err);
                        
            RegistryObjectListType rolt = qp.executeQuery(context, AuthenticationServiceImpl.getInstance().farrukh, query, responseOption, paramHolder);
            marshaller.marshal(rolt, System.err);            
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }

    public static Test suite() {
        return new TestSuite(FilterQueryProcessorTest.class);
    }
}
