/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/profile/ws/wsdl/cataloger/WSDLDocument.java,v 1.2 2006/10/03 01:57:53 psterk Exp $
 * ====================================================================
 */

package org.freebxml.omar.server.profile.ws.wsdl.cataloger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.freebxml.omar.common.Utility;

import org.freebxml.omar.common.exceptions.CatalogingException;
import org.freebxml.omar.server.util.ServerResourceBundle;

/**
 *
 * @author psterk
 */
public class WSDLDocument {
    String publicId = null;
    String systemId = null;
    List importedDocuments = null;
    Document document = null;
    Map namespaceLookup = new HashMap();
    WSDLDocument parentDoc = null;
    
    /**
     * Default Constructor
     * 
     * Creates a new instance of WSDLDocument
     */
    public WSDLDocument(InputSource docInputSource) 
        throws CatalogingException {
        try {
            publicId = docInputSource.getPublicId();
            systemId = docInputSource.getSystemId();
            document = parseXML(docInputSource);
            verifyTagNSRootElement(document.getDocumentElement(), WSDLConstants.QNAME_DEFINITIONS);
            collectAllNamespaces();
            // Resolve imported documents
            resolveImports();
        } catch (FileNotFoundException fe) {
            String message = ServerResourceBundle.getInstance()
                                                 .getString("message.error.missingFile", 
                                                             new Object[]{fe.getMessage()});
            throw new CatalogingException(message, fe);
        } catch (CatalogingException ce) {
            throw ce;
        } catch (Throwable t) {
            throw new CatalogingException(t);
        }
    }
    
    /**
     * Constructor
     * This constructor is used when a parent WSDLDocument exists and a 
     * reference to it is passed to the child WSDLDocument via this constructor
     * The child can use the parent reference to inspect the parent's state.
     */
    public WSDLDocument(InputSource docInputSource, WSDLDocument parentDoc) 
        throws CatalogingException {
        this(docInputSource);
        this.parentDoc = parentDoc;
    }
    
    /**
     * Return the public id of the InputSource for the contained 
     * org.w3c.dom.Document
     *
     * @return
     *  String - public id
     */
    public String getPublicId() {
        return publicId;
    }
    
    /**
     * Return the system id of the InputSource for the contained 
     * org.w3c.dom.Document
     *
     * @return
     *  String - system id
     */    
    public String getSystemId() {
        return systemId;
    }
    
    /**
     * Returns a List of documents imported by the contained org.w3c.dom.Document
     *
     * @return
     *  java.util.List of imported documents
     */
    public List getImportedDocuments() {
        if (importedDocuments == null) {
            importedDocuments = new ArrayList();
        }
        return importedDocuments;
    }
    
    /**
     * Returns the org.w3c.dom.Document that is wrapped by this class
     * 
     * @return
     *  org.w3c.dom.Document
     */
    public Document getDocument() {
        return document;
    }
    
    /**
     * Returns a required Element based on the QName.  If element not found,
     * a CatalogingException is thrown
     *
     * @arg qName
     *  A javax.xml.namespace.QName
     * @return
     *  A org.w3c.dom.Element
     */
    public Element getRequiredElement(QName qName) 
        throws CatalogingException {
        Element element = getElement(qName);
        if (element == null) {
            throw new CatalogingException(ServerResourceBundle.getInstance()
                                                              .getString("message.missingRequiredElement",
                                                                      new Object[] {publicId, qName.toString()}));
        }
        return element;
    }
    
    /**
     * Returns an Element based on the QName.
     *
     * @arg qName
     *  A javax.xml.namespace.QName
     * @return
     *  A org.w3c.dom.Element
     */
    public Element getElement(QName qName) throws CatalogingException {
        return getElement(document.getDocumentElement(), qName);
    }
    
