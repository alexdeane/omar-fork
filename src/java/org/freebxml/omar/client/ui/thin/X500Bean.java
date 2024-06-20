/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/thin/X500Bean.java,v 1.1 2005/05/13 16:58:42 doballve Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.thin;

import java.io.IOException;
import sun.security.x509.X500Name;

/**
 *
 * @author Diego Ballve / Digital Artefacts
 */
public class X500Bean {

    /**
     * Holds value of property name.
     */
    private String name;

    /**
     * Holds value of property unit.
     */
    private String unit;

    /**
     * Holds value of property organization.
     */
    private String organization;

    /**
     * Holds value of property city.
     */
    private String city;

    /**
     * Holds value of property stateOrProvince.
     */
    private String stateOrProvince;

    /**
     * Holds value of property country.
     */
    private String country;
    
    /** Creates a new instance of X500Bean */
    public X500Bean() {
    }

    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public String getName() {

        return this.name;
    }

    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Getter for property unit.
     * @return Value of property unit.
     */
    public String getUnit() {

        return this.unit;
    }

    /**
     * Setter for property unit.
     * @param unit New value of property unit.
     */
    public void setUnit(String unit) {

        this.unit = unit;
    }

    /**
     * Getter for property organization.
     * @return Value of property organization.
     */
    public String getOrganization() {

        return this.organization;
    }

    /**
     * Setter for property organization.
     * @param organization New value of property organization.
     */
    public void setOrganization(String organization) {

        this.organization = organization;
    }

    /**
     * Getter for property city.
     * @return Value of property city.
     */
    public String getCity() {

        return this.city;
    }

    /**
     * Setter for property city.
     * @param city New value of property city.
     */
    public void setCity(String city) {

        this.city = city;
    }

    /**
     * Getter for property stateOrProvince.
     * @return Value of property stateOrProvince.
     */
    public String getStateOrProvince() {

        return this.stateOrProvince;
    }

    /**
     * Setter for property stateOrProvince.
     * @param stateOrProvince New value of property stateOrProvince.
     */
    public void setStateOrProvince(String stateOrProvince) {

        this.stateOrProvince = stateOrProvince;
    }

    /**
     * Getter for property country.
     * @return Value of property country.
     */
    public String getCountry() {

        return this.country;
    }

    /**
     * Setter for property country.
     * @param country New value of property country.
     */
    public void setCountry(String country) {

        this.country = country;
    }

    public void clear() {
        this.name = null;
        this.unit = null;
        this.organization = null;
        this.city = null;
        this.stateOrProvince = null;
        this.country = null;
    }
    
    public X500Name toX500Name() {
        try {
            X500Name x500Name = new X500Name(name, unit, organization, city, stateOrProvince, country);
            return x500Name;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
    
}
