/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Rm.java,v 1.11 2005/06/07 01:57:10 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import org.apache.tools.ant.types.selectors.SelectorUtils;

import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminFunctionContext;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.client.xml.registry.util.QueryUtil;
import org.freebxml.omar.common.Utility;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


public class Rm extends AbstractAdminFunction {
    boolean deleteAnyway = false;
    boolean deleteRecursively = false;

    public void execute(AdminFunctionContext context, String args)
        throws Exception {
        this.context = context;

        if (args == null) {
            context.printMessage(format(rb,"argumentRequired"));

            return;
        }

        String[] tokens = args.split("\\s+");

        int tIndex = 0;

        for (tIndex = 0;
                ((tIndex < tokens.length) && tokens[tIndex].startsWith("-"));
                tIndex++) {
            String option = tokens[tIndex];

            if (collator.compare(option, "-d") == 0) {
                deleteAnyway = true;
            } else if (collator.compare(option, "-r") == 0) {
                deleteRecursively = true;
            } else {
                context.printMessage(format(rb,"invalidArgument",
					    new Object[] { option }));
            }
        }

        Collection selectedRO = selectAllMembers();

        if (context.getDebug()) {
            context.printMessage(format(rb,"selectingMatchingMembers"));
        }

        HashSet membersToDelete = new HashSet();

        // Number of objects matched by command.
        int objectCount = 0;

        Iterator iter = selectedRO.iterator();

        while (iter.hasNext()) {
            RegistryObject ro = (RegistryObject) iter.next();
            String roName = ro.getName().getValue();

            // tIndex is at position after any option tokens
            for (int index = tIndex; index < tokens.length; index++) {
                String pattern = tokens[index];

                // Match against pattern as either ID or globbing pattern.
                // Currently can't match against null names.

                if ((Utility.getInstance().isValidURN(pattern) &&
                        ro.getKey().getId().equals(pattern)) ||
                        (!Utility.getInstance().isValidURN(pattern) &&
                        (roName != null) &&
                        SelectorUtils.match(pattern, roName))) {
                    if (context.getDebug()) {
                        context.printMessage(ro.getKey().getId() + "  " +
                            ro.getName());
                    }

                    objectCount++;

                    membersToDelete.add(ro);

                    continue;
                }
            }
        }

        context.printMessage(format(rb,"objectsFound",
				    new Object[] {
					    new Integer(objectCount) }));

        // Nothing to do if there were no matching objects
        if (membersToDelete.isEmpty()) {
            if (context.getVerbose() || context.getDebug()) {
                context.printMessage(format(rb,"nothingToDelete"));
            }

            return;
        }

        // Full set of objects to delete (abbreviated as 'otd')
        Collection otdColl;

        if (deleteAnyway) {
            otdColl = deleteMembersAnyway(membersToDelete);
        } else {
            otdColl = deleteMembers(membersToDelete,
                    (context.getCurrentRP() == null) ? null
                                                     : context.getCurrentRP()
                                                              .getKey().getId());
        }

        if (context.getVerbose() || context.getDebug()) {
            if (context.getDebug()) {
                context.printMessage(format(rb,"listingObjectsToDelete"));
            }

            Iterator debugIter = otdColl.iterator();

            while (debugIter.hasNext()) {
                Key key = (Key) debugIter.next();

                context.printMessage(key.getId());
            }

            context.printMessage(format(rb,"objectsFound",
					new Object[] {
						new Integer(otdColl.size())}));
        }

        BulkResponse resp = context.getService().getLCM().deleteObjects(otdColl);

        JAXRUtility.checkBulkResponse(resp);
    }

