/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/security/authorization/AbstractRegistryFunction.java,v 1.2 2006/05/23 19:30:30 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.security.authorization;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.FunctionBase;
import com.sun.xacml.ctx.Status;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.util.ServerResourceBundle;

/**
 * Base class for extension functions.
 *
 * @author Diego Ballve / Digital Artefacts
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a> 
 */
public abstract class AbstractRegistryFunction extends FunctionBase {
    
    private static final Log log = LogFactory.getLog(AbstractRegistryFunction.class);

    protected QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    
    /** Creates a new instance of AbstractRegistryFunction */
    public AbstractRegistryFunction(String functionName, int functionId,
            String[] paramTypes, boolean[] paramIsBag, String returnType,
            boolean returnsBag) {
        super(functionName, functionId, paramTypes, paramIsBag, returnType, returnsBag);
    }
    
    public abstract EvaluationResult evaluate(List inputs, EvaluationCtx context);
    
    /** Returns an array with parameter logical names, for logging purposes. */
    protected abstract String[] getParameterNames();

    /**
     * Extends 'evalArgs' to check for required parameters.
     *
     * @param params
     * @param context
     * @param args
     * @param minParams Minimal number of parameters in 'params'.
     *
     * @return EvaluationResult if something is wrong. Null otherwise.
     */
    protected EvaluationResult evalArgs(List params, EvaluationCtx context, AttributeValue[] args, int minParams) {
        EvaluationResult result = super.evalArgs(params, context, args);
        
        if (result == null) {
            if (params.size() < minParams) {
                
                // Check which arguments are missing
                StringBuffer sb = new StringBuffer();
                for (int i = params.size(); i < minParams; i++) {
                    sb.append(ServerResourceBundle.getInstance().getString(
                        "message.xacmlExtFunctionParamMissing",
                        new Object[]{getFunctionName(), getParameterNames()[i], String.valueOf(i+1)}));
                    if (i+1 < minParams) {
                        sb.append(" ");
                    }
                }

                // Use an Exception to log
                RegistryException e = new RegistryException(sb.toString());
                log.error(ServerResourceBundle.getInstance().getString(
                        "message.xacmlExtFunctionEvalError", new Object[]{getFunctionName()}), e);
                
                // Return EvaluationResult with missing attribute code
                List codes = new ArrayList();
                codes.add(Status.STATUS_MISSING_ATTRIBUTE);
                result = new EvaluationResult(new Status(codes, e.getMessage()));
            }
        }
        
        return result;
    }
    
}
