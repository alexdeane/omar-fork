/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/security/authorization/HasSlotFunctionTest.java,v 1.2 2006/08/02 14:09:28 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.security.authorization;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.ctx.Status;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.freebxml.omar.server.common.ServerTest;

/**
 *
 * @author Diego Ballve / Digital Artefacts
 *
 */
public class HasSlotFunctionTest extends ServerTest {
    
    static String assId = null;
    static String eoId = null;
    
    public HasSlotFunctionTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new TestSuite(HasSlotFunctionTest.class);
    }
        
    public void testEvaluate() throws Exception {
        HasSlotFunction function = new HasSlotFunction();
        
        //Must be kept in sync with misc/samples/demoDB/SubmitObjectsRequest_Organization.xml
        List inputs = new ArrayList();
        String parentId = "urn:freebxml:registry:demoDB:Sun";
        String name = "urn:freebxml:registry:demo:NASDAQ-Symbol";
        String value = "SUNW";
        inputs.add(AnyURIAttribute.getInstance(parentId));
        inputs.add(AnyURIAttribute.getInstance(name));
        inputs.add(AnyURIAttribute.getInstance(value));
        EvaluationCtx context = null;
        EvaluationResult result = function.evaluate(inputs, context);
        assertTrue(result.getAttributeValue().encode().equalsIgnoreCase("true"));
        
        inputs.clear();
        inputs.add(AnyURIAttribute.getInstance(parentId));
        inputs.add(AnyURIAttribute.getInstance(name));
        inputs.add(AnyURIAttribute.getInstance("junk"));
        result = function.evaluate(inputs, context);
        assertTrue(result.getAttributeValue().encode().equalsIgnoreCase("false"));

        inputs.clear();
        inputs.add(AnyURIAttribute.getInstance(parentId));
        inputs.add(AnyURIAttribute.getInstance("urn:junk"));
        result = function.evaluate(inputs, context);
        assertTrue(result.getAttributeValue().encode().equalsIgnoreCase("false"));
        
        inputs.clear();
        result = function.evaluate(inputs, context);
        assertTrue(result.getStatus().getCode().contains(Status.STATUS_MISSING_ATTRIBUTE));

        inputs.clear();
        inputs.add(AnyURIAttribute.getInstance(parentId));
        result = function.evaluate(inputs, context);
        assertTrue(result.getStatus().getCode().contains(Status.STATUS_MISSING_ATTRIBUTE));        
    }
}