    /**
     * Returns a required Element based on the QName.  If element not found,
     * a CatalogingException is thrown
     *
     * @arg parentElement
     *  The parent element from which to get the required child Element
     * @arg qName
     *  A javax.xml.namespace.QName used to find the Element
     * @return
     *  A org.w3c.dom.Element
     */
    public Element getRequiredElement(Element parentElement, QName qName) 
        throws CatalogingException {
        Element element = getElement(parentElement, qName);
        if (element == null) {
            throw new CatalogingException(ServerResourceBundle.getInstance()
                                                              .getString("message.missingRequiredElement",
                                                                      new Object[] {publicId, qName.toString()}));
        }
        return element;
    }
        
    /**
     * Returns an Element based on the QName.
     *
     * @arg parentElement
     *  The parent element from which to get the required child Element
     * @arg qName
     *  A javax.xml.namespace.QName used to find the Element
     * @return
     *  A org.w3c.dom.Element
     */
    public Element getElement(Element parentElement, QName qName) 
        throws CatalogingException {
        NodeList nodeList = parentElement.getElementsByTagNameNS(qName.getNamespaceURI(), 
                                                                 qName.getLocalPart());
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element2 = (Element)node;
                return element2;
            }
        }
        return null;
    }

    /**
     * Returns a required Element based on the QName.
     *
     * @arg qName
     *  A javax.xml.namespace.QName used to find the Element
     * @return
     *  A org.w3c.dom.Element
     */
    public List getRequiredElements(QName qName) 
        throws CatalogingException {
        List elements = getElements(qName);
        if (elements.size() == 0) {
            throw new CatalogingException(ServerResourceBundle.getInstance()
                                                              .getString("message.missingRequiredElement",
                                                                      new Object[] {publicId, qName.toString()}));
        }
        return elements;
    }
    
    /**
     * Returns a List of Elements based on the QName.
     *
     * @arg qName
     *  A javax.xml.namespace.QName used to find the Element
     * @return
     *  A java.util.List
     */
    public List getElements(QName qName) throws CatalogingException {
        return getElements(document.getDocumentElement(), qName);
    }
        
    /**
     * Returns a List of required Elements based on the QName. If no Elements
     * are found, a CatalogingException is thrown
     *
     * @arg parentElement
     *  The parent element from which to get the required child Element
     * @arg qName
     *  A javax.xml.namespace.QName used to find the Element
     * @return
     *  A java.util.List
     */
    public List getRequiredElements(Element parentElement, QName qName) 
        throws CatalogingException {
        List elements = getElements(parentElement, qName);
        if (elements.size() == 0) {
            throw new CatalogingException(ServerResourceBundle.getInstance()
                                                              .getString("message.missingRequiredElement",
                                                                      new Object[] {publicId, qName.toString()}));
        }
        return elements;
    }
    
    /**
     * Returns a List of Elements based on the QName.
     *
     * @arg parentElement
     *  The parent element from which to get the required child Element
     * @arg qName
     *  A javax.xml.namespace.QName used to find the Element
     * @return
     *  A java.util.List
     */
    public List getElements(Element parentElement, QName qName) 
        throws CatalogingException {
        List list = new ArrayList();
        NodeList nodeList = parentElement.getElementsByTagNameNS(qName.getNamespaceURI(), 
                                                                 qName.getLocalPart());
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element2 = (Element)node;
                list.add(element2);
            }
        }
        // If no elements found, check for imported documents
        if (list.size() == 0) {
            Iterator importedDocItr = getImportedDocuments().iterator();
            while (importedDocItr.hasNext()) {
                WSDLDocument document = (WSDLDocument)importedDocItr.next();
                List importedElements = document.getElements(qName);
                list.addAll(importedElements);
            }
        }
        return list;
    }
    
    /**
     * Returns a List of Elements based on the QName. This method will not
     * traverse imported Document objects to find the Elements.
     *
     * @arg qName
     *  A javax.xml.namespace.QName used to find the Element
     * @return
     *  A java.util.List
     */
    public List getElementsNoImport(QName qName)
        throws CatalogingException {
        List list = new ArrayList();
        NodeList nodeList = document.getDocumentElement()
                                    .getElementsByTagNameNS(qName.getNamespaceURI(), 
                                                            qName.getLocalPart());
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element2 = (Element)node;
                list.add(element2);
            }
        }
        return list;
    }

    /**
     * This method returns the default namespace in the org.w3c.dom.Document
     * 
     * @return
     *  A String containing the default namespace
     */
    public String getDefaultNamespaceURI() {
        return (String)namespaceLookup.get("");
    }
    
    /**
     * This method returns the namespace URI in the org.w3c.dom.Document 
     * using the prefix
     * 
     * @arg prefix
     *  A String containing the prefix
     * @return
     *  A String containing the namespace
     */
    public String getNamespaceURI(String prefix) {
        String namespaceURI = (String)namespaceLookup.get(prefix);
        if (namespaceURI == null) {
           namespaceURI = getNamespaceURIFromImport(prefix);
        }
        return namespaceURI;
    }
    
    /**
     * This method returns the target namespace in the org.w3c.dom.Document
     * 
     * @return
     *  A String containing the target namespace
     */
    public String getTargetNamespaceURI() {
        return (String)namespaceLookup.get("targetNamespace");
    }
    
    /**
     * This method returns the target namespace in the org.w3c.dom.Document 
     * that contains the Element
     * 
     * @arg element
     *  This Element that is contained in the Document
     * @arg name
     *  The QName used to find the Element
     * @return
     *  A String containing the target namespace
     */
    public String getTargetNamespaceURI(Element element, QName name) 
        throws CatalogingException {
        String targetNamespaceURI = null;
        WSDLDocument doc = getWSDLCatalogedDocument(name, element);
        if (doc != null) {
            targetNamespaceURI = doc.getTargetNamespaceURI();
        }
        return targetNamespaceURI;
    }
    
    /**
     * This method returns all the namespaces in the Document
     * 
     * @return
     *  A java.util.Collection of namespaces
     */
    public Collection getAllNamespaceURIs() {
        return namespaceLookup.values();
    }
    
    /**
     * This method returns the required org.w3c.dom.Attribute for the 
     * org.w3c.dom.Element based on the name. If the Attribute is not found, a
     * CatalogingException is thrown.
     *
     * @arg element
     *  The Element from which to get the Attribute
     * @arg name
     *  The String name by which to select the Attribute
     */
    public String getRequiredAttribute(Element element, String name) 
        throws CatalogingException {
        String result = getAttribute(element, name);
        if (result == null) {
            throw new CatalogingException(ServerResourceBundle.getInstance()
                                                              .getString("message.missingRequiredAttribute",
                                                                          new Object[] {name}));
        }      
        return result;
    }
    
   /**
     * This method returns the org.w3c.dom.Attribute for the 
     * org.w3c.dom.Element based on the name. This method will return null, if
     * the Attribute is not found.
     *
     * @arg element
     *  The Element from which to get the Attribute
     * @arg name
     *  The String name by which to select the Attribute
     * @return
     *  The Attribute value as a String
     */
    public String getAttribute(Element e, String name) {
        Attr a = e.getAttributeNode(name);
        if (a == null)
            return null;
        return a.getValue();
    }    
    
    /**
     * This method returns a Collection of all org.w3c.dom.Attributes for the 
     * org.w3c.dom.Element.
     *
     * @arg element
     *  The Element from which to get the Attribute
     * @return
     *  A List of Attributes
     */
    public Collection getAttributes(Element e) {
        List attrList = new ArrayList();
        NamedNodeMap nnMap = e.getAttributes();
        int length = nnMap.getLength();
        for (int i = 0; i < length; i++) {
            attrList.add((Attr)nnMap.item(i));
        }
        return (Collection)attrList;
    }
