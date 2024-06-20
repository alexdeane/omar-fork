/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/interfaces/rest/FilePathURLHandler.java,v 1.16 2005/11/21 04:27:40 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.interfaces.rest;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.registry.InvalidRequestException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.common.exceptions.UnimplementedException;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;


/**
 *
 * @author  Uday Subbarayan(mailto:uday.s@sun.com)
 * @version
 */
public class FilePathURLHandler extends URLHandler {
    
    private Log log = LogFactory.getLog(this.getClass());
    private BindingUtility bu = BindingUtility.getInstance();
    private RegistryPackageType parentFolder = null;
    private List matchedObjects = null;
    
    //Following needed for multiple object matches where the matched portion of
    //the path may be a sub-segment of original path
    private String modifiedPathInfo = "";

    protected FilePathURLHandler() {
    }
    
    FilePathURLHandler(HttpServletRequest request,
        HttpServletResponse response) throws RegistryException {
            
        super(request, response);
    }
    
    /**
     * Processes a Get Request
     */
    void processGetRequest() throws IOException, RegistryException, InvalidRequestException, UnimplementedException, ObjectNotFoundException {
        String getRepositoryItem = request.getParameter("getRepositoryItem");
        getRegistryObjectByPackageHierarchy();
        
        if (matchedObjects.size() == 1) {
            RegistryObjectType ro = (RegistryObjectType)matchedObjects.get(0);
        
            if ((getRepositoryItem != null) && (getRepositoryItem.equalsIgnoreCase("true"))) {
                if (ro instanceof ExtrinsicObjectType) {                
                    writeRepositoryItem((ExtrinsicObjectType)ro);
                } else {
                    throw new InvalidRequestException(ServerResourceBundle.getInstance().getString("message.expectExtrinsicObject",
                            new Object[]{ro.getClass()}));
                }            
            } else {
                String pathInfo = request.getPathInfo();
                if ((ro instanceof RegistryPackageType) && (pathInfo.endsWith("/"))) {
                    //Matched 1 object that is a RegistryPackage and path ends in '/'. 
                    //Write directory listing with all member objects shown
                    writeDirectoryListing(pathInfo, (RegistryPackageType)ro);
                } else {
                    //Matched 1 object and path does not end in '/'
                    //Write XML for the matched object
                    writeRegistryObject(ro);
                }
            }
        } else {
            //Matched multiple objects.
            //Write directory listing with only matched objects shown
            writeDirectoryListing(modifiedPathInfo, parentFolder, matchedObjects);
        }
    }            
    
