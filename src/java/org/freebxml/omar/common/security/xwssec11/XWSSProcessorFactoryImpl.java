/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/security/xwssec11/XWSSProcessorFactoryImpl.java,v 1.1 2005/04/16 19:02:43 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security.xwssec11;

import java.io.InputStream;

import javax.security.auth.callback.CallbackHandler;

import com.sun.xml.wss.XWSSecurityException;

public  class XWSSProcessorFactoryImpl extends XWSSProcessorFactory {

    public XWSSProcessor createForSecurityConfiguration(
        InputStream securityConfiguration,
        CallbackHandler handler) throws XWSSecurityException {
        return new XWSSProcessorImpl(securityConfiguration, handler);
    }


    public XWSSProcessor createForApplicationSecurityConfiguration(
        InputStream securityConfiguration) throws XWSSecurityException {
        return new XWSSProcessorImpl(securityConfiguration);
    }
}
