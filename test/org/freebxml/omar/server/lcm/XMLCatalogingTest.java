/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/lcm/XMLCatalogingTest.java,v 1.12 2005/11/21 04:28:27 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.lcm;

import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.activation.DataHandler;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CredentialInfo;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.RepositoryItemImpl;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;


/**
 * @author Tony Graham
 */
public class XMLCatalogingTest extends ServerTest {
    
    /**
     * Constructor for XMLCatalogingTest
     *
     * @param name
     */
    public XMLCatalogingTest(String name) {
        super(name);
    }
        
    /** Tests submission of an ExtrinsicObject without a RepositoryItem */
    public void testEoIdIsUuidNoRI() throws Exception {        
        String eoId = org.freebxml.omar.common.Utility.getInstance().createId();

	try {
            submitExtrinsicObject(eoId,
                                  "eoId=UUID, no ri",
                                  bu.CPP_CLASSIFICATION_NODE_ID,
                                  null);
        } catch (Exception e) {
            e.printStackTrace();
            fail("submitExtrinsicObject() threw an exception");
        }
    }

    /** Tests submission of an ExtrinsicObject with a RepositoryItem */
    public void testEoIdIsUuidHasRI() throws Exception {
        URL url = getClass().getResource("/resources/CPP1.xml");

	DataHandler dataHandler =
	    new javax.activation.DataHandler(url);
        String eoId = org.freebxml.omar.common.Utility.getInstance().createId();

        try {
            submitExtrinsicObject(eoId,
                                  "eoId=UUID has ri",
                                  bu.CPP_CLASSIFICATION_NODE_ID,
                                  dataHandler);
        } catch (Exception e) {
            e.printStackTrace();
            fail("submitExtrinsicObject() threw an exception");
        }
    }

    public void testEoIdIsNonUuidRHasRI() throws Exception {        
        URL url = getClass().getResource("/resources/CPP1.xml");

	DataHandler dataHandler =
	    new javax.activation.DataHandler(url);
        String eoId = "nonUUID";
	String riId = eoId;

	try {
            submitExtrinsicObject(eoId,
                                  "eoId=nonUUID has ri",
                                  bu.CPP_CLASSIFICATION_NODE_ID,
                                  dataHandler);
        } catch (Exception e) {
            e.printStackTrace();
            fail("submitExtrinsicObject() threw an exception");
        }
    }

    void submitExtrinsicObject(String eoId,
			       String eoName,
			       String eoObjectType,
			       DataHandler riDataHandler)
	throws Exception {

	ExtrinsicObject eo = bu.rimFac.createExtrinsicObject();
        eo.setId(eoId);
	eo.setObjectType(eoObjectType);

	eo.setName(bu.createInternationalStringType(eoName));
        eo.setContentVersionInfo(bu.rimFac.createVersionInfoType());

        ArrayList objects = new ArrayList();
        objects.add(eo);
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        RegistryObjectListType roList = bu.rimFac.createRegistryObjectList();
        
        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);

	HashMap idToRepositoryItemMap = new HashMap();
	if (riDataHandler != null) {
	    eo.setMimeType(riDataHandler.getContentType());
	    RepositoryItem ri =
		createRepositoryItem(riDataHandler, eoId);
	    idToRepositoryItemMap.put(eoId, ri);
	}
           
        ServerRequestContext context = new ServerRequestContext("XMLCatalogingTest:submitExtrinsicObject", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);

        RegistryResponse resp = lcm.submitObjects(context);
        BindingUtility.getInstance().checkRegistryResponse(resp);
    }

    /**
     * Creates a {@link RepositoryItem}.
     *
     * @param dh the {@link DataHandler} representing the payload
     * @param id the ID to use for the {@link RepositoryItem}
     * @exception Exception if an error occurs
     */
    RepositoryItem createRepositoryItem(DataHandler dh, String id)
        throws Exception {
        RepositoryItem ri = new RepositoryItemImpl(id, dh);
        return ri;
    }
    
    /**
     * Creates a {@link CredentialInfo} for a {@link DataHandler}.
     *
     * <p>Uses a dummy X500 name for signing the {@link CredentialInfo}.
     *
     * @param dh the <code>DataHandler</code>
     * @return the <code>CredentialInfo</code> for the <code>DataHandler</code>
     * @exception Exception if an error occurs
     */
    CredentialInfo createCredentialInfo(DataHandler dh) throws Exception {
        //Generating 512 bit DSA key pair and self-signed certificate (SHA1WithDSA) for TestUser-DN"));
        CertAndKeyGen certandkeygen = new CertAndKeyGen("DSA", "SHA1WithDSA");
        X500Name x500name = new X500Name("Tester", "Test Unit", "OMAR", "JKL", "KS", "FI");
        certandkeygen.generate(512);
        PrivateKey privateKey = certandkeygen.getPrivateKey();
        X509Certificate[] ax509cert = new X509Certificate[1];
        ax509cert[0] = certandkeygen.getSelfCertificate(x500name,  90 * 24 * 60 * 60);

        //Create the credentials to use.
        //TODO: this could be created only once per SOAPSender
        //TODO: now signing with null server alias. Might gen overhead by sending cert
	return new CredentialInfo((String)null,
				  ax509cert[0], ax509cert, privateKey);
    }

    public static Test suite() {
        return new TestSuite(XMLCatalogingTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
}
