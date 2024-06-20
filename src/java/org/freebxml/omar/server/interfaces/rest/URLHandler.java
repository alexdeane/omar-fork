/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/interfaces/rest/URLHandler.java,v 1.27 2007/02/28 02:44:46 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.interfaces.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CommonProperties;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.common.exceptions.UnimplementedException;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.interfaces.common.SessionManager;
import org.freebxml.omar.server.persistence.PersistenceManager;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.lcm.RepositoryItemListType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.query.ObjectFactory;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.LocalizedStringType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 *
 * @author  Uday Subbarayan(mailto:uday.s@sun.com)
 * @version
 */
public class URLHandler {
    private static final Log log = LogFactory.getLog(URLHandler.class);
    private BindingUtility bu = BindingUtility.getInstance();
    private AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();

    HttpServletRequest request = null;
    HttpServletResponse response = null;
    protected static TransformerFactory xFormerFactory;
    private String baseUrl = null;
    protected QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    protected UserType currentUser = null;
    
    protected URLHandler() {};
    
    URLHandler(HttpServletRequest request,
    HttpServletResponse response) throws RegistryException {
        
        this.request = request;
        this.response = response;
        
        //First see if user context can be gotten from cert is SSLContext for request
        getUserFromRequest();
        
        if (currentUser == null) {
            if (SessionManager.getInstance().isSessionEstablished(request)) {
                currentUser = SessionManager.getInstance().getUserFromSession(request);
            }
        }
        
        if (currentUser == null) {
            //Force authentication if so configured.
            boolean samlMode = Boolean.valueOf(CommonProperties.getInstance()
                .getProperty("omar.common.security.samlMode", "false"))
                .booleanValue();
            boolean forceAuthentication = Boolean.valueOf(CommonProperties.getInstance()
                .getProperty("omar.common.security.forceAuthentication", "false"))
                .booleanValue();
            
            if (samlMode && forceAuthentication) {
                //TODO: Need to redirect to AM Login screen
            }
            
            //See if user can be gotten from pricipal
            currentUser = findUserByPrincipal(request.getUserPrincipal());
            
            boolean establishSession = true;
            // If the UserType is registryGuest. there was no authentication, and,
            // thus, there is no need to create a session
            if (currentUser == AuthenticationServiceImpl.getInstance().registryGuest) {
                establishSession = false;
            }
            SessionManager.getInstance().establishSession(request,
                                                          currentUser,
                                                          establishSession);
        }
        
        if (xFormerFactory == null) {
            try {
                xFormerFactory = TransformerFactory.newInstance();
            } catch (Throwable t) {
                log.error(ServerResourceBundle.getInstance().getString("message.ProblemInitializingTransformerFactory",
                                                                        new Object[]{t.getMessage()}));
            }            
        }
    }
    
    private void getUserFromRequest() {
        Object certObj = request.getAttribute("javax.servlet.request.X509Certificate");
        
        if (certObj != null) {
            Certificate[] certs = (Certificate[])certObj;

            try {
                if (certs.length > 0) {
                    currentUser = ac.getUserFromCertificate((X509Certificate)certs[0]);
                }
            } catch (RegistryException e) {            
            }
        }
    }
    
    /**
     * Processes a Get Request
     */
    void processGetRequest() throws IOException, RegistryException, InvalidRequestException, UnimplementedException, ObjectNotFoundException {
        throw new UnimplementedException(ServerResourceBundle.getInstance().getString("message.unimplementedGETMethod"));
    }
    
    
    /**
     * Processes a POST Request
     */
    void processPostRequest() throws IOException, RegistryException, InvalidRequestException, UnimplementedException, ObjectNotFoundException {
        throw new UnimplementedException(ServerResourceBundle.getInstance().getString("message.unimplementedPOSTMethod"));
    }
    
