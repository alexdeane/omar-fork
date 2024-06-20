/*
 * SearchResultsBean.java
 *
 * Created on November 14, 2003, 5:38 PM
 */

package org.freebxml.omar.client.ui.thin;

import java.beans.*;
import java.util.*;

/**
 *
 * @author  xwsrrsf
 */
public class SearchResultValueBean implements java.io.Serializable {        
    
    private String _header;    
    private Object _value;
    
    static final long serialVersionUID = -3326494001436824271L;
    
    /** Creates new SearchResultsBean */
    public SearchResultValueBean(String header, Object value) {
         _header = header;
         _value = value;
    }
    
    public String getHeader() {
        return _header;
    }
  
    public Object getValue() {
        return _value;
    }
       
}
