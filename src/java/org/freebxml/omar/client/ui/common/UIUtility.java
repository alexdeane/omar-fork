/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/common/UIUtility.java,v 1.21 2006/06/01 18:03:25 geomurr Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.common;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Slot;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.ui.common.conf.bindings.Configuration;
import org.freebxml.omar.client.ui.common.conf.bindings.MethodParameter;
import org.freebxml.omar.client.ui.common.conf.bindings.ObjectTypeConfig;
import org.freebxml.omar.client.ui.common.conf.bindings.ObjectTypeConfigType;
import org.freebxml.omar.client.ui.common.conf.bindings.SearchResultsColumnType;
import org.freebxml.omar.client.ui.swing.RegistryBrowser;
import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ConceptImpl;
import org.freebxml.omar.client.xml.registry.infomodel.InternationalStringImpl;
import org.freebxml.omar.common.BindingUtility;

/**
 * Class Declaration.
 * @see
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 * @version   1.2, 05/02/00
 */
public class UIUtility {
    private static final Log log = LogFactory.getLog(UIUtility.class);
    
    private Map objectTypeToConfigMap = new HashMap();
    private Connection connection = null;
    JAXBContext uiJaxbContext = null;
    public org.freebxml.omar.client.ui.common.conf.bindings.ObjectFactory fac = null;
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /* # private Utility _utility; */
    private static UIUtility instance = null;

    /**
     * Class Constructor.
     *
     *
     * @see
     */
    protected UIUtility() {
        try {
            getUIJAXBContext();
            fac = new org.freebxml.omar.client.ui.common.conf.bindings.ObjectFactory();
        } catch (JAXBException e) {
            throw new UndeclaredThrowableException(e);
        }
        
        Configuration cfg = getConfiguration();
        if (cfg == null) {
            log.error(UICommonResourceBundle.getInstance().getString("message.nullConfig"));
        }
        List otCfgs = cfg.getObjectTypeConfig();
        Iterator iter = otCfgs.iterator();

        while (iter.hasNext()) {
            ObjectTypeConfigType otCfg = (ObjectTypeConfigType) iter.next();
            String id = otCfg.getId();
            objectTypeToConfigMap.put(id, otCfg);
        }
    }
    
    //The UI specific JAXB context
    public JAXBContext getUIJAXBContext() throws JAXBException {
        if (uiJaxbContext == null) {
            uiJaxbContext = JAXBContext.newInstance(
                    "org.freebxml.omar.client.ui.common.conf.bindings", 
                    this.getClass().getClassLoader());
        }

        return uiJaxbContext;
    }        
    
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    
    public BusinessQueryManager getBusinessQueryManager() throws JAXRException {
        if (connection == null) {
	    throw new JAXRException(UICommonResourceBundle.getInstance().getString("message.mustCallsetConnection"));
        } else {
            return connection.getRegistryService().getBusinessQueryManager();
        }        
    }
    
