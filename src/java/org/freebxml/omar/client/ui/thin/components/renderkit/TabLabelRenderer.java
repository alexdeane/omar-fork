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

// TabLabelRenderer.java

package org.freebxml.omar.client.ui.thin.components.renderkit;


import org.freebxml.omar.client.ui.thin.components.components.PaneSelectedEvent;
import org.freebxml.omar.client.ui.thin.WebUIResourceBundle;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Map;
import java.util.MissingResourceException;
import org.freebxml.omar.client.ui.thin.components.components.PaneSelectedPreRequestEvent;

/**
 * <B>TabLabelRenderer</B> is a class that renders a button control
 * on an individual pane of a tabbed pane control.  The button can
 * be rendered with a label or an image as its face.
 *
 * <B>Lifetime And Scope</B> <P>
 *
 * @version $Id: TabLabelRenderer.java,v 1.10 2006/09/05 17:32:36 nstojano Exp $
 * @see	Blah
 * @see	Bloo
 */

public class TabLabelRenderer extends BaseRenderer {

    private static final String INITIALIZE_TO_FIRST_PANEL = "-1";
    //
    // Protected Constants
    //

    //
    // Class Variables
    //

    //
    // Instance Variables
    //

    // Attribute Instance Variables


    // Relationship Instance Variables

    //
    // Constructors and Initializers    
    //

    public TabLabelRenderer() {
        super();
    }

    //
    // Class methods
    //

    //
    // General Methods
    //

    /**
     * Follow the UE Spec for Button:
     * http://javaweb.sfbay.sun.com/engineering/jsue/j2ee/WebServices/
     * JavaServerFaces/uispecs/UICommand_Button.html
     */
    protected String padLabel(String label) {
        if (label.length() == 3) {
            label = "&nbsp;&nbsp;" + label + "&nbsp;&nbsp;";
        } else if (label.length() == 2) {
            label = "&nbsp;&nbsp;&nbsp;" + label + "&nbsp;&nbsp;&nbsp;";
        }
        return label;
    }


    /**
     * @return the image src if this component is configured to display
     *         an image label, null otherwise.
     */

    protected String getImageSrc(FacesContext context,
                                 UIComponent component) {
        String result = (String) component.getAttributes().get("image");

        if (result != null) {
            if (!result.startsWith("/")) {
                result = "/" + result;
                component.getAttributes().put("image", result);
            }
        }

        if (result == null) {
            try {
                result = getKeyAndLookupInBundle(context, component,
                                                 "imageKey");
            } catch (MissingResourceException e) {
                // Do nothing since the absence of a resource is not an
                // error.
            }
        }
        if (result == null) {
            return result;
        }
        String contextPath = context.getExternalContext()
            .getRequestContextPath();
        StringBuffer sb = new StringBuffer();
        if (result.startsWith("/")) {
            sb.append(contextPath);
        }
        sb.append(result);
        return (context.getExternalContext().encodeResourceURL(sb.toString()));
    }


    protected String getLabel(FacesContext context,
                              UIComponent component) throws IOException {
        String result = null;

        try {
            result = getKeyAndLookupInBundle(context, component, "key");
        } catch (MissingResourceException e) {
            // Do nothing since the absence of a resource is not an
            // error.
        }
        if (null == result) {
            result = (String) component.getAttributes().get("label");
        }
        return result;
    }

    
    //
    // Methods From Renderer
    //

    public void decode(FacesContext context, UIComponent component) {
        if (context == null || component == null) {
            throw new NullPointerException(
                WebUIResourceBundle.getInstance().getString("excNullFacesContext"));
        }

        // Was our command the one that caused this submission?
        // we don' have to worry about getting the value from request parameter
        // because we just need to know if this command caused the submission. We
        // can get the command name by calling currentValue. This way we can 
        // get around the IE bug.
        String clientId = component.getClientId(context);
        Map requestParameterMap = (Map) context.getExternalContext().
            getRequestParameterMap();
        String value = (String) requestParameterMap.get(clientId);
        if (value == null) {
            if (requestParameterMap.get(clientId + ".x") == null &&
                requestParameterMap.get(clientId + ".y") == null) {
                return;
            }
        }

        // Search for this component's parent "tab" component..
        UIComponent tabComponent = findParentForRendererType(component, "Tab");
        
        // set the "tab" component's "id" in the event...
        tabComponent.queueEvent(new PaneSelectedPreRequestEvent(component,
                                                      tabComponent.getId()));
        tabComponent.queueEvent(new PaneSelectedEvent(component,
                                                      tabComponent.getId()));

        return;
    }


    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {
        if (context == null || component == null) {
            throw new NullPointerException(
                WebUIResourceBundle.getInstance().getString("excNullFacesContext"));
        }
        // suppress rendering if "rendered" property on the component is
        // false.
        if (!component.isRendered()) {
            return;
        }

        // Which button type (SUBMIT, RESET, or BUTTON) should we generate?
        String paneTabLabelClass = null;
        
        String onClick = null;

        ResponseWriter writer = context.getResponseWriter();

        String imageSrc = getImageSrc(context, component);
        String label = getLabel(context, component);
        String type = "submit";

        if (imageSrc != null || label != null) {
            writer.write("<input type=");
            if (null != imageSrc) {
                writer.write("\"image\" src=\"");
                writer.write(imageSrc);
                writer.write("\"");
                writer.write(" id=\"");
                writer.write(component.getClientId(context));
                writer.write("\"");                
                writer.write(" name=\"");
                writer.write(component.getClientId(context));
                writer.write("\"");
            } else {
                writer.write("\"");
                writer.write(type.toLowerCase());
                writer.write("\"");
                writer.write(" id=\"");
                writer.write(component.getClientId(context));
                writer.write("\"");                
                writer.write(" name=\"");
                writer.write(component.getClientId(context));
                writer.write("\"");
                writer.write(" value=\"");
                writer.write(padLabel(label));
                writer.write("\"");
            }
        }

        writer.write(Util.renderPassthruAttributes(context, component));
        writer.write(Util.renderBooleanPassthruAttributes(context, component));
        if (null != (paneTabLabelClass = (String)
            component.getAttributes().get("paneTabLabelClass"))) {
            writer.write(" class=\"" + paneTabLabelClass + "\" ");
        }
        
        if (null != (onClick = (String)
            component.getAttributes().get("onClick"))) {
            writer.write(" onClick=\"" + onClick + "\" ");
        }
        
        writer.write(">");
        
        // Search for this component's parent "tab" component..
        UIComponent tabComponent = findParentForRendererType(component, "Tab");
        
        // set the "tab" component's "id" in the event...
//        tabComponent.queueEvent(new PaneSelectedEvent(component, 
//                                                      INITIALIZE_TO_FIRST_PANEL));
    }


    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException {
        if (context == null || component == null) {
            throw new NullPointerException(
                WebUIResourceBundle.getInstance().getString("excNullFacesContext"));
        }
    }


    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {
        if (context == null || component == null) {
            throw new NullPointerException(
                WebUIResourceBundle.getInstance().getString("excNullFacesContext"));
        }
    }


    private UIComponent findParentForRendererType(UIComponent component, String rendererType) {
        Object facetParent = null;
        UIComponent currentComponent = component;
        
        // Search for an ancestor that is the specified renderer type; 
        while (null != (currentComponent = currentComponent.getParent())) {
            if (currentComponent.getRendererType().equals(rendererType)) {
                break;
            }
        }
        return currentComponent;
    }

} // end of class TabLabelRenderer
