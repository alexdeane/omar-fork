/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/registration/KeyRegistrationDialog.java,v 1.2 2006/04/10 10:59:06 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.registration;

import org.freebxml.omar.client.common.userModel.KeyModel;
import org.freebxml.omar.client.ui.swing.JBDialog;
import org.freebxml.omar.client.ui.swing.RegistryBrowser;
import org.freebxml.omar.client.common.Model;

import java.awt.BorderLayout;

/**
 * A dialog for key registration.
 *
 * @author Diego Ballve / Digital Artefacts
 */
public class KeyRegistrationDialog extends JBDialog {
    
    /** The underlying model */
    private final Model model;

    /**
     * Creates a new KeyRegistrationDialog object.
     *
     * @param panel The main panel
     * @param m The underlying model
     */
    public KeyRegistrationDialog(CertificateInfoPanel panel, Model m) {
        super(RegistryBrowser.getInstance(), true);
        getMainPanel().add(panel, BorderLayout.CENTER);
        pack();
        setLocation(100, 20);
        this.model = m;
        setTitle(resourceBundle.getString("dialog.keyreg.title"));
        setEditable(true);
    }

    /** Action performed when the OK button is pressed. Validate the dialog's
     * contents and register the user's key with the client keystore.
     */
    protected void okAction() {
        try {
            this.model.validate();
            KeyManager.saveKey((KeyModel)model);
            status = OK_STATUS;
            dispose();
        } catch (Exception e) {
            RegistryBrowser.displayError(e);
        }
    }
}
