/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2005 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/thin/RegistryBrowser.java,v 1.67 2007/03/22 17:44:27 geomurr Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.thin;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.security.ProviderException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.lang.reflect.UndeclaredThrowableException;
import java.io.File;

import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.event.ActionEvent;
import javax.xml.registry.infomodel.User;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.JAXRException;

import org.freebxml.omar.client.ui.common.UIUtility;
import org.freebxml.omar.client.ui.common.conf.bindings.Configuration;
import org.freebxml.omar.client.ui.common.conf.bindings.ObjectTypeConfigType;
import org.freebxml.omar.client.ui.thin.security.SecurityUtil;
import org.freebxml.omar.client.xml.registry.BusinessLifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.ConnectionImpl;
import org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.client.xml.registry.infomodel.PersonNameImpl;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rim.PersonNameType;
import org.freebxml.omar.common.CanonicalSchemes;
import org.freebxml.omar.common.CommonProperties;
import org.freebxml.omar.common.spi.QueryManagerFactory;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest; 
import javax.servlet.ServletContext;

import javax.security.auth.callback.CallbackHandler;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import org.freebxml.omar.client.xml.registry.infomodel.UserImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
  *
  * @author  dhilder
  */
public class RegistryBrowser {
    
    private static final Log log = LogFactory.getLog(RegistryBrowser.class);
    
    private Properties _bundledProperties;
    private boolean isSearchRendered = true;
    private boolean isExploreRendered = true;
    private boolean isSessionExpired = true;
    private String redirectPage = null;
    private String errorMessage = null;
    private String helpLink = null;
    private String deletionScopeCode =
          CanonicalSchemes.CANONICAL_DELETION_SCOPE_TYPE_ID_DeleteAll;
    private String browserTitle = null;
    private String title = null;
    private boolean connectionTested = false;
    private String publishOperationMessage = null;
    private boolean isCertLoaded = false;
    private URL standardContextPath = null;
    private HashMap selectedTabs = null;
    private String principalName = null;
    private boolean atAuthentication = false;
    /**
     * Holds value of property userPreferencesBean.
     */
    private UserPreferencesBean userPreferencesBean;
    
    /** Creates a new instance of RegistryBrowser */
    public RegistryBrowser() {
        try {
            initStaticData();
            init();
        }
        catch (Exception e) {
            log.error(WebUIResourceBundle.getInstance().getString("message.AnExceptionOccurredDuringApplicationInitialization"), e);
        }
    }
    
    /** Initialize the application and store application "globals"
      * in the session context.
      */
    private static void initStaticData() throws Exception {
        
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        if (sessionMap.get("connection") == null) {

            String registryUrl = getRegistryUrl();

            // Create a connection to the registry and store it in the session context
            ConnectionImpl connection = (ConnectionImpl)JAXRUtility.getConnectionFactory()
                                                                   .createConnection();

            sessionMap.put("connection", connection);
            UIUtility.getInstance().setConnection(connection);
                
            // Load the config.xml file
            loadConfiguration();
        }
    }
    
    private void init() {
        this.setStandardContextPath();
        selectedTabs = new HashMap();
    }
    
