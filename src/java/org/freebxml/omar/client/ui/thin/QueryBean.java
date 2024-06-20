/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/thin/QueryBean.java,v 1.14 2006/06/14 01:02:10 psterk Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.thin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.registry.JAXRException;

import org.freebxml.omar.common.CanonicalConstants;
import org.freebxml.omar.client.xml.registry.infomodel.AdhocQueryImpl;


import javax.xml.registry.infomodel.InternationalString;
import org.freebxml.omar.client.xml.registry.infomodel.InternationalStringImpl;
import org.freebxml.omar.client.ui.common.conf.bindings.Query;
import org.freebxml.omar.client.ui.common.conf.bindings.Parameter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;

/**
  *
  * @author  dhilder
  */
public class QueryBean extends java.lang.Object implements java.io.Serializable {
    
    private String name;
    private String description;
    private InternationalString i18nName;
    private InternationalString i18nDescription;
    private String queryId;
    private String queryString;
    private Map parameters;
    private Log log = LogFactory.getLog(QueryBean.class);
    
   
    public QueryBean(Query query, AdhocQueryImpl adhocQuery) 
        throws JAXRException {
        this.queryId = query.getAdhocQueryRef().getId();
        this.i18nName = adhocQuery.getName();
        this.name = getLocalizedValue(i18nName);
        this.i18nDescription = adhocQuery.getDescription();
        this.description = getLocalizedValue(i18nDescription);
        this.queryString = adhocQuery.toString();
        this.parameters = getParameterBeans(query);
    }
    
    public QueryBean(String name, String description, String queryId, String queryString) {
        this.name = name;
        this.description = description;
        this.queryId = queryId;
        this.queryString = queryString;
        parameters = parseParameters(queryString);
    }
    
    private Map getParameterBeans(Query query) {
        Map beans = new TreeMap();
        Iterator queryItr = query.getParameter().iterator();
        while (queryItr.hasNext()) {
            Parameter param = (Parameter)queryItr.next();
            String value = param.getParameterName();
            // Replace the dot with an underscore to match the method binding
            // created by the QueryPanelComponent. Reason is that JSF will
            // misinterpret the '.' as a method dereference in the method binding.
            // The replacement must only happen in the key. The value must 
            // retain the '.' so it matches the value of the query parameter
            // in the stored query.
            String paramBeanKey = value.replace('.', '_');
            beans.put(paramBeanKey, new ParameterBean(value));
        }
        return beans;
    }
    
    private String getLocalizedValue(InternationalString i18n) throws JAXRException {
        String value = ((InternationalStringImpl)i18n).getClosestValue(FacesContext.getCurrentInstance().getViewRoot().getLocale());
        
        return value;
    }
   
    private Map parseParameters(String s) {
        TreeMap p = new TreeMap();
        s = s.trim();
        int index = s.lastIndexOf(';');
        if (index != -1) {
            s = s.substring(0, index);
        }
        String[] tokens = s.split("[ ,'()=%;]");
        for (int i=0; i<tokens.length; i++) {
            if (tokens[i].startsWith("$")) {
                // Assume trailing numbers indicate a multi value. Trim
                // them off and just create a single parameter.
                String[] arrayToken = tokens[i].split("[0-9]$");
                tokens[i] = arrayToken[0];
                p.put(tokens[i], new ParameterBean(tokens[i]));
            }
        }
        return p;
    }
    
    public String getName() {
        String value = name;
        try {
            if (i18nName != null) {
                value = getLocalizedValue(i18nName);
            }
	} catch (JAXRException e) {
            log.error(e, e);
        }
        return value;
    }

    public void setName(String name) {
        log.debug("Setting current query name to:" + name);
    }
    
    public String getDescription() {
        String value = description;
        try {
            if (i18nDescription != null) {
                value = getLocalizedValue(i18nDescription);
            }
	} catch (JAXRException e) {
            log.error(e, e);
        }
        return value;
    }
    
    public String getQueryId() {
        return queryId;
    }
    
    public String getQueryString() {
        return queryString;
    }
    
    public Map getParameters() {
        return parameters;
    }
    
    public Map getQueryParameters() {
        HashMap queryParameters = new HashMap();
        String queryIdURN = CanonicalConstants.CANONICAL_SLOT_QUERY_ID;
        queryParameters.put(queryIdURN, queryId);
        Iterator parameterBeans = parameters.values().iterator();
        while (parameterBeans.hasNext()) {
            ParameterBean parameterBean = (ParameterBean)parameterBeans.next();
            log.debug("Param Bean text value: " + parameterBean.getTextValue());
            parameterBean.addQueryParameters(queryParameters);
        }
        return queryParameters;
    }
    
}
