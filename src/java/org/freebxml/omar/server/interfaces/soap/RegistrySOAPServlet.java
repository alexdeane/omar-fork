/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/interfaces/soap/RegistrySOAPServlet.java,v 1.28 2007/02/17 18:53:21 yamanu Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.interfaces.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.mail.internet.ParseException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.freebxml.omar.common.CommonProperties;
import org.w3c.dom.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CredentialInfo;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.RepositoryItemImpl;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.common.security.SoapSecurityUtil;
import org.freebxml.omar.server.cache.ServerCache;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.Utility;
import org.freebxml.omar.server.interfaces.Request;
import org.freebxml.omar.server.interfaces.Response;
import org.freebxml.omar.server.interfaces.common.SOAPServlet;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


/**
 * The SOAPServlet for the OASIS ebXML registry. It receives SOAP messages.
 *
 * @see org.freebxml.omar.server.interfaces.soap.SOAPSender class under test tree to send SOAP messages to this servlet.
 * @author Farrukh S. Najmi
 */
public class RegistrySOAPServlet extends SOAPServlet {
    private static final Log log = LogFactory.getLog(RegistrySOAPServlet.class);
    private org.freebxml.omar.common.BindingUtility bu = org.freebxml.omar.common.BindingUtility.getInstance();

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        init();
    }

    public void init() throws ServletException {
        try {
            boolean initCacheOnServerInit = Boolean.valueOf(RegistryProperties.getInstance()
                .getProperty("omar.server.cache.initCacheOnServerInit", "true")).booleanValue();

            if (initCacheOnServerInit) {
                //Initialize cache so first client does not have to wait.
                ServerCache.getInstance().initialize();
            }
            log.info(ServerResourceBundle.getInstance().getString("message.init"));
        } catch (Exception ex) {
            log.fatal(ServerResourceBundle.getInstance().getString("message.RegistrySOAPServletInitFailed"), ex);
            throw new ServletException(ServerResourceBundle.getInstance().getString("message.registrySOAPServletInitFailed",
                    new Object[]{ex.getMessage()}));
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        resp.setContentType("text/plain");

        String requestUri = req.getRequestURI();
        String servletPath = req.getServletPath();
        String scheme = req.getScheme();
        String serverName = req.getServerName();
        String queryString = req.getQueryString();
        String omarName = RegistryProperties.getInstance().getProperty("omar.name", "omar");
        int serverPort = req.getServerPort();
        StringBuffer sb = new StringBuffer();
        sb.append(scheme).append("://").append(serverName).append(":");
        sb.append(serverPort);
        sb.append("/");
        sb.append(omarName);
        sb.append("/registry/thin/browser.jsp");
        String url  = sb.toString();


        PrintWriter wt = resp.getWriter();
        wt.println(ServerResourceBundle.getInstance().getString("message.urlForSOAP"));
        wt.println(ServerResourceBundle.getInstance().getString("message.urlForWebAccess", new Object[]{url}));
        wt.flush();
        wt.close();
    }

    public SOAPMessage onMessage(SOAPMessage msg, HttpServletRequest req, HttpServletResponse resp) {
        //System.err.println("onMessage called for RegistrySOAPServlet");
        SOAPMessage soapResponse = null;
	SOAPHeader sh = null;

        try {
	    // set 'sh' variable ASAP (before "firstly")
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            SOAPBody sb = se.getBody();
            sh = se.getHeader();

            // Firstly we put save the attached repository items in a map
            HashMap idToRepositoryItemMap = new HashMap();
            Iterator apIter = msg.getAttachments();
            while (apIter.hasNext()) {
                AttachmentPart ap = (AttachmentPart) apIter.next();

                //Get the content for the attachment
                RepositoryItem ri = processIncomingAttachment(ap);
                idToRepositoryItemMap.put(ri.getId(), ri);
            }

            // Log received message
            if (log.isTraceEnabled()) {
                // Warning! BAOS.toString() uses platform's default encoding
                ByteArrayOutputStream msgOs = new ByteArrayOutputStream();
                msg.writeTo(msgOs);
                msgOs.close();
                log.trace("incoming message:\n" + msgOs.toString());
            }

            // verify signature
            // returns false if no security header, throws exception if invalid
            CredentialInfo credentialInfo = new CredentialInfo();
            
            boolean noRegRequired = 
                Boolean.valueOf(CommonProperties.getInstance()
                    .getProperty("omar.common.noUserRegistrationRequired", "false")).booleanValue();
            
            if (!noRegRequired) {
                boolean isValidSignature =
                        SoapSecurityUtil.getInstance().verifySoapMessage(msg, credentialInfo);
            }
            
            //The ebXML registry request is the only element in the SOAPBody
            StringWriter requestXML = new StringWriter(); //The request as an XML String
            String requestRootElement = null;
            Iterator iter = sb.getChildElements();
            int i = 0;

            while (iter.hasNext()) {
                Object obj = iter.next();

                if (!(obj instanceof SOAPElement)) {
                    continue;
                }

                if (i++ == 0) {
                    SOAPElement elem = (SOAPElement) obj;
                    Name name = elem.getElementName();
                    requestRootElement = name.getLocalName();

                    StreamResult result = new StreamResult(requestXML);
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer trans = tf.newTransformer();
                    trans.transform(new DOMSource(elem), result);
                } else {
                    throw new RegistryException(
                        ServerResourceBundle.getInstance().getString("message.invalidRequest"));
                }
            }

            if (requestRootElement == null) {
                throw new RegistryException(
                    ServerResourceBundle.getInstance().getString("message.noebXMLRegistryRequest"));
            }

            Object requestObject = bu.getRequestObject(requestRootElement,
                    requestXML.toString());
            Request request = new Request(req, credentialInfo, requestObject,
                    idToRepositoryItemMap);

            Response response = request.process();
            soapResponse = createResponseSOAPMessage(response);

            if (response.getIdToRepositoryItemMap().size() > 0 &&
                (response.getMessage().getStatus().equals(
                BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success))) {
                idToRepositoryItemMap = response.getIdToRepositoryItemMap();
                Iterator mapKeysIter = idToRepositoryItemMap.keySet().iterator();

                while (mapKeysIter.hasNext()) {
                    String id = (String)mapKeysIter.next();
                    RepositoryItem repositoryItem = (RepositoryItem)idToRepositoryItemMap.get(id);

                    String cid = SoapSecurityUtil.convertUUIDToContentId(id);
                    DataHandler dh = repositoryItem.getDataHandler();
                    AttachmentPart ap = soapResponse.createAttachmentPart(dh);
                    ap.setContentId(cid);
                    soapResponse.addAttachmentPart(ap);

                    if (log.isTraceEnabled()) {
                        log.trace("adding attachment: contentId=" + id);
                    }
                }
            }


        } catch (Throwable t) {
            //Do not log ObjectNotFoundException as it clutters the log
            if (!(t instanceof ObjectNotFoundException)) {
                log.error(ServerResourceBundle.getInstance().getString("message.CaughtException", new Object[]{t.getMessage()}), t);
                Throwable cause = t.getCause();
                while (cause != null) {
                    log.error(ServerResourceBundle.getInstance().getString("message.CausedBy", new Object[]{cause.getMessage()}), cause);
                    cause = cause.getCause();
                }
            }

            soapResponse = createFaultSOAPMessage(t, sh);
        }

        if (log.isTraceEnabled()) {
            try {
                ByteArrayOutputStream rspOs = new ByteArrayOutputStream();
                soapResponse.writeTo(rspOs);
                rspOs.close();
                // Warning! BAOS.toString() uses platform's default encoding
                log.trace("response message:\n" + rspOs.toString());
            } catch (Exception e) {
                log.error(ServerResourceBundle.getInstance().getString("message.FailedToLogResponseMessage", new Object[]{e.getMessage()}), e);
            }
        }
        return soapResponse;
    }

    private SOAPMessage createFaultSOAPMessage(java.lang.Throwable e,
					       SOAPHeader sh) {
        SOAPMessage msg = null;
        if (log.isDebugEnabled()) {
            log.debug("Creating Fault SOAP Message with Throwable:", e);
        }
        try {
	    // Will this method be "legacy" ebRS 3.0 spec-compliant and
	    // return a URN as the <faultcode/> value?  Default expectation
	    // is of a an older client.  Overridden to instead be SOAP
	    // 1.1-compliant and return a QName as the faultcode value when
	    // we know (for sure) client supports new approach.
	    boolean legacyFaultCode = true;

	    // get SOAPHeaderElement list from the received message
	    // TODO: if additional capabilities are needed, move code to elsewhere
	    if (null != sh) {
		Iterator headers = sh.examineAllHeaderElements();
		while (headers.hasNext()) {
		    Object obj = headers.next();

		    // confirm expected Iterator content
		    if (obj instanceof SOAPHeaderElement) {
			SOAPHeaderElement header = (SOAPHeaderElement)obj;
			Name headerName = header.getElementName();

			// check this SOAP header for relevant capability signature
			if (headerName.getLocalName().
			    equals(BindingUtility.SOAP_CAPABILITY_HEADER_LocalName)
			    && headerName.getURI().
			    equals(BindingUtility.SOAP_CAPABILITY_HEADER_Namespace)
			    && header.getValue().
			    equals(BindingUtility.SOAP_CAPABILITY_ModernFaultCodes)
			    ) {
			    legacyFaultCode = false;
			    // only interested in one client capability
			    break;
			}
		    }
		}
	    }

            msg = MessageFactory.newInstance().createMessage();
            SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
            SOAPFault fault = msg.getSOAPBody().addFault();

            // set faultCode
	    String exceptionName = e.getClass().getName();
	    // TODO: SAAJ 1.3 has introduced preferred QName interfaces
	    Name name = env.createName(exceptionName,
				       "ns1",
				       BindingUtility.SOAP_FAULT_PREFIX);
	    fault.setFaultCode(name);
	    if (legacyFaultCode) {
		// we now have an element child, munge its text (hack alert)
		Node faultCode =
		    fault.getElementsByTagName("faultcode").item(0);
		// Using Utility.setTextContent() implementation since Java
		// WSDP 1.5 (containing an earlier DOM API) does not
		// support Node.setTextContent().
		Utility.setTextContent(faultCode,
				       BindingUtility.SOAP_FAULT_PREFIX + ":" +
				       exceptionName);
	    }

	    // set faultString
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = "NULL";
            }
            fault.setFaultString(errorMsg);

            //create faultDetail with one entry
            Detail det = fault.addDetail();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String str = sw.toString();

            name =
		env.createName("StackTrace",
			       "rs",
			       "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0");
            DetailEntry de = det.addDetailEntry(name);
            de.setValue(str);
            //de.addTextNode(str);

            //TODO: Need to put baseURL for this registry here

            msg.saveChanges();
        } catch (SOAPException ex) {
            log.warn(ex, ex);
	    // otherwise ignore the problem updating part of the message
	}

        return msg;
    }

    private SOAPMessage createResponseSOAPMessage(Object obj) {
        SOAPMessage msg = null;

        try {
            RegistryResponseType resp = null;

            if (obj instanceof org.freebxml.omar.server.interfaces.Response) {
                Response r = (Response) obj;
                resp = r.getMessage();
            } else if (obj instanceof java.lang.Throwable) {
                Throwable t = (Throwable) obj;
                resp = org.freebxml.omar.server.common.Utility.getInstance()
                                                              .createRegistryResponseFromThrowable(t,
                        "RegistrySOAPServlet", "Unknown");
            }

            //Now add resp to SOAPMessage
            StringWriter sw = new StringWriter();
            javax.xml.bind.Marshaller marshaller = bu.rsFac.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(resp, sw);

            //Now get the RegistryResponse as a String
            String respStr = sw.toString();

            // Use Unicode (utf-8) to getBytes (server and client). Rely on platform default encoding is not safe.
            InputStream soapStream = org.freebxml.omar.server.common.Utility.getInstance()
                .createSOAPStreamFromRequestStream(new ByteArrayInputStream(respStr
                .getBytes("utf-8")));

            boolean signRequired = Boolean.valueOf(RegistryProperties.getInstance()
                .getProperty("omar.interfaces.soap.signedResponse")).booleanValue();


            msg = org.freebxml.omar.server.common.Utility.getInstance()
                    .createSOAPMessageFromSOAPStream(soapStream);

            if (signRequired) {
                AuthenticationServiceImpl auService = AuthenticationServiceImpl.getInstance();
                PrivateKey privateKey = auService.getPrivateKey(AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR,
                        AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR);
                java.security.cert.Certificate[] certs = auService.getCertificateChain(AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR);

                CredentialInfo credentialInfo = new CredentialInfo(null, (X509Certificate)certs[0], certs, privateKey);
                msg = SoapSecurityUtil.getInstance().signSoapMessage(msg, credentialInfo);
            }

            // msg.writeTo(new FileOutputStream(new File("signedResponse.xml")));
            soapStream.close();
        } catch (IOException e) {
            log.warn(e, e);
	    // otherwise ignore the problem updating part of the message
        } catch (SOAPException e) {
            log.warn(e, e);
	    // otherwise ignore the problem updating part of the message
        } catch (javax.xml.bind.JAXBException e) {
            log.warn(e, e);
	    // otherwise ignore the problem updating part of the message
        } catch (ParseException e) {
            log.warn(e, e);
	    // otherwise ignore the problem updating part of the message
        } catch (RegistryException e) {
            log.warn(e, e);
	    // otherwise ignore the problem updating part of the message
        } catch (JAXRException e) {
            log.warn(e, e);
	    // otherwise ignore the problem updating part of the message
        }

        return msg;
    }

    private RepositoryItem processIncomingAttachment(AttachmentPart ap)
        throws RegistryException {
        RepositoryItem ri = null;

        try {
            //ContentId is the id of the repositoryItem
            String id = SoapSecurityUtil.convertContentIdToUUID(ap.getContentId());
            if (log.isInfoEnabled()) {
                log.info(ServerResourceBundle.getInstance().getString("message.ProcessingAttachmentWithContentId", new Object[]{id}));
            }

            if (log.isDebugEnabled()) {
                log.debug("Processing attachment (RepositoryItem):\n"
                + ap.getContent().toString());
            }

            DataHandler dh = ap.getDataHandler();
            ri = new RepositoryItemImpl(id, dh);
        } catch (SOAPException e) {
	    RegistryException toThrow = new RegistryException(e);
	    toThrow.initCause(e);
	    throw toThrow;
        }

        return ri;
    }
}
