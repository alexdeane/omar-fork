/* ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/security/authorization/AuthorizationResult.java,v 1.4 2005/02/23 23:15:38 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.security.authorization;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.freebxml.omar.common.exceptions.UnauthorizedRequestException;



/** 
  *
  * @author Derek Hilder
  */
public class AuthorizationResult {
    
    public static final int INDETERMINATE = 0;
    public static final int PERMIT_ALL = 1;     // 0001
    public static final int PERMIT_NONE = 2;    // 0010
    public static final int PERMIT_SOME = 4;    // 0100

    private String subjectId;
    private HashSet permittedResources;
    private HashMap deniedResourceExceptions;
    

    public AuthorizationResult(String subjectId) {
        this.subjectId = subjectId;
        permittedResources = new HashSet();
        deniedResourceExceptions = new HashMap();
    }
    
    public void addPermittedResource(String resourceId) {
        permittedResources.add(resourceId);
    }
    
    public void addPermittedResources(Collection resourceIds) {
        permittedResources.addAll(resourceIds);
    }
    
    public void addDeniedResourceException(UnauthorizedRequestException e) {
        deniedResourceExceptions.put(e.getId(), e);
    }
            
    public int getResult() {
        int result = INDETERMINATE;
        if (deniedResourceExceptions.isEmpty()) {
            result = PERMIT_ALL;
        }
        else if (permittedResources.isEmpty()) {
            result = PERMIT_NONE;
        }
        else {
            result = PERMIT_SOME;
        }
        return result;
    }
    
    public Set getPermittedResources() {
        return permittedResources;
    }
    
    public Set getDeniedResources() {
        return deniedResourceExceptions.keySet();
    }
    
    /** Throw an UnauthorizedRequestException if the authorization results
      * match any of the results specified.
      *
      * @param results
      *     The results to throw an exception on. This may be PERMIT_ALL,
      *     PERMIT_NONE, PERMIT_SOME, or any combination of these or'd together.
      */    
    public void throwExceptionOn(int results) 
        throws UnauthorizedRequestException
    {
        int result = getResult();
        if ((result & results) != 0) {
            String firstDeniedTargetId = (String)getDeniedResources().toArray()[0];
            UnauthorizedRequestException e = (UnauthorizedRequestException)deniedResourceExceptions.get(firstDeniedTargetId);
            throw e;
        }
    }
}
