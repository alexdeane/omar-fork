/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/RegistryObjectRef.java,v 1.13 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.lang.ref.Reference;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryObject;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.RegistryServiceImpl;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;


/**
 * Holds a direct or indirect reference to a RegistryObject
 * TODO: Add to JAXR 2.0 as new interface
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class RegistryObjectRef extends IdentifiableImpl {
    private Object ref = null;

    public RegistryObjectRef(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);
    }

    public RegistryObjectRef(LifeCycleManagerImpl lcm, ObjectRefType ebObject) throws JAXRException {
        super(lcm, ebObject);
    }

    public RegistryObjectRef(LifeCycleManagerImpl lcm, Object obj) throws JAXRException {
        super(lcm);
        String _id = null;
        String _home = null;
        if (obj instanceof RegistryObjectImpl) {
            RegistryObjectImpl ro = ((RegistryObjectImpl) obj);
            _id = ro.getKey().getId();
            _home = ro.getHome();
            if (ro.isNew() || ro.isModified()) {
                //System.err.println("RegistryObjectRef: for object id:" + _id + " new:" + ro.isNew() + " modified:" + ro.isModified());
                this.ref = ro;
            } else {
                ((RegistryServiceImpl) lcm.getRegistryService()).getObjectCache()
                 .putRegistryObject(ro);
            }
        } else if (obj instanceof IdentifiableType) {
            _id = ((IdentifiableType) obj).getId();
            _home = ((IdentifiableType) obj).getHome();
        } else if (obj instanceof String) {
            _id = ((String) obj);
            /* Following code is useful in debugging Unresolved reference problems and should not be removed.
            try {
                String queryStr = "SELECT * FROM RegistryObject WHERE id = '" +
                    (String)obj + "'";
                Query query = lcm.getRegistryService().getDeclarativeQueryManager().createQuery(Query.QUERY_TYPE_SQL, queryStr);
                AdhocQueryRequest req = ((QueryImpl)query).toBindingObject();
                UserType user = null;

                QueryManagerSOAPProxy serverQMProxy = new QueryManagerSOAPProxy(
                    ((ConnectionImpl)((RegistryServiceImpl)lcm.getRegistryService()).getConnection()).getQueryManagerURL(),
                    null);
                AdhocQueryResponseType resp = serverQMProxy.submitAdhocQuery(user, req);
                RegistryObjectListType sqlResult = resp.getSQLQueryResult();
                List items = sqlResult.getIdentifiable();
                Iterator iter = items.iterator();
                if (iter.hasNext()) {
                    IdentifiableType it = (IdentifiableType)iter.next();
                    if (it == null) {
                        throw new JAXRException("Got a null from query.");
                    } else {
                        System.err.println("Adding String for object. id:" + it.getId() + " class:" + it.getClass());
                    }
                } else {
                    throw new JAXRException("Cannot add string: '" + _id + "' to cache if it is not resolvable.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            */
        } else {
            JAXRException e = new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.unexpected.object", new Object[] {obj}));
            e.printStackTrace();
            throw e;
        }

        if (_id == null) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.reference.id.null"));
        }

        KeyImpl key = new KeyImpl(lcm);
        key.setId(_id);
        this.setKey(key);

        setHome(_home);
        //System.err.println("Created RegistryObjectRef for object of type: " + obj.getClass() + " id:" + _id + " object:" + obj.toString());


    }

    /*
     * Must be called only if ref is null or a Reference.
     *
     */
    private Reference getReference(String objectType) throws JAXRException {
        Reference _ref = null;

        //Need to also check if ref.get() == null as referrant could have been GCed
        if ((ref == null) || (ref instanceof Reference)) {
            if ((ref != null) && ((Reference)ref).get() != null) {
                _ref = (Reference)ref;
            } else {
                _ref = ((RegistryServiceImpl) lcm.getRegistryService()).getObjectCache()
                   .getReference(getId(), objectType);
            }

            ref = _ref;

        } else {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.expected.ref.null.or.reference",new Object[] {ref.getClass()}));
        }
        return _ref;
    }

    public RegistryObject getRegistryObject(String objectType)
        throws JAXRException {
        RegistryObject ro = null;

        if ((ref == null) || (ref instanceof Reference)) {
            Reference _ref = getReference(objectType);
            ro = (RegistryObject) (_ref.get());
        } else if (ref instanceof RegistryObject) {
            ro = (RegistryObject)ref;
        } else {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.expected.ref.null.reference.registryobject",new Object[] {ref.getClass()}));
        }

        return ro;
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            org.oasis.ebxml.registry.bindings.rim.ObjectFactory factory = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
            org.oasis.ebxml.registry.bindings.rim.ObjectRef ebObjectRef = factory.createObjectRef();

            setBindingObject(ebObjectRef);

            return ebObjectRef;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }

}
