/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/security/authorization/HasSlotFunction.java,v 1.2 2006/05/23 19:30:30 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.security.authorization;

import com.sun.xacml.ctx.Status;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;

import org.freebxml.omar.common.spi.QueryManagerFactory;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.cond.EvaluationResult;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.Value;

/** 
  * logical signature: boolean hasSlot(String parentId, String name, [String value])
  *
  * @author Diego Ballve / Digital Artefacts
  * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a> 
  */
public class HasSlotFunction extends AbstractRegistryFunction {
    
    private static final Log log = LogFactory.getLog(HasSlotFunction.class);
    
    // the name of the function, which will be used publicly
    public static final String NAME = "has-slot";
    
    // the parameter types, in order, and whether or not they're bags
    private static final String[] params = {
        AnyURIAttribute.identifier, //parent object id - required
        AnyURIAttribute.identifier, //slot name - required
        StringAttribute.identifier  //slot value - optional
    };
    
    // Parameter names, for logging purposes
    private static final String[] paramsNames = {
        "parentId",
        "name",
        "value"
    };

    // Number of required parameters
    private final int minParams = 2;
    
    //Dont forget to make sure that bagParams.length MUST BE same as params.length
    private static final boolean[] bagParams = { false, false, false };
        
    public HasSlotFunction() {
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
        String parentId = (argValues[0]).encode().trim();
        String name = (argValues[1]).encode().trim();
        String value = (argValues.length > 2) ? (argValues[2]).encode().trim() : null;
        
        boolean evalResult = false;
                
        try {
            ServerRequestContext requestContext = AuthorizationServiceImpl.getRequestContext(context);
            String assocTypeNodePath = null;
            
            evalResult = hasSlotInternal(requestContext, parentId, name, value);
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
    
    private boolean hasSlotInternal(ServerRequestContext requestContext, String parentId, String name, String value) throws RegistryException {
        boolean result = false;
        
        RegistryObjectType ro = QueryManagerFactory.getInstance().getQueryManager()
                .getRegistryObject(requestContext, parentId);
        
        if (ro != null) {
            List slots = ((RegistryObjectType)ro).getSlot();
            for (Iterator it = slots.iterator(); it.hasNext(); ) {
                SlotType1 s = (SlotType1)it.next();
                if (name.equals(s.getName())) {
                    if (value != null) {
                        List values = s.getValueList().getValue();
                        for (Iterator it2 = values.iterator(); it2.hasNext(); ) {
                            Value v = (Value)it2.next();
                            if (value.equals(v.getValue())) {
                                // name and value match
                                result = true;
                                break;
                            }
                        }
                    } else {
                        // name match, value not required
                        result = true;
                        break;
                    }
                }
            }
        }
        
        return result;
    }
    
}
