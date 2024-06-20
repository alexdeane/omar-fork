/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/MultiRegistrySystemTest.java,v 1.3 2005/06/10 17:38:21 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.util.ArrayList;
import java.util.Set;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Service;

import junit.framework.Test;

import org.freebxml.omar.client.common.ClientTest;
import org.freebxml.omar.client.xml.registry.infomodel.FederationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.IdentifiableImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryObjectRef;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;


/**
 * Base class for Client side System tests for testing multi-registry features.
 * Requires that there be 2 instance of registry deployed under omar.name of "omar" and "omar1"
 *
 * @author Farrukh Najmi
 */
public abstract class MultiRegistrySystemTest extends ClientTest {
    
    protected static Connection connection1 = null;
    protected static DeclarativeQueryManagerImpl dqm1 = null;
    protected static BusinessQueryManager bqm1 = null;
    protected static BusinessLifeCycleManagerImpl lcm1 = null;

    protected static Connection connection2 = null;
    protected static DeclarativeQueryManagerImpl dqm2 = null;
    protected static BusinessQueryManager bqm2 = null;
    protected static BusinessLifeCycleManagerImpl lcm2 = null;
    
    private static FederationImpl federation = null;
    private static FederationImpl federationReplica = null;
    protected static RegistryImpl registry1 = null;
    protected static RegistryImpl registry2 = null;
    protected static RegistryImpl registry1Replica = null;
    protected static RegistryImpl registry2Replica = null;
    
    protected static Service service1 = null;
    protected static Service service2 = null;
    protected static Service service1Replica = null;
    protected static Service service2Replica = null;

    protected final String regSoapUrl2 = regSoapUrl.replaceFirst("/omar","/omar1");
    
    /** Creates a new instance of FederationTest */
    public MultiRegistrySystemTest(String name) {
        super(name, false);                 
    }
    
    
    /**
     * Tests getting of Registry instance for the two test registries.
     */
    public void testGetRegistries() throws Exception {
        if (connection1 == null) {
            String keystorePath1 = RegistryProperties.getInstance().getProperty("omar.security.keystoreFile");
            String keystorePath2 = keystorePath1.replaceFirst("omar", "omar1"); //Need to improve this in future
            connection1 = getConnection(AuthenticationServiceImpl.ALIAS_FARRUKH, 
		regSoapUrl,
                keystorePath1);
            dqm1 = (DeclarativeQueryManagerImpl)connection1.getRegistryService().getDeclarativeQueryManager();
            bqm1 = connection1.getRegistryService().getBusinessQueryManager();
            lcm1 = (BusinessLifeCycleManagerImpl)connection1.getRegistryService().getBusinessLifeCycleManager();
            
            connection2 = getConnection(AuthenticationServiceImpl.ALIAS_FARRUKH, 
                regSoapUrl2,
                keystorePath2);
            dqm2 = (DeclarativeQueryManagerImpl)connection2.getRegistryService().getDeclarativeQueryManager();
            bqm2 = connection2.getRegistryService().getBusinessQueryManager();
            lcm2 = (BusinessLifeCycleManagerImpl)connection2.getRegistryService().getBusinessLifeCycleManager();            
        }

        Query query = dqm1.createQuery(Query.QUERY_TYPE_SQL, "SELECT r.* FROM Registry r WHERE (r.home IS NULL OR r.home = '" + regSoapUrl + "')");
        BulkResponse br = dqm1.executeQuery(query);
        assertResponseSuccess("Query to get registry1 failed.", br);                
        assertTrue("Query for registry1 did not return exactly 1 object.", (br.getCollection().size() == 1));
        registry1 = (RegistryImpl)br.getCollection().iterator().next();
        
        query = dqm2.createQuery(Query.QUERY_TYPE_SQL, "SELECT r.* FROM Registry r WHERE (r.home IS NULL OR r.home = '" + regSoapUrl2 + "')");
        br = dqm2.executeQuery(query);
        assertResponseSuccess("Query to get registry2 failed.", br);                
        assertTrue("Query for registry1 did not return exactly 1 object.", (br.getCollection().size() == 1));
        registry2 = (RegistryImpl)br.getCollection().iterator().next();        
    }
    
    /*
     * Saves a specified and returns the Identifiable after reading it back
     * from registry.
     */
    public IdentifiableImpl saveAndGetIdentifiable(IdentifiableImpl obj) throws Exception {
        ArrayList objects = new ArrayList();
        objects.add(obj);
        LifeCycleManager _lcm = obj.getLifeCycleManager();
        DeclarativeQueryManager _dqm = obj.getDeclarativeQueryManager();
        BulkResponse br = _lcm.saveObjects(objects); // publish to registry1
        assertResponseSuccess("Error during save of object to registry", br);        
        
        //read back obj from registry
        obj = (IdentifiableImpl)_dqm.getRegistryObject(obj.getKey().getId());
        assertNotNull("object could not be read back after save", obj);
        
        return obj;
     }
        
}
