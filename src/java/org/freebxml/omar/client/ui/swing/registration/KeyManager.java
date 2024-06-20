/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/registration/KeyManager.java,v 1.2 2006/04/10 10:59:06 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.registration;

import java.util.ArrayList;
import javax.xml.registry.JAXRException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.common.userModel.KeyModel;
import org.freebxml.omar.client.ui.swing.JavaUIResourceBundle;
import org.freebxml.omar.client.ui.swing.RegistryBrowser;
import org.freebxml.omar.client.xml.registry.util.CertificateUtil;
import org.freebxml.omar.client.xml.registry.util.UserRegistrationInfo;



/**
 * User key registration tool.
 */
public class KeyManager {

    /** Singleton */
    private static final KeyManager instance = new KeyManager();

    /** Create a static reference to the logging service. */
    private static Log log = LogFactory.getLog(KeyManager.class);

    /**
     * Creates a new KeyManager object.
     */
    private KeyManager() {
    }

    /**
     * Singleton accessor
     *
     * @return KeyManager singleton instance.
     */
    public static KeyManager getInstance() {
        return instance;
    }

    /**
     * Shows dialog to register a new key
     */
    public void registerNewKey() throws Exception {
        try {

            KeyModel keyModel = new KeyModel();
            keyModel.setCAIssuedCert(true);

            CertificateInfoPanel certificateInfoPanel = new CertificateInfoPanel(keyModel);
            certificateInfoPanel.setEnabledCertificateTypeButtons(false);

            KeyRegistrationDialog dialog = new KeyRegistrationDialog(certificateInfoPanel, keyModel);
            dialog.setVisible(true);

            if (dialog.getStatus() != KeyRegistrationDialog.OK_STATUS) {
                return;
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    /**
     * Save a key to client keystore. 
     *
     * @throw Exception relate to keystore manipulation
     */
    public static void saveKey(KeyModel keyModel) throws Exception {
        try {
            if (CertificateUtil.certificateExists(keyModel.getAlias(), keyModel.getStorePassword())) {
                throw new JAXRException(JavaUIResourceBundle.getInstance().getString("error.keyAliasAlreadyExists", new Object[] {keyModel.getAlias()}));
            } else {
                CertificateUtil.importCAIssuedCert(keyModel);
                RegistryBrowser.displayInfo(JavaUIResourceBundle.getInstance().getString("message.certificateImported"));
            }
        } finally {
            RegistryBrowser.setDefaultCursor();
        }
    }

}
