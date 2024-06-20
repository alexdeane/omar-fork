/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/registration/PostalAddressPanel.java,v 1.6 2006/04/10 10:59:06 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.registration;

import java.util.Locale;
import org.freebxml.omar.client.common.userModel.PostalAddressModel;
import org.freebxml.omar.client.ui.swing.swing.MappedDocumentListener;
import org.freebxml.omar.client.ui.swing.swing.RegistryComboBoxListener;
import org.freebxml.omar.client.ui.swing.swing.TextField;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.PostalAddress;
import org.freebxml.omar.client.ui.swing.swing.RegistryMappedPanel;


/**
 * Panel for PostalAddress
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 * @author Fabian Ritzmann
 */
public class PostalAddressPanel extends RegistryMappedPanel {

    private final PostalAddressModel model;
    private final JTextField streetNumText = new JTextField();
    private final JTextField streetText = new JTextField();
    private final JTextField cityText = new JTextField();
    private final JTextField stateText = new JTextField();
    private final JTextField postalCodeText = new JTextField();
    private final JTextField countryText = new JTextField();
    private JLabel streetNumLabel;
    private JLabel streetLabel;
    private JLabel cityLabel;
    private JLabel stateLabel;
    private JLabel postalCodeLabel;
    private JLabel countryLabel;
    private JLabel typeLabel;
    private StreetNumListener streetNumListener;
    private StreetListener streetListener;
    private CityListener cityListener;
    private StateListener stateListener;
    private PostalCodeListener postalCodeListener;
    private CountryListener countryListener;
    private AddressTypeListener addressTypeListener;
            

    PostalAddressPanel(PostalAddressModel address) {
        super(address, resourceBundle.getString("error.displayPostalAddressFailed"));
        this.model = address;
        this.model.addObserver(this);

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        streetNumLabel = new JLabel(resourceBundle.getString("label.streetNumber"), SwingConstants.LEFT);
        setConstraints(streetNumLabel, c, gbl, 0, 0, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(streetNumLabel);

        streetNumListener = new StreetNumListener();
        streetNumText.getDocument().addDocumentListener(streetNumListener);
        setConstraints(streetNumText, c, gbl, 0, 1, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return streetNumText;
                }

                public String getText() throws JAXRException {
                    PostalAddress address = getPostalAddressModel().getAddress();

                    if (address != null) {
                        return address.getStreetNumber();
                    }

                    return null;
                }
            });

