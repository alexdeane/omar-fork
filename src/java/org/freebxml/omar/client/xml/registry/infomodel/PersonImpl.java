/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/xml/registry/infomodel/PersonImpl.java,v 1.20 2007/03/23 18:39:00 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.infomodel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;

import javax.xml.bind.JAXBException;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;
import javax.xml.registry.InvalidRequestException;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRResourceBundle;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.PersonType;

/**
 * Implements future JAXR API interface named Person.
 * TODO: Add Person interface to JAXR 2.0??
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class PersonImpl extends RegistryObjectImpl implements User {
    private PersonName personName = null;
    private ArrayList addresses = new ArrayList();
    private ArrayList emails = new ArrayList();
    private ArrayList phones = new ArrayList();
    private URL url = null;
    private RegistryObjectRef orgRef = null;
    
    public PersonImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);
        personName = new PersonNameImpl(lcm, this);
    }
    
    public PersonImpl(LifeCycleManagerImpl lcm, PersonType ebPerson) throws JAXRException {
        super(lcm, ebPerson);
        personName = new PersonNameImpl(lcm, this, ebPerson.getPersonName());
        // Sync up the IString version of PersonName with parent RO.Name attribute
        // This will enable users to query using BusinessQuery's Name attribute
        // TODO: consider adding Locale and Charset as attributes to ConnectionImpl
        // class so entire JAXR API has access to these attributes.
        setNameInternal();
        
        Iterator ebAddresses = ebPerson.getAddress().iterator();
        
        while (ebAddresses.hasNext()) {
            org.oasis.ebxml.registry.bindings.rim.PostalAddressType ebAddress = (org.oasis.ebxml.registry.bindings.rim.PostalAddressType) ebAddresses.next();
            addresses.add(new PostalAddressImpl(lcm, ebAddress));
        }
        
        Iterator tels = ebPerson.getTelephoneNumber().iterator();
        
        while (tels.hasNext()) {
            org.oasis.ebxml.registry.bindings.rim.TelephoneNumberType tel = (org.oasis.ebxml.registry.bindings.rim.TelephoneNumberType) tels.next();
            phones.add(new TelephoneNumberImpl(lcm, tel));
        }
        
        Iterator ebEmails = ebPerson.getEmailAddress().iterator();
        
        while (ebEmails.hasNext()) {
            org.oasis.ebxml.registry.bindings.rim.EmailAddressType ebEmail = (org.oasis.ebxml.registry.bindings.rim.EmailAddressType) ebEmails.next();
            emails.add(new EmailAddressImpl(lcm, ebEmail));
        }        
    }
        
    public PersonName getPersonName() throws JAXRException {
        return personName;
    }
    
    public void setPersonName(PersonName par1)
        throws JAXRException, InvalidRequestException {
        personName = par1;
        ((PersonNameImpl)personName).setPersonImpl(this);
        // Sync up the IString version of PersonName with parent RO.Name attribute
        // This will enable users to query using BusinessQuery's Name attribute
        setNameInternal();
        setModified(true);
    }
    
    public javax.xml.registry.infomodel.PostalAddress getPostalAddress()
        throws JAXRException {
        // We need to support this method for JAXR 1.x
        PostalAddress address = null;
        Collection addresses = getPostalAddresses();
        if (addresses.iterator().hasNext()) {        
            // Return the first member of the Collection
            address = (PostalAddress)addresses.iterator().next();
        }
        return address;
    }

    public void setPostalAddress(
        javax.xml.registry.infomodel.PostalAddress par1)
        throws JAXRException {
        // XXX Code assumes that par1 will always be a PostalAddressImpl
        PostalAddressImpl address = (PostalAddressImpl) par1;
        // We need to support this method for JAXR 1.x
        // Add this address as first member of the Collection
        List addresses = (List)getPostalAddresses();
        addresses.add(0, address);
        setModified(true);
    }
    
    // Add to JAXR 2.0 API?
    public Collection getPostalAddresses() {
        if (addresses == null) {
            addresses = new ArrayList();
        }
        return addresses;
    }
    
    //Add to JAXR 2.0??
    public void setPostalAddresses(Collection _addresses)
        throws JAXRException {
        removeAllPostalAddresses();
        addPostalAddresses(_addresses);
        
        setModified(true);
    }
    
    //Add to JAXR 2.0??
    public void addPostalAddresses(Collection addresses)
        throws JAXRException {
        java.util.Iterator iter = addresses.iterator();

        while (iter.hasNext()) {
            PostalAddress address = (PostalAddress) iter.next();
            addPostalAddress(address);
        }

        setModified(true);
    }
    
    //Add to JAXR 2.0??
    public void addPostalAddress(PostalAddress _address)
        throws JAXRException {
        getPostalAddresses().add(_address);
        setModified(true);
    }
    
    //Add to JAXR 2.0??
    public void removePostalAddress(PostalAddress _address)
        throws JAXRException {
        if (addresses != null) {
            getPostalAddresses().remove(_address);
            setModified(true);
        }
    }

    //Add to JAXR 2.0??
    public void removePostalAddresses(Collection _address)
        throws JAXRException {
        if (addresses != null) {
            getPostalAddresses().removeAll(_address);
            setModified(true);
        }
    }
    
    //??Add to JAXR 2.0. Apply same pattern to all Collection attributes in RIM.
    public void removeAllPostalAddresses() throws JAXRException {
        if (addresses != null) {
            removePostalAddresses(addresses);
            setModified(true);
        }
    }
    
    //TODO: Remove from JAXR 2.0
    public URL getUrl() throws JAXRException {
        URL url = null;
        Slot urlSlot = getSlot(BindingUtility.IMPL_SLOT_PERSON_URL);
        if (urlSlot != null) {
            String urlStr = (String)(urlSlot.getValues().toArray())[0];
            try {
                url = new URL(urlStr);
            } catch (MalformedURLException e) {
                throw new JAXRException(e);
            }
        }
        return url;
    }
    
    //TODO: Remove from JAXR 2.0
    public void setUrl(URL url) throws JAXRException {
        //user.url maps to impl specific slot
        Slot urlSlot = lcm.createSlot(BindingUtility.IMPL_SLOT_PERSON_URL, url.toString(),
            BindingUtility.CANONICAL_DATA_TYPE_LID_String);
        addSlot(urlSlot);
        //url = par1;
        //setModified(true);
    }
    
    //Add to JAXR 2.0??
    public void addTelephoneNumber(TelephoneNumber _phone)
    throws JAXRException {
        getTelephoneNumbers().add(_phone);
        setModified(true);
    }
    
    //Add to JAXR 2.0??
    public void addTelephoneNumbers(Collection telephoneNumbers)
    throws JAXRException {
        Iterator iter = telephoneNumbers.iterator();
        
        while (iter.hasNext()) {
            TelephoneNumber phone = (TelephoneNumber) iter.next();
            addTelephoneNumber(phone);
        }
        
        setModified(true);
    }
    
    //Add to JAXR 2.0??
    public void removeTelephoneNumber(TelephoneNumber _phone)
    throws JAXRException {
        if (phones != null) {
            getTelephoneNumbers().remove(_phone);
            setModified(true);
        }
    }
    
    //Add to JAXR 2.0??
    public void removeTelephoneNumbers(Collection _phones)
    throws JAXRException {
        if (phones != null) {
            getTelephoneNumbers().removeAll(_phones);
            setModified(true);
        }
    }
    
    //??Add to JAXR 2.0. Apply same pattern to all Collection attributes in RIM.
    public void removeAllTelephoneNumbers() throws JAXRException {
        if (phones != null) {
            removeTelephoneNumbers(phones);
            setModified(true);
        }
    }
    
    //Add to JAXR 2.0??
    public void setTelephoneNumbers(Collection _phones)
    throws JAXRException {
        removeAllTelephoneNumbers();
        
        addTelephoneNumbers(_phones);
        setModified(true);
    }
    
    //Add to JAXR 2.0??
    public Collection getTelephoneNumbers() throws JAXRException {
        if (phones == null) {
            phones = new ArrayList();
        }
        
        return phones;
    }
    
    //Deprecate in JAXR 2.0??
    public Collection getTelephoneNumbers(String phoneType)
        throws JAXRException {
        List selTypePhones = new ArrayList();
        Collection allPhones = getTelephoneNumbers();
        Iterator phoneIterator = allPhones.iterator();
        while (phoneIterator.hasNext()) {
            TelephoneNumber phone = (TelephoneNumber)phoneIterator.next();
            if ((null == phoneType) || (phoneType.equalsIgnoreCase(phone.getType()))) {
                selTypePhones.add(phone);
            }
        }
        return selTypePhones;
    }
    
    //Add to JAXR 2.0??
    public void addEmailAddress(EmailAddress _email)
        throws JAXRException {
        getEmailAddresses().add(_email);
        setModified(true);
    }

    //Add to JAXR 2.0??
    public void addEmailAddresses(Collection emailAddresses)
        throws JAXRException {
        java.util.Iterator iter = emailAddresses.iterator();

        while (iter.hasNext()) {
            EmailAddress email = (EmailAddress) iter.next();
            addEmailAddress(email);
        }

        setModified(true);
    }
    
     //Deprecate in JAXR 2.0??
    public Collection getEmailAddresses(String emailType)
        throws JAXRException {
        return getEmailAddresses();

        //return (Collection)(phones.clone());
    }
    
    //Add to JAXR 2.0??
    public Collection getEmailAddresses() throws JAXRException {
        if (emails == null) {
            emails = new ArrayList();
        }
        return emails;
    }
    
    //Add to JAXR 2.0??
    public void setEmailAddresses(Collection par1) throws JAXRException {
        emails.clear();
        emails.addAll(par1);
        setModified(true);
    }
    
    //Add to JAXR 2.0??
    public void removeEmailAddress(EmailAddress _email)
        throws JAXRException {
        if (emails != null) {
            getEmailAddresses().remove(_email);
            setModified(true);
        }
    }

    //Add to JAXR 2.0??
    public void removeEmailAddresses(Collection _email)
        throws JAXRException {
        if (emails != null) {
            getEmailAddresses().removeAll(_email);
            setModified(true);
        }
    }

    //??Add to JAXR 2.0. Apply same pattern to all Collection attributes in RIM.
    public void removeAllEmailAddresses() throws JAXRException {
        if (emails != null) {
            removeEmailAddresses(emails);
            setModified(true);
        }
    }
    
    public String getType() throws JAXRException {
        String type = null;
        Slot typeSlot = getSlot(BindingUtility.IMPL_SLOT_USER_TYPE);
        if (typeSlot != null) {
            type = (String)(typeSlot.getValues().toArray())[0];
        }
        return type;
    }
    
    public void setType(String type) throws JAXRException {
        //user.type maps to impl specific slot
        Slot typeSlot = lcm.createSlot(BindingUtility.IMPL_SLOT_USER_TYPE, type,
            BindingUtility.CANONICAL_DATA_TYPE_LID_String);
        addSlot(typeSlot);
    }
    
    /**
     * Expected to only be called by OrganizationImpl when addUser is called on the org.
     */
    void setOrganization(Organization org) throws JAXRException {
        //Only set if different
        if ((this.orgRef == null) || (!(this.orgRef.getId().equals(org.getKey().getId())))) {
            this.orgRef = new RegistryObjectRef(lcm, org);
            //No need to call setModified as this is not part of persistent state of Person
        }
        
    }
    
    /**
     *
     * This method should be deprecated in JAXR 2.0 because a person could be associated with multiple orgs.
     * For now return first org that person is affiliated with.
     */
    public Organization getOrganization() throws JAXRException {
        Organization org = null;
        
        //If existing object then now is the time to do lazy fetch from server
        if ((orgRef==null) && (!isNew())) {
            // See if Person is associated with an Org as sourceObject via AffiliatedWith associationType in server
            String id = getKey().getId();
            String queryStr =
                "SELECT ass.* FROM Association ass WHERE sourceObject = '" + id +
                "' AND associationType ='" + BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith + "'";
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
            BulkResponse response = dqm.executeQuery(query);
            checkBulkResponseExceptions(response);
            Collection associations = response.getCollection();
            Iterator iter = associations.iterator();
            while (iter.hasNext()) {
                Association ass = (Association)iter.next();
                RegistryObject targetObject = ass.getTargetObject();
                if (targetObject instanceof Organization) {
                    orgRef = new RegistryObjectRef(lcm, targetObject);
                    break;
                }
            }
        }
        
        if (orgRef != null) {
            org = (Organization)orgRef.getRegistryObject("Organization");
        }
        
        return org;
    }
    
    /**
     * Gets the Organizations that the Person is affiliated with.
     * This method should be added to JAXR 2.0 because a person could be associated with multiple orgs.
     * 
     */
    public Collection getOrganizations() throws JAXRException {
        Collection orgs = new ArrayList();
        
        //If existing object then now is the time to do lazy fetch from server
        // See if Person is associated with an Org as sourceObject via AffiliatedWith associationType in server
        Collection associations = getAssociations();
        Iterator iter = associations.iterator();
        while (iter.hasNext()) {
            Association ass = (Association)iter.next();
            if (ass.getAssociationType().getKey().getId().equals(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith)) {
                RegistryObject targetObject = ass.getTargetObject();
                if (targetObject instanceof Organization) {
                    orgs.add(targetObject);
                }                
            }
        }
        
        return orgs;
    }
    
    public HashSet getRIMComposedObjects() throws JAXRException {
        return getComposedObjects(); 
    }
    
    public HashSet getComposedObjects() throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();
        composedObjects.addAll(getTelephoneNumbers());
        composedObjects.addAll(getEmailAddresses());
        composedObjects.addAll(getPostalAddresses());
        return composedObjects;
    }
    
    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        try {
            ObjectFactory factory = BindingUtility.getInstance().rimFac;
            org.oasis.ebxml.registry.bindings.rim.Person ebPerson = factory.createPerson();
            
            setBindingObject(ebPerson);
            
            return ebPerson;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }
    
    protected void setBindingObject(
    org.oasis.ebxml.registry.bindings.rim.PersonType ebPerson)
    throws JAXRException {
        super.setBindingObject(ebPerson);
        
        org.oasis.ebxml.registry.bindings.rim.PersonName ebPersonName = (org.oasis.ebxml.registry.bindings.rim.PersonName) ((PersonNameImpl) personName).toBindingObject();
        ebPerson.setPersonName(ebPersonName);
        
        boolean compatibilityMode = Boolean.valueOf(org.freebxml.omar.client.xml.registry.util.ProviderProperties.getInstance()
        .getProperty("jaxr-ebxml.tck.compatibilityMode",
        "false")).booleanValue();
        
        //In compatibilityMode must have all required fields filled in by provider
        //since JAXR 1.0 TCK assumes these are optional due to UDDI bias. Fix in TCK later??
        if (compatibilityMode) {
            if (addresses.size() == 0) {
                PostalAddress address = lcm.createPostalAddress("streetNumber",
                "street", "city", "stateOrProvince", "country",
                "postalCode", "type");
                
                ArrayList _addresses = new ArrayList();
                _addresses.add(address);
                setPostalAddresses(_addresses);
            }
            
            if (phones.size() == 0) {
                TelephoneNumber phone = lcm.createTelephoneNumber();
                
                ArrayList _phones = new ArrayList();
                _phones.add(phone);
                setTelephoneNumbers(_phones);
            }
            
            if (emails.size() == 0) {
                EmailAddress email = lcm.createEmailAddress("person@somewhere.com");
                
                ArrayList _emails = new ArrayList();
                _emails.add(email);
                setEmailAddresses(_emails);
            }
        }
        
        Iterator iter = getPostalAddresses().iterator();
        
        while (iter.hasNext()) {
            PostalAddressImpl addr = (PostalAddressImpl) iter.next();
            org.oasis.ebxml.registry.bindings.rim.Address ebAddress = (org.oasis.ebxml.registry.bindings.rim.Address) addr.toBindingObject();
            ebPerson.getAddress().add(ebAddress);
            
            //??ebXML only allows one address for a Person
            break;
        }
        
        iter = getEmailAddresses().iterator();
        
        while (iter.hasNext()) {
            EmailAddressImpl email = (EmailAddressImpl) iter.next();
            org.oasis.ebxml.registry.bindings.rim.EmailAddress ebEmail = (org.oasis.ebxml.registry.bindings.rim.EmailAddress) email.toBindingObject();
            ebPerson.getEmailAddress().add(ebEmail);
        }
        
        iter = getTelephoneNumbers(null).iterator();
        
        while (iter.hasNext()) {
            TelephoneNumberImpl phone = (TelephoneNumberImpl) iter.next();
            org.oasis.ebxml.registry.bindings.rim.TelephoneNumber ebPhone = (org.oasis.ebxml.registry.bindings.rim.TelephoneNumber) phone.toBindingObject();
            ebPerson.getTelephoneNumber().add(ebPhone);
        }        
    }
    
    public void setName(InternationalString name) throws InvalidRequestException {
        String message = JAXRResourceBundle.getInstance().getString("message.warn.noSupportForSettingPersonName");
        throw new InvalidRequestException(message);
    }


    public String toString() {
        String str = super.toString();


        str += " name: " + personName;
 
        return str;
    }
    
    /*
     * This method is used to synchronize the RegistryObject's Name attribute
     * with the formatted name value returned by this.getName(). This enables
     * users to filter Person objects using the Basic/Business Query.
     * This method will synchronize the Name on the condition that a reference
     * to the parent personImpl class has been passed this class.
     *
     * @param locale
     *  A java.util.Locale for the locale of the InternationalString that contains the Name
     * @param charSet
     *  A java.lang.String for the charSet of the InternationalString that contains the Name
     */
    protected void setNameInternal() throws JAXRException {
        String nameStr = ((PersonNameImpl)personName).getFormattedName();
        InternationalString iName = null;
        if (nameStr.length() > 0) {
            iName = getName();
            if (iName == null) {
                iName = lcm.createInternationalString(nameStr);
            }
            LocalizedString ls = ((InternationalStringImpl)iName).getClosestLocalizedString();
            if (ls == null) {
                ls = lcm.createLocalizedString(null, nameStr);
                iName.addLocalizedString(ls);
            } else {
                String value = ls.getValue();
                if (value == null || ! value.equals(nameStr)) {
                    ls.setValue(nameStr);
                }
            }
        } else {
            if (iName == null) {
                iName = lcm.createInternationalString(null);
            }
        }
        super.setName(iName);
    }
    
}