    /**
     * Selects for deletion each <code>RegistryObject</code> in
     * <code>membersToDelete</code>, if it is allowed.  Also selects
     * for deletion any <code>Assocation</code> objects that should be
     * deleted.  When <code>deleteRecursively</code> is
     * <code>true</code>, selects for deletion all descendants of any
     * <code>RegistryPackage</code>, if it is allowed.
     *
     * @param membersToDelete a <code>Collection</code> of
     * <code>RegistyObject</code>
     * @param parentRPId ID of parent <code>RegistryPackage</code>
     * @return a <code>Collection</code> of objects to delete
     * @exception Exception if an error occurs
     */
    Collection deleteMembers(Collection membersToDelete, String parentRPId)
        throws Exception {
        HashSet otdColl = new HashSet();

        Iterator mtdIter = membersToDelete.iterator();

        // Iterate through the members to see which can really be deleted
        while (mtdIter.hasNext()) {
            RegistryObject member = (RegistryObject) mtdIter.next();

            Collection memberMembers = null;

            // The only time to *not* delete the association between the
            // current RP and a member is when the member is an RP that itself
            // has members and deleteRecursively is false.
            if (member instanceof RegistryPackage) {
                memberMembers = ((RegistryPackage) member).getRegistryObjects();

                if ((memberMembers.size() != 0) && !deleteRecursively) {
                    context.printMessage(format(rb,"nonEmptyRP",
						new Object[] {
							member.getKey().getId()
						}));

                    continue;
                }
            }

            // Select for deletion all associations, not just
            // 'HasMember' associations, between the current RP and
            // the member.
            Collection currentRPMemberAssoc = selectRPMemberAssociations(parentRPId,
                    member.getKey().getId());

            otdColl.addAll(JAXRUtility.getKeysFromObjects(currentRPMemberAssoc));

            // 'Other associations' is every association except any
            // associations to the parent RP and except any
            // 'HasMember' associations where the current member is
            // the source.
            Collection otherAssoc = selectOtherAssociations(member.getKey()
                                                                  .getId(),
                    currentRPMemberAssoc);

            if (otherAssoc.size() != 0) {
                if (doNotDeleteMember(member, memberMembers, otherAssoc)) {
                    if (context.getVerbose() || context.getDebug()) {
                        context.printMessage(format(rb,
						    "hasOtherAssociations",
						    new Object[] {
							    member.getKey().getId()
						    }));
                    }

                    continue;
                }
            }

            otdColl.add(member.getKey());

            if (deleteRecursively && (memberMembers != null)) {
                otdColl.addAll(deleteMembers(memberMembers,
                        member.getKey().getId()));
            }
        }

        return otdColl;
    }

    /**
     * Selects for deletion each <code>RegistryObject</code> in
     * <code>membersToDelete</code> and all <code>Assocation</code>
     * objects associated with each <code>RegistryObject</code>.  When
     * <code>deleteRecursively</code> is <code>true</code>, selects
     * for deletion all descendants of any
     * <code>RegistryPackage</code> and all their related
     * <code>Association</code> objects.
     *
     * @param membersToDelete a <code>Collection</code> of
     * <code>RegistyObject</code>
     * @return a <code>Collection</code> of objects to delete
     * @exception Exception if an error occurs
     */
    Collection deleteMembersAnyway(Collection membersToDelete)
        throws Exception {
        if (context.getDebug()) {
            context.printMessage(format(rb,"deletingAnyway"));
        }

        HashSet otdColl = new HashSet();

        Iterator mtdIter = membersToDelete.iterator();

        // Iterate through the members to see which can really be deleted
        while (mtdIter.hasNext()) {
            RegistryObject member = (RegistryObject) mtdIter.next();

            String memberAssocQueryStr = "SELECT ass.* " +
                "FROM Association ass WHERE " + "ass.targetObject = '" +
                member.getKey().getId() + "' OR ass.sourceObject = '" +
                member.getKey().getId() + "'";

            if (context.getDebug()) {
                context.printMessage(format(rb,"selectingAllAssociations",
					    new Object[] {
						    member.getKey().getId()}));
            }

            Collection memberAssoc = queryRO(context, memberAssocQueryStr);

            otdColl.add(member.getKey());
            otdColl.addAll(JAXRUtility.getKeysFromObjects(memberAssoc));

            if (deleteRecursively && member instanceof RegistryPackage) {
                otdColl.addAll(deleteMembersAnyway(
                        ((RegistryPackage) member).getRegistryObjects()));
            }
        }

        return otdColl;
    }

    Collection queryRO(AdminFunctionContext context, String queryStr)
        throws Exception {
        if (context.getDebug()) {
            context.printMessage(queryStr);
        }

        javax.xml.registry.Query query = context.getService().getDQM()
                                                .createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL,
                queryStr);

        // make JAXR request
        BulkResponse resp = context.getService().getDQM().executeQuery(query);

        JAXRUtility.checkBulkResponse(resp);

        Collection registryObjects = resp.getCollection();

        // Print query and results if debugging
        if (context.getDebug()) {
            Iterator debugIter = registryObjects.iterator();

            while (debugIter.hasNext()) {
                RegistryObject ro = (RegistryObject) debugIter.next();

                context.printMessage(ro.getKey().getId() + "  " + ro.getName());
            }

            context.printMessage(format(rb,"objectsFound",
					new Object[] {
						new Integer(registryObjects.size()) }));
        }

