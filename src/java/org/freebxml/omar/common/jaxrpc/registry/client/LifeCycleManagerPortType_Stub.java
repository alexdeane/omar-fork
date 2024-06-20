// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.2_01, build R40)
// Generated source version: 1.1.2

package org.freebxml.omar.common.jaxrpc.registry.client;
import org.freebxml.omar.common.CommonResourceBundle;

import com.sun.xml.rpc.server.http.MessageContextProperties;
import com.sun.xml.rpc.streaming.*;
import com.sun.xml.rpc.encoding.*;
import com.sun.xml.rpc.encoding.soap.SOAPConstants;
import com.sun.xml.rpc.encoding.soap.SOAP12Constants;
import com.sun.xml.rpc.encoding.literal.*;
import com.sun.xml.rpc.soap.streaming.*;
import com.sun.xml.rpc.soap.message.*;
import com.sun.xml.rpc.soap.SOAPVersion;
import com.sun.xml.rpc.soap.SOAPEncodingConstants;
import com.sun.xml.rpc.wsdl.document.schema.SchemaConstants;
import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.lang.reflect.*;
import java.lang.Class;
import com.sun.xml.rpc.client.SenderException;
import com.sun.xml.rpc.client.*;
import com.sun.xml.rpc.client.http.*;
import javax.xml.rpc.handler.*;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.soap.SOAPFaultException;

