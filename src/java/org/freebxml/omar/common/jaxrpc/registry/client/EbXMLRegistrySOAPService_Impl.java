// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.2_01, build R40)
// Generated source version: 1.1.2

package org.freebxml.omar.common.jaxrpc.registry.client;

import com.sun.xml.rpc.encoding.*;
import com.sun.xml.rpc.client.ServiceExceptionImpl;
import com.sun.xml.rpc.util.exception.*;
import com.sun.xml.rpc.soap.SOAPVersion;
import com.sun.xml.rpc.client.HandlerChainImpl;
import javax.xml.rpc.*;
import javax.xml.rpc.encoding.*;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.namespace.QName;

public class EbXMLRegistrySOAPService_Impl extends com.sun.xml.rpc.client.BasicService implements EbXMLRegistrySOAPService {
    private static final QName serviceName = new QName("urn:your:urn:goes:here", "ebXMLRegistrySOAPService");
    private static final QName ns1_QueryManagerPort_QNAME = new QName("urn:your:urn:goes:here", "QueryManagerPort");
    private static final Class queryManagerPortType_PortClass = org.freebxml.omar.common.jaxrpc.registry.client.QueryManagerPortType.class;
    private static final QName ns1_LifeCycleManagerPort_QNAME = new QName("urn:your:urn:goes:here", "LifeCycleManagerPort");
    private static final Class lifeCycleManagerPortType_PortClass = org.freebxml.omar.common.jaxrpc.registry.client.LifeCycleManagerPortType.class;
    
    public EbXMLRegistrySOAPService_Impl() {
        super(serviceName, new QName[] {
                        ns1_QueryManagerPort_QNAME,
                        ns1_LifeCycleManagerPort_QNAME
                    },
            new org.freebxml.omar.common.jaxrpc.registry.client.EbXMLRegistrySOAPService_SerializerRegistry().getRegistry());
        
    }
    
    public java.rmi.Remote getPort(QName portName, Class serviceDefInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (portName.equals(ns1_QueryManagerPort_QNAME) &&
                serviceDefInterface.equals(queryManagerPortType_PortClass)) {
                return getQueryManagerPort();
            }
            if (portName.equals(ns1_LifeCycleManagerPort_QNAME) &&
                serviceDefInterface.equals(lifeCycleManagerPortType_PortClass)) {
                return getLifeCycleManagerPort();
            }
        } catch (Exception e) {
            throw new ServiceExceptionImpl(new LocalizableExceptionAdapter(e));
        }
        return super.getPort(portName, serviceDefInterface);
    }
    
    public java.rmi.Remote getPort(Class serviceDefInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (serviceDefInterface.equals(queryManagerPortType_PortClass)) {
                return getQueryManagerPort();
            }
            if (serviceDefInterface.equals(lifeCycleManagerPortType_PortClass)) {
                return getLifeCycleManagerPort();
            }
        } catch (Exception e) {
            throw new ServiceExceptionImpl(new LocalizableExceptionAdapter(e));
        }
        return super.getPort(serviceDefInterface);
    }
    
    public org.freebxml.omar.common.jaxrpc.registry.client.QueryManagerPortType getQueryManagerPort() {
        String[] roles = new String[] {};
        HandlerChainImpl handlerChain = new HandlerChainImpl(getHandlerRegistry().getHandlerChain(ns1_QueryManagerPort_QNAME));
        handlerChain.setRoles(roles);
        org.freebxml.omar.common.jaxrpc.registry.client.QueryManagerPortType_Stub stub = new org.freebxml.omar.common.jaxrpc.registry.client.QueryManagerPortType_Stub(handlerChain);
        try {
            stub._initialize(super.internalTypeRegistry);
        } catch (JAXRPCException e) {
            throw e;
        } catch (Exception e) {
            throw new JAXRPCException(e.getMessage(), e);
        }
        return stub;
    }
    public org.freebxml.omar.common.jaxrpc.registry.client.LifeCycleManagerPortType getLifeCycleManagerPort() {
        String[] roles = new String[] {};
        HandlerChainImpl handlerChain = new HandlerChainImpl(getHandlerRegistry().getHandlerChain(ns1_LifeCycleManagerPort_QNAME));
        handlerChain.setRoles(roles);
        org.freebxml.omar.common.jaxrpc.registry.client.LifeCycleManagerPortType_Stub stub = new org.freebxml.omar.common.jaxrpc.registry.client.LifeCycleManagerPortType_Stub(handlerChain);
        try {
            stub._initialize(super.internalTypeRegistry);
        } catch (JAXRPCException e) {
            throw e;
        } catch (Exception e) {
            throw new JAXRPCException(e.getMessage(), e);
        }
        return stub;
    }
}