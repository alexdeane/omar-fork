/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/query/QueryTest.java,v 1.17 2006/11/28 23:34:46 farrukh_najmi Exp $
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003-2006 freebxml.org. All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.server.query;

import junit.framework.Test;
import junit.framework.TestSuite;
import java.io.StringWriter;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import org.freebxml.omar.common.Utility;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;

/**
 * @author Farrukh Najmi
 * @author Doug Bunting, Sun Microsystems
 * @version $Revision: 1.17 $
 */
public class QueryTest extends ServerTest {
    private static String xmlText = null;

    /**
     * Very simple Handler instance which fails immediately when any syntax
     * error or warning is encountered during validation of an XML instance
     * against our expected schemas.
     */
    protected static class Handler extends DefaultHandler {
        /**
	 * {@inheritDoc}
         * @param sAXParseException
         * @throws SAXException
         */
        public void error(SAXParseException sAXParseException)
	    throws SAXException {
	    System.err.println("Invalid response:\n" + xmlText);
            fail(sAXParseException.toString());
        }

        /**
	 * {@inheritDoc}
         * @param sAXParseException
         * @throws SAXException
         */
        public void fatalError(SAXParseException sAXParseException)
	    throws SAXException {
	    System.err.println("Invalid response:\n" + xmlText);
            fail(sAXParseException.toString());
        }

        /**
	 * {@inheritDoc}
         * @param sAXParseException
         * @throws SAXException
         */
        public void warning(SAXParseException sAXParseException)
	    throws SAXException {
	    System.err.println("Invalid response:\n" + xmlText);
            fail(sAXParseException.toString());
        }
    }

    /** Handler instance for use when performing XML Schema validation. */
    private static Handler handler = new Handler();
    /** Marshaller used to extract XML content from returned Java trees. */
    private static Marshaller marshaller = null;
    /** JAXR query manager */
    private static QueryManager qm =
	QueryManagerFactory.getInstance().getQueryManager();
    /** JAXB Validator for response Java tree. */
    private static javax.xml.bind.Validator bindValidator = null;
    /** XML Schema validator */
    //private static javax.xml.validation.Validator xmlValidator = null;

    /**
     * Can we double-check validation?  Such validation works only if
     * ebxmlrr-spec tree is available.
     */
    private boolean doXMLValidation = canUseEbxmlrrSpecHome;

    /**
     * What is the directory containing the XML Schema instances we need?
     * This directory and all derived sources are used only when
     * doXMLValidation is true.
     *
     * http://www.oasis-open.org/committees/regrep/documents/3.0/schema/
     * may be somewhat more reliable than ../ebxmlrr-spec/.. (because that
     * workspace may not have been downloaded) but does not contain schema
     * instances with the correct target namespace.
     */
    private String schemaLoc = ebxmlrrSpecHome + "/misc/3.0/schema/";

    /** schema for urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0 namespace */
    private StreamSource rimSource = new StreamSource(schemaLoc + "rim.xsd");
    /** schema for urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0 namespace */
    private StreamSource rsSource = new StreamSource(schemaLoc + "rs.xsd");
    /** schema for urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0 namespace */
    private StreamSource querySource = new StreamSource(schemaLoc+"query.xsd");
    /** all of the schema instances we need */
    private StreamSource sources[] = {rimSource, rsSource, querySource};

    /**
     * Constructor for XalanVersionTest.
     *
     * @param name
     */
    public QueryTest(String name) {
        super(name);
    }

    public void testSelectAdhocQuery() throws Exception {
        testSelectQuery("AdhocQuery");
    }

    public void testSelectAssociation() throws Exception {
        testSelectQuery("Association");
    }

    public void testSelectAuditableEvent() throws Exception {
        testSelectQuery("AuditableEvent");
    }

    public void testSelectClassification() throws Exception {
        testSelectQuery("Classification");
    }

    public void testSelectClassificationNode() throws Exception {
        testSelectQuery("ClassificationNode");
    }

    public void testSelectClassificationScheme() throws Exception {
        testSelectQuery("ClassificationScheme");
    }

    public void testSelectExternalIdentifier() throws Exception {
        testSelectQuery("ExternalIdentifier");
    }

    public void testSelectExternalLink() throws Exception {
        testSelectQuery("ExternalLink");
    }

    public void testSelectExtrinsicObject() throws Exception {
        testSelectQuery("ExtrinsicObject");
    }

    public void testSelectFederation() throws Exception {
        testSelectQuery("Federation");
    }

    public void testSelectOrganization() throws Exception {
        testSelectQuery("Organization");
    }

    public void testSelectRegistry() throws Exception {
        testSelectQuery("Registry");
    }

    // ?? SHould we comment this out as it takes an excessive amount of time to run
    public void testSelectRegistryObject() throws Exception {
        testSelectQuery("RegistryObject");
    }

    public void testSelectRegistryPackage() throws Exception {
        testSelectQuery("RegistryPackage");
    }

    public void testSelectService() throws Exception {
        testSelectQuery("Service");
    }

    public void testSelectServiceBinding() throws Exception {
        testSelectQuery("ServiceBinding");
    }

    public void testSelectSpecificationLink() throws Exception {
        testSelectQuery("SpecificationLink");
    }

    public void testSelectSubscription() throws Exception {
        testSelectQuery("Subscription");
    }

    public void testSelectUser() throws Exception {
        testSelectQuery("User");
    }

    private void testSelectQuery(String rimClass) throws Exception {
	// Get a Validator (and other private fields) if not already available
	if (null == bindValidator) {
	    marshaller = bu.getJAXBContext().createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
				   Boolean.TRUE);

	    bindValidator = bu.getJAXBContext().createValidator();
	}

        //System.err.println("Querying class: " + rimClass);
	rimClass = Utility.getInstance().mapTableName(rimClass);
        String sqlString = "SELECT * FROM " + rimClass;
        AdhocQueryRequest req = bu.createAdhocQueryRequest(sqlString);
        ServerRequestContext context =
	    new ServerRequestContext("QueryTest:testSelectQuery", req);
        context.setUser(ac.registryGuest);

        AdhocQueryResponseType resp = qm.submitAdhocQuery(context);

        // Make sure that there is at least one object that matched the query
	int cnt = resp.getRegistryObjectList().getIdentifiable().size();
        assertTrue("Found 0 " + rimClass +
		   " objects to match the query. Expected at least one.",
		   0 < cnt);

	// Display count
	System.out.println(rimClass + ":\t" + cnt);

	// Get response content as a String
	StringWriter strWriter = new StringWriter();
	marshaller.marshal(resp, strWriter);
	xmlText = strWriter.toString();

	// Validate the response
	if (!bindValidator.validateRoot(resp)) {
	    System.err.println("Invalid response:\n" + xmlText);
	    fail("Validation failed for response");
	}

        /* Removed as it depends upon JDK 1.5
	if (doXMLValidation) {
	    // Get an XML Validator if not already available
	    if (null == xmlValidator) {
		SchemaFactory sf = SchemaFactory.
		    newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		sf.setErrorHandler(handler);

		Schema respSchema = sf.newSchema(sources);
		xmlValidator = respSchema.newValidator();
		xmlValidator.setErrorHandler(handler);
	    }

	    // Confirm the response was valid (paranoia)
	    xmlValidator.validate(new StreamSource(new StringReader(xmlText)));
	}
         **/
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(QueryTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new QueryTest("testSelectAssociation"));
        return suite;
        
    }
}
