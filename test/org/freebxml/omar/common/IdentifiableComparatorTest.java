/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/common/IdentifiableComparatorTest.java,v 1.1 2004/11/02 10:56:13 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.common;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.freebxml.omar.common.OMARTest;

import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import java.util.Comparator;

/**
 * Tests <code>IdentifiableComparator</code>.
 */
public class IdentifiableComparatorTest extends OMARTest {
    protected Comparator comparator;
    protected IdentifiableType object1;
    protected IdentifiableType object2;
    protected IdentifiableType object3;
    protected BindingUtility bu;
                
    /**
     * Constructor for IdentifiableComparatorTest.
     *
     * @param name
     */
    public IdentifiableComparatorTest(String name) {
        super(name);

	bu = BindingUtility.getInstance();

	comparator = new IdentifiableComparator();

	try {
	    object1 = bu.rimFac.createIdentifiable();
	    object2 = bu.rimFac.createIdentifiable();
	    object3 = bu.rimFac.createIdentifiable();
	} catch (Exception e) {
	    fail("Couldn't create IdentifiableType objects.");
	}

        object1.setId(org.freebxml.omar.common.Utility.getInstance().createId());
        object2.setId(org.freebxml.omar.common.Utility.getInstance().createId());
        object3.setId(org.freebxml.omar.common.Utility.getInstance().createId());
    }
        
    public static Test suite() {
        return new TestSuite(IdentifiableComparatorTest.class);
    }

    public void testNonIdentifiable1() {
	try {
	    comparator.compare(this, object1);
	    fail("Comparator should throw exception on non-Identifiable input.");
	} catch (Exception e) {
	}
    }

    public void testNonIdentifiable2() {
	try {
	    comparator.compare(object1, this);
	    fail("Comparator should throw exception on non-Identifiable input.");
	} catch (Exception e) {
	}
    }

    public void testNullIdentifiable1() {
	try {
	    comparator.compare(null, object1);
	    fail("Comparator should throw exception on null input.");
	} catch (Exception e) {
	}
    }

    public void testNullIdentifiable2() {
	try {
	    comparator.compare(object1, null);
	    fail("Comparator should throw exception on null input.");
	} catch (Exception e) {
	}
    }

    public void testEqual() {
	assertEquals("Comparing the same service should return 0.",
		     0,
		     comparator.compare(object1,
					object1));
    }

    public void testNonEqual() {
	assertFalse("Comparing two different services should not return 0.",
		    comparator.compare(object1,
				       object2) == 0);
    }

    public void testOrdering() {
	boolean signOneWay = comparator.compare(object1,
						object2) < 0;
	boolean signOtherWay = comparator.compare(object2,
						  object1) < 0;

	assertTrue("Reversing order should give opposite sign.",
		   signOneWay == !signOtherWay);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
}