    /** Submit the SQL query to the registry as the specified user.
     *
     * @param queryString
     * @param user
     * @throws RegistryException
     * @return
     *     A List of IdentifiableType objects representing the registry objects
     *     that match the query.
     * @see #findUserByPrincipal(Principal)
     */
    List submitQueryAs(String queryString, UserType user) throws RegistryException {
        try {                        
            AdhocQueryRequest req = BindingUtility.getInstance().createAdhocQueryRequest(queryString);
            ObjectFactory queryFac = BindingUtility.getInstance().queryFac;
            ResponseOption responseOption = queryFac.createResponseOption();
            responseOption.setReturnComposedObjects(true);
            responseOption.setReturnType(ReturnType.LEAF_CLASS);
            req.setResponseOption(responseOption);
            
            ServerRequestContext context = new ServerRequestContext("URLHandler.submitQueryAs", req);
            context.setUser(user);
            AdhocQueryResponseType resp = qm.submitAdhocQuery(context);
            
            RegistryObjectListType rolt = resp.getRegistryObjectList();
            List results = rolt.getIdentifiable();
            return results;
        }
        catch (Exception e) {
            throw new RegistryException(e);
        }
    }
    
    List invokeParameterizedQuery(ServerRequestContext context, 
            String queryId, 
            Map queryParams, 
            UserType user, 
            int startIndex, 
            int maxResults) throws RegistryException {
        List res = null;
        
        try {
            AdhocQueryRequest req = BindingUtility.getInstance().createAdhocQueryRequest("SELECT * FROM DummyTable");
            req.setStartIndex(BigInteger.valueOf(startIndex));
            req.setMaxResults(BigInteger.valueOf(maxResults));

            Map slotsMap = new HashMap();
            slotsMap.put(BindingUtility.CANONICAL_SLOT_QUERY_ID, queryId);
            if ((queryParams != null) && (queryParams.size() > 0)) {
                slotsMap.putAll(queryParams);
            }
            BindingUtility.getInstance().addSlotsToRequest(req, slotsMap);                

            //Now execute the query
            Map idToRepositoryItemMap = new HashMap();
            context.setRepositoryItemsMap(idToRepositoryItemMap);

            boolean doCommit = false;
            try {
                context.pushRegistryRequest(req);
                AdhocQueryResponseType resp = qm.submitAdhocQuery(context);
                bu.checkRegistryResponse(resp);
                res = resp.getRegistryObjectList().getIdentifiable();
                doCommit = true;
            } finally {
                context.popRegistryRequest();
                try {
                    closeContext(context, doCommit);
                } catch (Exception ex) {                    
                    log.error(ex, ex);
                }
            }
        } catch (JAXBException e) {
            throw new RegistryException(e);
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }
        
        return res;
    }
    
    protected void closeContext(ServerRequestContext context, boolean doCommit) throws Exception {
        if (doCommit) {
            context.commit();
        } else {
            context.rollback();
        }
    }
    
    
    /** Get the User object that is associated with a Slot named
     * 'urn:oasis:names:tc:ebxml-regrep:3.0:rim:User:principalName'
     * whose value matches the principal name specified. If the principal is
     * <code>null</code>, use the value of the
     * <code>omar.security.anonymousUserPrincipalName</code> property
     * as the principal name. If the property is not set, or no User is found,
     * return the RegistryGuest user.
     *
     * @param principal
     * @throws JAXRException
     * @return
     */
    protected UserType findUserByPrincipal(Principal principal) throws RegistryException {
        try {
            UserType user = null;
            if (principal == null) {
                String principalName = CommonProperties.getInstance().getProperty
                ("omar.security.anonymousUserPrincipalName");
                if (principalName != null) {
                    user = findUserByPrincipalName(principalName);
                }
            }
            else {
                user = findUserByPrincipalName(principal.getName());
            }
            if (user == null) {
                user = AuthenticationServiceImpl.getInstance().registryGuest;
            }
            return user;
        }
        catch (RegistryException re) {
            throw re;
        }
        catch (Exception e) {
            throw new RegistryException(e);
        }
    }
    
