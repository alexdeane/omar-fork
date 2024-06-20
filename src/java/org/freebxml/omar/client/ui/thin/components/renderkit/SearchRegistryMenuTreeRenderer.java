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

package org.freebxml.omar.client.ui.thin.components.renderkit;


import org.freebxml.omar.client.ui.thin.components.components.GraphComponent;
import org.freebxml.omar.client.ui.thin.components.model.Graph;
import org.freebxml.omar.client.ui.thin.components.model.Node;
import org.freebxml.omar.client.ui.thin.components.model.RegistryObjectNode;
import org.freebxml.omar.client.xml.registry.infomodel.ClassificationSchemeImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ConceptImpl;

import org.freebxml.omar.client.ui.thin.WebUIResourceBundle;

import javax.xml.registry.JAXRException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.faces.application.FacesMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Render our current value (which must be a <code>Graph</code>)
 * as a tree control, with individual nodes expanded or contracted based on
 * their current status.</p>
 */

public class SearchRegistryMenuTreeRenderer extends SearchRegistryMenuBarRenderer {

    /**
     * The names of tree state images that we need.
     */
    public static final String IMAGE_HANDLE_DOWN_LAST = "handledownlast.gif";
    public static final String IMAGE_HANDLE_DOWN_MIDDLE = "handledownmiddle.gif";
    public static final String IMAGE_HANDLE_RIGHT_LAST = "handlerightlast.gif";
    public static final String IMAGE_HANDLE_RIGHT_MIDDLE = "handlerightmiddle.gif";
    public static final String IMAGE_LINE_LAST = "linelastnode.gif";
    public static final String IMAGE_LINE_MIDDLE = "linemiddlenode.gif";
    public static final String IMAGE_LINE_VERTICAL = "linevertical.gif";
    public static final String IMAGE_DOCUMENT = "document.gif";
    public static final String IMAGE_FOLDER = "folder.gif";
 
    String imageLocation = null;

    private static Log log = LogFactory.getLog(SearchRegistryMenuTreeRenderer.class);

    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {
        try {
            Graph graph = null;
            // Acquire the root node of the graph representing the menu
            graph = (Graph) ((GraphComponent) component).getValue();
            if (graph == null) {
                throw new FacesException(WebUIResourceBundle.getInstance().getString("excGraphNotLocated"));
            }
            Node root = graph.getRoot();
            if (root == null) {
                throw new FacesException(WebUIResourceBundle.getInstance().getString("excGraphNoRootNode"));
            }
            if (root.hasChild() == false) {
                return; // Nothing to render
            }
            this.component = component;
            this.context = context;
            clientId = component.getClientId(context);
            imageLocation = getImagesLocation(context);

            treeClass = (String) component.getAttributes().get("graphClass");
            selectedClass = (String) component.getAttributes().get("selectedClass");
            unselectedClass =
                (String) component.getAttributes().get("unselectedClass");
            treeSelect = (String)component.getAttributes().get("treeSelect");

            showNoConcept = new Boolean(((String) component.getAttributes().get("showNoConcept"))).booleanValue();
            ResponseWriter writer = context.getResponseWriter();

            writer.write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"");
            if (treeClass != null) {
                writer.write(" class=\"");
                writer.write(treeClass);
                writer.write("\"");
            }
            writer.write(">");
            writer.write("\n");

            int level = 0;
            encodeNode(writer, root, level, graph.getSelectedNodeDepth(), true);
            writer.write("<input type=\"hidden\" name=\"" + clientId + "\" />");
            writer.write("</table>");
            writer.write("\n");
        } catch (Throwable t) {
            String message = WebUIResourceBundle.getInstance().getString("searchPanelNotInitialized");
            log.error(message, t);
            message += " " + WebUIResourceBundle.getInstance().getString("registrySupport");
            FacesContext.getCurrentInstance()
                        .addMessage(null, 
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                     message, 
                                                     null));
        }
    }


    protected void encodeNode(ResponseWriter writer, Node node,
                              int level, int width, boolean last)
        throws IOException {

        boolean showLink = false;
        if (node instanceof RegistryObjectNode) {
            if (((RegistryObjectNode)node).getRegistryObject() instanceof ClassificationSchemeImpl && "CS".equals(treeSelect) ||
                ((RegistryObjectNode)node).getRegistryObject() instanceof ConceptImpl && "CN".equals(treeSelect) || "CSANDCN".equals(treeSelect)) {
                showLink = true;
            }
        }
        // Render the beginning of this node
        writer.write("  <tr valign=\"middle\">");
      
        // Create the appropriate number of indents
        for (int i = 0; i < level; i++) {
            int levels = level - i;
            Node parent = node;
            for (int j = 1; j <= levels; j++)
                parent = parent.getParent();
            if (parent.isLast())
                writer.write("    <td></td>");
            else {
                writer.write("    <td><img src=\"");
                writer.write(imageLocation);
                writer.write("/");
                writer.write(IMAGE_LINE_VERTICAL);
                writer.write("\"");
                writer.write("alt=\"");
                writer.write("\" ");
                writer.write("border=\"0\"></td>");
            }
            writer.write("\n");
        }

        // Render the tree state image for this node. use the "onmousedown" event 
        // handler to track which node was clicked. The images are rendered
        // as links.
        writer.write("    <td>");
        if (node.hasChild()) {
            // The image links of the nodes that have children behave like
            // command buttons causing the form to be submitted so the state of 
            // node can be toggled
            writer.write("<a href=\"");
            try {
                writer.write(getSubmitScript(node.getPath(), context, true));
            } catch (Exception ex) {      
                String message = WebUIResourceBundle.getInstance()
                                                    .getString("message.errorGettingNodePath");
                FacesContext.getCurrentInstance()
                            .addMessage(null, 
                                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                         message, 
                                                         null));
            }
            writer.write(" >");
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            if (node.isExpanded()) {
                writer.write(IMAGE_HANDLE_DOWN_LAST);
            } else {
                writer.write(IMAGE_HANDLE_RIGHT_LAST);
            }
            writer.write("\" border=\"0\">");
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            if (node.isExpanded() || node.hasChild()) {
                writer.write(IMAGE_FOLDER);
            } 
            writer.write("\" border=\"0\">");
            writer.write("</a>");
            writer.write("&nbsp;</td>");
        } else {
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            if (node.isLast()) {
                writer.write(IMAGE_LINE_LAST);
            } else {
                writer.write(IMAGE_LINE_MIDDLE);
            }
            writer.write("\" border=\"0\">");
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            if (node.isLast() || node.isLeaf()) {
                writer.write(IMAGE_DOCUMENT);
            } else {
                writer.write(IMAGE_FOLDER);
            }	    
            writer.write("\" border=\"0\">");
            writer.write("&nbsp;</td>");
        }
        // Render the icon for this node (if any)
        writer.write("    <td colspan=\"");