        streetLabel = new JLabel(resourceBundle.getString("label.street"), SwingConstants.LEFT);
        setConstraints(streetLabel, c, gbl, 1, 0, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(streetLabel);

        streetListener = new StreetListener();
        streetText.getDocument().addDocumentListener(streetListener);
        setConstraints(streetText, c, gbl, 1, 1, 1, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return streetText;
                }

                public String getText() throws JAXRException {
                    PostalAddress address = getPostalAddressModel().getAddress();

                    if (address != null) {
                        return address.getStreet();
                    }

                    return null;
                }
            });

        cityLabel = new JLabel(resourceBundle.getString("label.city"), SwingConstants.LEFT);
        setConstraints(cityLabel, c, gbl, 0, 2, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(cityLabel);

        cityListener = new CityListener();
        cityText.getDocument().addDocumentListener(cityListener);
        setConstraints(cityText, c, gbl, 0, 3, 1, 0.5, GridBagConstraints.BOTH,
            GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return cityText;
                }

                public String getText() throws JAXRException {
                    PostalAddress address = getPostalAddressModel().getAddress();

                    if (address != null) {
                        return address.getCity();
                    }

                    return null;
                }
            });

        stateLabel = new JLabel(resourceBundle.getString("label.stateProvince"), SwingConstants.LEFT);
        setConstraints(stateLabel, c, gbl, 1, 2, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(stateLabel);

        stateListener = new StateListener();
        stateText.getDocument().addDocumentListener(stateListener);
        setConstraints(stateText, c, gbl, 1, 3, 1, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return stateText;
                }

                public String getText() throws JAXRException {
                    PostalAddress address = getPostalAddressModel().getAddress();

                    if (address != null) {
                        return address.getStateOrProvince();
                    }

                    return null;
                }
            });

        postalCodeLabel = new JLabel(resourceBundle.getString("label.postalCode"), SwingConstants.LEFT);
        setConstraints(postalCodeLabel, c, gbl, 0, 4, 1, 0.0,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        add(postalCodeLabel);

        postalCodeListener = new PostalCodeListener();
        postalCodeText.getDocument().addDocumentListener(postalCodeListener);
        setConstraints(postalCodeText, c, gbl, 0, 5, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return postalCodeText;
                }

                public String getText() throws JAXRException {
                    PostalAddress address = getPostalAddressModel().getAddress();

                    if (address != null) {
                        return address.getPostalCode();
                    }

                    return null;
                }
            });

        countryLabel = new JLabel(resourceBundle.getString("label.country"), SwingConstants.LEFT);
        setConstraints(countryLabel, c, gbl, 1, 4, 1, 0.0,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(countryLabel);

        countryListener = new CountryListener();
        countryText.getDocument().addDocumentListener(countryListener);
        setConstraints(countryText, c, gbl, 1, 5, 1, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return countryText;
                }

                public String getText() throws JAXRException {
                    PostalAddress address = getPostalAddressModel().getAddress();

                    if (address != null) {
                        return address.getCountry();
                    }

                    return null;
                }
            });

        typeLabel = new JLabel(resourceBundle.getString("label.addressType"), SwingConstants.LEFT);
        setConstraints(typeLabel, c, gbl, 0, 6, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        //add(typeLabel);

        JComboBox typeCombo = new JComboBox(PostalAddressModel.ADDRESS_TYPES);
        typeCombo.setEditable(true);
        addressTypeListener = new AddressTypeListener();
        typeCombo.addActionListener(addressTypeListener);
        setConstraints(typeCombo, c, gbl, 0, 7, 1, 0.5,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        //add(typeCombo);   Removed due to bug where change in this field changed all other fields in panel and was not being stored.   

    }

    public PostalAddressModel getPostalAddressModel() {
        return this.model;
    }
    
    /**
     * Processes a change in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    protected void processLocaleChange(Locale newLocale) {
        super.processLocaleChange(newLocale);
        updateUIText();
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
    protected void updateUIText() {
        super.updateUIText();

        setError(resourceBundle.getString("error.displayPostalAddressFailed"));
        
        streetNumLabel.setText(resourceBundle.getString("label.streetNumber"));
        streetLabel.setText(resourceBundle.getString("label.street"));
        cityLabel.setText(resourceBundle.getString("label.city"));
        stateLabel.setText(resourceBundle.getString("label.stateProvince"));
        postalCodeLabel.setText(resourceBundle.getString("label.postalCode"));
        countryLabel.setText(resourceBundle.getString("label.country"));
        typeLabel.setText(resourceBundle.getString("label.addressType"));

        streetNumListener.setError(resourceBundle.getString("error.setStreetNumberFailed"));
        streetListener.setError(resourceBundle.getString("error.setStreetFailed"));
        cityListener.setError(resourceBundle.getString("error.setCityFailed"));
        stateListener.setError(resourceBundle.getString("error.setStateFailed"));
        postalCodeListener.setError(resourceBundle.getString("error.setPostalCodeFailed"));
        countryListener.setError(resourceBundle.getString("error.setCountryFailed"));
    }

    class StreetNumListener extends MappedDocumentListener {
        public StreetNumListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setStreetNumberFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getPostalAddressModel().setStreetNum(text);
        }
    }

    class StreetListener extends MappedDocumentListener {
        public StreetListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setStreetFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getPostalAddressModel().setStreet(text);
        }
    }

    class CityListener extends MappedDocumentListener {
        public CityListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setCityFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getPostalAddressModel().setCity(text);
        }
    }

    class StateListener extends MappedDocumentListener {
        public StateListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setStateFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getPostalAddressModel().setState(text);
        }
    }

    class PostalCodeListener extends MappedDocumentListener {
        public PostalCodeListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setPostalCodeFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getPostalAddressModel().setPostalCode(text);
        }
    }

    class CountryListener extends MappedDocumentListener {
        public CountryListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setCountryFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getPostalAddressModel().setCountry(text);
        }
    }

    class AddressTypeListener extends RegistryComboBoxListener {
        AddressTypeListener() {
            super(getPostalAddressModel(), getRegistryMappedPanel());
        }
    }
}
