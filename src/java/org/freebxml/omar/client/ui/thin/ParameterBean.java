/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/thin/ParameterBean.java,v 1.7 2006/02/23 14:49:18 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.thin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
  *
  * @author  dhilder
  */
public class ParameterBean implements java.io.Serializable {
    
    private String name;
    private String textValue = null;    
    private Boolean booleanValue = null;    
    private ArrayList listValue = null;    

    
    public ParameterBean(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getTextValue() {
        return textValue;
    }
    
    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }
    
    public Boolean getBooleanValue() {
        return booleanValue;
    }
    
    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
    
    public java.lang.Object[] getListValue() {
        if (listValue == null) {
            listValue = new ArrayList();
        }
        return listValue.toArray();
    }
    
    public void setObjectArrayValue(java.lang.Object[] newListValue) {
        listValue = new ArrayList(Arrays.asList(newListValue));
    }
    
    public void setListValue(Collection newListValue) {
        if (newListValue == null) {
            listValue = null;
        } else {
            listValue = new ArrayList(newListValue);
        }
    }
    
    public void addQueryParameters(Map queryParameters) {
        if (textValue != null && textValue.length() > 0) {
            // skip empty string parameters
            textValue = textValue.trim();
            if (textValue.length() > 0) {
                queryParameters.put(name, textValue);
            }
        }
        else if (booleanValue != null) {
            String booleanStringValue = null;
            if (booleanValue.booleanValue()) {
                booleanStringValue = "1";
            } else {
                booleanStringValue = "0";
            }
            queryParameters.put(name, booleanStringValue);
        }
        else if (listValue != null && !listValue.isEmpty()) {
            int i = 1;
            Iterator values = listValue.iterator();
            while (values.hasNext()) {
                queryParameters.put(name + String.valueOf(i), values.next().toString());
                i++;
            }
        }
    }
}