public class LifeCycleManagerPortType_Stub
    extends com.sun.xml.rpc.client.StubBase
    implements org.freebxml.omar.common.jaxrpc.registry.client.LifeCycleManagerPortType {
    
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
    
    
    /*
     *  public constructor
     */
    public LifeCycleManagerPortType_Stub(HandlerChain handlerChain) {
        super(handlerChain);
        _setProperty(ENDPOINT_ADDRESS_PROPERTY, "http://your.server.com/soap");
    }
    
    
    /*
     *  implementation of approveObjects
     */
    public javax.xml.soap.SOAPElement approveObjects(javax.xml.soap.SOAPElement partApproveObjectsRequest)
        throws java.rmi.RemoteException {
        
        try {
            
            StreamingSenderState _state = _start(_handlerChain);
            
            InternalSOAPMessage _request = _state.getRequest();
            _request.setOperationCode(approveObjects_OPCODE);
            
            
            SOAPBlockInfo _bodyBlock = new SOAPBlockInfo(ns2_approveObjects_ApproveObjectsRequest_QNAME);
            _bodyBlock.setValue(partApproveObjectsRequest);
            _bodyBlock.setSerializer(ns1_ns1_AdhocQueryRequest_TYPE_QNAME_Serializer);
            _request.setBody(_bodyBlock);
            
            _state.getMessageContext().setProperty(HttpClientTransport.HTTP_SOAPACTION_PROPERTY, "urn:oasis:names:tc:ebxml-regrep:wsdl:registry:bindings:3.0:LifeCycleManagerPortType#approveObjects");
            
            _send((String) _getProperty(ENDPOINT_ADDRESS_PROPERTY), _state);
            
            javax.xml.soap.SOAPElement _result = null;
            Object _responseObj = _state.getResponse().getBody().getValue();
            if (_responseObj instanceof SOAPDeserializationState) {
                _result = (javax.xml.soap.SOAPElement)((SOAPDeserializationState) _responseObj).getInstance();
            } else {
                _result = (javax.xml.soap.SOAPElement)_responseObj;
            }
            
            return _result;
            
        } catch (RemoteException e) {
            // let this one through unchanged
            throw e;
        } catch (JAXRPCException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new RemoteException(e.getMessage(), e);
            }
        }
    }
    
    /*
     *  implementation of removeObjects
     */
    public javax.xml.soap.SOAPElement removeObjects(javax.xml.soap.SOAPElement partRemoveObjectsRequest)
        throws java.rmi.RemoteException {
        
        try {
            
            StreamingSenderState _state = _start(_handlerChain);
            
            InternalSOAPMessage _request = _state.getRequest();
            _request.setOperationCode(removeObjects_OPCODE);
            
            
            SOAPBlockInfo _bodyBlock = new SOAPBlockInfo(ns2_removeObjects_RemoveObjectsRequest_QNAME);
            _bodyBlock.setValue(partRemoveObjectsRequest);
            _bodyBlock.setSerializer(ns1_ns1_AdhocQueryRequest_TYPE_QNAME_Serializer);
            _request.setBody(_bodyBlock);
            
            _state.getMessageContext().setProperty(HttpClientTransport.HTTP_SOAPACTION_PROPERTY, "urn:oasis:names:tc:ebxml-regrep:wsdl:registry:bindings:3.0:LifeCycleManagerPortType#removeObjects");
            
            _send((String) _getProperty(ENDPOINT_ADDRESS_PROPERTY), _state);
            
            javax.xml.soap.SOAPElement _result = null;
            Object _responseObj = _state.getResponse().getBody().getValue();
            if (_responseObj instanceof SOAPDeserializationState) {
                _result = (javax.xml.soap.SOAPElement)((SOAPDeserializationState) _responseObj).getInstance();
            } else {
                _result = (javax.xml.soap.SOAPElement)_responseObj;
            }
            
            return _result;
            
        } catch (RemoteException e) {
            // let this one through unchanged
            throw e;
        } catch (JAXRPCException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new RemoteException(e.getMessage(), e);
            }
        }
    }
    
    /*
     *  implementation of updateObjects
     */
    public javax.xml.soap.SOAPElement updateObjects(javax.xml.soap.SOAPElement partUpdateObjectsRequest)
        throws java.rmi.RemoteException {
        
        try {
            
            StreamingSenderState _state = _start(_handlerChain);
            
            InternalSOAPMessage _request = _state.getRequest();
            _request.setOperationCode(updateObjects_OPCODE);
            
            
            SOAPBlockInfo _bodyBlock = new SOAPBlockInfo(ns2_updateObjects_UpdateObjectsRequest_QNAME);
            _bodyBlock.setValue(partUpdateObjectsRequest);
            _bodyBlock.setSerializer(ns2_ns2_UpdateObjectsRequest_TYPE_QNAME_Serializer);
            _request.setBody(_bodyBlock);
            
            _state.getMessageContext().setProperty(HttpClientTransport.HTTP_SOAPACTION_PROPERTY, "urn:oasis:names:tc:ebxml-regrep:wsdl:registry:bindings:3.0:LifeCycleManagerPortType#updateObjects");
            
            _send((String) _getProperty(ENDPOINT_ADDRESS_PROPERTY), _state);
            
            javax.xml.soap.SOAPElement _result = null;
            Object _responseObj = _state.getResponse().getBody().getValue();
            if (_responseObj instanceof SOAPDeserializationState) {
                _result = (javax.xml.soap.SOAPElement)((SOAPDeserializationState) _responseObj).getInstance();
            } else {
                _result = (javax.xml.soap.SOAPElement)_responseObj;
            }
            
            return _result;
            
        } catch (RemoteException e) {
            // let this one through unchanged
            throw e;
        } catch (JAXRPCException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new RemoteException(e.getMessage(), e);
            }
        }
    }
    
    /*
     *  implementation of submitObjects
     */
    public javax.xml.soap.SOAPElement submitObjects(javax.xml.soap.SOAPElement partSubmitObjectsRequest)
        throws java.rmi.RemoteException {
        
        try {
            
            StreamingSenderState _state = _start(_handlerChain);
            
            InternalSOAPMessage _request = _state.getRequest();
            _request.setOperationCode(submitObjects_OPCODE);
            
            
            SOAPBlockInfo _bodyBlock = new SOAPBlockInfo(ns2_submitObjects_SubmitObjectsRequest_QNAME);
            _bodyBlock.setValue(partSubmitObjectsRequest);
            _bodyBlock.setSerializer(ns2_ns2_SubmitObjectsRequest_TYPE_QNAME_Serializer);
            _request.setBody(_bodyBlock);
            
            _state.getMessageContext().setProperty(HttpClientTransport.HTTP_SOAPACTION_PROPERTY, "urn:oasis:names:tc:ebxml-regrep:wsdl:registry:bindings:3.0:LifeCycleManagerPortType#submitObjects");
            
            _send((String) _getProperty(ENDPOINT_ADDRESS_PROPERTY), _state);
            
            javax.xml.soap.SOAPElement _result = null;
            Object _responseObj = _state.getResponse().getBody().getValue();
            if (_responseObj instanceof SOAPDeserializationState) {
                _result = (javax.xml.soap.SOAPElement)((SOAPDeserializationState) _responseObj).getInstance();
            } else {
                _result = (javax.xml.soap.SOAPElement)_responseObj;
            }
            
            return _result;
            
        } catch (RemoteException e) {
            // let this one through unchanged
            throw e;
        } catch (JAXRPCException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new RemoteException(e.getMessage(), e);
            }
        }
    }
    
    /*
     *  implementation of undeprecateObjects
     */
    public javax.xml.soap.SOAPElement undeprecateObjects(javax.xml.soap.SOAPElement partUndeprecateObjectsRequest)
        throws java.rmi.RemoteException {
        
        try {
            
            StreamingSenderState _state = _start(_handlerChain);
            
            InternalSOAPMessage _request = _state.getRequest();
            _request.setOperationCode(undeprecateObjects_OPCODE);
            
            
            SOAPBlockInfo _bodyBlock = new SOAPBlockInfo(ns2_undeprecateObjects_UndeprecateObjectsRequest_QNAME);
            _bodyBlock.setValue(partUndeprecateObjectsRequest);
            _bodyBlock.setSerializer(ns1_ns1_AdhocQueryRequest_TYPE_QNAME_Serializer);
            _request.setBody(_bodyBlock);
            
            _state.getMessageContext().setProperty(HttpClientTransport.HTTP_SOAPACTION_PROPERTY, "urn:oasis:names:tc:ebxml-regrep:wsdl:registry:bindings:3.0:LifeCycleManagerPortType#undeprecateObjects");
            
            _send((String) _getProperty(ENDPOINT_ADDRESS_PROPERTY), _state);
            
            javax.xml.soap.SOAPElement _result = null;
            Object _responseObj = _state.getResponse().getBody().getValue();
            if (_responseObj instanceof SOAPDeserializationState) {
                _result = (javax.xml.soap.SOAPElement)((SOAPDeserializationState) _responseObj).getInstance();
            } else {
                _result = (javax.xml.soap.SOAPElement)_responseObj;
            }
            
            return _result;
            
        } catch (RemoteException e) {
            // let this one through unchanged
            throw e;
        } catch (JAXRPCException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new RemoteException(e.getMessage(), e);
            }
        }
    }
    
    /*
     *  implementation of deprecateObjects
     */
    public javax.xml.soap.SOAPElement deprecateObjects(javax.xml.soap.SOAPElement partDeprecateObjectsRequest)
        throws java.rmi.RemoteException {
        
        try {
            
            StreamingSenderState _state = _start(_handlerChain);
            
            InternalSOAPMessage _request = _state.getRequest();
            _request.setOperationCode(deprecateObjects_OPCODE);
            
            
            SOAPBlockInfo _bodyBlock = new SOAPBlockInfo(ns2_deprecateObjects_DeprecateObjectsRequest_QNAME);
            _bodyBlock.setValue(partDeprecateObjectsRequest);
            _bodyBlock.setSerializer(ns1_ns1_AdhocQueryRequest_TYPE_QNAME_Serializer);
            _request.setBody(_bodyBlock);
            
            _state.getMessageContext().setProperty(HttpClientTransport.HTTP_SOAPACTION_PROPERTY, "urn:oasis:names:tc:ebxml-regrep:wsdl:registry:bindings:3.0:LifeCycleManagerPortType#deprecateObjects");
            
            _send((String) _getProperty(ENDPOINT_ADDRESS_PROPERTY), _state);
            
            javax.xml.soap.SOAPElement _result = null;
            Object _responseObj = _state.getResponse().getBody().getValue();
            if (_responseObj instanceof SOAPDeserializationState) {
                _result = (javax.xml.soap.SOAPElement)((SOAPDeserializationState) _responseObj).getInstance();
            } else {
                _result = (javax.xml.soap.SOAPElement)_responseObj;
            }
            
            return _result;
            
        } catch (RemoteException e) {
            // let this one through unchanged
            throw e;
        } catch (JAXRPCException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new RemoteException(e.getMessage(), e);
            }
        }
    }
    
    
    /*
     *  this method deserializes the request/response structure in the body
     */
    protected void _readFirstBodyElement(XMLReader bodyReader, SOAPDeserializationContext deserializationContext, StreamingSenderState  state) throws Exception {
        int opcode = state.getRequest().getOperationCode();
        switch (opcode) {
            case approveObjects_OPCODE:
                _deserialize_approveObjects(bodyReader, deserializationContext, state);
                break;
            case removeObjects_OPCODE:
                _deserialize_removeObjects(bodyReader, deserializationContext, state);
                break;
            case updateObjects_OPCODE:
                _deserialize_updateObjects(bodyReader, deserializationContext, state);
                break;
            case submitObjects_OPCODE:
                _deserialize_submitObjects(bodyReader, deserializationContext, state);
                break;
            case undeprecateObjects_OPCODE:
                _deserialize_undeprecateObjects(bodyReader, deserializationContext, state);
                break;
            case deprecateObjects_OPCODE:
                _deserialize_deprecateObjects(bodyReader, deserializationContext, state);
                break;
            default:
                throw new SenderException("sender.response.unrecognizedOperation", Integer.toString(opcode));
        }
    }
    
    
    
    /*
     * This method deserializes the body of the approveObjects operation.
     */
    private void _deserialize_approveObjects(XMLReader bodyReader, SOAPDeserializationContext deserializationContext, StreamingSenderState state) throws Exception {
        Object mySOAPElementObj =
            ns3_ns3_RegistryResponseType_TYPE_QNAME_Serializer.deserialize(ns3_approveObjects_RegistryResponse_QNAME,
                bodyReader, deserializationContext);
        
        SOAPBlockInfo bodyBlock = new SOAPBlockInfo(ns3_approveObjects_RegistryResponse_QNAME);
        bodyBlock.setValue(mySOAPElementObj);
        state.getResponse().setBody(bodyBlock);
    }
    
    /*
     * This method deserializes the body of the removeObjects operation.
     */
    private void _deserialize_removeObjects(XMLReader bodyReader, SOAPDeserializationContext deserializationContext, StreamingSenderState state) throws Exception {
        Object mySOAPElementObj =
            ns3_ns3_RegistryResponseType_TYPE_QNAME_Serializer.deserialize(ns3_removeObjects_RegistryResponse_QNAME,
                bodyReader, deserializationContext);
        
        SOAPBlockInfo bodyBlock = new SOAPBlockInfo(ns3_removeObjects_RegistryResponse_QNAME);
        bodyBlock.setValue(mySOAPElementObj);
        state.getResponse().setBody(bodyBlock);
    }
    
    /*
     * This method deserializes the body of the updateObjects operation.
     */
    private void _deserialize_updateObjects(XMLReader bodyReader, SOAPDeserializationContext deserializationContext, StreamingSenderState state) throws Exception {
        Object mySOAPElementObj =
            ns2_ns2_UpdateObjectsRequest_TYPE_QNAME_Serializer.deserialize(ns2_updateObjects_UpdateObjectsRequest_QNAME,
                bodyReader, deserializationContext);
        
        SOAPBlockInfo bodyBlock = new SOAPBlockInfo(ns2_updateObjects_UpdateObjectsRequest_QNAME);
        bodyBlock.setValue(mySOAPElementObj);
        state.getResponse().setBody(bodyBlock);
    }
    
    /*
     * This method deserializes the body of the submitObjects operation.
     */
    private void _deserialize_submitObjects(XMLReader bodyReader, SOAPDeserializationContext deserializationContext, StreamingSenderState state) throws Exception {
        Object mySOAPElementObj =
            ns3_ns3_RegistryResponseType_TYPE_QNAME_Serializer.deserialize(ns3_submitObjects_RegistryResponse_QNAME,
                bodyReader, deserializationContext);
        
        SOAPBlockInfo bodyBlock = new SOAPBlockInfo(ns3_submitObjects_RegistryResponse_QNAME);
        bodyBlock.setValue(mySOAPElementObj);
        state.getResponse().setBody(bodyBlock);
    }
    
    /*
     * This method deserializes the body of the undeprecateObjects operation.
     */
    private void _deserialize_undeprecateObjects(XMLReader bodyReader, SOAPDeserializationContext deserializationContext, StreamingSenderState state) throws Exception {
        Object mySOAPElementObj =
            ns3_ns3_RegistryResponseType_TYPE_QNAME_Serializer.deserialize(ns3_undeprecateObjects_RegistryResponse_QNAME,
                bodyReader, deserializationContext);
        
        SOAPBlockInfo bodyBlock = new SOAPBlockInfo(ns3_undeprecateObjects_RegistryResponse_QNAME);
        bodyBlock.setValue(mySOAPElementObj);
        state.getResponse().setBody(bodyBlock);
    }
    
    /*
     * This method deserializes the body of the deprecateObjects operation.
     */
    private void _deserialize_deprecateObjects(XMLReader bodyReader, SOAPDeserializationContext deserializationContext, StreamingSenderState state) throws Exception {
        Object mySOAPElementObj =
            ns3_ns3_RegistryResponseType_TYPE_QNAME_Serializer.deserialize(ns3_deprecateObjects_RegistryResponse_QNAME,
                bodyReader, deserializationContext);
        
        SOAPBlockInfo bodyBlock = new SOAPBlockInfo(ns3_deprecateObjects_RegistryResponse_QNAME);
        bodyBlock.setValue(mySOAPElementObj);
        state.getResponse().setBody(bodyBlock);
    }
    
    
    
    protected String _getDefaultEnvelopeEncodingStyle() {
        return null;
    }
    
    public String _getImplicitEnvelopeEncodingStyle() {
        return "";
    }
    
    public String _getEncodingStyle() {
        return SOAPNamespaceConstants.ENCODING;
    }
    
    public void _setEncodingStyle(String encodingStyle) {
        throw new UnsupportedOperationException(resourceBundle.getString("message.encodingStyle"));
    }
    
    
    
    
    
    /*
     * This method returns an array containing (prefix, nsURI) pairs.
     */
    protected String[] _getNamespaceDeclarations() {
        return myNamespace_declarations;
    }
    
    /*
     * This method returns an array containing the names of the headers we understand.
     */
    public QName[] _getUnderstoodHeaders() {
        return understoodHeaderNames;
    }
    
    
    protected void _preHandlingHook(StreamingSenderState state) throws Exception {
        super._preHandlingHook(state);
    }
    
    
    protected boolean _preRequestSendingHook(StreamingSenderState state) throws Exception {
        boolean bool = false;
        bool = super._preRequestSendingHook(state);
        return bool;
    }
    
    protected void _preSendingHook(StreamingSenderState state) throws Exception {
        super._preSendingHook(state);
        switch (state.getRequest().getOperationCode()) {
            case updateObjects_OPCODE:
                addNonExplicitAttachment(state);
                break;
            case submitObjects_OPCODE:
                addNonExplicitAttachment(state);
                break;
        }
    }
    
    
    private void addNonExplicitAttachment(StreamingSenderState state) throws Exception {
        javax.xml.rpc.handler.soap.SOAPMessageContext smc = state.getMessageContext();
        javax.xml.soap.SOAPMessage message = state.getRequest().getMessage();
        Object c = _getProperty(StubPropertyConstants.SET_ATTACHMENT_PROPERTY);
        _setProperty(StubPropertyConstants.SET_ATTACHMENT_PROPERTY, null);
        if(c != null && c instanceof java.util.Collection) {
            for(java.util.Iterator iter = ((java.util.Collection)c).iterator(); iter.hasNext();) {
                Object attachment = iter.next();
                if(attachment instanceof javax.xml.soap.AttachmentPart) {
                    message.addAttachmentPart((javax.xml.soap.AttachmentPart)attachment);
                }
            }
        }
    }
    
    public void _initialize(InternalTypeMappingRegistry registry) throws Exception {
        super._initialize(registry);
        ns2_ns2_UpdateObjectsRequest_TYPE_QNAME_Serializer = (CombinedSerializer)registry.getSerializer("", javax.xml.soap.SOAPElement.class, ns2_UpdateObjectsRequest_TYPE_QNAME);
        ns1_ns1_AdhocQueryRequest_TYPE_QNAME_Serializer = (CombinedSerializer)registry.getSerializer("", javax.xml.soap.SOAPElement.class, ns1_AdhocQueryRequest_TYPE_QNAME);
        ns2_ns2_SubmitObjectsRequest_TYPE_QNAME_Serializer = (CombinedSerializer)registry.getSerializer("", javax.xml.soap.SOAPElement.class, ns2_SubmitObjectsRequest_TYPE_QNAME);
        ns3_ns3_RegistryResponseType_TYPE_QNAME_Serializer = (CombinedSerializer)registry.getSerializer("", javax.xml.soap.SOAPElement.class, ns3_RegistryResponseType_TYPE_QNAME);
    }
    
    private static final QName _portName = new QName("urn:your:urn:goes:here", "LifeCycleManagerPort");
    private static final int approveObjects_OPCODE = 0;
    private static final int removeObjects_OPCODE = 1;
    private static final int updateObjects_OPCODE = 2;
    private static final int submitObjects_OPCODE = 3;
    private static final int undeprecateObjects_OPCODE = 4;
    private static final int deprecateObjects_OPCODE = 5;
    private static final QName ns2_approveObjects_ApproveObjectsRequest_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", "ApproveObjectsRequest");
    private static final QName ns1_AdhocQueryRequest_TYPE_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "AdhocQueryRequest");
    private CombinedSerializer ns1_ns1_AdhocQueryRequest_TYPE_QNAME_Serializer;
    private static final QName ns3_approveObjects_RegistryResponse_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", "RegistryResponse");
    private static final QName ns3_RegistryResponseType_TYPE_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", "RegistryResponseType");
    private CombinedSerializer ns3_ns3_RegistryResponseType_TYPE_QNAME_Serializer;
    private static final QName ns2_removeObjects_RemoveObjectsRequest_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", "RemoveObjectsRequest");
    private static final QName ns3_removeObjects_RegistryResponse_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", "RegistryResponse");
    private static final QName ns2_updateObjects_UpdateObjectsRequest_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", "UpdateObjectsRequest");
    private static final QName ns2_UpdateObjectsRequest_TYPE_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", "UpdateObjectsRequest");
    private CombinedSerializer ns2_ns2_UpdateObjectsRequest_TYPE_QNAME_Serializer;
    private static final QName ns2_submitObjects_SubmitObjectsRequest_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", "SubmitObjectsRequest");
    private static final QName ns2_SubmitObjectsRequest_TYPE_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", "SubmitObjectsRequest");
    private CombinedSerializer ns2_ns2_SubmitObjectsRequest_TYPE_QNAME_Serializer;
    private static final QName ns3_submitObjects_RegistryResponse_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", "RegistryResponse");
    private static final QName ns2_undeprecateObjects_UndeprecateObjectsRequest_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", "UndeprecateObjectsRequest");
    private static final QName ns3_undeprecateObjects_RegistryResponse_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", "RegistryResponse");
    private static final QName ns2_deprecateObjects_DeprecateObjectsRequest_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", "DeprecateObjectsRequest");
    private static final QName ns3_deprecateObjects_RegistryResponse_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", "RegistryResponse");
    private static final String[] myNamespace_declarations =
                                        new String[] {
                                            "ns0", "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0"
                                        };
    
    private static final QName[] understoodHeaderNames = new QName[] {  };
}