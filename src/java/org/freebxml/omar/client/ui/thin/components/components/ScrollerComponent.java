/*
 * $Id: ScrollerComponent.java,v 1.9 2007/05/08 18:04:31 psterk Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *    
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *  
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *  
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

package org.freebxml.omar.client.ui.thin.components.components;

import org.freebxml.omar.client.ui.thin.components.renderkit.Util;
import org.freebxml.omar.client.ui.thin.RegistryObjectCollectionBean;
import org.freebxml.omar.client.ui.thin.ScrollerBean;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIForm;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import javax.faces.webapp.UIComponentTag;

import java.io.IOException;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

/**
 * This component produces a search engine style scroller that facilitates
 * easy navigation over results that span across several pages. It
 * demonstrates how a component can do decoding and encoding
 * without delegating it to a renderer.
 */
public class ScrollerComponent extends UICommand {

    public static final String NORTH = "NORTH";
    public static final String SOUTH = "SOUTH";
    public static final String EAST = "EAST";
    public static final String WEST = "WEST";
    public static final String BOTH = "BOTH";

    public static final int ACTION_NEXT = -1;
    public static final int ACTION_PREVIOUS = -2;
    public static final int ACTION_NUMBER = -3;

    public static final String FORM_NUMBER_ATTR = "com.sun.faces.FormNumber";

    /**
     * The component attribute that tells where to put the user supplied
     * markup in relation to the "jump to the Nth page of results"
     * widget.
     */
    public static final String FACET_MARKUP_ORIENTATION_ATTR =
        "navFacetOrientation";
    
    private String lastAction = "1";
    private UIForm scrollerForm = null;
    private UIForm searchResultsForm = null;
    private UIData data = null; 
    private UIForm form = null;

    public ScrollerComponent() {
        super();
        this.setRendererType(null);
    }
    
    public String getLastAction() {
        return lastAction;
    }
    
    public void decode(FacesContext context) {
        String curPage = null;
        String action = null;
        int actionInt = 0;
        int currentPage = 1;
        int currentRow = 1;
        String clientId = getClientId(context);
        Map requestParameterMap = (Map) context.getExternalContext().
            getRequestParameterMap();
        action = (String) requestParameterMap.get(clientId + "_action");
        if (action.equals("")) {
            action = lastAction;
        } 
        MethodBinding mb = null;
        if (UIComponentTag.isValueReference(action)) {
            mb = FacesContext.getCurrentInstance().getApplication()
                .createMethodBinding(action, null);
        } else {
            mb = Util.createConstantMethodBinding(action);
        }
        this.getAttributes().put("action", mb);            
        lastAction = action;

        curPage = (String) requestParameterMap.get(clientId + "_curPage");
        if (! curPage.equals("")) {
            currentPage = Integer.valueOf(curPage).intValue();
        } 

        // Assert that action's length is 1.
        switch (actionInt = Integer.valueOf(action).intValue()) {
            case ACTION_NEXT:
                currentPage++;
                break;
            case ACTION_PREVIOUS:
                // Assert 1 < currentPage
                if (currentPage > 1) {
                    currentPage--;
                }
                break;
            default:
                currentPage = actionInt;
                break;
        } 
        // from the currentPage, calculate the current row to scroll to.
        currentRow = (currentPage - 1) * getRowsPerPage(context);
        this.getAttributes().put("currentPage", new Integer(currentPage));
        this.getAttributes().put("currentRow", new Integer(currentRow));
        
        if (action == null || action.length() == 0) {
            // nothing to decode
            return;
        } else {
            this.queueEvent(new ActionEvent(this));
        }
    }


    public void encodeBegin(FacesContext context) throws IOException {
        return;
    }


    public void encodeEnd(FacesContext context) throws IOException {
        int currentPage = 1;

        ResponseWriter writer = context.getResponseWriter();

        String clientId = getClientId(context);
        /*
        Integer curPage = (Integer) getAttributes().get("currentPage");
        if (curPage == null || curPage.equals("")) {
            currentPage = getCurrentPage(context);
        } else {
            currentPage = curPage.intValue();
        }
         */
        currentPage = getCurrentPage(context);
        int totalPages = getTotalPages(context);

        writer.write("<table border=\"0\" cellpadding=\"0\" align=\"center\">");
        writer.write("<tr align=\"center\" valign=\"top\">");
        writer.write(
            "<td></td>");

        // write the Previous link if necessary
        writer.write("<td>");
        writeNavWidgetMarkup(context, clientId, ACTION_PREVIOUS,
                             (1 < currentPage));
        // last arg is true iff we're not the first page
        writer.write("</td>");

        // render the page navigation links
        int i = 0;
        int first = 1;
        int last = totalPages;

        if (10 < currentPage) {
            first = currentPage - 10;
        }
        if ((currentPage + 9) < totalPages) {
            last = currentPage + 9;
        }
        for (i = first; i <= last; i++) {
            writer.write("<td>");
            writeNavWidgetMarkup(context, clientId, i, (i != currentPage));
            writer.write("</td>");
        }

        // write the Next link if necessary
        writer.write("<td>");
        writeNavWidgetMarkup(context, clientId, ACTION_NEXT,
                             (currentPage < totalPages));
        writer.write("</td>");
        writer.write("</tr>");
        writer.write(getHiddenFields(clientId));
        writer.write("</table>");
    }