/******************************************************************************/
/*                            protected methods                               */
/******************************************************************************/     
    /*
     * This guards against circular references in an imported doc set. The id 
     * of the WSDLDocument is passed to this method and boolean will indicate
     * whether or not it is in the imported document list.
     */
    protected boolean hasImportedDocBeenPreviouslyAdded(String wsdlDocId) {
        boolean beenPreviouslyAdded = false;
        Iterator importedDocItr = getImportedDocuments().iterator();
        while (importedDocItr.hasNext()) {
            WSDLDocument parentImportedWsdlDoc = (WSDLDocument)importedDocItr.next();
            String parentImportedId = parentImportedWsdlDoc.getSystemId();
            if (parentImportedId.equalsIgnoreCase(wsdlDocId)) {
                beenPreviouslyAdded = true;
                break;
            }    
        }
        if (beenPreviouslyAdded == false) {
            // Check parent too, if it exists
            if (parentDoc != null) {
                beenPreviouslyAdded = parentDoc.hasImportedDocBeenPreviouslyAdded(wsdlDocId);
            }
        }
        return beenPreviouslyAdded;
    }
    
    /*
     * This method is used to get the WSDLDocument that contains the 
     * targetElement.  The QName is used to retrieve all Elements that match
     * the targetElement.
     */
    protected WSDLDocument getWSDLCatalogedDocument(QName name, 
                                                    Element targetElement) 
        throws CatalogingException {
        WSDLDocument doc = null;
        Iterator elementItr = getElementsNoImport(name).iterator();
        while (elementItr.hasNext()) {
            Element element = (Element)elementItr.next();
            if (element == targetElement) {
                doc = this;
                break;
            }            
        }
        // Check imported documents
        if (doc == null) {
            Iterator wsdlDocItr = getImportedDocuments().iterator();
            while (wsdlDocItr.hasNext()) {
                WSDLDocument wsdlDoc = (WSDLDocument)wsdlDocItr.next();
                doc = wsdlDoc.getWSDLCatalogedDocument(name, targetElement);
                if (doc != null) {
                    break;
                }
            }
        }
        return doc;
    }
    
