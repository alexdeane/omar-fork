/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/query/SQLQueryProcessorTest.java,v 1.12 2007/03/21 14:24:34 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.query;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.common.IterativeQueryParams;
import org.freebxml.omar.server.query.sql.SQLQueryProcessor;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;

/**
 *
 * @author najmi
 */
public class SQLQueryProcessorTest extends ServerTest {
    
    SQLQueryProcessor qp = SQLQueryProcessor.getInstance();
    org.oasis.ebxml.registry.bindings.query.ResponseOption responseOption = null;
    
    public SQLQueryProcessorTest(java.lang.String testName) {
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

    /*
     * Tests that SQLParser supports function calls
     * in SQLSelectCols
     */
    public void testFunctionInSelectCols() throws Exception {
        ServerRequestContext context = null;
        try {
            context = new ServerRequestContext("SQLQueryProcessorTest.testFunctionInSelectCols", null);
            String query=" SELECT o1.* FROM ExtrinsicObject o1 where o1.lid = 'urn:freebxml:registry:VersioningTest:TestExtrinsicObject' AND o1.versionname IN ( SELECT MAX ( o2.versionname ) FROM ExtrinsicObject o2 WHERE o2.lid = o1.lid )";
            IterativeQueryParams paramHolder = new IterativeQueryParams(0, -1);
            RegistryObjectListType rolt = qp.executeQuery(context, AuthenticationServiceImpl.getInstance().farrukh, query, responseOption, paramHolder);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
       
    }    
    
    /**
     * Test of submitAdhocQuery method, of class org.freebxml.omar.server.query.SQLQueryProcessorImpl.
     */
    public void testNullNameAndDesc() throws Exception {
        ServerRequestContext context = null;
        try {
            context = new ServerRequestContext("SQLQueryProcessorTest.testNullNameAndDesc", null);
            String query=" SELECT ro.* from RegistryObject ro, Name_ nm, Description d WHERE (1=1)  AND (objecttype IN (  SELECT id  FROM ClassificationNode WHERE path LIKE \'/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject/RegistryPackage%\'))  AND (nm.parent = ro.id AND nm.value LIKE \'$name\' )  AND (d.parent = ro.id AND d.value LIKE \'$description\' )  AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id  FROM ClassificationNode WHERE path LIKE \'$classificationPath1%\' ) ))  AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id  FROM ClassificationNode WHERE path LIKE \'$classificationPath2%\' ) ))  AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id  FROM ClassificationNode WHERE path LIKE \'$classificationPath3%\' ) ))  AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id  FROM ClassificationNode WHERE path LIKE \'classificationPath4%\' ) ))";
            //String query=" SELECT ro.* from RegistryObject ro, Name_ nm, Description d WHERE (1=1)  AND (objecttype IN (  SELECT id  FROM ClassificationNode WHERE path LIKE \'/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject/RegistryPackage%\'))  AND (nm.parent = ro.id AND nm.value LIKE \'$name\' )";
            IterativeQueryParams paramHolder = new IterativeQueryParams(0, -1);
            RegistryObjectListType rolt = qp.executeQuery(context, AuthenticationServiceImpl.getInstance().farrukh, query, responseOption, paramHolder);
            System.err.println();
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
       
    }
    
    
    
    public static Test suite() {
        return new TestSuite(SQLQueryProcessorTest.class);
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    
}
