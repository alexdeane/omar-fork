/*
 * $Id: PaneSelectedListener.java,v 1.2 2006/08/02 17:45:10 psterk Exp $
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

import java.util.HashMap;
import java.util.Map;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.ui.thin.RegistryBrowser;
import org.freebxml.omar.client.ui.thin.RegistryObjectCollectionBean;
import org.freebxml.omar.client.ui.thin.RegistryObjectBean;
import org.freebxml.omar.client.ui.thin.WebUIResourceBundle;

/**
 * <p>Faces Listener implementation which sets the selected tab
 * component;</p>
 */
public class PaneSelectedListener implements FacesListener, StateHolder {
    
    private static Log log = LogFactory.getLog(PaneSelectedListener.class);

    private static final String INITIALIZE_TO_FIRST_PANEL = "-1";

    public PaneSelectedListener() {
    }

    // process the event..

    public void processPaneSelectedEvent(FacesEvent event) {
        UIComponent source = event.getComponent();
        UIComponent currentPane = null;// Find the parent tab control so we can set all tabs
        // to "unselected";
        UIComponent tabControl = findParentForRendererType(source,
                                                           "Tabbed");            
        boolean supportsROB = true;
        String supportsROBStr = (String)tabControl.getAttributes().get("supportsROB");
        if (supportsROBStr != null && supportsROBStr.equalsIgnoreCase("false")) {
            supportsROB = false;
        }
        RegistryObjectCollectionBean rocBean = null;
        RegistryObjectBean rob = null;
        if (supportsROB) {
            Map sessionMap = FacesContext.getCurrentInstance()
                                             .getExternalContext()
                                             .getSessionMap();
            rocBean = (RegistryObjectCollectionBean)sessionMap.get("roCollection");
            rob = rocBean.getCurrentRegistryObjectBean();
            if (event instanceof PaneSelectedPreRequestEvent) {
                handlePaneSelectedPreRequestEvent((PaneSelectedPreRequestEvent)event);
                return;
            }
        }
        PaneSelectedEvent pevent = (PaneSelectedEvent) event;
        String id = pevent.getId();

        boolean paneSelected = false;

        int n = tabControl.getChildCount();
        for (int i = 0; i < n; i++) {
            PaneComponent pane = (PaneComponent) tabControl.getChildren()
                .get(i);
            // If id == INITIALIZE_TO_FIRST_PANEL it means to render the 
            // first child tab.  This is needed when doing multi drilldowns
            // on different members of the result set.
            if (id == INITIALIZE_TO_FIRST_PANEL) {
                if (pane.isFirstTab().equalsIgnoreCase("true")) {
                    pane.setRendered(true);
                    paneSelected = true;
                    currentPane = pane;
                } else {
                    pane.setRendered(false);
                }
            } else {
                if (pane.getId().equals(id)) {
                    pane.setRendered(true);
                    paneSelected = true;
                    currentPane = pane;
                    if (supportsROB) {
                        rob.setCurrentDetailsPaneId(id);
                    }
                } else {
                    pane.setRendered(false);
                }
            }
        }

        if (!paneSelected) {
            log.warn(WebUIResourceBundle.getInstance().getString("message.CannotSelectPaneForId", new Object[]{id}));
            ((PaneComponent) tabControl.getChildren().get(0)).setRendered(
                true);
        }

        // set the selected RegistryObject as current RegistryObject
        if (currentPane != null && supportsROB) {
            // String idValue = (String)currentPane.getAttributes().get("registryObjectId")

            // This resets the state after any related object drilldowns. 
            rocBean.resetCurrentComposedRegistryObjectBean();

            // get the Collection of the RegistryObjects for the current type
            String relationshipName = (String)currentPane.getAttributes().get("relationshipName");
            if (relationshipName != null) {
                rocBean.setCurrentRelatedObjectsData(relationshipName);
            }
        } 

        if (currentPane != null && !supportsROB) {
            HashMap selectedTabs = RegistryBrowser.getInstance().getSelectedTabs();
            selectedTabs.put(tabControl.getId(), currentPane.getId());
        }  
    }

    private void handlePaneSelectedPreRequestEvent(PaneSelectedPreRequestEvent event) {
        String id = event.getId();

        boolean paneSelected = false;
        UIComponent source = event.getComponent();
        Map sessionMap = FacesContext.getCurrentInstance()
                                             .getExternalContext()
                                             .getSessionMap();
        RegistryObjectCollectionBean rocBean = 
            (RegistryObjectCollectionBean)sessionMap.get("roCollection");
        // Find the parent tab control so we can set all tabs
        // to "unselected";
        UIComponent tabControl = findParentForRendererType(source,
                                                           "Tabbed");
        int n = tabControl.getChildCount();
        for (int i = 0; i < n; i++) {
            PaneComponent pane = (PaneComponent) tabControl.getChildren()
                .get(i);
            // If id == INITIALIZE_TO_FIRST_PANEL it means to render the 
            // first child tab.  This is needed when doing multi drilldowns
            // on different members of the result set.
            if (id == INITIALIZE_TO_FIRST_PANEL) {
                if (pane.isFirstTab().equalsIgnoreCase("true")) {
                    rocBean.getCurrentRegistryObjectBean().setFormUpdateIgnored(false);
                }
            } else {
                if (pane.getId().equals(id)) {
                    if (pane.isFirstTab().equalsIgnoreCase("true")) {
                        rocBean.getCurrentRegistryObjectBean().setFormUpdateIgnored(false);
                    } else {
                        rocBean.getCurrentRegistryObjectBean().setFormUpdateIgnored(true);
                    }
                }
            }
        }
    }
    
    private UIComponent findParentForRendererType(UIComponent component, String rendererType) {
        Object facetParent = null;
        UIComponent currentComponent = component;
        
        // Search for an ancestor that is the specified renderer type;
        // search includes the facets.
        while (null != (currentComponent = currentComponent.getParent())) {
            if (currentComponent.getRendererType().equals(rendererType)) {
                break;
            }
        }
        return currentComponent;
    }    

    // methods from StateHolder
    Object testState = "test-state";

    public Object saveState(FacesContext context) {
        return testState;
    }

    public void restoreState(FacesContext context, Object state) {
        testState = state;
    }

    boolean transientValue;
    public void setTransient(boolean newTransientValue) {
        newTransientValue = newTransientValue;
    }

    public boolean isTransient() {
        return transientValue;
    }
}