    private String[] getPathElements(String pathInfo) {
        String[] pathElements = null;
        
        Pattern p = Pattern.compile("/");
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1);
        }
        pathElements = p.split(pathInfo);
        
        return pathElements;
    }
    
    /**
     * Attempt to get a RegistryObject by its URI by matching it against the package
     * membership hierarchy it is contained in.
     *
     */
    private void getRegistryObjectByPackageHierarchy() 
        throws IOException, RegistryException, ObjectNotFoundException, InvalidRequestException
    {
        String pathInfo = request.getPathInfo();
        
        String[] pathElements = getPathElements(pathInfo);

        if (pathElements.length == 0) {
            throw new InvalidRequestException(ServerResourceBundle.getInstance().getString("message.URLContextPath",
                    new Object[]{pathInfo}));            
        } else {                                

            //Iterate and retrieve each object along path. 
            //Remember parentFolder as we iterate.
            for (int i=0; i<pathElements.length; i++) {
                String pathElement = pathElements[i];
                modifiedPathInfo += "/" + pathElement;
                matchedObjects = getRegistryObjectForPathElement(pathElement, parentFolder); 

                if (matchedObjects.size() > 1) {
                    //Multiple objects matched. Stop here and show partial directory listing 
                    break;
                } else if (matchedObjects.size() == 1) {
                
                    //Matched 1 object. If not last in path then must be a RegistryPackage                    
                    if (i != pathElements.length-1) {                    
                        RegistryObjectType ro = (RegistryObjectType)matchedObjects.get(0);
                        if (ro instanceof RegistryPackageType) {
                            parentFolder = (RegistryPackageType)ro;
                        }
                        else {
                            throw new InvalidRequestException(ServerResourceBundle.getInstance().getString("message.expectedRegistryPackage",
                                    new Object[]{ro.getClass(), pathInfo}));
                        }
                    }
                }
            }
        }                                
    }
    
    private List getRegistryObjectForPathElement(String objectName, RegistryPackageType parentFolder) 
        throws IOException, RegistryException, ObjectNotFoundException {
        String pathInfo = request.getPathInfo();
        String caseSensitive = request.getParameter("caseSensitive");
        
        
        String queryStr = null;
        
        if (parentFolder != null) {
            if ((caseSensitive != null) && (caseSensitive.equalsIgnoreCase("false"))) {            
                queryStr = "SELECT DISTINCT ro.* FROM RegistryObject ro, RegistryPackage p, Name_ nm, Association ass WHERE ( UPPER (nm.value) LIKE '" +
                    objectName.toUpperCase() + "' AND nm.parent = ro.id) AND (p.id = '" +
                    parentFolder.getId() + "') AND (ass.associationType='" + bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
                    "' AND ass.sourceObject = p.id AND ass.targetObject = ro.id) ";
            } else {
                queryStr = "SELECT DISTINCT ro.* FROM RegistryObject ro, RegistryPackage p, Name_ nm, Association ass WHERE (nm.value LIKE '" +
                    objectName + "' AND nm.parent = ro.id) AND (p.id = '" +
                    parentFolder.getId() + "') AND (ass.associationType='" + bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
                    "' AND ass.sourceObject = p.id AND ass.targetObject = ro.id) ";
            }
        } else {
            if ((caseSensitive != null) && (caseSensitive.equalsIgnoreCase("false"))) {            
                queryStr = "SELECT DISTINCT ro.* FROM RegistryObject ro, Name_ nm1 WHERE UPPER (nm1.value) LIKE '" +
                            objectName.toUpperCase() + "' AND nm1.parent = ro.id";
            } else {
                queryStr = "SELECT DISTINCT ro.* FROM RegistryObject ro, Name_ nm1 WHERE nm1.value LIKE '" +
                            objectName + "' AND nm1.parent = ro.id";
            }
        }

        List list = submitQueryAs(queryStr, currentUser);
        if (list.size() == 0) {                
            throw new ObjectNotFoundException(
                ServerResourceBundle.getInstance().getString("message.registryObjectNotFound",
                    new Object[]{pathInfo}));                
        }
        
        return list;
    }
    
    /** Write the complete directory listing showing all members for the specified folder */
    private void writeDirectoryListing(String pathInfo, RegistryPackageType pkg) throws IOException, RegistryException {
        List members = getPackageMembers(pkg);
        writeDirectoryListing(pathInfo, pkg, members);
    }
    
    /** Write the partial directory listing showing specified members for the specified folder */
    private void writeDirectoryListing(String pathInfo, RegistryPackageType pkg, List members) throws IOException, RegistryException {
        ServletOutputStream sout = null;
        try {
            sout = response.getOutputStream();

            sout.println("<html>");
            sout.println("<head>");
            sout.println("<title>Index of " + pathInfo + "</title>");
            sout.println("</head>");
            sout.println("<body>");
            sout.println("<h1>Index of " + pathInfo + "</h1>");
            sout.println("<pre>ObjectType\t\t<a href=\"?N=D\">Name</a>\t\t<a ref=\"?D=A\">Description</a>");
            sout.println("<hr/>");
            sout.println("<a href=\"./..\">Goto Parent Directory</a> <br/>");
            
            Iterator iter = members.iterator();
            while(iter.hasNext()) {
                RegistryObjectType member = (RegistryObjectType)iter.next();
                writeDirectoryListingItem(sout, member);
            }
            
            sout.println("</pre>");
            sout.println("<hr/>");
            sout.println("<address>freebXML Regisry Server version 3.0</address>");
            sout.println("</body>");
            sout.println("</html>");
        }
        finally {
            if ( sout != null ) {
                sout.close();
            }
        }
    }
    
    private void writeDirectoryListingItem(ServletOutputStream os, RegistryObjectType member) throws IOException, RegistryException {
        String name = getClosestValue(member.getName());
        String desc = getClosestValue(member.getDescription());
        
        ServerRequestContext context = new ServerRequestContext("FilePathURLHandler.writeDirectoryListingItem", null);
        ClassificationNodeType node = (ClassificationNodeType)qm.getRegistryObject(context, member.getObjectType());
        String objectType = node.getCode();
        
        if (member instanceof RegistryPackageType) {            
            os.println("<a href=\"./" + name + "\">" + objectType + "</a>\t\t<a href=\"./" + name + "/\">" + name + "</a> \t" + desc + " <br/>");            
        } else {
            if (member instanceof ExtrinsicObjectType) {
                os.println("<a href=\"./" + name + "\">" + objectType + "</a>\t\t<a href=\"./" + name + "?getRepositoryItem=true\">" + name + "</a> \t" + desc + " <br/>");
            } else {
                os.println("<a href=\"./" + name + "\">" + objectType + "</a>\t\t" + name + "\t" + desc + " <br/>");
            }            
        }
    }
    
    private List getPackageMembers(RegistryPackageType pkg) throws RegistryException {
        String queryStr = "SELECT ro.* FROM RegistryObject ro, RegistryPackage p, Association ass WHERE (p.id = '" +
            pkg.getId() + "') AND (ass.associationType='" + bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
            "' AND ass.sourceObject = p.id AND ass.targetObject = ro.id) ";
        
        List results = submitQueryAs(queryStr, currentUser);
        return results;
    }
       
}
