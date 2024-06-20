/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/security/authorization/AssociationExistsFunctionTest.java,v 1.3 2006/03/08 10:19:30 doballve Exp $
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
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 *
 */
public class AssociationExistsFunctionTest extends ServerTest {
    
    static String assId = null;
    static String eoId = null;
    
    public AssociationExistsFunctionTest(java.lang.String testName) {
        super(testName);
        
    }
    
    
    public static Test suite() {
        return new TestSuite(AssociationExistsFunctionTest.class);
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
        
    public void testEvaluate() throws Exception {
        AssociationExistsFunction function = new AssociationExistsFunction();
        
        //Must be kept in sync with misc/samples/demoDB/SubmitObjectsRequest_Picture1.xml
        List inputs = new ArrayList();
        String sourceObjectId = "urn:freebxml:registry:demoDB:acp:customACP1";
        String targetObjectId = "urn:freebxml:registry:demoDB:ExtrinsicObject:zeusDescription";
        String assocTypeId = "urn:oasis:names:tc:ebxml-regrep:AssociationType:AccessControlPolicyFor";
        inputs.add(AnyURIAttribute.getInstance(sourceObjectId));
        inputs.add(AnyURIAttribute.getInstance(targetObjectId));
        inputs.add(AnyURIAttribute.getInstance(assocTypeId));
        EvaluationCtx context = null;
        EvaluationResult result = function.evaluate(inputs, context);
        assertTrue(result.getAttributeValue().encode().equalsIgnoreCase("true"));
        
        inputs.clear();
        inputs.add(AnyURIAttribute.getInstance(sourceObjectId));
        inputs.add(AnyURIAttribute.getInstance(targetObjectId));
        inputs.add(AnyURIAttribute.getInstance(assocTypeId + ":some:junk:that:will:never:match"));
        result = function.evaluate(inputs, context);
        assertTrue(result.getAttributeValue().encode().equalsIgnoreCase("false"));

        inputs.clear();
        inputs.add(AnyURIAttribute.getInstance(sourceObjectId));
        inputs.add(AnyURIAttribute.getInstance(targetObjectId));
        result = function.evaluate(inputs, context);
        assertTrue(result.getAttributeValue().encode().equalsIgnoreCase("true"));
        
        inputs.clear();
        result = function.evaluate(inputs, context);
        assertTrue(result.getStatus().getCode().contains(Status.STATUS_MISSING_ATTRIBUTE));

        inputs.clear();
        inputs.add(AnyURIAttribute.getInstance(sourceObjectId));
        result = function.evaluate(inputs, context);
        assertTrue(result.getStatus().getCode().contains(Status.STATUS_MISSING_ATTRIBUTE));        
    }
}
