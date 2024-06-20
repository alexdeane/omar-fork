/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/registration/UserManager.java,v 1.18 2006/08/24 20:41:58 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.registration;
import java.util.ArrayList;

import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.User;
import javax.xml.registry.infomodel.ExtrinsicObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.common.userModel.UserModel;

import org.freebxml.omar.client.ui.swing.JAXRClient;
import org.freebxml.omar.client.ui.swing.RegistryBrowser;
import org.freebxml.omar.client.ui.swing.JavaUIResourceBundle;
import org.freebxml.omar.client.xml.registry.ConnectionImpl;
import org.freebxml.omar.client.xml.registry.RegistryServiceImpl;
import org.freebxml.omar.client.xml.registry.infomodel.PersonNameImpl;
import org.freebxml.omar.client.xml.registry.util.CertificateUtil;
import org.freebxml.omar.client.xml.registry.util.UserRegistrationInfo;



/**
 * User registration tool.
 */
public class UserManager {
    
    /** Singleton instance */
    private static final UserManager instance = new UserManager();
    
    static JAXRClient client = null;
    static BusinessLifeCycleManager lcm = null;

    private static final Log log = LogFactory.getLog(UserManager.class);

    static {
        try {
            client = RegistryBrowser.getInstance().getClient();
            lcm = client.getBusinessLifeCycleManager();
        } catch (JAXRException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new UserManager object.
     */
    private UserManager() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static UserManager getInstance() {
        return instance;
    }

    /*
     * Register a new user
     *
     */
    public void registerNewUser() throws Exception {
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            
            //Make sure you are logged off when registering new user so new user is not owned by old user.
            ((ConnectionImpl)client.getConnection()).logoff();
            
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();

            UserModel userModel = new UserModel(lcm.createUser());
            UserRegistrationPanel userRegPanel = new UserRegistrationPanel(userModel);
            UserRegistrationDialog dialog = new UserRegistrationDialog(userRegPanel,
                    userModel);

            dialog.setVisible(true);

            //Make sure you are logged off after registering new user as Java UI does not show authenticated UI state after user reg.
            ((ConnectionImpl)client.getConnection()).logoff();
            
            if (dialog.getStatus() != UserRegistrationDialog.OK_STATUS) {
                return;
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    /** First check if certificate already exists in client keystore. If it does,
      * use it. If not then create a self signed certificate for the user and use it to
      * authenticate with the ebxmlrr server.
      * If the authentication is sucessful, save the user model to the server.
      *
      * @throw Exception
      *     An exception could indicate either a communications problem or an
      *     authentication error.
      */
    public static void authenticateAndSaveUser(UserModel userModel)
        throws Exception 
    {
        boolean generatedCert = false;
        UserRegistrationInfo userRegInfo = userModel.getUserRegistrationInfo();
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
            RegistryServiceImpl rs = (RegistryServiceImpl) lcm.getRegistryService();
            ConnectionImpl connection = (ConnectionImpl) rs.getConnection();

            if (!userRegInfo.isCAIssuedCert()) {
                if (!CertificateUtil.certificateExists(userRegInfo.getAlias(), userRegInfo.getStorePassword())) {
                    CertificateUtil.generateRegistryIssuedCertificate(userRegInfo);
                }
            } else {
                try {
                    CertificateUtil.importCAIssuedCert(userRegInfo);
                } catch (Exception e) {
                    throw new JAXRException(JavaUIResourceBundle.getInstance().getString("error.importCertificateFailed"), e);
                }
            }

            // Force re-authentication in case credentials are already set
            connection.authenticate();

            RegistryBrowser.setWaitCursor();

            // Now save the User
            ArrayList objects = new ArrayList();
            objects.add(userModel.getUser());
            client.saveObjects(objects, false, false);

            // saveObjects uses XML-Security which overwrites the log4j
            // configuration and we never get to see this:
            log.info(JavaUIResourceBundle.getInstance().getString("message.SavedUserOnServer", new Object[]{((PersonNameImpl)(userModel.getUser().getPersonName())).getFormattedName()}));
        } 
        catch (Exception e) {
            // Remove the self-signed certificate from the keystore, if one
            // was created during the self-registration process
            try {
                if (userRegInfo != null) {
                    String alias = userRegInfo.getAlias();

                    if ((alias != null) && (!userRegInfo.isCAIssuedCert())) {
                        CertificateUtil.removeCertificate(alias,
                            userRegInfo.getStorePassword());
                    }
                }
            } catch (Exception removeCertException) {
                log.warn(JavaUIResourceBundle.getInstance().getString("message.FailedToRemoveTheCertificateFromTheKeystoreGenerated"),
                    removeCertException);
            }

            throw e;
        } finally {
            RegistryBrowser.setDefaultCursor();
        }
    }

}