    public String[] getAssociationTypes() {
        String[] assocTypes = null;

        try {
            Collection concepts = ((BusinessQueryManagerImpl)getBusinessQueryManager()).findConceptsByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/%");

            int size = concepts.size();
            assocTypes = new String[size];

            Iterator iter = concepts.iterator();
            int i = 0;

            while (iter.hasNext()) {
                Concept concept = (Concept) iter.next();
                assocTypes[i++] = concept.getValue();
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return assocTypes;
    }

    public String[] getPhoneTypes() {
        String[] phoneTypes = null;

        try {
            Collection concepts = ((BusinessQueryManagerImpl)getBusinessQueryManager()).findConceptsByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_PhoneType + "/%");

            int size = concepts.size();
            phoneTypes = new String[size];

            Iterator iter = concepts.iterator();
            int i = 0;

            while (iter.hasNext()) {
                Concept concept = (Concept) iter.next();
                phoneTypes[i++] = concept.getValue();
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return phoneTypes;
    }

    public String[] getEmailTypes() {
        String[] emailTypes = null;

        try {
            Collection concepts = ((BusinessQueryManagerImpl)getBusinessQueryManager()).findConceptsByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_EmailType + "/%");

            int size = concepts.size();
            emailTypes = new String[size];

            Iterator iter = concepts.iterator();
            int i = 0;

            while (iter.hasNext()) {
                Concept concept = (Concept) iter.next();
                emailTypes[i++] = concept.getValue();
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return emailTypes;
    }
    
    /**
     * A method to load the Configuration Object for the UI
     * from a XML file. The file should be located at
     * "&lt;jaxr-ebxml.home&gt;/registry-browser-config.xml". If not,
     * then it wil be copied there from classpath
     * ("./conf/config.xml") in order to allow the user to customize
     * the UI.
     *
     * @return DOCUMENT ME!
     */
    public org.freebxml.omar.client.ui.common.conf.bindings.Configuration getConfiguration() {
        org.freebxml.omar.client.ui.common.conf.bindings.Configuration cfg = null;
        java.io.InputStream is = null;
        File cfgFile = null;
        boolean readCfgFromHome = true;
        String jaxrHome = null;

        jaxrHome = org.freebxml.omar.client.xml.registry.util.ProviderProperties.getInstance()
                                                                                .getProperty("jaxr-ebxml.home");

        java.io.BufferedInputStream bis = null;
        java.io.BufferedOutputStream bos = null;

        try {
            File jaxrHomeDir = new File(jaxrHome);

            if (!jaxrHomeDir.exists()) {
                jaxrHomeDir.mkdir();
            }

            cfgFile = new File(jaxrHomeDir, "registry-browser-config.xml");

            if (!cfgFile.canRead()) {
                URL cfgFileUrl = getClass().getResource("/org/freebxml/omar/client/ui/common/conf/config.xml");
                bis = new java.io.BufferedInputStream(cfgFileUrl.openStream());
                bos = new java.io.BufferedOutputStream(new java.io.FileOutputStream(
                            cfgFile));

                byte[] buffer = new byte[1024];
                int bytesRead = 0;

                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException ioe) {
            readCfgFromHome = false;
            log.warn(UICommonResourceBundle.getInstance().getString("message.browserConfigHome"));

        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }

            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                }
            }
        }

        // Now get the configuration
        try {
            if (readCfgFromHome) {
                is = new java.io.FileInputStream(cfgFile);
            } else {
                URL cfgFileUrl = getClass().getResource("conf/config.xml");
                is = cfgFileUrl.openStream();
            }

            javax.xml.bind.Unmarshaller unmarshaller = (new org.freebxml.omar.client.ui.common.conf.bindings.ObjectFactory()).createUnmarshaller();
            cfg = (org.freebxml.omar.client.ui.common.conf.bindings.Configuration) unmarshaller.unmarshal(is);
        } catch (IOException e) {
            log.error(UICommonResourceBundle.getInstance().getString("message.browserConfig", new Object[]{e}));
        } catch (javax.xml.bind.JAXBException e) {
            log.error(UICommonResourceBundle.getInstance().getString("message.browserConfig", new Object[]{e}));
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        return cfg;
    }
    
    public Object getColumnValue(SearchResultsColumnType srCol, 
                                     String className,
                                     RegistryObject registryObject) 
        throws ClassNotFoundException, NoSuchMethodException, 
        IllegalArgumentException, IllegalAccessException, 
        InvocationTargetException, ExceptionInInitializerError, JAXRException {
            
        Locale locale = Locale.getDefault();
        String charSet = "UTF-8";
        return getColumnValue(srCol, className, registryObject, locale, charSet);
    }
    
    public Object getColumnValue(SearchResultsColumnType srCol, 
                                     String className,
                                     RegistryObject registryObject,
                                     Locale locale,
                                     String charSet) 
        throws ClassNotFoundException, NoSuchMethodException, 
        IllegalArgumentException, IllegalAccessException, 
        InvocationTargetException, ExceptionInInitializerError, JAXRException {
    
        Object value = null;
        String methodName = srCol.getMethod();
        Class clazz = Class.forName(className);

        List params = srCol.getMethodParameter();
        int numParams = params.size();
        Class[] parameterTypes = new Class[numParams];
        Object[] parameterValues = new Object[numParams];
        Iterator paramsIter = params.iterator();
        int i = 0;

        //Setup parameterTypes
        while (paramsIter.hasNext()) {
            MethodParameter mp = (MethodParameter) paramsIter.next();
            String paramTypeName = mp.getType();
            parameterTypes[i] = Class.forName(paramTypeName);

            String paramValue = mp.getValue();
            parameterValues[i] = paramValue;

            i++;
        }

        Method method = clazz.getMethod(methodName, parameterTypes);

        //Invoke method to get Value as object. Convert the object to a format suitable for display
        try {
            value = method.invoke(registryObject, parameterValues);
        } catch (java.lang.IllegalArgumentException ex) {
            //TODO: why does this happen?
             log.error(UICommonResourceBundle.getInstance().getString("message.methodInvocationError", new Object[]{ex}));
        }
        // handle value returned from method.invoke(...)
        if (value == null) {
            value = "";
        } else {
            value = convertValue(value, locale, charSet);
            if (value instanceof Collection) {
                value = formatCollectionString((Collection)value);
            }
        }
        return value;
    }
    
    public Object convertValue(Object value) {     
        Locale defaultLocale = Locale.getDefault();
        String charSet = "utf-8";
        return convertValue(value, defaultLocale, charSet);
    }
    
    public Object convertValue(Object value, Locale locale, String charSet) {
        Object finalValue = null;

        try {
            if (value instanceof InternationalString) {
                LocalizedString iString = ((InternationalStringImpl) value)
                    .getClosestLocalizedString(locale, charSet);
                // LS might be null
                if (iString == null) {
                    value = "";
                } else {
                    value = iString.getValue();
                }
            }
            if (value instanceof ExternalLink) {
                finalValue = ((ExternalLink) value).getExternalURI();

                try {
                    URL url = new URL(((ExternalLink) value).getExternalURI());
                    finalValue = url;
                } catch (MalformedURLException e) {
                }
            } else if (value instanceof Collection) {
                //Converts elements of Collection
                Collection c1 = (Collection) value;
                Collection c2 = new ArrayList();
                Iterator iter = c1.iterator();

                while (iter.hasNext()) {
                    c2.add(convertValue(iter.next()));
                }

                finalValue = c2;
            } else if (value instanceof Slot) {
                Collection c = ((Slot) value).getValues();
                finalValue = c;
            } else if (value instanceof Concept) {
                finalValue = ((Concept) value).getValue();
            } else {
                finalValue = value;
            }
        } catch (JAXRException e) {
            log.error(e);
        }

        return finalValue;
    }
    
    public String formatCollectionString(Collection collection) {
        String value = collection.toString();
        StringBuffer sb3 = new StringBuffer(value);
        // remove leading straight bracket '['
        sb3.deleteCharAt(0);
        // remove ending straight bracket ']'
        sb3.deleteCharAt(sb3.length()-1);
        value = sb3.toString();
                
        StringBuffer sb = new StringBuffer();
        String[] tokens = value.split(", ");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (i > 0) {
                sb.append(", ");
            }
            // construct hyperlink
            if (token.indexOf("://") != -1) {
                StringBuffer sb2 = new StringBuffer();
                sb2.append("<a href=\"");
                sb2.append(token);
                sb2.append("\" target=\"_new\">");
                sb2.append(token);
                sb2.append("</a>");
                token = sb2.toString();
            }
            sb.append(token);
        }
        value = sb.toString();
        return value;
    }
    
    public Concept getCommonObjectType(Collection registryObjects)
        throws JAXRException 
    {
        Concept commonType = null;
        String commonPath = null;

        Iterator iter = registryObjects.iterator();
        while (iter.hasNext()) {
            RegistryObject ro = (RegistryObject)iter.next();
            if (ro != null) {
                ConceptImpl type = (ConceptImpl)ro.getObjectType();

                String path = null;
                if (ro instanceof ExternalLink) {
                    path = commonPath = "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + 
                        "/RegistryObject/ExternalLink";
                } else {
                    path = type.getPath();
                }

                if (commonPath == null) {
                    commonPath = path;
                } 
                else {
                    if (!(commonPath.equals(path))) {
                        //Determine common base type for both:
                        if (commonPath.startsWith(path + "/")) {
                            //The new type is a baseType of current commonType
                            //Set commonType to new type
                            commonPath = path;
                        } else if (path.startsWith(commonPath + "/")) {
                            //The current commonType is a baseType of new type
                            //Leave commonType unchanged
                            continue;
                        } else {
                            //The current commonType and new type 
                            //do not have an ancestor/descendant relationship
                            //Find a common base type between them.
                            String smallerPath = commonPath;
                            String biggerPath = path;
                            boolean swap = false;

                            if (commonPath.length() > path.length()) {
                                smallerPath = path;
                                biggerPath = commonPath;
                                swap = true;
                            }

                            int len = smallerPath.length();

                            for (int i = 0; i < len; i++) {
                                if (smallerPath.charAt(i) != biggerPath.charAt(i)) {
                                    commonPath = smallerPath.substring(0,
                                            smallerPath.lastIndexOf('/', i));

                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (commonPath == null) {
            //Use RegistryObject path
            commonPath = "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject";
        }

        commonType = (Concept) getBusinessQueryManager().findConceptByPath(commonPath);

        return commonType;
    }
        
    public ObjectTypeConfig getObjectTypeConfig(Concept commonObjectType)
        throws JAXRException {
        ObjectTypeConfig cfg = null;

        while (true) {
            if (commonObjectType == null) {
                //This could happen if there is some problem with access control on the server and the ObjectType node for RegistryObject is not returned.
                //Use RegistryObject as default objectType
                cfg = (ObjectTypeConfig) objectTypeToConfigMap.get(
                            BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryObject);                
            } else {            
                //Now get the ObjectTypeConfig for the commonObjectType
                cfg = (ObjectTypeConfig) objectTypeToConfigMap.get(commonObjectType.getKey().getId());
            }                                                                       

            if (cfg != null) {
                break;
            } else {
                Concept parent = commonObjectType.getParentConcept();

                if (parent == null) {
                    //Use RegistryObject (id BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryObject) as default objectType
                    cfg = (ObjectTypeConfig) objectTypeToConfigMap.get(
                            BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryObject);

                    break;
                } else {
                    commonObjectType = parent;
                }
            }
        }
        return cfg;
    }
        
    public Object convertToRimBinding(Object uiBindingObj) throws JAXRException {
        Object rimBindingObj = null;
        StringWriter sw = new StringWriter();
        try {
            Marshaller marshaller = uiJaxbContext.createMarshaller();
            marshaller.marshal(uiBindingObj, sw);

            Unmarshaller unmarshaller = BindingUtility.getInstance().getJAXBContext().createUnmarshaller();
            //unmarshaller.setValidating(true);
            unmarshaller.setEventHandler(new ValidationEventHandler() {
                public boolean handleEvent(ValidationEvent event) {
                    boolean keepOn = false;

                    return keepOn;
                }
            });
            
            
            rimBindingObj = unmarshaller.unmarshal(new StreamSource( new StringReader( sw.toString() ) ));
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new JAXRException( e);
        }
        return rimBindingObj;
    }
    
    

    public static String initCapString(String capString) {
        return (capString.substring(0, 1).toUpperCase() + capString.substring(1));
    }
        
    public static String lowerCamelCase(String capString) {
        return (capString.substring(0, 1).toLowerCase() + capString.substring(1));
    }

    /**
     * Method Declaration.
     *
     *
     * @return
     *
     * @see
     */
    public synchronized static UIUtility getInstance() {
        if (instance == null) {
            instance = new UIUtility();
        }

        return instance;
    }
    
}
