/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/security/authorization/AssociationExistsFunction.java,v 1.6 2006/05/23 19:30:30 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.security.authorization;

import com.sun.xacml.ctx.Status;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.cond.EvaluationResult;

/** 
  * logical signature: boolean isAssociatedWith(String sourceObject, String targetObject, [String associationType])
  *
  * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a> 
  */
public class AssociationExistsFunction extends AbstractRegistryFunction {
    
    private static final Log log = LogFactory.getLog(AssociationExistsFunction.class);

    private static BindingUtility bu = BindingUtility.getInstance();
    
    // the name of the function, which will be used publicly
    public static final String NAME = "association-exists";
    
    // the parameter types, in order, and whether or not they're bags
    private static final String[] params = {
        AnyURIAttribute.identifier, //sourceObject id - required
        AnyURIAttribute.identifier, //targetObject id - required
        AnyURIAttribute.identifier  //if of AssociationType node - optional
    };
    
    // Parameter names, for logging purposes
    private static final String[] paramsNames = {
        "sourceObjectId",
        "targetObjectId",
        "assocTypeId"
    };

    // Number of required parameters
    private final int minParams = 2;
    
    //Dont forget to make sure that bagParams.length MUST BE same as params.length
    private static final boolean[] bagParams = { false, false, false };
        
    public AssociationExistsFunction() {
        // use the constructor that handles mixed argument types
        super(AuthorizationServiceImpl.FUNCTION_NS + NAME, 0, params, bagParams, BooleanAttribute.identifier, false);
    }
    
    protected String[] getParameterNames() {
        return paramsNames;
    }

    public EvaluationResult evaluate(List inputs, EvaluationCtx context) {
        // Evaluate the arguments using the helper method...this will
        // catch any errors, and return values that can be compared
        AttributeValue[] argValues = new AttributeValue[inputs.size()];
        EvaluationResult result = evalArgs(inputs, context, argValues, minParams);
        
        if (result != null) {
            return result;
        }
        
        // cast the resolved values into specific types
        String sourceObjectId = (argValues[0]).encode().trim();
        String targetObjectId = (argValues[1]).encode().trim();
        String assocTypeId = (argValues.length > 2) ? (argValues[2]).encode().trim() : null;
        
        boolean evalResult = false;
                
        try {
            ServerRequestContext requestContext = AuthorizationServiceImpl.getRequestContext(context);
            String assocTypeNodePath = null;
            if (assocTypeId != null) {
                ClassificationNodeType assocTypeNode = (ClassificationNodeType)qm.getRegistryObject(requestContext, assocTypeId);
                assocTypeNodePath = assocTypeNode.getPath();
            }
            
            evalResult = associationExistsInternal(requestContext, sourceObjectId, targetObjectId, assocTypeNodePath);
        } catch (ObjectNotFoundException e) {
            //Do nothing as evalResult = false; already
        } catch (RegistryException e) {
            log.error(ServerResourceBundle.getInstance().getString(
                    "message.xacmlExtFunctionEvalError", new Object[]{getFunctionName()}), e);
            List codes = new ArrayList();
            codes.add(Status.STATUS_PROCESSING_ERROR);
            return new EvaluationResult(new Status(codes, e.getMessage()));
        }
        
        // boolean returns are common, so there's a getInstance() for that
        return EvaluationResult.getInstance(evalResult);
    }
    
    private boolean associationExistsInternal(ServerRequestContext requestContext, String sourceObject, String targetObject, String assocTypeNodePath) throws RegistryException {
        boolean assExists = false;
        
        String queryStr = "SELECT a.* from Association a, ClassificationNode assocTypeNode WHERE a.sourceObject='" + sourceObject + "' AND a.targetObject='" + targetObject + "' ";
        
        if (assocTypeNodePath != null) {            
            //The a.associationType may be a sub-class of assocTypeNodePath
            //Check exact match: a.associationType = assocTypeNodePath
            //Also check sub-class match: a.associationType LIKE assocTypeNodePath/%
            queryStr += " AND a.associationType = assocTypeNode.id AND (assocTypeNode.path = '" + assocTypeNodePath + "' OR assocTypeNode.path LIKE '" + assocTypeNodePath + "/%')";
        }
        
        try {
            org.oasis.ebxml.registry.bindings.query.ResponseOption responseOption =
                bu.queryFac.createResponseOption();
            responseOption.setReturnType(org.oasis.ebxml.registry.bindings.query.ReturnType.LEAF_CLASS);
            responseOption.setReturnComposedObjects(true);


            List asses = PersistenceManagerFactory.getInstance()
                .getPersistenceManager().executeSQLQuery(requestContext, queryStr, responseOption, "Association", new ArrayList());        
            if (asses.size() > 0) {
                assExists = true;
            }
        } catch (JAXBException e) {
            throw new RegistryException(e);
        }
    
        return assExists;
    }
    
}