    /** Get the User object that is associated with a Slot named
     * 'urn:oasis:names:tc:ebxml-regrep:3.0:rim:User:principalName'
     * whose value matches the principal name specified. If no User is found,
     * return <code>null</code>.
     * <p/>
     * This method must query the persitance manager directly so as to avoid
     * the authorization restrictions imposed by the QueryManager.
     *
     * @param principalName
     * @throws JAXRException
     * @return
     */
    protected UserType findUserByPrincipalName(String principalName) throws RegistryException {
        UserType user = null;
        ServerRequestContext context = null;
        
        try {
            context = new ServerRequestContext("URLHandler.findUserByPrincipalName", null);
            
            String sqlQuery =
            "SELECT u.* " +
            "FROM user_ u, slot s " +
            "WHERE u.id = s.parent AND s.name_='" + BindingUtility.CANONICAL_PRINCIPAL_NAME_URI + "' AND value='" + principalName + "'";
            ResponseOption responseOption =
            BindingUtility.getInstance().queryFac.createResponseOption();
            responseOption.setReturnComposedObjects(true);
            responseOption.setReturnType(ReturnType.LEAF_CLASS);
            List objectRefs = new ArrayList();
            PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();
            Iterator results = pm.executeSQLQuery(context, sqlQuery, responseOption, "RegistryObject", objectRefs).iterator();
            while (results.hasNext()) {
                user = (UserType)results.next();
                break;
            }
        }
        catch (RegistryException re) {
            context.rollback();
            throw re;
        } catch (Exception e) {
            context.rollback();
            throw new RegistryException(e);
        }
        
        context.commit();
        return user;
    }
    
    private synchronized Transformer createTransformer() 
        throws TransformerConfigurationException, MalformedURLException,
        IOException {
        Transformer xFormer = null;
        if (xFormerFactory == null) {
            xFormer = null;
        } else {
            //TODO: Replace next line with server.common.Utility.getBaseURL();
            URL url = new URL(getBaseUrl()+"?interface=QueryManager&"+
                "method=getRepositoryItem&"+
                "param-id=urn:uuid:82239fb0-c075-44e3-ac37-a8ea69383907");
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            StreamSource source = new StreamSource(is);
            xFormer = xFormerFactory.newTransformer(source);
        }
        return xFormer;
    }

    void transformResponse(StringWriter sWriter, 
                                   Writer out, 
                                   HttpServletResponse response,
                                   String flavor) 
                                   throws IOException {
        if (sWriter == null || out == null) {
            throw new IllegalArgumentException(ServerResourceBundle.getInstance().getString("message.nullTransformResponseParammeter",
                    new Object[]{sWriter, out}));
        }
        try {
            Transformer transformer = createTransformer();
            if (transformer == null) {
                out.write(sWriter.toString());
            } else {
                String responseStr = sWriter.toString();
                // The next two string replacements take care of some strange
                // processing behavior by Xalan
                responseStr = responseStr.replaceFirst("xmlns=", "xmlns:rim=");
                responseStr = responseStr.replaceAll("<LocalizedString", 
                    " <LocalizedString");
                StreamSource inputSrc = new StreamSource(new StringReader(
                    responseStr));
                StreamResult sResult = new StreamResult(out);
                response.setContentType(flavor);
                transformer.transform(inputSrc, sResult);
            }
        } catch (Throwable t) {
            log.error(ServerResourceBundle.getInstance().getString("message.ProblemTransformingResponseToNonXml",
								   new Object[]{t.getMessage()}), t);
            response.setContentType("text/xml; charset=UTF-8");
            out.write(sWriter.toString());
        }
    }
    
    protected String getBaseUrl() {
        if (baseUrl == null) {
            String requestUri = request.getRequestURI();
            String servletPath = request.getServletPath();
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            String queryString = request.getQueryString();
            int serverPort = request.getServerPort();
            StringBuffer sb = new StringBuffer();
            sb.append(scheme).append("://").append(serverName).append(":");
            sb.append(serverPort);
            sb.append(requestUri);
            baseUrl  = sb.toString();
            log.info(ServerResourceBundle.getInstance().getString("message.BaseURL", new Object[]{baseUrl}));
        } 
        
        return baseUrl;
    }
    
