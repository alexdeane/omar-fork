/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/BigExtrinsicObjectTest.java,v 1.2 2006/11/07 20:11:20 dougb62 Exp $
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2006 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.ExtrinsicObject;

import junit.extensions.RepeatedTest;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.client.common.ClientTest;

/**
 * Junit Test for large Extrinsic Objects.
 *
 * Test pays attention to ${test.repetitions.inner} and
 * ${test.repetitions.outer}, using them for the number of Extrinsic
 * Objects created, read, deleted at once and the number of overall test
 * runs, respectively.  Ignoring the much smaller delete operations and
 * expected-to-fail queries, ${test.repetitions.inner} controls the size of
 * messages sent and received, approximately in multiples of 1MB per
 * message.  ${test.repetitions.outer} controls the number of messages sent
 * and received, again ignoring the tiny ones.
 *
 * @author Doug Bunting, Sun Microsystems
 * @version $Revision: 1.2 $
 */
public class BigExtrinsicObjectTest extends ClientTest {
    /** Repository Item handler */
    private static DataHandler repositoryItem = null;
    /** Byte array with content of our Repository Item for comparisons */
    private static byte blobContent[] = null;

    /** Base Identifier (id and lid) of our Extrinsic Objects */
    private static final String eoId =
	"urn:freebxml:registry:test:BigExtrinsicObjectTest::EO";
    /** Base name of our Extrinsic Objects */
    private static final String eoName = "Big Extrinsic Object ";

    /** Instance of this class for use in static setup methods */
    private static BigExtrinsicObjectTest instance =
	new BigExtrinsicObjectTest("testExtrinsicObjectCReD");
    /** Number of Extrinsic Objects created / read / deleted at once */
    private static final int numEOs = testRepetitionsInner;

    /** Where are we in the overall test run? */
    private int iteration = 0;

    public BigExtrinsicObjectTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        suite.addTest(instance);

	// repeat cycle as needed
        RepeatedTest repeats = new RepeatedTest(suite, testRepetitionsOuter);

	// ensure setup occurs, just once for all repetitions
	TestSetup wrapped = new TestSetup(repeats) {
		/** Set up our Repository Item */
		public void setUp() throws Exception {
		    super.setUp();

		    // pre test cleanup
		    for (int i = 0; numEOs > i; i++) {
			instance.
			    deleteIfExist(eoId + i,
					  LifeCycleManager.EXTRINSIC_OBJECT);
		    }

		    // get the RepositoryItem (random blob)
		    String riResourceName = "/resources/rand.blob";
		    URL riResourceUrl = getClass().getResource(riResourceName);
		    assertNotNull("Missing test resource: " + riResourceName,
				  riResourceUrl);
		    File repositoryItemFile =
			new File(riResourceUrl.getFile());
		    assertTrue("Unreadable test resource: " +
			       riResourceUrl.getFile(),
			       repositoryItemFile.canRead());
		    repositoryItem =
			new DataHandler(new
					FileDataSource(repositoryItemFile));

		    // save content for later comparison
		    blobContent =
			instance.readBytes(repositoryItem.getInputStream());
		}

		/** Ensure (one more time) that EO has been deleted */
		public void tearDown() throws Exception {
		    super.tearDown();

		    // post test cleanup
		    for (int i = 0; numEOs > i; i++) {
			instance.
			    deleteIfExist(eoId + i,
					  LifeCycleManager.EXTRINSIC_OBJECT);
		    }
		}
	    };

	return wrapped;
    }

    /** create Extrinsic Object(s) **/
    private void createExtrinsicObject() throws Exception {
        ArrayList objectsToSave = new ArrayList();
	for (int i = 0; numEOs > i; i++) {
	    // create Extrinsic Object
	    ExtrinsicObject eo =
		getLCM().createExtrinsicObject(repositoryItem);
	    objectsToSave.add(eo);

	    // make object slightly unique
	    eo.setKey(getLCM().createKey(eoId + i));
	    ((RegistryObjectImpl)eo).setLid(eoId + i);
	    eo.setName(getLCM().
		       createInternationalString(eoName + i));
	}

	// write out to Registry (and database)
        assertResponseSuccess("Unable to create Extrinsic Objects",
			      lcm.saveObjects(objectsToSave,
					      dontVersionSlotsMap));

	++iteration;
    }

    /** query for Extrinsic Object(s) **/
    private void readExtrinsicObject() throws Exception {
        // what objects are we looking for?
	ArrayList keys = new ArrayList();
	for (int i = 0; numEOs > i; i++) {
	    keys.add(getLCM().createKey(eoId + i));
	}

	// query objects created in createExtrinsicObject()
	BulkResponse resp =
	    getBQM().getRegistryObjects(keys,
					LifeCycleManager.EXTRINSIC_OBJECT);
        assertResponseSuccess("Unable to query Extrinsic Objects", resp);
	assertFalse("Partial information returned in query",
		    resp.isPartialResponse());

	// check returned collection is as large as we expect
	Collection objs = resp.getCollection();
	assertNotNull("Unexpected <null> collection returned", objs);
	int cnt = objs.size();
	assertTrue("Found " + cnt + " Extrinsic Objects.  Expected " + numEOs,
		   numEOs == cnt);

	// check each item in the returned collection
	for (Iterator iter = objs.iterator(); iter.hasNext(); ) {
	    // get Extrinsic Object
	    ExtrinsicObject eo = (ExtrinsicObject)iter.next();
	    assertNotNull("Extrinsic Object missing", eo);

	    String name = eo.getName().getValue();
	    // get Repository Item
	    DataHandler handler = eo.getRepositoryItem();
	    assertNotNull(name + "missing RepositoryItem",
			  handler);

	    // compare RI content just returned with the original resource
	    assertTrue(name + " has changed Repository Item content",
		       Arrays.equals(blobContent,
				     readBytes(handler.getInputStream())));
	}
    }

    /** delete Extrinsic Object(s) **/
    private void deleteExtrinsicObject() throws Exception {
        // delete the objects that were created in createExtrinsicObject()
	ArrayList keys = new ArrayList();
	for (int i = 0; numEOs > i; i++) {
	    keys.add(getLCM().createKey(eoId + i));
	}
        assertResponseSuccess("Unable to delete Extrinsic Objects",
			      getLCM().deleteObjects(keys,
						     LifeCycleManager.
						     EXTRINSIC_OBJECT));

	// confirm deletions
	for (int i = 0; numEOs > i; i++) {
	    ExtrinsicObject eo = (ExtrinsicObject)getBQM().
		getRegistryObject(eoId + i, LifeCycleManager.EXTRINSIC_OBJECT);
	    assertNull("Extrinsic Object " + i + " not deleted correctly", eo);
	}

	System.out.println("deleteExtrinsicObject() " + iteration);
    }

    /** Create, Read, and Delete Extrinsic Object(s); CReD, no Update */
    public void testExtrinsicObjectCReD() throws Exception {
	createExtrinsicObject();
	readExtrinsicObject();
	deleteExtrinsicObject();
    }
}
