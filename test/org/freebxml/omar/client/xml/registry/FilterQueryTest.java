/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/FilterQueryTest.java,v 1.1 2005/03/28 19:03:11 farrukh_najmi Exp $
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.io.File;
import java.io.StringWriter;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.Query;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.oasis.ebxml.registry.bindings.query.FilterQueryType;


/**
 * jUnit Test for testing Filter Queries using JAXR API.
 *
 * @author Farrukh Najmi
 */
public class FilterQueryTest extends ClientTest {
    
    public FilterQueryTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(FilterQueryTest.class);
        return suite;
    }
    
    /*
     * Tests a RegistryObjectQuery.
     */
    public void testRegistryObjectQuery() throws Exception {
        //Read RegistryObjectQuery from a file and write it to a String
        Unmarshaller unmarshaller = bu.getJAXBContext().createUnmarshaller();
        FilterQueryType fq = (FilterQueryType)unmarshaller.unmarshal(new File("misc/samples/query/filter/RegistryObjectQuery.xml"));

        Marshaller marshaller = bu.getJAXBContext().createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
        
        StringWriter sw = new StringWriter();
        marshaller.marshal(fq, sw);
        String queryStr = sw.toString();
        
        Query query = dqm.createQuery(Query.QUERY_TYPE_EBXML_FILTER_QUERY, queryStr);
        BulkResponse resp = dqm.executeQuery(query);
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