    public boolean getRendersChildren() {
        return true;
    }


    /**
     * <p>Return the component family for this component.</p>
     */
    public String getFamily() {

        return ("Scroller");

    }
     
    //
    // Helper methods
    // 

    /**
     * Write the markup to render a navigation widget.  Override this to
     * replace the default navigation widget of link with something
     * else.
     */
    protected void writeNavWidgetMarkup(FacesContext context,
                                        String clientId,
                                        int navActionType,
                                        boolean enabled) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String facetOrientation = NORTH;
        String facetName = null;
        String linkText = null;
        String localLinkText = null;
        UIComponent facet = null;
        boolean isCurrentPage = false;
        boolean isPageNumber = false;

        // Assign values for local variables based on the navActionType
        switch (navActionType) {
            case ACTION_NEXT:
                facetName = "next";
                linkText = "Next";
                break;
            case ACTION_PREVIOUS:
                facetName = "previous";
                linkText = "Previous";
                break;
            default:
                facetName = "number";
                linkText = "" + navActionType;
                isPageNumber = true;
                // heuristic: if navActionType is number, and we are not
                // enabled, this must be the current page.
                if (!enabled) {
                    facetName = "current";
                    isCurrentPage = true;
                }
                break;
        }

        // leverage any navigation facets we have
        writer.write("\n&nbsp;");
        if (enabled) {
            writer.write("<a " + getAnchorAttrs(context, clientId,
                                                navActionType) + ">");
        }

        facet = getFacet(facetName);
        // render the facet pertaining to this widget type in the NORTH
        // and WEST cases.
        if (facet != null) {
            // If we're rendering a "go to the Nth page" link
            if (isPageNumber) {
                // See if the user specified an orientation
                String facetO = (String) getAttributes().get(
                    FACET_MARKUP_ORIENTATION_ATTR);
                if (facet != null) {
                    facetOrientation = facetO;
                    // verify that the orientation is valid
                    if (!(facetOrientation.equalsIgnoreCase(NORTH) ||
                        facetOrientation.equalsIgnoreCase(SOUTH) ||
                        facetOrientation.equalsIgnoreCase(EAST) ||
                        facetOrientation.equalsIgnoreCase(WEST))) {
                        facetOrientation = NORTH;
                    }
                }
            }

            // output the facet as specified in facetOrientation
            if (facetOrientation.equalsIgnoreCase(NORTH) ||
                facetOrientation.equalsIgnoreCase(EAST)) {
                facet.encodeBegin(context);
                if (facet.getRendersChildren()) {
                    facet.encodeChildren(context);
                }
                facet.encodeEnd(context);
            }
            // The difference between NORTH and EAST is that NORTH
            // requires a <br>.
            if (facetOrientation.equalsIgnoreCase(NORTH)) {
                writer.startElement("br", null); // PENDING(craigmcc)
                writer.endElement("br");
            }
        }

        // if we have a facet, only output the link text if
        // navActionType is number
        if (null != facet) {
            if (navActionType != ACTION_NEXT &&
                navActionType != ACTION_PREVIOUS) {
                writer.write(linkText);
            }
        } else {
            if (enabled){
                writer.write(linkText);
            } else {
                writer.startElement("b", null); // PENDING(craigmcc)
                writer.write(linkText);
                writer.endElement("b");
            }
        }

        // output the facet in the EAST and SOUTH cases
        if (null != facet) {
            if (facetOrientation.equalsIgnoreCase(SOUTH)) {
                writer.startElement("br", null); // PENDING(craigmcc)
                writer.endElement("br");
            }
            // The difference between SOUTH and WEST is that SOUTH
            // requires a <br>.
            if (facetOrientation.equalsIgnoreCase(SOUTH) ||
                facetOrientation.equalsIgnoreCase(WEST)) {
                facet.encodeBegin(context);
                if (facet.getRendersChildren()) {
                    facet.encodeChildren(context);
                }
                facet.encodeEnd(context);
            }
        }

