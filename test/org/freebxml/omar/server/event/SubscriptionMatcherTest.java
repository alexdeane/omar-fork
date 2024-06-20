/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2002 freebxml.org. All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/server/event/SubscriptionMatcherTest.java,v 1.2 2006/03/22 18:10:26 doballve Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.common.ServerTest;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.rim.AdhocQuery;
import org.oasis.ebxml.registry.bindings.rim.QueryExpression;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackage;
import org.oasis.ebxml.registry.bindings.rim.Subscription;

/**
 * @author Diego Ballve / Digital Artefacts
 */
public class SubscriptionMatcherTest extends ServerTest {

    public SubscriptionMatcherTest(String name) {
        super(name);
    }
        
    public static Test suite() {
        return new TestSuite(SubscriptionMatcherTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    /*
     * Gets the List of Subscriptions that definitely match the specified event.
     */
    public void testGetMatchedSubscriptionsMap() throws Exception {
        final String CONTEXT_ID = "SubscriptionMatcherTest:testGetMatchedSubscriptionsMap:";
        
        final String pkg1Id = "urn:org:freebxml:omar:server:event:SubscriptionMatcherTest:testGetMatchedSubscriptionsMap:pkg1";
        final String pkg2Id = "urn:org:freebxml:omar:server:event:SubscriptionMatcherTest:testGetMatchedSubscriptionsMap:pkg2";
        final String subscripId = "urn:org:freebxml:omar:server:event:SubscriptionMatcherTest:testGetMatchedSubscriptionsMap:subscription";
        final String selQueryId = "urn:org:freebxml:omar:server:event:SubscriptionMatcherTest:testGetMatchedSubscriptionsMap:selectorQuery";

        // final clean-up
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), pkg1Id);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), pkg2Id);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), subscripId);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), selQueryId);
        
        // Selector query without event id, limited to test objects
        String sqlQuerySel = "SELECT DISTINCT ro.* FROM RegistryObject ro, AuditableEvent e, AffectedObject ao"
            + " WHERE e.id = ''$currentEventId''"
            + " AND e.eventType = ''" + BindingUtility.CANONICAL_EVENT_TYPE_ID_Updated + "''"
            + " AND ao.id = ro.id AND ao.eventId = e.id"
            + " AND ro.id like ''urn:org:freebxml:omar:server:event:SubscriptionMatcherTest:testGetMatchedSubscriptionsMap:pkg%''";

        // Create subscription and selector query
        ServerRequestContext context2 = new ServerRequestContext(CONTEXT_ID + "2", null);
        context2.setUser(AuthenticationServiceImpl.getInstance().farrukh);

        AdhocQuery selQuery = bu.rimFac.createAdhocQuery();
        selQuery.setId(selQueryId);
        QueryExpression queryExp = bu.rimFac.createQueryExpression();
        queryExp.setQueryLanguage(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92);
        queryExp.getContent().add(sqlQuerySel);
        selQuery.setQueryExpression(queryExp);

        Subscription subscript = bu.rimFac.createSubscription();
        subscript.setId(subscripId);
        subscript.setSelector(selQueryId);

        ArrayList objects = new ArrayList();
        objects.add(selQuery);
        objects.add(subscript);
        submit(context2, objects);

        // Create reg packs
        ServerRequestContext context3 = new ServerRequestContext(CONTEXT_ID + "3", null);
        context3.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        RegistryPackage pkg1 = bu.rimFac.createRegistryPackage();
        pkg1.setId(pkg1Id);
        RegistryPackage pkg2 = bu.rimFac.createRegistryPackage();
        pkg2.setId(pkg2Id);
        objects.clear();
        objects.add(pkg1);
        objects.add(pkg2);
        submit(context3, objects);

        // check creation event, expect no match
        SubscriptionMatcher matcher = new SubscriptionMatcher();
        Map subMap = matcher.getMatchedSubscriptionsMap(context3, context3.getUpdateEvent());
        assertTrue("Expecting no subscription match.", subMap.isEmpty());

        // modify pkg1
        ServerRequestContext context4 = new ServerRequestContext(CONTEXT_ID + "4", null);
        context4.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        submit(context4, pkg1);

        // check update event, expect 1 match for pkg1
        subMap = matcher.getMatchedSubscriptionsMap(context4, context4.getUpdateEvent());
        assertEquals("Wrong number of subscriptions matched.", 1, subMap.size());
        assertEquals("Wrong subscriptions matched.", subscripId, ((Subscription)subMap.keySet().iterator().next()).getId());
        assertEquals("Wrong number of objects matched.", 1, ((Collection)subMap.values().iterator().next()).size());
        assertEquals("Wrong object matched.", pkg1Id, ((RegistryPackage)((Collection)subMap.values().iterator().next()).iterator().next()).getId());

        // modify pkg2
        ServerRequestContext context5 = new ServerRequestContext(CONTEXT_ID + "5", null);
        context5.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        submit(context5, pkg2);

        // check update event, expect only 1 match for pkg2
        subMap = matcher.getMatchedSubscriptionsMap(context5, context5.getUpdateEvent());
        assertEquals("Wrong number of subscriptions matched.", 1, subMap.size());
        assertEquals("Wrong subscriptions matched.", subscripId, ((Subscription)subMap.keySet().iterator().next()).getId());
        assertEquals("Wrong number of objects matched.", 1, ((Collection)subMap.values().iterator().next()).size());
        assertEquals("Wrong object matched.", pkg2Id, ((RegistryPackage)((Collection)subMap.values().iterator().next()).iterator().next()).getId());

        // final clean-up
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), pkg1Id);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), pkg2Id);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), subscripId);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), selQueryId);
    }    
    
}