    /**
     * Writes XML RepositoryItems as a RepositoryItemList.
     * Ignores any other type of RepositoryItem.s 
     */ 
    void writeRepositoryItems(List eos) 
        throws IOException, RegistryException, ObjectNotFoundException {
        ServerRequestContext context = new ServerRequestContext("URLHandler.writeRepositoryItem", null);
        ServletOutputStream sout = response.getOutputStream();
        boolean doCommit = false;
        try {
            RepositoryItemListType riList = bu.lcmFac.createRepositoryItemList();
                        
            Iterator iter = eos.iterator();
            while(iter.hasNext()) {
                ExtrinsicObjectType eo = (ExtrinsicObjectType)iter.next();
                String id = eo.getId();
                
                RepositoryItem ri = QueryManagerFactory.getInstance()
                                                       .getQueryManager()
                                                       .getRepositoryItem(context, id);

                if (ri == null) {
                    throw new ObjectNotFoundException(id, ServerResourceBundle.getInstance().getString("message.repositoryItem"));
                } else {
                    if (eo.getMimeType().equals("text/xml")) {
                        DataHandler dataHandler = ri.getDataHandler();
                        InputStream fStream = dataHandler.getInputStream();
                        
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        factory.setNamespaceAware(true);
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document document = builder.parse(fStream);
                        Element rootElement = document.getDocumentElement();                        
                        
                        riList.getAny().add(rootElement);
                    }
                }
            }
            javax.xml.bind.Marshaller marshaller = bu.lcmFac.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            
            marshaller.marshal(riList, sout);
            doCommit = true;
            
        } catch (JAXBException e) {
            throw new RegistryException(e);
        } catch (ParserConfigurationException e) {
            throw new RegistryException(e);
        } catch (SAXException e) {
            throw new RegistryException(e);
        } finally {
            if (sout != null) {
                sout.close();
                sout=null;
            }
            try {
                closeContext(context, doCommit);
            } catch (Exception ex) {                    
                log.error(ex, ex);
            }
        }
    }
    
    void writeRepositoryItem(ExtrinsicObjectType eo) 
        throws IOException, RegistryException, ObjectNotFoundException {
        String id = eo.getId();
        ServerRequestContext context = new ServerRequestContext("URLHandler.writeRepositoryItem", null);
        
        try {
            RepositoryItem ri = QueryManagerFactory.getInstance()
                                                   .getQueryManager()
                                                   .getRepositoryItem(context, id);

            if (ri == null) {
                throw new ObjectNotFoundException(id, ServerResourceBundle.getInstance().getString("message.repositoryItem"));
            } else {
                response.setContentType(eo.getMimeType());

                DataHandler dataHandler = ri.getDataHandler();
                ServletOutputStream sout = response.getOutputStream();
                InputStream fStream = dataHandler.getInputStream();
                int bytesSize = fStream.available();
                byte[] b = new byte[bytesSize];
                fStream.read(b);
                sout.write(b);
                sout.close();
            }
            context.commit();
            context = null;
        } finally {
            if (context != null) {
                context.rollback();
            }

        }
    }
    
    void writeRegistryObject(RegistryObjectType ro) throws IOException, RegistryException, ObjectNotFoundException { 
        PrintWriter out = null;
        try {
            log.info(ServerResourceBundle.getInstance().getString("message.FoundRegistryObjectWithId", new Object[]{ro.getId()}));
            response.setContentType("text/xml; charset=UTF-8");

            out = response.getWriter();

            Marshaller marshaller = bu.rimFac.createMarshaller();
            marshaller.marshal(ro, out);
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    protected String getClosestValue(InternationalStringType is) {
        String str = null;
        List l = is.getLocalizedString();
        if (l != null && l.size() > 0) {
            str = ((LocalizedStringType)l.get(0)).getValue();
        }
        return str;
    }
    
}