    public HashMap getSelectedTabs() {
        return selectedTabs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
    public String getHelpLink() {
        return helpLink;
    }
    
    public void setHelpLink(String helpLink) {
        this.helpLink = helpLink;
    }
    
    public void setErrorMessage(String errorMessage){
        this.errorMessage = errorMessage;
        // Clear help link
        this.helpLink = null;
    }

    public void clearSelectedTabs(ActionEvent e) {
        selectedTabs.clear();
    }    
    
    public void doClientCertAuthentication() {
        isSessionExpired = false;
        String message = null;
        boolean noCredentialSet = false;
        try {
            String principalName = CommonProperties.getInstance().getProperty
                ("omar.security.anonymousUserPrincipalName");
            
            ConnectionImpl connection = getConnection();
            if (principalName != null) {
                // Set the CallbackHandler for JAAS authentication
                connection.setCallbackHandler(getCallbackHandler());

                // Get the credentials from the keystore and set them on the connection.
                // If no credentials were found, and the user is not a guest, 
                // generate the credentials.
                SecurityUtil.getInstance().handleCredentials(getPrincipal(), connection);
            } else {
                if (connection != null) {
                    // Set the X509Certificate that will be presented to server side
                    X509Certificate cert = getRequestCertificate();
                    if (cert == null)  {
                        reportAuthenticationFailure("cert");
                    } else {
                        // Verify that the cert we obtained from JSSE has a valid user
                        // This is needed because some web browsers, such as Mozilla,
                        // cache the client certs and resubmit the cert on the 
                        // secure connection
                        UserType user = null;
                        try {
                            user = QueryManagerFactory.getInstance().getQueryManager().getUser(cert);
                        } catch (Throwable ex) {
                            log.error(WebUIResourceBundle.getInstance()
                                                         .getString("message.userNotFound", 
                                                                     new Object[]{cert.getSubjectDN().getName()}));
                        }
                        if (user == null) {
                            reportAuthenticationFailure("user");
                        } else {
                            connection.setX509Certificate(cert);
                            if(cert != null) { 
                                HashSet credentials = new HashSet();
                                credentials.add(cert);
                                connection.setCredentials(credentials);
                                this.principalName = getDisplayName();
                            }
                            this.isCertLoaded = true;
                        }
                    }            
                }
            }
        } catch (Throwable t) {
            log.warn("Could not authenticate user with client cert", t);
            if (noCredentialSet){
                this.errorMessage = WebUIResourceBundle.getInstance()
                                                    .getString("missingOrInvalidClientCert");
                helpLink=getUserRegistrationHelp();
            }
        }
    }        
    
    public void reportAuthenticationFailure(String missingType) {
        StringBuffer sb = new StringBuffer();
        if(this.getPublishOperationMessage() != null) {
            sb.append(this.getPublishOperationMessage()+ " "); 
        }
        sb.append(WebUIResourceBundle.getInstance()
                   .getString("publishNotAllowed")+"  ");
        if (missingType.equals("cert")) {
            sb.append(WebUIResourceBundle.getInstance()
                       .getString("missingClientCert"));
        } else {
            sb.append(WebUIResourceBundle.getInstance()
                       .getString("missingUser"));
        }
        
        String message = sb.toString();
        this.errorMessage = message;
        this.setPublishOperationMessage(null);
        helpLink=getUserRegistrationHelp();
        FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }
    /** 
     * Saves RedirectPage value while page goes for client Authentication.
     */
    public String getAuthenticationStatus() {
        String status = "authenticationRequired";
        atAuthentication = true;
        try {
            String servletPath = FacesContext.getCurrentInstance()
                                                    .getExternalContext()
                                                    .getRequestServletPath();
            String requestPathInfo = FacesContext.getCurrentInstance()
                                                    .getExternalContext()
                                                    .getRequestPathInfo();
            redirectPage = servletPath+requestPathInfo;
            HttpServletRequest request = (HttpServletRequest)
                                                     FacesContext.getCurrentInstance()
                                                     .getExternalContext()
                                                     .getRequest();
            String queryString = request.getQueryString();
            if (queryString != null && ! queryString.equals("")) {
                redirectPage = redirectPage + "?" + queryString;
            }
        } catch (Throwable t) {
            log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotGetAuthenticateStatus"), t);
        }
        return status;
    }
    
    private static CallbackHandler getCallbackHandler() {
        CallbackHandler callbackHandler = null;
        Properties properties = JAXRUtility.getBundledClientProperties();
        String callbackHandlerClassName = properties.getProperty(
            "jaxr-ebxml.security.jaas.callbackHandlerClassName");

        if (callbackHandlerClassName != null && ! callbackHandlerClassName.equals("")) {
            Class clazz = null;
            try {
                clazz = Class.forName(callbackHandlerClassName);
            } catch (ClassNotFoundException ex) {
                log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotInstantiateCallbackHandler", new Object[] {callbackHandlerClassName}), ex);
            }            
            try {
                callbackHandler = (CallbackHandler) Class.forName(callbackHandlerClassName)
                                                              .newInstance();
            } catch (Throwable t) {
                log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotInstantiateCallbackHandler1", new Object[] {callbackHandlerClassName}), t);
            }
        }
        return callbackHandler;
    }
        
    private static String getRegistryUrl() {
        ProviderProperties props = ProviderProperties.getInstance();
        String registryUrl = props.getProperty("jaxr-ebxml.soap.url");
        
        if (registryUrl == null || registryUrl.length() == 0) {
            HttpServletRequest request =
                (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
            final String SUFFIX = "/registry/soap";
            try {
                URL url = new URL(request.getScheme(), request.getServerName(),
                    request.getServerPort(), request.getContextPath() + SUFFIX);
                registryUrl = url.toString();
            } catch (MalformedURLException e) {
                // This should not happen
                log.error(WebUIResourceBundle.getInstance().getString("message.FailedToDefineRegistrySoapURL"), e);
            }
        }
        props.put("javax.xml.registry.queryManagerURL", registryUrl);
        return registryUrl;
    }
    
    /** Load the config.xml file.
      */
    private static void loadConfiguration() throws JAXRException {
        Configuration configuration = UIUtility.getInstance().getConfiguration();
        if (configuration == null) {
            throw new ProviderException(WebUIResourceBundle.getInstance().getString("errorLoadingConfFile"));
        }
        Map objectTypeToConfigMap = new HashMap();
        Iterator objectTypeConfigurations = configuration.getObjectTypeConfig().iterator();
        while (objectTypeConfigurations.hasNext()) {
            ObjectTypeConfigType objectTypeConfiguration = 
                (ObjectTypeConfigType)objectTypeConfigurations.next();
            String id = objectTypeConfiguration.getId();
            objectTypeToConfigMap.put(id, objectTypeConfiguration);
        }
        FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap()
                    .put("objectTypeToConfigMap", objectTypeToConfigMap);
    }

    public boolean isSessionExpired() {
        return isSessionExpired;
    }
    
    public void setSessionExpired(boolean isSessionExpired) {
        this.isSessionExpired = isSessionExpired;
    }
    
    /** Get the connection from the the session context.
      */
    public static ConnectionImpl getConnection() throws Exception {
        initStaticData();
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        return (ConnectionImpl)sessionMap.get("connection");
    }
    
    /** Get the BusinessQueryManager from the connection stored in the session 
      * context.
      */
    public static BusinessQueryManagerImpl getBQM() throws Exception {
        return (BusinessQueryManagerImpl)getConnection().getRegistryService().getBusinessQueryManager();
    }
    
    /** Get the DeclarativeQueryManager from the connection stored in the session 
      * context.
      */
    public static DeclarativeQueryManagerImpl getDQM() throws Exception {
        return (DeclarativeQueryManagerImpl)getConnection().getRegistryService().getDeclarativeQueryManager();
    }
    
    /** Get the BusinessLifeCycleManager from the connection stored in the session 
      * context.
      */
    public static BusinessLifeCycleManagerImpl getBLCM() throws Exception {
        return (BusinessLifeCycleManagerImpl)getConnection().getRegistryService().getBusinessLifeCycleManager();
    }
    

    /** Get certificate from request if in ssl mode w/ cert */
    private static X509Certificate getRequestCertificate() {
        FacesContext context = FacesContext.getCurrentInstance();
        Object certObj = context.getExternalContext().getRequestMap()
                .get("javax.servlet.request.X509Certificate");
        if (certObj != null) {
            java.security.cert.Certificate[] certs = (java.security.cert.Certificate[])certObj;
            return (X509Certificate)certs[0];
        } else {
            return null;
        }
    }
    
    public String getRedirectPage() {
        return redirectPage;
    }
    
    public static Principal getPrincipal() {
        Principal p = null;
        X509Certificate cert = getRequestCertificate();
        // Get principal from the client certificate
        if (cert != null) {
            p = cert.getSubjectX500Principal();
        }
        // If this isn't possible, get from context
        if (p == null) {
            p = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
        }
        return p;
    }
    
    public String getPrincipalName() {
        if (this.principalName == null) {
            this.principalName = WebUIResourceBundle.getInstance().getString("RegistryGuest");
        }
        return this.principalName;
    }
    
    private boolean isAsianLocale(Locale locale) {
        String iso3Country = locale.getISO3Country();
        if (iso3Country.equalsIgnoreCase("CHN") || 
            iso3Country.equalsIgnoreCase("JPN") ||
            iso3Country.equalsIgnoreCase("KOR")) {
            return true;
        } else {
            return false;
        }
    }
    
    public String getRegistrationNeeded() throws Exception {
        String result = "unknown";
        try {
            if (SecurityUtil.getInstance().isRegistrationNeeded(getConnection(), getPrincipal())) {
                result = "true";
            }
            else {
                result = "false";
            }
        }
        catch (Exception e) {
            log.error(WebUIResourceBundle.getInstance().getString("message.FailedToCheckIfTheUserIsRegistered"), e);
            result = "error";
        }
        return result;
    }
    
   
    // Action handlers
    
        
    public String doSearch() {
        String resultString = "failure";
        try {
            testConnection();
            clearExplorePanel();
            clearSearchPanelBean();
            isSearchRendered = false;
            resultString = "showSearchPanel";
        } catch (Throwable t) {          
            // TODO: factor out the code below into a utility class
            String causeMessage = null;
            if (t instanceof UndeclaredThrowableException) {
                causeMessage = t.getCause().getMessage();
            } else if (t instanceof JAXRException) {
                causeMessage = t.getMessage();
            }
            String errorMessage = WebUIResourceBundle.getInstance()
                                                     .getString("searchPanelNotInitialized");
            
            StringBuffer sb = new StringBuffer(errorMessage);
            sb.append(" ");
            if (causeMessage != null) {
                sb.append(causeMessage).append(" ");
            }
            sb.append(WebUIResourceBundle.getInstance()
                                         .getString("checkLogForDetails"));
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                               sb.toString(), 
                                               null);
            FacesContext.getCurrentInstance()
                        .addMessage(null,fm);
        }
        return resultString;
    }
    
    public boolean isSearchRendered() {
        return isSearchRendered;
    }
    
    public void setSearchRendered(boolean isSearchRendered) {
        this.isSearchRendered = isSearchRendered;
    }
    
    public String doExplore() {
        String resultString = "failure";
        try {
            testConnection();
            clearSearchPanel();
            clearSearchPanelBean();
            isExploreRendered = false;
            resultString = "showExplorePanel";
        } catch (Throwable t) {
            String causeMessage = null;
            if (t instanceof UndeclaredThrowableException) {
                causeMessage = t.getCause().getMessage();
            } else if (t instanceof JAXRException) {
                causeMessage = t.getMessage();
            }
            String errorMessage = WebUIResourceBundle.getInstance()
                                                     .getString("explorePanelNotInitialized");
            
            StringBuffer sb = new StringBuffer(errorMessage);
            sb.append(" ");
            if (causeMessage != null) {
                sb.append(causeMessage).append(" ");
            }
            sb.append(WebUIResourceBundle.getInstance()
                                         .getString("checkLogForDetails"));
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                               sb.toString(), 
                                               null);
            FacesContext.getCurrentInstance()
                        .addMessage(null,fm);
        }
        return resultString;
    }

