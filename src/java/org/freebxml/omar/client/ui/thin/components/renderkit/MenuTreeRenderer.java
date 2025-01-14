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

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.freebxml.omar.client.ui.thin.WebUIResourceBundle;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>Render our current value (which must be a <code>Graph</code>)
 * as a tree control, with individual nodes expanded or contracted based on
 * their current status.</p>
 */

public class MenuTreeRenderer extends MenuBarRenderer {

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

    String imageLocation = null;


    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {
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
        encodeNode(writer, root, level, root.getDepth(), true);
        writer.write("<input type=\"hidden\" name=\"" + clientId + "\" />");
        writer.write("</table>");
        writer.write("\n");
    }


    protected void encodeNode(ResponseWriter writer, Node node,
                              int level, int width, boolean last)
        throws IOException {

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
                writer.write("\" border=\"0\"></td>");
            }
            writer.write("\n");
        }

        // Render the tree state image for this node. use the "onmousedown" event 
        // handler to track which node was clicked. The images are rendered
        // as links.
        writer.write("    <td>");
        if (!node.isLeaf()) {
            // The image links of the nodes that have children behave like
            // command buttons causing the form to be submitted so the state of 
            // node can be toggled
            writer.write("<a href=\"");
            Throwable throwable = null;
            try {
                writer.write(getSubmitScript(node.getPath(), context));
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
            if (node.isLast()) {
                writer.write(IMAGE_HANDLE_RIGHT_LAST);
            } else {
                writer.write(IMAGE_HANDLE_RIGHT_MIDDLE);
            }
            writer.write("\" border=\"0\">");
            writer.write("</a>");
            if (throwable != null) {
                writer.write(throwable.getMessage());
            }
            writer.write("</td>");
        } else {
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            if (node.isLeaf()) {
                if (node.isLast()) {
                    writer.write(IMAGE_LINE_LAST);
                } else {
                    writer.write(IMAGE_LINE_MIDDLE);
                }
            } else if (node.isExpanded()) {
                if (node.isLast()) {
                    writer.write(IMAGE_HANDLE_DOWN_LAST);
                } else {
                    writer.write(IMAGE_HANDLE_DOWN_MIDDLE);
                }
            }
            writer.write("\" border=\"0\">");
            writer.write("</td>");
        }

        // Render the icon for this node (if any)
        writer.write("    <td colspan=\"");
        writer.write(String.valueOf(width - level + 1));
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
            writer.write("\" border=\"0\">");
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
            if (node.isEnabled()) {
                // Note: we assume that the links do not act as command button,
                // meaning they do not cause the form to be submitted.
                writer.write("<a href=\"");
                writer.write(href(node.getAction()));
                writer.write("\"");
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
            if (node.getLabel() != null) {
                writer.write("</a>");
            } else if (labelStyle != null) {
                writer.write("</span>");
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
                encodeNode(writer, nextChild, newLevel, width, lastNode);
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
            images = "/images";
        }
        sb.append(images);
        return (sb.toString());
    }


}
