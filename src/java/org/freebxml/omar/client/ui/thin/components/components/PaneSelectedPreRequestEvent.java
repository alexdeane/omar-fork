/*
 * PaneSelectedPreRequestEvent.java
 *
 * Created on June 20, 2005, 1:03 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.freebxml.omar.client.ui.thin.components.components;

import javax.faces.component.UIComponent;
import javax.faces.event.PhaseId;

/**
 *
 * @author psterk
 */
public class PaneSelectedPreRequestEvent extends PaneSelectedEvent {
    
    /** Creates a new instance of PaneSelectedPreRequestEvent */
    public PaneSelectedPreRequestEvent(UIComponent component, String id) {
        super(component, id);
    }
    
    public PhaseId getPhaseId() {
        return PhaseId.APPLY_REQUEST_VALUES;
    }
}