    public boolean isExploreRendered() {
        return isExploreRendered;
    }
    
    public String doPublish() {
        String resultString = "failure";
        try {
            testConnection();
            clearExplorePanel();
            isSearchRendered = true;
            isExploreRendered = true;
            clearSearchPanelBean();
            RegistryObjectCollectionBean.getInstance().doPublishAdd();
            resultString = "showPublishPage";
        } catch (Throwable t) {
            String causeMessage = null;
            if (t instanceof UndeclaredThrowableException) {
                causeMessage = t.getCause().getMessage();
            } else if (t instanceof JAXRException) {
                causeMessage = t.getMessage();
            }
            String errorMessage = WebUIResourceBundle.getInstance()
                                                     .getString("publishNotInitialized");
            
            StringBuffer sb = new StringBuffer(errorMessage);
            sb.append(" ");
            if (causeMessage != null) {
                sb.append(causeMessage).append(" ");
            }
            sb.append(WebUIResourceBundle.getInstance()
                                         .getString("checkLogForDetails"));
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                               sb.toString(), 
                                               null);
            FacesContext.getCurrentInstance()
                        .addMessage(null,fm);
        }
        return resultString;
    }
    
    public String doRegister() {
        String resultString = "failure";
        try {
            testConnection();
            clearExplorePanel();
            clearSearchPanelBean();
            RegistryObjectCollectionBean.getInstance().doRegister();
            isSearchRendered = true;
            isExploreRendered = true;
            resultString = "showRegisterPage";
        } catch (Throwable t) {
            String causeMessage = null;
            if (t instanceof UndeclaredThrowableException) {
                causeMessage = t.getCause().getMessage();
            } else if (t instanceof JAXRException) {
                causeMessage = t.getMessage();
            }
            String errorMessage = WebUIResourceBundle.getInstance()
                                                     .getString("registerNotInitialized");
            
            StringBuffer sb = new StringBuffer(errorMessage);
            sb.append(" ");
            if (causeMessage != null) {
                sb.append(causeMessage).append(" ");
            }
            sb.append(WebUIResourceBundle.getInstance()
                                         .getString("checkLogForDetails"));
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                               sb.toString(), 
                                               null);
            FacesContext.getCurrentInstance()
                        .addMessage(null,fm);
        }
        return resultString;
    }
    
    public String doCustomize() {
        isSearchRendered = true;
        isExploreRendered = true;
        return "showCustomizePage";
    }
    
    private void testConnection() throws JAXRException, Exception {
        if (connectionTested == false) {
            getDQM().getRegistryObject("");
            connectionTested = true;
        }
    }
    
    public String clearSearchPanel() throws Exception {
        isSearchRendered = true;
        clearSearchPanelBean();
        return "showSearchPanel";
    }
    
    public String clearExplorePanel() throws Exception {
        isExploreRendered = true;   
        clearSearchPanelBean();
        return "showExplorePanel";
    }
    
    public static RegistryBrowser getInstance() {
        RegistryBrowser rb = (RegistryBrowser)FacesContext.getCurrentInstance()
                                                          .getExternalContext()
                                                          .getSessionMap()
                                                          .get("registryBrowser");
        if (rb == null) {
            rb = new RegistryBrowser();
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getSessionMap()
                        .put("registryBrowser", rb);
        }
        return rb;
    }
    
    // when dismissing the explore panel, we must also clear the search
    // results. This is required due to a <f:facet> bug in the SearchResults.jsp
    // page. When this bug is fixed, the call to doClear() will be removed.
    private void clearSearchPanelBean() throws Exception {
        Map sessionMap = (Map)FacesContext.getCurrentInstance()
                                          .getExternalContext()
                                          .getSessionMap();
        SearchPanelBean bean = (SearchPanelBean)sessionMap.get("searchPanel");
        if (bean != null) {
            bean.doClear();
        }
    }
    
    public String doLogout() {
        HttpSession httpSession = (HttpSession)FacesContext.getCurrentInstance()
                                                           .getExternalContext()
                                                           .getSession(false);
        httpSession.invalidate();
        return "logout";
    }

    /**
     * Getter for property userPreferencesBean.
     * @return Value of property userPreferencesBean.
     */
    public UserPreferencesBean getUserPreferencesBean() {

        return this.userPreferencesBean;
    }

    /**
     * Setter for property userPreferencesBean.
     * @param userPreferencesBean New value of property userPreferencesBean.
     */
    public void setUserPreferencesBean(UserPreferencesBean userPreferencesBean) {

        this.userPreferencesBean = userPreferencesBean;
    }


    /**
     * Authanticate the user and returning a boolean. If the user 
     * authenticity fails it return boolean "true" else it will return 
     * "false".   
     * @return boolean authenticity of user.
     */    
    public boolean isAuthenticated(){
        boolean authenticated = false;
        try{
            Set userCredentials = getConnection().getCredentials();
            if (!userCredentials.isEmpty()){
                authenticated = true;
            } 
        } catch(Exception e){
            log.error(WebUIResourceBundle.getInstance().getString("message.FailedToCheckTheUserCredentails"), e);

        }
        return authenticated;
    } 
    
    public String getAuthenticatedAsString() {
        String isAuthn = new String();
        isAuthn = isAuthn.valueOf(isAuthenticated());
        return isAuthn;
    }
    
    /**
     * getter for property title which is used display title in a web browser
     * @return title .
     */    
    public String getBrowserTitle() {
        if (this.browserTitle == null) {
            String property = ProviderProperties.getInstance()
                                                .getProperty("omar.client.thinbrowser.title", "applicationTitle");
            String title = WebUIResourceBundle.getInstance()
                                                 .getString(property, "ebXML Registry Repository");
            // Remove any markup in the text
            boolean skip = false;
            char[] titleChars = title.toCharArray();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < titleChars.length; i++) {
                char c = titleChars[i];
                if (c == '<') {
                    skip = true;
                } else if (c == '>') {
                    skip = false;
                } else {
                    if (!skip) {
                        sb.append(c);
                    }
                }
            }
            this.browserTitle = sb.toString();
        }
        return this.browserTitle;
    }
    
    /**
     * getter for property title which is used display title. 
     * @return title .
     */    
    public String getTitle() {
        if (this.title == null) {
            String property = ProviderProperties.getInstance().getProperty("omar.client.thinbrowser.title", "applicationTitle");
            this.title = WebUIResourceBundle.getInstance().getString(property, "ebXML Registry Repository");
        }
        return this.title;
    }
    
    /**
     * getter for company copyright 
     * @return title .
     */    
    public String getCompanyCopyright() {
        String companyCopyright = null;
        String property = ProviderProperties.getInstance()
                                            .getProperty("omar.client.thinbrowser.companyCopyright", "footerMessage");
        if (property != null) {
            companyCopyright = WebUIResourceBundle.getInstance().getString(property);
        }
        return companyCopyright;
    }
    
    /**
     * Getter for property cssFile.
     * @return Value of property cssFile.
     */
    public String getCssFile() {
       return ProviderProperties.getInstance().getProperty("omar.client.thinbrowser.cssFile", "ebxml.css");
    }

    public boolean isLogoFileDisplayed() {
        boolean isDisplayed = true;
        String logoFile = ProviderProperties.getInstance()
                                            .getProperty("omar.client.thinbrowser.logoFile", "images/freebxmlLogo.jpg");
        if (logoFile != null && logoFile.length() > 0) {
            if (logoFile.equalsIgnoreCase("doNotDisplay")) {
                isDisplayed = false;
            }
        }          
        return isDisplayed;
    }
     
    /**
     * Getter for property logoFile.
     * @return Value of property logoFile.
     */
    public String getLogoFile() {
        return ProviderProperties.getInstance().getProperty("omar.client.thinbrowser.logoFile", "images/freebxmlLogo.jpg");
    }
    
    /**
     * Getter for property logoFile.
     * @return Value of property logoFile.
     */
    public String getWelcomeMessagePage() {
        return ProviderProperties.getInstance()
                                 .getProperty("omar.client.thinbrowser.welcomeMessagePage", 
                                              "/WelcomeMessage.jsp");
    }
    
    /**
     * Getter for delete condition for LCM Operations.
     * @return Value of delete condition.
     */
    public String getDeletionScopeCode() {
        return deletionScopeCode;
    }
    /**
     * Setter for delete conditions for LCM Operations.
     * @return none
     */
    public void setDeletionScopeCode(String deletionScopeCode) {
        this.deletionScopeCode = deletionScopeCode;
    }
    
    /**
     * Getter for list of delete conditions for LCM Operations.
     * @return List of delete condition.
     */
    public List getDeletionScopeCodes() {
        ArrayList list = new ArrayList();

        list.add(new SelectItem(CanonicalSchemes.CANONICAL_DELETION_SCOPE_TYPE_ID_DeleteAll,
                WebUIResourceBundle.getInstance().getString("deleteObjectAndRepositoryItem")));

        list.add(new SelectItem(CanonicalSchemes.CANONICAL_DELETION_SCOPE_TYPE_ID_DeleteRepositoryItemOnly,
                WebUIResourceBundle.getInstance().getString("deleteRepositoryItemOnly")));

        return list;
    }
    
    public String getDocumentation() {
        String doc2Show = ProviderProperties.getInstance()
                                            .getProperty("omar.client.thinbrowser.doc.documentation");
        if (doc2Show == null || doc2Show.length() == 0) {
            String contextPath = 
                ((HttpServletRequest)FacesContext.getCurrentInstance()
                                                 .getExternalContext()
                                                 .getRequest())
                                                 .getContextPath();
            doc2Show = getLocalizedHTMLFile(contextPath+"/registry/thin/doc/index.html");
        }
        return doc2Show;
    }
    
    public boolean isDocumentationDisplayed() {
        boolean isDocDisplayed = true;
        String documentationDisplayed =  ProviderProperties.getInstance()
                                                           .getProperty("omar.client.thinbrowser.doc.documentation");
        if (documentationDisplayed != null &&
            documentationDisplayed.equalsIgnoreCase("doNotDisplay")) {
            isDocDisplayed = false;
        }
        return isDocDisplayed;
    }
    
    public String getUserGuide() {
        String userGuide = getLocalizedHTMLFile(ProviderProperties.getInstance()
                                             .getProperty("omar.client.thinbrowser.doc.userGuide"));
        if (userGuide == null || userGuide.length() == 0) {
            String contextPath = 
                ((HttpServletRequest)FacesContext.getCurrentInstance()
                                                 .getExternalContext()
                                                 .getRequest())
                                                 .getContextPath();
            userGuide = getLocalizedHTMLFile(contextPath+"/registry/thin/doc/thinBrowser/UserGuide.html");
        }
        return userGuide;
    }
    
    public boolean isUserGuideDisplayed() {
        boolean isUserGuideDisplayed = true;
        String userGuideDisplayed =  ProviderProperties.getInstance()
                                                       .getProperty("omar.client.thinbrowser.doc.userGuide");
        if (userGuideDisplayed != null &&
            userGuideDisplayed.equalsIgnoreCase("doNotDisplay")) {
            isUserGuideDisplayed = false;
        }
        return isUserGuideDisplayed;
    }
     
    public String getFaq() {
        String faq = ProviderProperties.getInstance()
                                            .getProperty("omar.client.thinbrowser.doc.faq", 
                                                         "http://ebxmlrr.sourceforge.net/aboutFAQ/About_freebXML_Registry.html");
        return faq;
    }
    
    public boolean isFaqDisplayed() {
        boolean isFaqDisplayed = true;
        String faqDisplayed =  ProviderProperties.getInstance()
                                                 .getProperty("omar.client.thinbrowser.doc.faq");
        if (faqDisplayed != null &&
            faqDisplayed.equalsIgnoreCase("doNotDisplay")) {
            isFaqDisplayed = false;
        }
        return isFaqDisplayed;
    }
    
    public String getAboutEbxml() {
        String aboutEbxml = ProviderProperties.getInstance()
                                            .getProperty("omar.client.thinbrowser.doc.aboutEbxml", 
                                                         "http://www.ebxml.org");
        return aboutEbxml;
    }

    public boolean isAboutEbxmlDisplayed() {
        boolean isFaqDisplayed = true;
        String faqDisplayed =  ProviderProperties.getInstance()
                                                 .getProperty("omar.client.thinbrowser.doc.aboutEbxml");
        if (faqDisplayed != null && 
            faqDisplayed.equalsIgnoreCase("doNotDisplay")) {
            isFaqDisplayed = false;
        }
        return isFaqDisplayed;
    }
    
    public String getAboutRegistry() {
        String aboutRegistry = ProviderProperties.getInstance()
                                            .getProperty("omar.client.thinbrowser.doc.aboutRegistry", 
                                                         "http://www.freebxml.org");
        return aboutRegistry;
    }

    public boolean isAboutRegistryDisplayed() {
        boolean isAboutRegistryDisplayed = true;
        String aboutRegistryDisplayed =  ProviderProperties.getInstance()
                                                 .getProperty("omar.client.thinbrowser.doc.aboutRegistry");
        if (aboutRegistryDisplayed != null && 
            aboutRegistryDisplayed.equalsIgnoreCase("doNotDisplay")) {
            isAboutRegistryDisplayed = false;
        }
        return isAboutRegistryDisplayed;
    }
    
    public String getDetailsHelp() {
        String detailsHelp = getLocalizedHTMLFile(ProviderProperties.getInstance()
                                               .getProperty("omar.client.thinbrowser.doc.detailsHelp"));
        if (detailsHelp == null || detailsHelp.length() == 0) {
            String contextPath = 
                ((HttpServletRequest)FacesContext.getCurrentInstance()
                                                 .getExternalContext()
                                                 .getRequest())
                                                 .getContextPath();
            detailsHelp = getLocalizedHTMLFile(contextPath+"/registry/thin/doc/thinBrowser/UserGuide.html#search_result_drill_downs");
        }
        return detailsHelp;
    }
   
    public String getExploreHelp() {
        String exploreHelp = getLocalizedHTMLFile(ProviderProperties.getInstance()
                                               .getProperty("omar.client.thinbrowser.doc.exploreHelp"));
        if (exploreHelp == null || exploreHelp.length() == 0) {
            String contextPath = 
                ((HttpServletRequest)FacesContext.getCurrentInstance()
                                                 .getExternalContext()
                                                 .getRequest())
                                                 .getContextPath();
            exploreHelp = getLocalizedHTMLFile(contextPath+"/registry/thin/doc/thinBrowser/UserGuide.html#explore_registry");
        }
        return exploreHelp;
    }

    public String getPublishHelp() {
        String publishHelp = getLocalizedHTMLFile(ProviderProperties.getInstance()
                                               .getProperty("omar.client.thinbrowser.doc.publishHelp"));
        if (publishHelp == null || publishHelp.length() == 0) {
            String contextPath = 
                ((HttpServletRequest)FacesContext.getCurrentInstance()
                                                 .getExternalContext()
                                                 .getRequest())
                                                 .getContextPath();
            publishHelp = getLocalizedHTMLFile(contextPath+"/registry/thin/doc/thinBrowser/UserGuide.html#publish");
        }
        return publishHelp;
    }
    
    public String getRelationshipHelp() {
        String relateHelp = getLocalizedHTMLFile(ProviderProperties.getInstance()
                                               .getProperty("omar.client.thinbrowser.doc.relationshipHelp"));
        if (relateHelp == null || relateHelp.length() == 0) {
            String contextPath = 
                ((HttpServletRequest)FacesContext.getCurrentInstance()
                                                 .getExternalContext()
                                                 .getRequest())
                                                 .getContextPath();
            relateHelp = getLocalizedHTMLFile(contextPath+"/registry/thin/doc/thinBrowser/UserGuide.html#creating_relationships");
        }
        return relateHelp;
    }
    
    public String getSearchHelp() {
        String searchHelp = getLocalizedHTMLFile(ProviderProperties.getInstance()
                                               .getProperty("omar.client.thinbrowser.doc.searchHelp"));
        if (searchHelp == null || searchHelp.length() == 0) {
            String contextPath = 
                ((HttpServletRequest)FacesContext.getCurrentInstance()
                                                 .getExternalContext()
                                                 .getRequest())
                                                 .getContextPath();
            searchHelp = getLocalizedHTMLFile(contextPath+"/registry/thin/doc/thinBrowser/UserGuide.html#search_registry");
        }
        return searchHelp;
    }
    
    public String getRegistryObjectHelp() {
        String roHelp = getLocalizedHTMLFile(ProviderProperties.getInstance()
                                          .getProperty("omar.client.thinbrowser.doc.registryObjectHelp"));
        if (roHelp == null || roHelp.length() == 0) {
            String contextPath = 
                ((HttpServletRequest)FacesContext.getCurrentInstance()
                                                 .getExternalContext()
                                                 .getRequest())
                                                 .getContextPath();
            roHelp = getLocalizedHTMLFile(contextPath+"/registry/thin/doc/thinBrowser/UserGuide.html#displaying_search_results");
        }
        return roHelp;
    }
    
    public String getUserRegistrationHelp() {
        String userRegHelp = getLocalizedHTMLFile(ProviderProperties.getInstance()
                                          .getProperty("omar.client.thinbrowser.doc.userRegistrationHelp"));
        if (userRegHelp == null || userRegHelp.length() == 0) {
            String contextPath = 
                ((HttpServletRequest)FacesContext.getCurrentInstance()
                                                 .getExternalContext()
                                                 .getRequest())
                                                 .getContextPath();
            userRegHelp = getLocalizedHTMLFile(contextPath+"/registry/thin/doc/UserRegistrationGuide.html");
        }
        return userRegHelp;
    }
    
    public String getPinObjectsHelp() {
        String pinObjectsHelp = getLocalizedHTMLFile(ProviderProperties.getInstance()
						     .getProperty("omar.client.thinbrowser.doc.pinObjectsHelp"));
        if (pinObjectsHelp == null || pinObjectsHelp.length() == 0) {
            String contextPath = 
                ((HttpServletRequest)FacesContext.getCurrentInstance()
                                                 .getExternalContext()
                                                 .getRequest())
                                                 .getContextPath();
            pinObjectsHelp = getLocalizedHTMLFile(contextPath+"/registry/thin/doc/thinBrowser/UserGuide.html#pinning_objects");
        }
        return pinObjectsHelp;
    }
    
    public String getPublishOperationMessage() {
        return this.publishOperationMessage;
    }
    
    public void setPublishOperationMessage(String publishOperationMessage) {
        this.publishOperationMessage = publishOperationMessage;
    }
    
    public void clearCredentials() throws Exception {
        ConnectionImpl connection = getConnection();
        if (connection != null) {
            connection.setX509Certificate(null);
            connection.logoff();
        }
    }
    public void  doEndSession() {
        try{
            clearCredentials();
            if (isAuthenticated()) {
                isCertLoaded = false;
            }
            HttpSession session = (HttpSession)FacesContext.getCurrentInstance()
                                                       .getExternalContext()
                                                       .getSession(false);
            session.invalidate();
        }catch(Exception ex){
            log.error(WebUIResourceBundle.getInstance()
              .getString("errorWhileRemovingSession")+ex.getMessage());
        }
        this.principalName = null;        
    }
    
    public boolean isCertLoaded() {
        return this.isCertLoaded;
    }

    public boolean isLogin() throws Exception {
        // If user is not authenticated, return 'false' to enable the button
        // If user is authenticated, return 'true' to disable the button
        return getConnection().isAuthenticated();
    }


    public String doLogin() {
        String status = "failure";
        if (!isCertLoaded()) {
            status = RegistryBrowser.getInstance().getAuthenticationStatus();
        } else {
            status = "loginSuccessful";           
        }
        return status;
    }
    
    public void setStandardContextPath(){
      try{  
      HttpServletRequest request = ((HttpServletRequest)FacesContext
        .getCurrentInstance()
        .getExternalContext()
        .getRequest());
        this.standardContextPath = new java.net.URL(request.getScheme(), 
                request.getServerName(), request.getServerPort(), 
                request.getContextPath());
      }catch(Exception ex){
         log.error(WebUIResourceBundle.getInstance()
              .getString("errorSettingStandardContext")+ex.getMessage());
      }
    }
    
    public URL getStandardContextPath(){
        return this.standardContextPath;
    }

    /**
     * Returns the localized version of an English html file
     * English file is something like <root dir>/doc/webUI/userGuide.html
     * the localized file will be <root dir>/doc/webUI/locale/userGuide.html
     */
    public String getLocalizedHTMLFile(String htmlFile) {

        if(htmlFile == null || htmlFile.equals(""))
            return htmlFile;

        // split the file into directory and file name. file name could be followed
        // by #anchor or \#anchor or something else
        int dirIndex = htmlFile.lastIndexOf('/');
        int extensionIndex = htmlFile.indexOf(".html");
        if(dirIndex == -1 || extensionIndex == -1 || (dirIndex + 1) > extensionIndex)
            return htmlFile;
        String dirName = htmlFile.substring(0, dirIndex);
        String fileName = htmlFile.substring(dirIndex + 1, extensionIndex);
        String anchor =htmlFile.substring(extensionIndex + 5);

        UserPreferencesBean userBean = new UserPreferencesBean();
        Locale uiLocale = userBean.getUiLocale();

        ServletContext ctx = (ServletContext)FacesContext.getCurrentInstance()
                                             .getExternalContext()
                                             .getContext();

        String localizedFile = dirName + "/" + uiLocale.toString() + "/" + fileName + ".html";
        // start with .. because the callers are passing the context path
        String realPath = ctx.getRealPath("../" + localizedFile);
	File f = new File(realPath);
	if(f.exists())
	   return localizedFile + anchor;

        // try to use language country, without variant
        if (uiLocale.getVariant() != null && !"".equals(uiLocale.getVariant())) {
            localizedFile = dirName + "/" + uiLocale.getLanguage() + "_" + uiLocale.getCountry() + "/" + fileName +  ".html";
            realPath = ctx.getRealPath("../" + localizedFile);
	    f = new File(realPath);
	    if(f.exists())
	        return localizedFile + anchor;
        }
 
        // try to use language without country and variant
        if (uiLocale.getCountry() != null && !"".equals(uiLocale.getCountry())) {
            localizedFile = dirName + "/" +  uiLocale.getLanguage() + "/" + fileName + ".html";
            realPath = ctx.getRealPath("../" + localizedFile);
	    f = new File(realPath);
	    if(f.exists())
	        return localizedFile + anchor;
        }
        //fall back to original file
        return htmlFile;
    }
    
    public String getLogoutLabel() {
        String label = null;
        if (isAuthenticated()) {
            label = WebUIResourceBundle.getInstance().getString("Logout");
        } else {
            label = WebUIResourceBundle.getInstance().getString("endSession");
        }      
        return label;
    }
    
    public String getAuthenticationMessage() {
        String message = null;
        if (isAuthenticated()) {
            message = WebUIResourceBundle.getInstance().getString("authenticationApproved");
        } else {
            message = WebUIResourceBundle.getInstance().getString("message.CouldNotAuthenticateUserWithClientCert");
        }
        return message;
    }

    private String getDisplayName() {
        String displayName = null;
        User user = null;
        try {
            user = getDQM().getCallersUser();
            if (user == null) {
                displayName = WebUIResourceBundle.getInstance()
                                                   .getString("RegistryGuest");
            } else {
                PersonName name = ((UserImpl)user).getPersonName();
                displayName = ((PersonNameImpl)name).getFormattedName();
            }
        } catch (Throwable t) {
            //log.error(WebUIResourceBundle.getInstance().getString("message.FailedToGetUserFromCert"), t);
            String msg = WebUIResourceBundle.getInstance().getString("registrySupport");
            OutputExceptions.error(log, msg, t);            
        }
        return displayName;
    }    

    public boolean getAtAuthentication() {
        boolean retVal = atAuthentication;
        atAuthentication = false;
        return retVal;
    }

    public void setAtAuthentication(boolean atAuthentication) {
        this.atAuthentication = atAuthentication;
    }

}
