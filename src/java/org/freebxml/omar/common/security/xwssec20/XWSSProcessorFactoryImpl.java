package org.freebxml.omar.common.security.xwssec20;

import java.io.InputStream;

import com.sun.xml.wss.XWSSecurityException;
import javax.security.auth.callback.CallbackHandler;


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
