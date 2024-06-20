/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/ObjectCache.java,v 1.13 2007/03/23 18:38:59 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.RegistryObject;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.exceptions.UnresolvedReferenceException;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;


/**
 * An cache for JAXR objects fetched from the registry.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ObjectCache {
    private RegistryServiceImpl service = null;
    private HashMap idToReferenceMap = null;

    private ObjectCache() {
    }

    public ObjectCache(RegistryServiceImpl service) {
        this.service = service;
        idToReferenceMap = new HashMap(1024);
    }


    public Reference getReference(String id, String objectType)
    throws JAXRException {
        // First attemp to get cached reference
        Reference ref = (Reference) idToReferenceMap.get(id);
        //Need to also check if ref.get() == null as referrant could have been GCed
        if ((ref == null) || (ref.get() == null)) {
            //Cache miss. Get from registry
            //System.err.println("ObjectCache: cache miss for id: " + id);
            DeclarativeQueryManager dqm = service.getDeclarativeQueryManager();
            String tablename = org.freebxml.omar.common.Utility.getInstance().mapTableName(objectType);
            dqm.getRegistryObject(id, objectType);

            //executeQuery should have fetched object and put it in cache
            ref = (Reference) idToReferenceMap.get(id);

            if (ref == null) {
                throw new UnresolvedReferenceException(
                    JAXRResourceBundle.getInstance().getString("message.error.Unresolved.ref.object.id",new Object[] {id}));
                //System.err.println("Unresolved reference for object with id: " + id);
            }
        } else {
            //System.err.println("ObjectCache: cache hit for id: " + id);
        }

        return ref;
    }

    private void putReference(String id, Reference ref) {
        idToReferenceMap.put(id, ref);
    }


    public void putRegistryObject(RegistryObject ro) throws JAXRException {
        putReference(ro.getKey().getId(), new SoftReference(ro));
    }

    /**
     * Returns a RegistryObject of type 'objectType' with id  equals to 'ids'.
     *
     * @param id desired UUID (string)
     * @param objectType the desired object type (string name)
     * @return RegistryObject
     * @throw JAXRException if (id,objectType) not found or other JAXRException happens.
     */
    public RegistryObject getRegistryObject(String id, String objectType)
    throws JAXRException {
        return (RegistryObject)getReference(id, objectType).get();
    }

    /**
     * Returns a Collection of RegistryObjects of type 'objectType' with id in 'ids'.
     *
     * @param ids Collection of UUIDs (strings)
     * @param objectType the desired object type (string name)
     * @return Collection of RegistryObjects
     * @throw JAXRException if (id,objectType) not found or other JAXRException happens.
     */
    public Collection getRegistryObjects(Collection ids, String objectType)
    throws JAXRException {
        // Get cached objects to 'result' and not cached ids to 'notCached'
        Collection result = new ArrayList(ids.size());
        Collection notCached = new ArrayList();
        Iterator itIds = ids.iterator();
        while (itIds.hasNext()) {
            String id = itIds.next().toString();
            Reference ref = (Reference)idToReferenceMap.get(id);
            if ((ref != null) && (ref.get() != null)) {
                //Cache hit. Add it to result
                result.add(ref.get());
            } else {
                // Cache miss. add to notCached
                notCached.add(id);
            }
        }

        if (!notCached.isEmpty()) {
            // objects where not found in the cache. Fetch from registry.
            DeclarativeQueryManager dqm = service.getDeclarativeQueryManager();
            String tablename = org.freebxml.omar.common.Utility.getInstance().mapTableName(objectType);
            StringBuffer sb = new StringBuffer("SELECT ro.* FROM ");
            sb.append(tablename).append(" ro WHERE id IN (");
            Iterator idNotCached = notCached.iterator();
            while (idNotCached.hasNext()) {
                sb.append("'").append(idNotCached.next()).append("'");
                if (idNotCached.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            Query query = dqm.createQuery(
            Query.QUERY_TYPE_SQL, sb.toString());
            dqm.executeQuery(query).getCollection().iterator();
        }

        //executeQuery should have fetched objects and put them in cache
        Iterator idNotCached = notCached.iterator();
        while (idNotCached.hasNext()) {
            String id = idNotCached.next().toString();
            Reference ref = (Reference)idToReferenceMap.get(id);
            if (ref != null && ref.get() != null) {
                //Cache hit. Add it to result
                result.add(ref.get());
            } else {
                throw new JAXRException(
                    JAXRResourceBundle.getInstance().getString("message.error.Unresolved.ref.object.id",new Object[] {id}));
            }
        }

        return result;
    }

    /**
     * Checks if 'id' is cached.
     *
     * @param id String id to be tested.
     * @returns boolean true if object is cached. False otherwise.
     */
    public boolean isCached(String id) {
        Reference ref = (Reference) idToReferenceMap.get(id);
        //Need to also check if ref.get() == null as referrant could have been GCed
        if ((ref == null) || (ref.get() == null)) {
            return false;
        } else {
            return true;
        }
    }

}