//        writer.write(String.valueOf(width - level + 1));
        writer.write(String.valueOf(width + 1));
        writer.write("\">");
        if (node.getIcon() != null) {
            // Label and action link
            // Note: we assume that the links do not act as command button,
            // meaning they do not cause the form to be submitted.
            if (node.getAction() != null) {
                writer.write("<a href=\"");
                writer.write(href(node.getAction()));
                writer.write("\">");
            }
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            writer.write(node.getIcon());
            writer.write("\" ");
            
            writer.write("alt=\"");
            writer.write("\" ");
            
            writer.write(" border=\"0\">");
            if (node.getAction() != null) {
                writer.write("</a>");
            }
        }
        // Render the label for this node (if any) as link.
        if (node.getLabel() != null) {
            writer.write("   ");
            String labelStyle = null;
            if (node.isSelected() && (selectedClass != null)) {
                labelStyle = selectedClass;
            } else if (!node.isSelected() && (unselectedClass != null)) {
                labelStyle = unselectedClass;
            }
            if (node.isEnabled() && showLink) {
                // Note: we assume that the links do not act as command button,
                // meaning they do not cause the form to be submitted.
                // writer.write("<a href=\"");
                // writer.write(href(node.getAction()));
                // writer.write("\"");
                writer.write("<a href=\"");
                try {
                    writer.write(getSubmitScript(node.getPath(), context, false));
                } catch (Exception ex) {      
                    String message = WebUIResourceBundle.getInstance()
                                                        .getString("message.errorGettingNodePath");
                    FacesContext.getCurrentInstance()
                                .addMessage(null, 
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                             message, 
                                                             null));
                }
                if (labelStyle != null) {
                    writer.write(" class=\"");
                    writer.write(labelStyle);
                    writer.write("\"");
                }
                writer.write(">");
            } else if (labelStyle != null) {
                writer.write("<span class=\"");
                writer.write(labelStyle);
                writer.write("\">");
            }
            writer.write(node.getLabel());
            if (node.getLabel() != null && showLink) {
                writer.write("</a>");
            } else if (labelStyle != null) {
                writer.write("</span>");
            }
            if (node.isAnchored()) {
                writer.write("<a name=\"here\"></a>");
                node.setAnchor(false);
            }
        }
        writer.write("</td>");
        writer.write("\n");

        // Render the end of this node
        writer.write("  </tr>");
        writer.write("\n");

        // Render the children of this node
        if (node.isExpanded()) {
            Iterator children = node.getChildren();
            int lastIndex = (node.getChildCount()) - 1;
            int newLevel = level + 1;
            while (children.hasNext()) {
                Node nextChild = (Node) children.next();
                boolean lastNode = nextChild.isLast();
                if (showNoConcept && newLevel > 1){
                    break;
                } else {
                    encodeNode(writer, nextChild, newLevel, width, lastNode);
                }
            }
        }
    }


    /**
     * Returns the location of images by looking up the servlet context
     * init parameter. If parameter is not found, default to "/images".
     * Image location can be configured by setting this property.
     */
    protected String getImagesLocation(FacesContext context) {
        StringBuffer sb = new StringBuffer();

        // First, add the context path
        String contextPath = context.getExternalContext()
            .getRequestContextPath();
        sb.append(contextPath);

        // Next, add the images directory path
        Map initParameterMap = context.getExternalContext()
            .getInitParameterMap();
        String images = (String) initParameterMap.get("tree.control.images");
        if (images == null) {
            images = "/images/tree";
        }
        sb.append(images);
        return (sb.toString());
    }


}