        return registryObjects;
    }

    public String getUsage() {
        return format(rb, "usage.rm");
    }

    /**
     * Select all members of the current RegistryPackage (or all
     * RegistryObject that don't have a 'HasMember' Association if the
     * current RegistryPackage is the root).
     */
    Collection selectAllMembers() throws Exception {
        String queryStr;

        if (context.getCurrentRP() == null) {
            queryStr = "SELECT ro.* from RegistryObject ro WHERE " +
                "ro.id NOT IN (SELECT targetObject FROM Association) OR " +
                "ro.id IN (SELECT DISTINCT targetObject FROM Association " +
                "WHERE associationType != '" +
                bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember + "')";
        } else {
            queryStr = "SELECT DISTINCT ro.* " +
                "FROM RegistryObject ro, RegistryPackage p, " +
                "Association ass WHERE ((p.id = '" +
                context.getCurrentRP().getKey().getId() +
                "') AND (ass.associationType='" +
                bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
                "' AND ass.sourceObject = p.id AND ass.targetObject = ro.id)) ";
        }

        return queryRO(context, queryStr);
    }

    /**
     * Determines if cannot delete member because of its associations
     * other than its 'HasMember' associations to its parent
     * RegistryPackage.
     *
     * This code assumes that deleteRecursively is true, i.e., that
     * it's okay to delete member if it's a RegistryPackage that has
     * members of its own.
     */
    boolean doNotDeleteMember(RegistryObject member, Collection memberMembers,
        Collection otherAssoc) throws Exception {
        boolean doNotDeleteMember = false;

        // 'Other associations' may be 'HasMember'
        // associations of RegistryPackage.  Can delete a
        // RegistryPackage only if all of its 'other'
        // associations are 'HasMember' associations where it
        // is the sourceObject.
        if (member instanceof RegistryPackage) {
            // If other associations but no members, really has other
            // associations
            if (memberMembers.size() == 0) {
                doNotDeleteMember = true;
            } else {
                // Find the 'HasMember' associations of member.
                String memberMembersAssocQueryStr = "SELECT ass.* " +
                    "FROM Association ass WHERE " + "(ass.sourceObject = '" +
                    member.getKey().getId() + "' AND ass.associationType = '" +
                    bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember + "')";

                // Make this collection a type that implements removeAll()
                HashSet memberMembersAssoc = new HashSet(queryRO(context,
                            memberMembersAssocQueryStr));

                // If collection sizes don't match, then
                // member really has other associations.
                if (memberMembersAssoc.size() != otherAssoc.size()) {
                    doNotDeleteMember = true;
                } else {
                    // Get difference between other associations
                    // and 'HasMember' associations.
                    memberMembersAssoc.removeAll(otherAssoc);

                    // If there are any differences, there'll be
                    // objects left in memberMemberAssoc.  If so,
                    // can't delete member.
                    if (memberMembersAssoc.size() != 0) {
                        doNotDeleteMember = true;
                    }
                }
            }
        } else {
            doNotDeleteMember = true;
        }

        return doNotDeleteMember;
    }

    /**
     * Selects the <code>Association</code> objects that have
     * parentRPId as sourceObject and member as targetObject.  This
     * selects <em>all</em> types of associations, not just
     * 'HasMember' associations.
     *
     * @param parentRPId ID of the parent RegistryPackage of member
     * @param memberId ID of a member of parent RegistryPackage
     * @return a <code>Collection</code> of the
     * <code>Association</code> objects
     * @exception Exception if an error occurs
     */
    Collection selectRPMemberAssociations(String parentRPId, String memberId)
        throws Exception {
        String currentRPMemberAssocQueryStr = "SELECT ass.* " +
            "FROM Association ass WHERE " + "(ass.sourceObject = '" +
            parentRPId + "' AND ass.targetObject = '" + memberId +
            "') OR (ass.targetObject = '" + parentRPId +
            "' AND ass.sourceObject = '" + memberId + "')";

        if (context.getDebug()) {
            context.printMessage(format(rb,"selectingMemberAssociations",
					new Object[] { parentRPId, memberId}));
        }

        return queryRO(context, currentRPMemberAssocQueryStr);
    }

    /**
     * Selects associations other than between memberId and its parent
     * RegistryPackage.
     *
     * @param parentRPId ID of the parent RegistryPackage of member
     * @param memberId ID of a member of parent RegistryPackage
     * @param currentRPMemberAssoc a <code>Collection</code> value
     * @return a <code>Collection</code> of the
     * <code>Association</code> objects
     * @exception Exception if an error occurs
     */
    Collection selectOtherAssociations(String memberId,
        Collection currentRPMemberAssoc) throws Exception {
        // 'Other associations' is every association except any
        // associations to the parent RP
        String otherAssocQueryStr = "SELECT DISTINCT ass.* " +
            "FROM Association ass WHERE " + "(ass.sourceObject = '" + memberId +
            "' OR " + "ass.targetObject = '" + memberId + "')";

        if (currentRPMemberAssoc.size() != 0) {
            otherAssocQueryStr += " AND (";

            Iterator crmIter = currentRPMemberAssoc.iterator();

            while (crmIter.hasNext()) {
                String assocID = ((RegistryObject) crmIter.next()).getKey()
                                  .getId();
                otherAssocQueryStr += ("ass.id != '" + assocID + "'");

                if (crmIter.hasNext()) {
                    otherAssocQueryStr += " AND ";
                }
            }

            otherAssocQueryStr += ")";
        }

        if (context.getDebug()) {
            context.printMessage(format(rb,"selectingOtherAssociations",
					new Object[] { memberId }));
        }

        return queryRO(context, otherAssocQueryStr);
    }
}
