/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/LocalizedStringPanel.java,v 1.5 2004/03/16 14:24:15 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.text.MessageFormat;

import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.LocalizedString;


/**
 * Panel to edit/inspect a LocalizedString.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 *         <a href="mailto:diego.ballve@republica.fi">Diego Ballve</a>
 */
public class LocalizedStringPanel extends JBPanel {
    JComboBox localeCombo = null;
    JTextField charsetText = null;
    JTextArea valueText = null;
	JLabel charsetLabel = null;
	JLabel localeLabel= null;
	JLabel valueLabel = null;
    protected GridBagConstraints c = null;
    protected GridBagLayout gbl = null;

    /**
     * Creates new LocalizedStringsPanel
     */
    public LocalizedStringPanel() {
        setBorder(BorderFactory.createTitledBorder("LocalizedString Details"));

        gbl = new GridBagLayout();
        c = new GridBagConstraints();
        setLayout(gbl);

        //The name Text
        localeLabel = new JLabel(resourceBundle.getString("label.locale"),
								 SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(localeLabel, c);
        add(localeLabel);

        localeCombo = new JComboBox();
        localeCombo.setModel(new DefaultComboBoxModel(
                Locale.getAvailableLocales()));
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(localeCombo, c);
        add(localeCombo);

        charsetLabel = new JLabel(resourceBundle.getString("label.charset"),
								  SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(charsetLabel, c);
        add(charsetLabel);

        charsetText = new JTextField();
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(charsetText, c);
        add(charsetText);

        valueLabel = new JLabel(resourceBundle.getString("label.value"),
								SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(valueLabel, c);
        add(valueLabel);

        valueText = new JTextArea();
        valueText.setLineWrap(true);
        valueText.setRows(2);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(valueText, c);
        add(valueText);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, LocalizedString.class);

        super.setModel(obj);

        LocalizedString lString = (LocalizedString) obj;

        try {
            if (lString != null) {
                Locale lStringLocale = lString.getLocale();
                localeCombo.setSelectedItem(lStringLocale);

                String lStringCharset = lString.getCharsetName();

                if (lStringCharset != null) {
                    charsetText.setText(lStringCharset);
                }

                String lStringValue = lString.getValue();

                if (lStringValue != null) {
                    valueText.setText(lStringValue);
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            LocalizedString lString = (LocalizedString) model;

            lString.setLocale((Locale) localeCombo.getSelectedItem());

            lString.setCharsetName(charsetText.getText());

            lString.setValue(valueText.getText());

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        if (localeCombo.getSelectedItem() == null) {
            throw new JAXRException(resourceBundle.getString("error.localizedStringNullLocale"));
        }

        if (charsetText.getText() == null) {
            throw new JAXRException(resourceBundle.getString("error.localizedStringNullCharset"));
        }

        if (valueText.getText().length() > 256) {
            throw new JAXRException(resourceBundle.getString("error.localizedStringValueLength"));
        }
    }

    public void clear() throws JAXRException {
        super.clear();
        localeCombo.setSelectedItem(Locale.getDefault());
        charsetText.setText("");
        valueText.setText("");
    }
}
