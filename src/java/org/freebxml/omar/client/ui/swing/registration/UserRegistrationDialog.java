/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/registration/UserRegistrationDialog.java,v 1.7 2006/04/10 10:59:06 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.registration;

import org.freebxml.omar.client.common.userModel.UserModel;
import org.freebxml.omar.client.ui.swing.JBDialog;
import org.freebxml.omar.client.ui.swing.RegistryBrowser;
import org.freebxml.omar.client.common.Model;

import java.awt.BorderLayout;


/**
 * DOCUMENT ME!
 *
 * @author Fabian Ritzmann
 */
public class UserRegistrationDialog extends JBDialog {

    /** DOCUMENT ME! */
    private final Model model;

    /**
     * Creates a new UserRegistrationDialog object.
     *
     * @param panel DOCUMENT ME!
     * @param m DOCUMENT ME!
     */
    public UserRegistrationDialog(UserRegistrationPanel panel, Model m) {
        super(RegistryBrowser.getInstance(), true);
        getMainPanel().add(panel, BorderLayout.CENTER);
        pack();
        setLocation(100, 20);
        this.model = m;
        setTitle(resourceBundle.getString("dialog.userreg.title"));
        setEditable(true);
    }

    /** Action performed when the OK button is pressed. Validate the
      * dialog's contents and register the user with the server.
      */
    protected void okAction() {
        try {
            this.model.validate();
            UserManager.authenticateAndSaveUser((UserModel) model);
            status = OK_STATUS;
            dispose();
        } catch (Exception e) {
            RegistryBrowser.displayError(e);
        }
    }
}