        if (enabled) {
            writer.write("</a>");
        }

    }


    /**
     * <p>Build and return the string consisting of the attibutes for a
     * result set navigation link anchor.</p>
     *
     * @param context  the FacesContext
     * @param clientId the clientId of the enclosing UIComponent
     * @param action   the value for the rhs of the =
     *
     * @return a String suitable for setting as the value of a navigation
     *         href.
     */
    private String getAnchorAttrs(FacesContext context, String clientId,
                                  int action) {
        int currentPage = 1;
      
        int lastIndex = clientId.lastIndexOf(':');
        String formClientId = clientId.substring(0, lastIndex);
        Integer curPage = (Integer) getAttributes().get("currentPage");
        if (curPage != null) {
            currentPage = curPage.intValue();
        }
        String result =
            "href=\"#\" " +
            "onclick=\"" +
            "document.forms['" + formClientId + "']['" + clientId +
            "_action'].value='" +
            action +
            "'; " +
            "document.forms['" + formClientId + "']['" + clientId +
            "_curPage'].value='" +
            currentPage +
            "'; " +
            "document.forms['" + formClientId + "'].submit(); return false;\"";

        return result;
    }


    private String getHiddenFields(String clientId) {
        String result =
            "<input type=\"hidden\" name=\"" + clientId + "_action\"/>\n" +
            "<input type=\"hidden\" name=\"" + clientId + "_curPage\"/>";

        return result;
    }


    protected UIForm getScrollerForm(FacesContext context) {       
        String forValue = (String) getAttributes().get("for");
        return getUIForm(context, forValue);
    }
    
    protected UIForm getUIForm(FacesContext context, String id) {
        UIData data = getUIData(context, id);
        return this.form;
    }
    
    protected UIData getUIData(FacesContext context, String id) {
        UIData data = null;
        if (this.data != null) {
            data = this.data;
        } else {
            UIViewRoot root = context.getViewRoot();
            data = findUIData(root.getChildren(), id);
            if (data != null) {
                this.data = data; // cache it
            }
        }
        return data;
    }
    
    private UIData findUIData(List children, String id) {
        Iterator itr = children.iterator();
        UIData data = null;
        UIComponent component = null;
        while (itr.hasNext()) {
            component = (UIComponent)itr.next();
            if (component instanceof UIForm) {
                data = getUIDataFromUIForm(component.getChildren(), id);
                if (data != null) {
                    this.form = (UIForm)component; //cache the form
                    break;
                }
            } else {
                if (component.getChildCount() > 0) {
                    // Search through this component's children too
                    data = findUIData(component.getChildren(), id);
                    if (data != null) {
                        break;
                    }
                }
            }
        }
        return data;
    }
    
    private UIData getUIDataFromUIForm(List children, String id) {
        UIData data = null;
        Iterator itr = children.iterator();
        while (itr.hasNext()) {
            UIComponent component = (UIComponent)itr.next();
            if (component instanceof UIData) {
                String dataId = component.getId();
                if (dataId.equals(id)) {
                    data = (UIData)component;
                    break;
                }
            } else {
                if (component.getChildCount() > 0) {
                    data = getUIDataFromUIForm(component.getChildren(), id);
                    if (data != null) {
                        break;
                    }
                }
            }
        }
        return data;
    }
    
    
    protected int getFormNumber(FacesContext context) {
        Map requestMap = context.getExternalContext().getRequestMap();
        int numForms = 0;
        Integer formsInt = null;
        // find out the current number of forms in the page.
        if (null != (formsInt = (Integer)
            requestMap.get(FORM_NUMBER_ATTR))) {
            numForms = formsInt.intValue();
// since the form index in the document starts from 0.
            numForms--;
        }
        return numForms;
    }


    /**
     * Returns the total number of pages in the result set based on
     * <code>rows</code> and <code>rowCount</code> of <code>UIData</code>
     * component that this scroller is associated with.
     * For the purposes of this demo, we are assuming the <code>UIData</code> to
     * be child of <code>UIForm</code> component and not nested inside a custom
     * NamingContainer.
     */
    protected int getTotalPages(FacesContext context) {
        
        String forValue = (String) getAttributes().get("for");
        UIData uiData = (UIData)getUIData(context, forValue);
        if (uiData == null) {
            return 0;
        }
        int rowsPerPage = uiData.getRows();
        int totalRows = 0;
        int result = 0;
        /* replace with count from scrollerbean
        totalRows = uiData.getRowCount();
         */
        ScrollerBean sbean = RegistryObjectCollectionBean.getInstance().getScrollerBean();
        totalRows = sbean.getTotalResultCount();
        result = totalRows / rowsPerPage;
        if (0 != (totalRows % rowsPerPage)) {
            result++;
        }
        return result;
    }
    
     public int getCurrentPage(FacesContext context) {
        String forValue = (String) getAttributes().get("for");
        UIData uiData = (UIData)getUIData(context, forValue);
        if (uiData == null) {
            return 0;
        }
        int rowsPerPage = uiData.getRows();
        // occasionally this object is invalided, and it longer has the current
        // row.  In these cases, get current row from ScrollerBean
        Map sessionMap = FacesContext.getCurrentInstance()
                                             .getExternalContext()
                                             .getSessionMap();
        RegistryObjectCollectionBean searchPanelBean = 
            (RegistryObjectCollectionBean)sessionMap.get("roCollection");
        int currentRow = searchPanelBean.getScrollerBean().getCurrentRow();
        uiData.setFirst(currentRow);
        int result = currentRow / rowsPerPage + 1;
        return result;
    }


    /**
     * Returns the number of rows to display by looking up the
     * <code>UIData</code> component that this scroller is associated with.
     * For the purposes of this demo, we are assuming the <code>UIData</code> to
     * be child of <code>UIForm</code> component and not nested inside a custom
     * NamingContainer.
     */
    protected int getRowsPerPage(FacesContext context) {
        String forValue = (String) getAttributes().get("for");
        UIData uiData = (UIData)getUIData(context, forValue);
        if (uiData == null) {
            return 0;
        }
        return uiData.getRows();
    }
} 
