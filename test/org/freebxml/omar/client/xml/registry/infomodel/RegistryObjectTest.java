/*
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/infomodel/RegistryObjectTest.java,v 1.10 2006/02/06 18:26:19 farrukh_najmi Exp $
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CanonicalConstants;



/**
 * jUnit Test for RegistryObject
 *
 * @author Nikita Sawant
 */
public class RegistryObjectTest extends ClientTest {
    
    static String eoId = null;
    
    public RegistryObjectTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(RegistryObjectTest.class);
        return suite;
    }
    
    /*
     * Tests the getRegistryPackages() method to ensure that it returns
     * all RegistryPackages that this object is a member of.
     */
    public void testGetRegistryPackages() throws Exception {
        //USe id of an object in minDB that is known to be a 
        //member of at least one RegistryPackage
        String memberId = CanonicalConstants.CANONICAL_USERDATA_FOLDER_ID;
        RegistryObject ro = bqm.getRegistryObject(memberId, lcm.REGISTRY_PACKAGE);
        Collection packages = ro.getRegistryPackages();
        assertTrue(packages.size() > 0);
    }
    
    public void testSetGetExternalIdentifiers() throws Exception {
        RegistryPackage pkg1 = lcm.createRegistryPackage("testSetGetExternalIdentifier.pkg1");
        ExternalIdentifier extId1 = createExternalIdentifier(bu.CANONICAL_CLASSIFICATION_SCHEME_ID_AssociationType, 
            bu.CANONICAL_ASSOCIATION_TYPE_CODE_AffiliatedWith);
        ExternalIdentifier extId2 = createExternalIdentifier(bu.CANONICAL_CLASSIFICATION_SCHEME_ID_AssociationType, 
            bu.CANONICAL_ASSOCIATION_TYPE_CODE_RelatedTo);
        Collection extIds = new ArrayList();
        extIds.add(extId1);
        extIds.add(extId2);
        pkg1.addExternalIdentifiers(extIds);
        extIds = pkg1.getExternalIdentifiers();
        assertTrue("pkg1.getExternalIdentifiers did not contain extId1", (extIds.contains(extId1)));
        assertTrue("pkg1.getExternalIdentifiers did not contain extId2", (extIds.contains(extId2)));
    }
    
    /** Test creation of an Extrinsic Object */
    public void testGetAssociatedObjects() throws Exception {
        RegistryPackage pkg1 = lcm.createRegistryPackage("RegistryObjectTest.pkg1");
        String pkg1Id = pkg1.getKey().getId();        
        
        RegistryPackage pkg2 = lcm.createRegistryPackage("RegistryObjectTest.pkg2");
        String pkg2Id = pkg2.getKey().getId();        
        
        Concept relatedToConcept = (Concept)dqm.getRegistryObject(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_RelatedTo, lcm.CONCEPT);
                
        //Add pkg2 as associatedObject of pkg1        
        Association ass = (Association)lcm.createAssociation(pkg2, relatedToConcept);
        String assId = ass.getKey().getId();        
        pkg1.addAssociation(ass);
                
        //Now save pkg1 and its nested Classification
        Collection saveObjects = new ArrayList();
        saveObjects.add(pkg1);
        BulkResponse response = lcm.saveObjects(saveObjects);
        
        //Now read back pkg1 to verify that it was saved
        pkg1 = (RegistryPackage)dqm.getRegistryObject(pkg1Id);
        assertNotNull("pkg1 was not saved", pkg1);
        
        //Now read back pkg1 to verify that it was saved
        pkg2 = (RegistryPackage)dqm.getRegistryObject(pkg2Id);
        assertNotNull("pkg2 was not saved", pkg2);
        
        //Now make sure that assObjects has pkg1
        Collection assObjects = pkg1.getAssociatedObjects();
        
        boolean found = false;
        Iterator iter = assObjects.iterator();
        while (iter.hasNext()) {
            RegistryObject ro = (RegistryObject)iter.next();
            if (ro.getKey().getId().equals(pkg2.getKey().getId())) {
                found = true;
                break;
            }
        }
        assertEquals("pkg1 does not have associated object", Boolean.TRUE, Boolean.valueOf(found));
                
                
        //Now delete pkg1, pkg2 and ass
        ArrayList deleteObjects = new ArrayList();
        deleteObjects.add(ass.getKey());
        deleteObjects.add(pkg1.getKey());
        deleteObjects.add(pkg2.getKey());
        lcm.deleteObjects(deleteObjects);
        
        //Now read back bto verify that they were deleted
        pkg1=(RegistryPackage)dqm.getRegistryObject(pkg1Id);
        assertNull("pkg1 was not deleted", pkg1);
        
        pkg2=(RegistryPackage)dqm.getRegistryObject(pkg2Id);
        assertNull("pkg1 was not deleted", pkg2);
        
        ass=(Association)dqm.getRegistryObject(assId);
        assertNull("Association was not deleted", ass);
    }
    
    /**
     * Test adding multiple ExternalLinks to multiple RegistryObjects.
     *
     * @Exception if an error occurs or an assertion fails
     */
    public void testAddExternalLinks() throws Exception {
        // Strings used for names and URIs
        final String orgName = "testAddExternalLinks:: Org";
        final String URI1 = "testAddExternalLinks/URI1";
        final String URI2 = "testAddExternalLinks/URI2";
        final String linkName = "testAddExternalLinks";

        /*
         * Predefine objects used in test.
         */

        // Organization 1.
        Organization org1 = createOrganization(orgName + "1");
        org1.setKey(lcm.createKey());

        String org1Id = org1.getKey().getId();

        // Organization 2.
        Organization org2 = createOrganization(orgName + "2");
        org2.setKey(lcm.createKey());

        String org2Id = org2.getKey().getId();

        // External link A.
        ExternalLink extLinkA = lcm.createExternalLink(URI1,
                (InternationalString) null);
        extLinkA.setKey(lcm.createKey());

        String extLinkAId = extLinkA.getKey().getId();

        // External link B.
        ExternalLink extLinkB = lcm.createExternalLink(URI2,
                (InternationalString) null);
        extLinkB.setKey(lcm.createKey());

        String extLinkBId = extLinkB.getKey().getId();

        // External link C is just like ExternalLink B with an added name.
        ExternalLink extLinkC = lcm.createExternalLink(URI2,
                (InternationalString) null);
        extLinkC.setName(lcm.createInternationalString(linkName));
        extLinkC.setKey(lcm.createKey());

        String extLinkCId = extLinkC.getKey().getId();

        // External link that is not added to anything
        ExternalLink noOrgExtLink = lcm.createExternalLink("testAddExternalLinks/noOrg",
                (InternationalString) null);
        noOrgExtLink.setKey(lcm.createKey());

        String noOrgExtLinkId = noOrgExtLink.getKey().getId();

        /*
         * Pre-define External Link collections used in tests.
         */

        // extLinkA + extLinkB
        final Collection abColl = new ArrayList(2);
        abColl.add(extLinkA);
        abColl.add(extLinkB);

        // extLinkA + extLinkC
        final Collection acColl = new ArrayList(2);
        acColl.add(extLinkA);
        acColl.add(extLinkC);

        /*
         * Add selected External Links to Organizations
         */
        org1.addExternalLinks(abColl);
        org2.addExternalLinks(acColl);

        Collection savedObjs;

        try {
            /*
             * Submit the objects.
             */
            Collection objs = new ArrayList();
            objs.add(org1);
            objs.add(org2);
            objs.add(noOrgExtLink);

            BulkResponse br = lcm.saveObjects(objs);
            assertResponseSuccess("Saving objects should succeed.", br);

            // Save the saved objects' keys for deletion later.
            savedObjs = new ArrayList(br.getCollection());

            /*
             * Compare objects as saved to what is expected.
             */
            ExternalLink savedExtLinkC = (ExternalLink) dqm.getRegistryObject(extLinkCId);
            assertNotNull("Saved 'extLinkC' External Link should not be null.",
                savedExtLinkC);
            assertEquals("'extLinkC' External Link should have one linked object.",
                1, savedExtLinkC.getLinkedObjects().size());

            Iterator extLinkCIter = savedExtLinkC.getLinkedObjects().iterator();
            RegistryObject ro = (RegistryObject) extLinkCIter.next();
            assertTrue("Linked object should be an Organization.",
                ro instanceof Organization);
            assertEquals("Organization should be 'org2'.", org2Id,
                ro.getKey().getId());

            ExternalLink savedExtLinkB = (ExternalLink) dqm.getRegistryObject(extLinkBId);
            assertNotNull("Saved 'extLinkB' External Link should not be null.",
                savedExtLinkB);
            assertEquals("'extLinkB' External Link should have one linked object.",
                1, savedExtLinkB.getLinkedObjects().size());

            ExternalLink savedExtLinkA = (ExternalLink) dqm.getRegistryObject(extLinkAId);
            assertNotNull("Saved 'extLinkA' External Link should not be null.",
                savedExtLinkA);
            assertEquals("'extLinkA' External Link should have two linked objects.",
                2, savedExtLinkA.getLinkedObjects().size());

            ExternalLink savedNoOrgExtLink = (ExternalLink) dqm.getRegistryObject(noOrgExtLinkId);
            assertNotNull("Saved 'noOrg' External Link should not be null.",
                savedNoOrgExtLink);
            assertEquals("'noOrg' External Link should have zero linked objects.",
                0, savedNoOrgExtLink.getLinkedObjects().size());
        } finally {
            //BulkResponse br = lcm.deleteObjects(savedObjs);
            //assertResponseSuccess("Deleting objects at end of test should succeed.", br);
        }
    }
    
    /**
     * Test the isModified() method of RegistryObjects's InternationalStrings.
     */
    public void testIsModified() throws Exception {
        System.out.println("testIsModified");

        // Use a RegistryPackage for tests
        RegistryObjectImpl ro = (RegistryObjectImpl)lcm.createRegistryPackage("TestPackage");
        assertModified(ro);

        // add name
        ro.setName(lcm.createInternationalString(Locale.ENGLISH, "name"));
        assertModified(ro);

        // add description
        ro.setDescription(lcm.createInternationalString(Locale.ENGLISH, "desc"));
        assertModified(ro);

        // modify IS
        ro.getName().setValue("new name");
        assertModified(ro);

        // set same value to IS
        ro.getName().setValue("new name");
        assertTrue(!ro.isModified());        

        // modify IS
        ((LocalizedString)ro.getName().getLocalizedStrings().iterator().next()).setValue("new name 2");
        assertModified(ro);

        // add LS to IS
        ro.getName().addLocalizedString(lcm.createLocalizedString(Locale.CANADA, "Name"));
        assertModified(ro);

        // remove LS from IS
        ro.getDescription().removeLocalizedString((LocalizedString)ro.getDescription()
            .getLocalizedStrings().iterator().next());
        assertModified(ro);
    }
    
    // used by test modified
    private void assertModified(RegistryObjectImpl ro) throws Exception {
        assertTrue(ro.isModified());
        ro.setModified(false);
        assertTrue(!ro.isModified());        
    }

    public static void main(String[] args) {
	System.out.println("Get into the program...\n");
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}