/******************************************************************************/
/*                            private methods                                 */
/******************************************************************************/ 
    /*
     * Method takes an InputSource and returns a org.w3c.dom.Document
     */
    private Document parseXML(InputSource source) 
        throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory builderFactory =
            DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setValidating(false);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException e)
                throws SAXParseException {
                throw e;
            }

            public void fatalError(SAXParseException e)
                throws SAXParseException {
                throw e;
            }

            public void warning(SAXParseException err)
                throws SAXParseException {
                // do nothing
            }
        });

        return builder.parse(source);
    }
    
    /*
     * This method resolves all the wsdl:import elements in a Document. 
     * For each instance of these elements, a new WSDLDocument is created 
     * and added to the imported documents list.
     */
    private void resolveImports() throws CatalogingException {
        try {
            List importElements = getElements(WSDLConstants.QNAME_IMPORT);
            Iterator importElementItr = importElements.iterator();
            while (importElementItr.hasNext()) {
                Element importElement = (Element)importElementItr.next();            
                String namespaceAttr = getAttribute(importElement, WSDLConstants.ATTR_NAMESPACE);
                String locationAttr = getAttribute(importElement, WSDLConstants.ATTR_LOCATION);
                String namespaceURN = null;
                if (locationAttr == null) {
                    // Support case of just namespace attribute. e.g.
                    // <xsd:import namespace="http://www.w3.org/2001/xml.xsd"/  
                    locationAttr = namespaceAttr;
                }
                String qualifiedSystemId = getQualifiedSystemIdWithBase(systemId, locationAttr);
                // This guards against circular references in the doc set
                if (hasImportedDocBeenPreviouslyAdded(qualifiedSystemId) == false) {
                    URL url = null;
                    try {
                        url = new URL(qualifiedSystemId);
                    } catch (MalformedURLException e) {
                        url = new File(qualifiedSystemId).toURL();
                    }
                    String systemId = url.toString();
                    InputSource source = new InputSource(systemId);
                    getImportedDocuments().add(new WSDLDocument(source, this));
                }
            }
        } catch (CatalogingException ce) {
            throw ce;
        } catch (Throwable t) {
            throw new CatalogingException(t);
        }
    }      

    /*
     * This method is used to get a qualified system id using the baseSystemId
     * as the base Id.
     */
    private String getQualifiedSystemIdWithBase(String baseSystemId, 
                                                String systemId) 
        throws CatalogingException {
        String qualifiedId = null;
        try {
            if (baseSystemId == null) {
                // if id is null, use a default
                baseSystemId = WSDLCatalogerEngine.TMP_DIR;
            }
            URL base = null;
            try {
                base = new URL(baseSystemId);
            } catch (MalformedURLException e) {
                base = new File(baseSystemId).toURL();
            }
            URL url = new URL(base, systemId);
            qualifiedId = url.toString();
        } catch (MalformedURLException me) {
            String message = ServerResourceBundle.getInstance()
                                                 .getString("message.error.missingFile", 
                                                             new Object[]{systemId});
            throw new CatalogingException(message, me);
        } catch (Throwable t) {
            throw new CatalogingException(t);
        }
        return qualifiedId;
    }
        
    /*
     * This helper method collects all namespace URIs from the Document Element
     */
    private Set collectAllNamespaces() {
        return collectAllNamespaces(document.getDocumentElement());
    }
    
    /*
     * This method colects all the namespace URIs for the passed Element
     */    
    private Set collectAllNamespaces(Element element) {
        Set namespaces = null;
        Collection values = namespaceLookup.values();
        if (values.size() == 0) {
            namespaces = new HashSet();
            if (element.getNamespaceURI() != null) {
                namespaces.add(element.getNamespaceURI());
            }
            NamedNodeMap nnMap = element.getAttributes();
            int length = nnMap.getLength();
            for (int i = 0; i < length; i++) {
                Attr attr = (Attr)nnMap.item(i);
                if (attr.getName().equals(WSDLConstants.PREFIX_XMLNS)) {
                    // default namespace declaration
                    String value = attr.getValue();
                    namespaces.add(value);
                    String prefix = attr.getPrefix();
                    if (prefix == null || prefix.length() == 0) {
                        // default namespace
                        namespaceLookup.put("", value);
                    } else {
                        namespaceLookup.put(prefix, value);
                    }
                } else {
                    String prefix = Utility.getPrefix(attr.getName());
                    if (prefix != null && prefix.equals(WSDLConstants.PREFIX_XMLNS)) {
                        String uri = attr.getValue();
                        namespaces.add(uri);
                        String localName = attr.getLocalName();
                        namespaceLookup.put(localName, uri);
                    }
                }            
            }
            String targetNamespace = getTargetNamespaceURI();
            if (targetNamespace == null) {
                Element definitionsElement = document.getDocumentElement();
                Attr attribute = definitionsElement.getAttributeNode("targetNamespace");
                targetNamespace = attribute.getValue();
                namespaces.add(targetNamespace);
                namespaceLookup.put("targetNamespace", targetNamespace);
            }
        } else {
            namespaces = new HashSet(values);
        }
        return namespaces;
    }
    
    /*
     * This method gets the namespace URI that maps to the prefix in the 
     * imported document list.
     */
    private String getNamespaceURIFromImport(String prefix) {
        String namespaceURI = null;
        Iterator impDocItr = getImportedDocuments().iterator();
        while (impDocItr.hasNext()) {
            WSDLDocument wsdlDoc = (WSDLDocument)impDocItr.next();
            namespaceURI = wsdlDoc.getNamespaceURI(prefix);
            if (namespaceURI == null) {
                namespaceURI = wsdlDoc.getNamespaceURIFromImport(prefix);
            } else {
                break;
            }
        }
        return namespaceURI;
    }
    
    /*
     * This method validates that the Document Element has local part 
     * 'definitions' and namespace URI 'http://schemas.xmlsoap.org/wsdl/'
     */
    private void verifyTagNSRootElement(Element element, QName name) 
        throws CatalogingException {
        if (!element.getLocalName().equals(name.getLocalPart())
            || (element.getNamespaceURI() != null
                && !element.getNamespaceURI().equals(name.getNamespaceURI()))) {
            throw new CatalogingException(ServerResourceBundle.getInstance()
                                                              .getString("message.incorrectRootElement",
                                                                         new Object[] {name.getLocalPart()}));
        }
    }
}
