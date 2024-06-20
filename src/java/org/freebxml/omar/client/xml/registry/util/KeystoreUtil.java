/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/util/KeystoreUtil.java,v 1.5 2005/12/18 09:47:29 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.util;

import java.io.File;

import javax.xml.registry.JAXRException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Provides methods to check that jaxr-ebxml keystore properties are set
 * correctly and keystore file exists.
 *
 * @author Fabian Ritzmann
 */
public class KeystoreUtil {
    private static final Log log = LogFactory.getLog(KeystoreUtil.class);
    /**
     * Return location of keystore file defined in jaxr-ebxml properties
     *
     * @return Path to keystore file
     * @throws JAXRException Thrown if properties are not set
     */
    public static File getKeystoreFile() throws JAXRException {
        String jaxrHomeFileName = ProviderProperties.getInstance().getProperty("jaxr-ebxml.home");

        if ((jaxrHomeFileName == null) || (jaxrHomeFileName.length() == 0)) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.no.property.jaxr-ebxml.home"));
        }

        File jaxrHomeFile = new File(jaxrHomeFileName);

        String keystoreFileName = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.keystore");

        if ((keystoreFileName == null) || (keystoreFileName.length() == 0)) {
            throw new JAXRException(
                JAXRResourceBundle.getInstance().getString("message.error.no.property.jaxr-ebxml.security.keystore"));
        }

        return new File(jaxrHomeFile.getAbsolutePath(), keystoreFileName);
    }

    /**
     * Returns if keystore file can be read, throws an exception otherwise
     *
     * @param keystoreFile Path to keystore file
     * @throws JAXRException Thrown if keystore file can not be read
     */
    public static void canReadKeystoreFile(File keystoreFile)
        throws JAXRException {
        try {
            if (!keystoreFile.exists()) {
                throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.keystore.file.not.exist", new Object[] {keystoreFile.getAbsolutePath()}));
            }

            if (!keystoreFile.canRead()) {
                throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.read.keysore.file", new Object[] {keystoreFile.getAbsolutePath()}));
            }
        } catch (SecurityException e) {
            log.error(e);
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.read.keysore.file", new Object[] {keystoreFile.getAbsolutePath()}));
        }
    }

    /**
     * Create keystore directory if it does not already exist
     *
     * @param keystoreFile Path to keystore file
     * @throws JAXRException Thrown if directory could not be created
     */
    public static void createKeystoreDirectory(File keystoreFile)
        throws JAXRException {
        File keystoreDir = keystoreFile.getParentFile();

        try {
            // Ignore return value of mkdirs, returns false if directories
            // already exist
            keystoreDir.mkdirs();
        } catch (SecurityException e) {
            log.error(e);
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.not.create.directory",new Object[] {keystoreDir.getAbsolutePath()}));
        }
    }
}
