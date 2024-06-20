/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.jaas;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Paul Sterk
 * @version 1.0
 *
 * This class is a CallbackHandler implementation that is suitable for a thi client server
 * logging into a server-side keystore on behalf of the user.
 * The handle(Callback[]) method has been customized such that it can only handle the login method
 * of LoginContext.
 */
public class ThinClientCallbackHandler implements CallbackHandler {
    /* The ResourceBundle for Sun's Auth package. */
    private static final ResourceBundle authResBundle = ResourceBundle.getBundle(
            "sun.security.util.AuthResources");
    private Frame ownerFrame;
    private static final Log log = LogFactory.getLog(ThinClientCallbackHandler.class);
    private boolean handleStorePass = true;
    
    /**
     * Default constructor
     */
    public ThinClientCallbackHandler() {
    }

    /** Implementation of the handle method specified by
     * <code> javax.security.auth.callback.CallbackHandler </code>
     * @param callbacks <code>Array of 
     * javax.security.auth.callback.CallbackHandler</code>
     *
     */
    public void handle(Callback[] callbacks)
        throws UnsupportedCallbackException {

        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof TextOutputCallback) {
                // Ignore this section for now. This will be used when a generic callback handler
                // is being implemented. In our current implementation, we are only expecting the
                //login type callback handler.
            } else if (callbacks[i] instanceof NameCallback) {
                // For now hard-code the alias of the the RegistryOperator account
                NameCallback nc = (NameCallback) callbacks[i];
                String alias = ProviderProperties.getInstance().
                    getProperty("jaxr-ebxml.security.alias");
                if (alias == null) {
                    String message = "Error: the jaxr-ebxml.security.alias "+
                        "property must be set";
                    log.error(message);
                    System.err.println(message);
                    alias = "";
                }
                nc.setName(alias);
            } else if (callbacks[i] instanceof PasswordCallback) {
                // For now hard-code the password of the the RegistryOperator account
                PasswordCallback pc = (PasswordCallback) callbacks[i];
                char[] password = null;
                if (handleStorePass) {
                    String storepass = ProviderProperties.getInstance().
                        getProperty("jaxr-ebxml.security.storepass");
                    if (storepass == null) {
                        storepass = "ebxmlrr";
                    }
                    password = storepass.toCharArray();
                    handleStorePass = false;
                } else {
                    String keypass = ProviderProperties.getInstance().
                        getProperty("jaxr-ebxml.security.keypass");
                    if (keypass == null) {
                        String message = "Error: the jaxr-ebxml.security.keypass "+
                            "property must be set";
                        log.error(message);
                        System.err.println(message);
                        keypass = "";
                    }
                    password = keypass.toCharArray();
                }
                pc.setPassword(password);
            } else if (callbacks[i] instanceof ConfirmationCallback) {
                ConfirmationCallback cc = (ConfirmationCallback) callbacks[i];
                cc.setSelectedIndex(ConfirmationCallback.OK);
            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                    JAXRResourceBundle.getInstance().getString("message.error.unrecognized.callback"));
            }
        }
    }
}
