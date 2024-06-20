/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/profile/ws/wsdl/CanonicalConstants.java,v 1.2 2006/04/13 02:29:30 psterk Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.profile.ws.wsdl;

/**
 * This interface should contains all Canonical Constants defined by this profile.
 *
 * @author Farrukh.Najmi@sun.com
 */
public interface CanonicalConstants extends org.freebxml.omar.common.CanonicalConstants {
    
    //Canonical Slot names
    public final static String CANONICAL_SLOT_WSDL_PROFILE_REFERENCED_NAMESPACES =
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:wsdl:referencedNamespaces";
    public final static String CANONICAL_SLOT_WSDL_PROFILE_TARGET_NAMESPACE =
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:wsdl:targetNamespace";
        
    //TODO: Add other TYPE_CODES?
    public static final String CANONICAL_OBJECT_TYPE_CODE_WSDL = "WSDL";
    public static final String CANONICAL_OBJECT_TYPE_ID_WSDL = 
        "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL";
    public static final String CANONICAL_OBJECT_TYPE_ID_WSDL_SERVICE = 
        "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:Service";
    public static final String CANONICAL_OBJECT_TYPE_ID_WSDL_PORT = 
        "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:Port";
    public static final String CANONICAL_OBJECT_TYPE_ID_WSDL_BINDING = 
        "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:Binding";
    public static final String CANONICAL_OBJECT_TYPE_ID_WSDL_PORT_TYPE = 
        "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:PortType";
    
    public static final String CANONICAL_PROTOCOL_TYPE_ID_SOAP = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:ProtocolType:SOAP";
    public static final String CANONICAL_PROTOCOL_TYPE_ID_AS2 = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:ProtocolType:AS2";
    public static final String CANONICAL_PROTOCOL_TYPE_ID_ATOM = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:ProtocolType:Atom";
    
    public static final String CANONICAL_TRANSPORT_TYPE_ID_HTTP = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:TransportType:HTTP";
    public static final String CANONICAL_TRANSPORT_TYPE_ID_MOM = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:TransportType:MOM";
    public static final String CANONICAL_TRANSPORT_TYPE_ID_BEEP = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:TransportType:BEEP";
    
    public static final String CANONICAL_SOAP_STYLE_TYPE_ID_RPC = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:SOAPStyleType:RPC";
    public static final String CANONICAL_SOAP_STYLE_TYPE_ID_DOCUMENT = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:SOAPStyleType:Document";
    
    public static final String CANONICAL_QUERY_WSDL_DISCOVERY = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:query:WSDLDiscoveryQuery";
    public static final String CANONICAL_QUERY_SERVICE_DISCOVERY = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:query:ServiceDiscoveryQuery";
    public static final String CANONICAL_QUERY_PORT_DISCOVERY = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:query:PortDiscoveryQuery";
    public static final String CANONICAL_QUERY_BINDING_DISCOVERY = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:query:BindingDiscoveryQuery";
    public static final String CANONICAL_QUERY_PORTTYPE_DISCOVERY = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:query:PortTypeDiscoveryQuery";
}
