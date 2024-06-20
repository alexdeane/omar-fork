/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/UserPanel.java,v 1.6 2004/07/30 17:41:36 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/UserPanel.java,v 1.6 2004/07/30 17:41:36 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.User;


/**
 * Panel for User
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class UserPanel extends PersonPanel {

    /**
     * Used for displaying Userobjects
     */
    public UserPanel() {
        super();
    }

    public User getUser() throws JAXRException {
        User user = (User)super.getPerson();

        return user;
    }

    public void setUser(User user) throws JAXRException {
        super.setModel(user);
    }
    
    protected String getPanelName() {
        return resourceBundle.getString("title.userDetails");
    }
    

}
