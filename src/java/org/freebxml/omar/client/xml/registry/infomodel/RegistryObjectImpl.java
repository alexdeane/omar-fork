/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/RegistryObjectImpl.java,v 1.63 2007/05/04 15:46:18 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryEntry;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CanonicalConstants;
import org.freebxml.omar.common.exceptions.UnresolvedReferenceException;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;
import org.freebxml.omar.client.xml.registry.RegistryServiceImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;

/**
 * Implements JAXR API interface named RegistryObject.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public abstract class RegistryObjectImpl extends IdentifiableImpl
    implements RegistryObject {

    private static final Log log = LogFactory.getLog(RegistryObjectImpl.class);

    protected InternationalString description = null;
    protected InternationalString name = null;
    protected String lid = null;
    User newObjOwner = null;

    /** The ObjectRef to the ObjectType Concept */
    protected RegistryObjectRef objectTypeRef = null;

    protected RegistryObjectRef statusRef = null;

    // lookup map for related object types
    protected Map relatedObjectTypesLookup = new HashMap();

    /** Composed objects */
    protected Collection classifications = new ArrayList();
    protected Collection externalIds = new ArrayList();

    protected VersionInfoType versionInfo = null;

    /** Even though in JAXR Association-s are non-composed objects, their
    *         save behavior should be similar to composed objects. */
    protected Collection associations = null;

    //Following are collection of non-composed objects that are cached by this
    //implementation for performance efficiency. They are initialized on first access.
    protected HashSet externalLinks = null;
    protected Collection packages = null;

    private Organization org = null;

    RegistryObjectImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);

        String str = getClass().getName();

        String clazzName = this.getClass().getName();
        if (clazzName.startsWith("org.freebxml.omar.client.xml.registry.infomodel")) {
            objectTypeRef = lcm.getObjectTypeRefFromJAXRClassName(clazzName);
        }
        lid = getKey().getId();
        newObjOwner = null;
        ((RegistryServiceImpl)(lcm.getRegistryService())).getObjectCache().putRegistryObject(this);
    }

    RegistryObjectImpl(LifeCycleManagerImpl lcm, RegistryObjectType ebObject)
        throws JAXRException {
        // Pass ebObject to superclass so slot-s can be initialized
        super(lcm, ebObject);

        lid = ebObject.getLid();

        if (ebObject.getName() != null) {
            name = new InternationalStringImpl(lcm, ebObject.getName());
        }

        if (ebObject.getDescription() != null) {
            description = new InternationalStringImpl(lcm,
                    ebObject.getDescription());
        }

        String ebStatus = ebObject.getStatus();
        if (ebStatus == null) {
            ebStatus = BindingUtility.CANONICAL_STATUS_TYPE_ID_Submitted;
            System.err.println("Warning: Server sent object with null status. id: " + ebObject.getId());
        } else {
            statusRef = new RegistryObjectRef(lcm, ebObject.getStatus());
        }

        List ebClasses = ebObject.getClassification();
        Iterator iter = ebClasses.iterator();

        while (iter.hasNext()) {
            ClassificationType ebClass = (ClassificationType) iter.next();
            internalAddClassification(new ClassificationImpl(lcm, ebClass, this));
        }

        List extIds = ebObject.getExternalIdentifier();
        iter = extIds.iterator();

        while (iter.hasNext()) {
            ExternalIdentifierType ebExtIdentifier = (ExternalIdentifierType) iter.next();
            internalAddExternalIdentifier(new ExternalIdentifierImpl(lcm,
                    ebExtIdentifier));
        }

        objectTypeRef = new RegistryObjectRef(lcm, ebObject.getObjectType());

        versionInfo = ebObject.getVersionInfo();

        newObjOwner = null;
    }

    public int getStatus() throws JAXRException {
        //TODO: Need to move status to RegistryObject in JAXR 2.0
        int status = RegistryEntry.STATUS_SUBMITTED;
        try {
            if (statusRef != null) {
                String ebStatus = statusRef.getId();

                if (ebStatus.equals(BindingUtility.CANONICAL_STATUS_TYPE_ID_Approved)) {
                    status = RegistryEntry.STATUS_APPROVED;
                } else if (ebStatus.equals(BindingUtility.CANONICAL_STATUS_TYPE_ID_Deprecated)) {
                    status = RegistryEntry.STATUS_DEPRECATED;
                } else if (ebStatus.equals(BindingUtility.CANONICAL_STATUS_TYPE_ID_Submitted)) {
                    status = RegistryEntry.STATUS_SUBMITTED;
                } else if (ebStatus.equals(BindingUtility.CANONICAL_STATUS_TYPE_ID_Withdrawn)) {
                    status = RegistryEntry.STATUS_WITHDRAWN;
                } else {
                    status = RegistryEntry.STATUS_WITHDRAWN + 1; //Unknown?
                }
            }
        } catch (JAXRException e) {
            //cannot happen
            log.error(e);
        }
        return status;
    }

    public String getStatusAsString() {
        String statusAsString = "";
        try {
            if (statusRef != null) {
                ConceptImpl statusConcept = (ConceptImpl)statusRef.getRegistryObject(lcm.CONCEPT);
                statusAsString = statusConcept.getDisplayName();
            }
        } catch (JAXRException e) {
            //cannot happen
            log.error(e);
        }

        return statusAsString;
    }

    public boolean isModified() {
        return super.isModified()
            || (name != null && ((InternationalStringImpl)name).isModified())
            || (description != null && ((InternationalStringImpl)description).isModified());
    }

    /**
     * Implementation private
     */
    public void setModified(boolean modified) {
        super.setModified(modified);

        // propagate clear flag
        if (!modified && name != null) {
            ((InternationalStringImpl)name).setModified(modified);
        }
        if (!modified && description != null) {
            ((InternationalStringImpl)description).setModified(modified);
        }

        if (modified == true) {
            lcm.addModifiedObject(this);
        } else {
            lcm.removeModifiedObject(this);
        }
    }

    //??JAXR 2.0
    public RegistryObjectRef getStatusRef() throws JAXRException {
        return statusRef;
    }

    //??JAXR 2.0
    public void setStatusRef(RegistryObjectRef statusRef)
        throws JAXRException {

        //Only set if different
        if ((this.statusRef == null) || (!(this.statusRef.getId().equals(statusRef.getId())))) {
            this.statusRef = statusRef;
            setModified(true);
        }
    }

    //??JAXR 2.0
    public RegistryObjectRef getObjectTypeRef() throws JAXRException {
        return objectTypeRef;
    }

    public Concept getObjectType() throws JAXRException {
        Concept objectType = null;

        if (objectTypeRef != null) {
            objectType = (Concept)objectTypeRef.getRegistryObject("ClassificationNode");
        }

        return objectType;
    }

    /**
     * Internal method to set the objectType
     */
    void setObjectTypeInternal(Concept objectType)
        throws JAXRException {

        if (objectType == null) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.objectTypeConceptMustNotBeNull"));            
        }
        
        if (!objectType.getClassificationScheme().getKey().getId().
                equals(bu.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType)) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.mustBeObjectTypeConcept"));
        }
        objectTypeRef = new RegistryObjectRef(lcm, objectType);
        setModified(true);
    }

    public InternationalString getDescription() throws JAXRException {
        if (description == null) {
            description = lcm.createInternationalString(null);
        }

        return description;
    }

    public void setDescription(InternationalString desc)
        throws JAXRException {
        description = desc;
        setModified(true);
    }

    /**
     * Add to JAXR 2.0??
     */
    public String getLid() throws JAXRException {
        return lid;
    }

    /**
     * Add to JAXR 2.0??
     */
    public void setLid(String lid)
        throws JAXRException {
        this.lid = lid;
        setModified(true);
    }

    /**
     * Add to JAXR 2.0??
     */
    public VersionInfoType getVersionInfo() throws JAXRException {
        return versionInfo;
    }

    /**
     * Add to JAXR 2.0??
     */
    public void setVersionInfo(VersionInfoType versionInfo)
        throws JAXRException {
        this.versionInfo = versionInfo;
        setModified(true);
    }

    //Added for convenience in use in config.xml for RegistryBrowser. Should not be in JAXR API
    public String getVersionName() throws JAXRException {
        String versionName = "";
        if (versionInfo != null) {
            versionName = versionInfo.getVersionName();
        }
        return versionName;
    }

    //Added for convenience in use in config.xml for RegistryBrowser. Should not be in JAXR API
    public String getComment() throws JAXRException {
        String comment = "";
        if (versionInfo != null) {
            comment = versionInfo.getComment();
        }
        return comment;
    }

    /**
     * Gets the name of this object that is suitable for display in UIs.
     * This is typically the String in closest matching locale for the name of the object.
     * Add to JAXR2.0??
     *
     * @return the name suitable for display.
     */
    public String getDisplayName() throws JAXRException {
        String displayName = null;

        if (name != null) {
            displayName = ((InternationalStringImpl) name).getClosestValue();
        }

        if (displayName == null) {
            displayName = "";
        }

        return displayName;
    }

    public InternationalString getName() throws JAXRException {
        if (name == null) {
            name = lcm.createInternationalString(null);
        }

        return name;
    }

    public void setName(InternationalString name) throws JAXRException {
        this.name = name;
        setModified(true);
    }

    public void setKey(Key key) throws JAXRException {
        this.key = key;
        setModified(true);
    }

    /** Internal method, does not set modified flag. */
    private void internalAddClassification(Classification c)
        throws JAXRException {
        getClassifications().add(c);
        c.setClassifiedObject(this);
    }

    public void addClassification(Classification c) throws JAXRException {
        internalAddClassification(c);
        setModified(true);
    }

    public void addClassifications(Collection classifications)
        throws JAXRException {
        Iterator iter = classifications.iterator();

        while (iter.hasNext()) {
            Classification cls = (Classification) iter.next();
            internalAddClassification(cls);
        }

        setModified(true);
    }

    public void removeClassification(Classification c)
        throws JAXRException {
        if (classifications != null) {
            getClassifications().remove(c);
            setModified(true);
        }
    }

    public void removeClassifications(Collection classifications)
        throws JAXRException {
        if (classifications != null) {
            getClassifications().removeAll(classifications);
            setModified(true);
        }
    }

    //??Add to JAXR 2.0. Apply same pattern to all Collection attributes in RIM.
    public void removeAllClassifications() throws JAXRException {
        if (classifications != null) {
            removeClassifications(classifications);
            setModified(true);
        }
    }

    public void setClassifications(Collection classifications)
        throws JAXRException {
        removeAllClassifications();

        addClassifications(classifications);
        setModified(true);
    }

    public Collection getClassifications() throws JAXRException {
        if (classifications == null) {
            classifications = new ArrayList();
        }

        return classifications;
    }

    /**
     * Gets all Concepts classifying this object that have specified path as prefix.
     * Used in RegistryObjectsTableModel.getValueAt via reflections API if so configured.
     */
    public Collection getClassificationConceptsByPath(String pathPrefix)
        throws JAXRException {
        Collection matchingClassificationConcepts = new ArrayList();
        Collection _classifications = getClassifications();
        Iterator iter = _classifications.iterator();

        while (iter.hasNext()) {
            Classification cl = (Classification) iter.next();
            Concept concept = cl.getConcept();
            String conceptPath = concept.getPath();

            if (conceptPath.startsWith(pathPrefix)) {
                matchingClassificationConcepts.add(concept);
            }
        }

        return matchingClassificationConcepts;
    }

    public Collection getAuditTrail() throws JAXRException {
        Collection auditTrail = null;
        if (!isNew()) {
            String queryStr = "SELECT ae.* FROM AuditableEvent ae, AffectedObject ao, RegistryObject ro WHERE ro.lid='" + lid + "' AND ro.id = ao.id AND ao.eventId = ae.id ORDER BY ae.timeStamp_ ASC";
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
            BulkResponse response = dqm.executeQuery(query);

            checkBulkResponseExceptions(response);
            auditTrail = response.getCollection();
        }

        if (auditTrail == null) {
            auditTrail = new ArrayList();
        }

        return auditTrail;
    }

    /**
     * Add this to the JAXR 2.0 API??
     *
     * @return owner, ie. creator or null if this is a new object
     */
    public User getOwner() throws JAXRException {
        User user = null;
        if (!isNew()) {
            // Ask server who our creator is
            Collection events = getAuditTrail();

            for (Iterator it = events.iterator(); it.hasNext();) {
                AuditableEventImpl ev = (AuditableEventImpl) it.next();

                if ((ev.getEventType() == AuditableEvent.EVENT_TYPE_CREATED) ||
                    (ev.getEventType() == AuditableEventImpl.EVENT_TYPE_RELOCATED)) {
                    user =  ev.getUser();
                }
            }
        } else {
            user = getNewObjectOwner();
        }

        return user;
    }

    public void addAssociation(Association ass) throws JAXRException {
        getAssociations();

        if (!(associations.contains(ass))) {
            associations.add(ass);
        }

        ((AssociationImpl) ass).setSourceObjectInternal(this);
    }

    public void addAssociations(Collection asses) throws JAXRException {
        for (Iterator it = asses.iterator(); it.hasNext();) {
            Association ass = (Association) it.next();
            addAssociation(ass);
        }
    }

    public void removeAssociation(Association ass) throws JAXRException {
        getAssociations();

        if (associations.contains(ass)) {
            associations.remove(ass);

            //Need to mark as deleted and only remove from server on Save in future.
            //For now leaving as is in order to minimize change.???
            // Remove from server only if Association exists there
            if (!((AssociationImpl) ass).isNew()) {
                // assert(Association must exist on server)
                ArrayList keys = new ArrayList();
                keys.add(ass.getKey());

                BulkResponse response = lcm.deleteObjects(keys);
                JAXRException ex = getBulkResponseException(response);

                if (ex != null) {
                    throw ex;
                }
            }

            //No need to call setModified(true) since RIM modified object is an Assoociation
            //setModified(true);
        }
    }

    public void removeAssociations(Collection asses) throws JAXRException {
        Collection savedAsses = getAssociations();

        if (associations.removeAll(asses)) {
            // Remove from server only if Association exists there
            ArrayList keys = new ArrayList();

            for (Iterator it = asses.iterator(); it.hasNext();) {
                AssociationImpl ass = (AssociationImpl) it.next();

                if (!ass.isNew()) {
                    // assert(Association must exist on server)
                    keys.add(ass.getKey());
                }
            }

            //TODO: IN future only mark as deleted and delete on save.
            BulkResponse response = lcm.deleteObjects(keys);
            JAXRException ex = getBulkResponseException(response);

            if (ex != null) {
                // Undo remove
                // ??eeg Assumes all-or-nothing delete
                associations = savedAsses;
                throw ex;
            }
        }
    }

    public void setAssociations(Collection asses) throws JAXRException {
        // We make a copy of this.associations to avoid a
        // concurrent modification exception
        removeAssociations((Collection) new ArrayList(getAssociations()));
        addAssociations(asses);
    }

    public Collection getAssociations() throws JAXRException {
        if (associations == null) {
            associations = new HashSet();

            //If existing object then now is the time to do lazy fetch from server
            if (!isNew()) {
                // Return Collection from server
                String id = getKey().getId();
                String queryStr =
                    "SELECT ass.* FROM Association ass WHERE sourceObject = '" + id +
                    "'";
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
                BulkResponse response = dqm.executeQuery(query);
                checkBulkResponseExceptions(response);
                addAssociations(response.getCollection());
            }
        }

        return associations;
    }

    public Collection getAssociatedObjects() throws JAXRException {
        Collection assObjects = null;
        if (isNew()) {
            assObjects = new ArrayList();
            if (associations != null) {
                Iterator iter = associations.iterator();
                while (iter.hasNext()) {
                    Association ass = (Association)iter.next();
                    assObjects.add(ass.getTargetObject());
                }
            }
        }
        else {
            String id = getKey().getId();
            String queryStr = "SELECT ro.* FROM RegistryObject ro, Association ass WHERE ass.sourceObject = '" +
                id + "' AND ass.targetObject = ro.id";
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
            BulkResponse response = dqm.executeQuery(query);
            checkBulkResponseExceptions(response);
            assObjects = response.getCollection();
        }

        return assObjects;
    }

    public Collection getAllAssociations() throws JAXRException {
        if (isNew()) {
            // ??eeg Still can have client side associated objects!
            // Return an empty Collection instead of null
            return new ArrayList();
        }

        String id = getKey().getId();
        String queryStr = "SELECT ass.* FROM Association ass WHERE sourceObject = '" +
            id + "' OR targetObject = '" + id + "'" + " ORDER BY " +
            "sourceObject, targetObject, associationType";
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        BulkResponse response = dqm.executeQuery(query);
        checkBulkResponseExceptions(response);

        return response.getCollection();
    }

    /** Internal method, does not set modified flag. */
    private void internalAddExternalIdentifier(ExternalIdentifier ei)
        throws JAXRException {
        getExternalIdentifiers().add(ei);
        ((ExternalIdentifierImpl)ei).setRegistryObject(this);
    }

    public void addExternalIdentifier(ExternalIdentifier ei)
        throws JAXRException {
        internalAddExternalIdentifier(ei);
        setModified(true);
    }

    public void addExternalIdentifiers(Collection extIds)
        throws JAXRException {
        Iterator iter = extIds.iterator();

        while (iter.hasNext()) {
            ExternalIdentifier extId = (ExternalIdentifier) iter.next();
            internalAddExternalIdentifier(extId);
        }
        setModified(true);
    }

    public void removeExternalIdentifier(ExternalIdentifier ei)
        throws JAXRException {
        if (externalIds != null) {
            externalIds.remove(ei);
            setModified(true);
        }
    }

    public void removeExternalIdentifiers(Collection ei)
        throws JAXRException {
        if (externalIds != null) {
            externalIds.removeAll(ei);
            setModified(true);
        }
    }

    //??Add to JAXR 2.0. Apply same pattern to all Collection attributes in RIM.
    public void removeAllExternalIdentifiers() throws JAXRException {
        if (externalIds != null) {
            removeExternalIdentifiers(externalIds);
            setModified(true);
        }
    }

    public void setExternalIdentifiers(Collection extIds)
        throws JAXRException {
        removeAllExternalIdentifiers();

        addExternalIdentifiers(extIds);
        setModified(true);
    }

    public Collection getExternalIdentifiers() throws JAXRException {
        if (externalIds == null) {
            externalIds = new ArrayList();
        }

        return externalIds;
    }

    public void addExternalLink(ExternalLink extLink) throws JAXRException {
        getExternalLinks();

        // If the external link is not in this object's in-memory-cache of
        // external links, add it.
        if (!(externalLinks.contains(extLink))) {
            // Check that an ExternallyLinks association exists between this
            // object and its external link.
            boolean associationExists = false;
            BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                                                          .getBusinessQueryManager());
            Concept assocType = bqm.findConceptByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                    BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_ExternallyLinks);
            Collection linkAssociations = extLink.getAssociations();

            if (linkAssociations != null) {
                Iterator assIter = linkAssociations.iterator();

                while (assIter.hasNext()) {
                    Association ass = (Association) assIter.next();

                    if (ass.getSourceObject().equals(extLink) &&
                            ass.getTargetObject().equals(this) &&
                            ass.getAssociationType().equals(assocType)) {
                        associationExists = true;

                        break;
                    }
                }
            }

            // Create the association between the external link and this object,
            // if necessary.
            if (!associationExists) {
                Association ass = lcm.createAssociation(this, assocType);
                extLink.addAssociation(ass);
            }

            externalLinks.add(extLink);

            // Note: There is no need to call setModified(true) since
            // the RIM modified object is an Association
        }
    }

    public void addExternalLinks(Collection extLinks) throws JAXRException {
        Iterator iter = extLinks.iterator();

        while (iter.hasNext()) {
            ExternalLink extLink = (ExternalLink) iter.next();
            addExternalLink(extLink);
        }

        //No need to call setModified(true) since RIM modified object is an Assoociation
    }

    public void removeExternalLink(ExternalLink extLink)
        throws JAXRException {
        getExternalLinks();

        if (externalLinks.contains(extLink)) {
            externalLinks.remove(extLink);

            //Now remove the ExternallyLinks association that has extLink as src and this object as target
            // We make a copy of this.externalLinks to avoid a
            // concurrent modification exception in the removeExternalLinks
            Collection linkAssociations = new ArrayList(extLink.getAssociations());

            if (linkAssociations != null) {
                Iterator iter = linkAssociations.iterator();

                while (iter.hasNext()) {
                    Association ass = (Association) iter.next();

                    if (ass.getTargetObject().equals(this)) {
                        if (ass.getAssociationType().getValue()
                                   .equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_ExternallyLinks)) {
                            extLink.removeAssociation(ass);
                        }
                    }
                }
            }

            //No need to call setModified(true) since RIM modified object is an Assoociation
            //setModified(true);
        }
    }

    public void removeExternalLinks(Collection extLinks)
        throws JAXRException {
        getExternalLinks();

        //Avoid ConcurrentModificationException
        ArrayList _extLinks = new ArrayList(extLinks);
        Iterator iter = _extLinks.iterator();

        while (iter.hasNext()) {
            ExternalLink extLink = (ExternalLink) iter.next();
            removeExternalLink(extLink);
        }

        //No need to call setModified(true) since RIM modified object is an Assoociation    }
    }

    /** Set this object's list of external links to the list specified. If the
      * current list of external links contains links that are not in the specified
      * list, they will be removed and the association between them and this object
      * will be removed from the server. For any external links that are in the
      * list specified, an association will be created (in-memory, not on the
      * server) and they will be added to this object's list of external links.
      *
      * @param newExternalLinks
      *     A Collection of ExternalLink objects.
      * @throws JAXRException
      */
    public void setExternalLinks(Collection newExternalLinks)
        throws JAXRException {
        //Avoid ConcurrentModificationException by using a copy
        Collection currentExternalLinks = new ArrayList(getExternalLinks());

        // Add any external links that are not currently in this object's list.
        Iterator newExtLinksIter = newExternalLinks.iterator();

        while (newExtLinksIter.hasNext()) {
            ExternalLink externalLink = (ExternalLink) newExtLinksIter.next();

            if (!currentExternalLinks.contains(externalLink)) {
                addExternalLink(externalLink);
            }
        }

        // Remove any external links that are currently in this object's list,
        // but are not in the new list.
        Iterator currentExternalIter = currentExternalLinks.iterator();

        while (currentExternalIter.hasNext()) {
            ExternalLink externalLink = (ExternalLink) currentExternalIter.next();

            if (!newExternalLinks.contains(externalLink)) {
                removeExternalLink(externalLink);
            }
        }
    }

    public Collection getExternalLinks() throws JAXRException {
        if (externalLinks == null) {
            externalLinks = new HashSet();

            //If existing object then now is the time to do lazy fetch from server
            if (!isNew()) {
                String id = getId();
                String queryStr =
                    "SELECT el.* FROM ExternalLink el, Association ass WHERE ass.targetObject = '" +
                    id + "' AND ass.associationType = '" +
                    BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_ExternallyLinks +
                    "' AND ass.sourceObject = el.id ";
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);

                BulkResponse response = dqm.executeQuery(query);
                checkBulkResponseExceptions(response);
                addExternalLinks(response.getCollection());
            }
        }

        return externalLinks;
    }

    public Organization getSubmittingOrganization() throws JAXRException {
        return org;
    }

    public Collection getRegistryPackages() throws JAXRException {
        if (packages == null) {
            if (!isNew()) {
                HashMap parameters = new HashMap();
                parameters.put(CanonicalConstants.CANONICAL_SLOT_QUERY_ID,
                        CanonicalConstants.CANONICAL_QUERY_GetRegistryPackagesByMemberId);
                parameters.put("$memberId", getId());
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL);
                BulkResponse response = dqm.executeQuery(query, parameters);
                checkBulkResponseExceptions(response);
                Collection registryObjects = response.getCollection();
                packages = response.getCollection();
            }

            if (packages == null) {
                packages = new ArrayList();
            }
        }

        return packages;
    }

    protected void setBindingObject(
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectType ebObject)
        throws JAXRException {
        // Pass ebObject to superclass so slot-s can be initialized
        super.setBindingObject(ebObject);

        //Set by registry, but we need to specify it as XACML ACPs may use objectType as resource attribute
        if (objectTypeRef != null) {
            ebObject.setObjectType(objectTypeRef.getId());
        }

        ebObject.setLid(lid);
        ebObject.setVersionInfo(versionInfo);

        try {
            org.oasis.ebxml.registry.bindings.rim.ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.Name ebName = factory.createName();
            ((InternationalStringImpl) getName()).setBindingObject(ebName);
            ebObject.setName(ebName);

            org.oasis.ebxml.registry.bindings.rim.Description ebDesc = factory.createDescription();
            ((InternationalStringImpl) getDescription()).setBindingObject(ebDesc);
            ebObject.setDescription(ebDesc);

            Iterator iter = getClassifications().iterator();

            while (iter.hasNext()) {
                ClassificationImpl cls = (ClassificationImpl) iter.next();
                org.oasis.ebxml.registry.bindings.rim.Classification ebCls = (org.oasis.ebxml.registry.bindings.rim.Classification) cls.toBindingObject();
                ebObject.getClassification().add(ebCls);
            }

            iter = getExternalIdentifiers().iterator();

            while (iter.hasNext()) {
                ExternalIdentifierImpl extId = (ExternalIdentifierImpl) iter.next();
                org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier ebExtId =
                    (org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier) extId.toBindingObject();
                ebObject.getExternalIdentifier().add(ebExtId);
            }
        } catch (JAXBException ex) {
            throw new JAXRException(ex.getMessage());
        }
    }

    /**
     * RIM Composed objects are composed objects as defined by RIM.
     * Composed objects are composed objects as defined by JAXR.
     * JAXR defines more composed objects than RIM does.
     */
    public HashSet getRIMComposedObjects() throws JAXRException {
        HashSet composedObjects = super.getRIMComposedObjects();

        Collection classifications = getClassifications();
        composedObjects.addAll(classifications);

        Collection extIds = getExternalIdentifiers();
        composedObjects.addAll(extIds);

        Collection slotIds = getSlots();
        composedObjects.addAll(slotIds);

        return composedObjects;
    }

    public HashSet getComposedObjects()
        throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();

        Collection classifications = getClassifications();
        composedObjects.addAll(classifications);

        Collection extIds = getExternalIdentifiers();
        composedObjects.addAll(extIds);

        Collection extLinks = getExternalLinks();
        composedObjects.addAll(extLinks);

        composedObjects.addAll(getAssociationsAndAssociatedObjects());

        Collection slotIds = getSlots();
        composedObjects.addAll(slotIds);

        return composedObjects;
    }

    /**
     * @return First exception in BulkResponse if there is one else null
     */
    RegistryException getBulkResponseException(BulkResponse response)
        throws JAXRException {
        Collection exceptions = response.getExceptions();

        if (exceptions != null) {
            return (RegistryException) exceptions.iterator().next();
        }

        return null;
    }

    /**
     * Throw first exception in BulkResponse if there is one else return
     */
    void checkBulkResponseExceptions(BulkResponse response)
        throws JAXRException {
        RegistryException ex = getBulkResponseException(response);

        if (ex != null) {
            throw ex;
        }

        return;
    }

    /**
     * Gest all Associations and their targets for which this object is a source.
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet getAssociationsAndAssociatedObjects()
        throws JAXRException {
        HashSet assObjects = new HashSet();

        // Automatically save any Association-s with an object in the save
        // list along with the target object of the Association per JAXR 1.0 spec.
        Collection asses = getAssociations();

        // Add the Association targets
        for (Iterator j = asses.iterator(); j.hasNext();) {
            AssociationImpl ass = (AssociationImpl) j.next();
            try {
                RegistryObject target = ass.getTargetObject();
                assObjects.add(target);
            } catch (UnresolvedReferenceException e) {
                //This happens when the targetObject is a remote ObjectRef
                //Handle this by adding the RegistryObjectRef instead
                RegistryObjectRef target = ass.getTargetObjectRef();
                assObjects.add(target);
            }
        }

        // Add also the Association-s themselves
        assObjects.addAll(asses);

        return assObjects;
    }

    /**
     * Gets all referenced objects for which this object is a referant.
     * Extended by base classes.
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet getRegistryObjectRefs() {
        HashSet refs = new HashSet();

        return refs;
    }

    /*
     * Lazy fetches and returns newObjectOwner.
     * Fixes performance penalty where query was made to server when a
     * new RegistryObject was created.
     *
     */
    private User getNewObjectOwner() throws JAXRException {
        if (newObjOwner == null) {
            newObjOwner = dqm.getCallersUser();
        }

        return newObjOwner;
    }

}
