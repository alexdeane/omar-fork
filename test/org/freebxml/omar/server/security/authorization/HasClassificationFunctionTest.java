/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/security/authorization/HasClassificationFunctionTest.java,v 1.1 2006/03/08 10:19:30 doballve Exp $
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
public class HasClassificationFunctionTest extends ServerTest {
    
    static String assId = null;
    static String eoId = null;
    
    public HasClassificationFunctionTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new TestSuite(HasClassificationFunctionTest.class);
    }
        
    public void testEvaluate() throws Exception {
        HasClassificationFunction function = new HasClassificationFunction();
        
        //Must be kept in sync with misc/samples/minDB/SubmitObjectsRequest_SubjectRoleScheme.xml
        List inputs = new ArrayList();
        String parentId = "urn:freebxml:registry:predefinedusers:registryoperator";
        String conceptId = "urn:oasis:names:tc:ebxml-regrep:SubjectRole:RegistryAdministrator";
        inputs.add(AnyURIAttribute.getInstance(parentId));
        inputs.add(AnyURIAttribute.getInstance(conceptId));
        EvaluationCtx context = null;
        EvaluationResult result = function.evaluate(inputs, context);
        assertTrue(result.getAttributeValue().encode().equalsIgnoreCase("true"));
        
        inputs.clear();
        inputs.add(AnyURIAttribute.getInstance(parentId));
        inputs.add(AnyURIAttribute.getInstance(conceptId + ":junk"));
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