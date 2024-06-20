/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/LocaleSelectorDialog.java,v 1.6 2004/03/16 14:24:15 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;

import java.text.Collator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


/**
 * A Dialog to allow selection of a Locale. The Locale can be selected from
 * the available Locales or customized by selecting language and country.
 *
 * @author  <a href="mailto:doballve@users.sorceforge.net">Diego Ballve</a> / Republica Corp.
 */
public class LocaleSelectorDialog extends JBDialog {
    private JComboBox jcbAvailableLocales;
    private JComboBox jcbCustomCountry;
    private JLabel jlSelectLocale;
    private JCheckBox jchbCountry;
    private JCheckBox jchbVariant;
    private JRadioButton jrbAvailableLocales;
    private JRadioButton jrbCustom;
    private JComboBox jcbCustomLanguage;
    private JPanel jPanel1;
    private ButtonGroup bgSelectionMode;
    private JTextField jtfVariant;

    /** The selected Locale */
    private Locale selectedLocale = null;

    private class LSDLocale implements Comparable {
        private Locale locale;

        LSDLocale(Locale locale) {
            this.locale = locale;
        }

        public String toString() {
            return locale.getDisplayName(locale);
        }

        public Locale getLocale() {
            return locale;
        }


        public int compareTo(Object o) {
            String oString = ((LSDLocale)o).toString();
            return this.toString().compareTo(oString);
        }
    }

    private class LSDLocaleComparator implements Comparator {
        private Collator collator;

        LSDLocaleComparator(Collator collator) {
            this.collator = collator;
        }

        public int compare(Object o1, Object o2) {
            return collator.compare(((LSDLocale)o1).toString(),
                                    ((LSDLocale)o2).toString());
        }
    }

	LSDLocale[] lsdlArray = new LSDLocale[Locale.getAvailableLocales().length];
	LSDLocale cbmSelectedItem;

	LSDLocaleComparator localeComparator;

    /** Creates new form LocaleSelectorDialog */
    public LocaleSelectorDialog(Locale initialLocale, JFrame parent,
        boolean modal) {
        super(parent, modal, true);
        LocaleSelectorDialog_initialize(initialLocale);
    }

    /** Creates new form LocaleSelectorDialog */
    public LocaleSelectorDialog(Locale initialLocale, JDialog parent,
        boolean modal) {
        super(parent, modal, true);
        LocaleSelectorDialog_initialize(initialLocale);
    }

    private void LocaleSelectorDialog_initialize(Locale initialLocale) {
        setEditable(true);
        selectedLocale = initialLocale;
        setTitle(resourceBundle.getString("dialog.locale.title"));

        jPanel1 = getMainPanel();
        jPanel1.setLayout(new java.awt.GridBagLayout());

        GridBagConstraints gridBagConstraints;

        bgSelectionMode = new javax.swing.ButtonGroup();
        jlSelectLocale = new javax.swing.JLabel();
        jrbAvailableLocales = new javax.swing.JRadioButton();
        jrbCustom = new javax.swing.JRadioButton();
        jcbAvailableLocales = new JComboBox();
        jcbCustomLanguage = new JComboBox();
        jcbCustomCountry = new JComboBox();
        jchbCountry = new javax.swing.JCheckBox();
        jchbVariant = new javax.swing.JCheckBox();
        jtfVariant = new javax.swing.JTextField();

        jlSelectLocale.setText(resourceBundle.getString("dialog.locale.select"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        jPanel1.add(jlSelectLocale, gridBagConstraints);

        jrbAvailableLocales.setSelected(true);
        jrbAvailableLocales.setText(resourceBundle.getString("dialog.locale.available"));
        jrbAvailableLocales.setToolTipText(
            resourceBundle.getString("dialog.locale.available.tip"));
        bgSelectionMode.add(jrbAvailableLocales);
        jrbAvailableLocales.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    jrbAvailableLocalesActionPerformed(evt);
                }
            });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        jPanel1.add(jrbAvailableLocales, gridBagConstraints);

        jrbCustom.setText(resourceBundle.getString("dialog.locale.custom"));
        jrbCustom.setToolTipText(resourceBundle.getString("dialog.locale.custom.tip"));
        bgSelectionMode.add(jrbCustom);
        jrbCustom.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    jrbCustomActionPerformed(evt);
                }
            });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        jPanel1.add(jrbCustom, gridBagConstraints);

		localeComparator =
			new LSDLocaleComparator(Collator.getInstance(selectedLocale));

        for(int i = 0; i < Locale.getAvailableLocales().length; i++) {
            LSDLocale currentLSDLocale =
                new LSDLocale(Locale.getAvailableLocales()[i]);

            if (Locale.getAvailableLocales()[i].equals(selectedLocale)) {
                cbmSelectedItem = currentLSDLocale;
            }

            lsdlArray[i] = currentLSDLocale;
        }

        jcbAvailableLocales.setModel(getAvailableLocalesCBModel());

        //TO DO: fix case selectedLocale is not in Locale.getAvailableLocales()
        jcbAvailableLocales.setSelectedItem(selectedLocale);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        jPanel1.add(jcbAvailableLocales, gridBagConstraints);

        jcbCustomLanguage.setModel(getCustomLanguageCBModel());
        jcbCustomLanguage.setSelectedItem(selectedLocale.getLanguage());
        jcbCustomLanguage.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        jPanel1.add(jcbCustomLanguage, gridBagConstraints);

        jcbCustomCountry.setModel(getCustomCountryCBModel());
        jcbCustomCountry.setSelectedItem(selectedLocale.getCountry());
        jcbCustomCountry.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        jPanel1.add(jcbCustomCountry, gridBagConstraints);

        jchbCountry.setText(resourceBundle.getString("dialog.locale.customCountry"));
        jchbCountry.setEnabled(false);
        jchbCountry.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    jchbCountryActionPerformed(evt);
                }
            });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 12, 0, 4);
        jPanel1.add(jchbCountry, gridBagConstraints);

        if (selectedLocale.getVariant() != null) {
            jtfVariant.setText(selectedLocale.getVariant());
        }

        jtfVariant.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        jPanel1.add(jtfVariant, gridBagConstraints);

        jchbVariant.setText(resourceBundle.getString("dialog.locale.customVariant"));
        jchbVariant.setEnabled(false);
        jchbVariant.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    jchbVariantActionPerformed(evt);
                }
            });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 12, 0, 4);
        jPanel1.add(jchbVariant, gridBagConstraints);

        pack();
    }

    private void jchbCountryActionPerformed(ActionEvent evt) {
        jcbCustomCountry.setEnabled(jchbCountry.isSelected());
        jchbVariant.setEnabled(jchbCountry.isSelected());
        jtfVariant.setEnabled(jchbCountry.isSelected() &&
            jchbVariant.isSelected());
    }

    private void jchbVariantActionPerformed(ActionEvent evt) {
        jtfVariant.setEnabled(jchbVariant.isSelected());
    }

    private void jrbCustomActionPerformed(ActionEvent evt) {
        jcbAvailableLocales.setEnabled(false);
        jcbCustomLanguage.setEnabled(true);
        jchbCountry.setEnabled(true);
        jcbCustomCountry.setEnabled(jchbCountry.isSelected());
        jchbVariant.setEnabled(jchbCountry.isSelected());
        jtfVariant.setEnabled(jchbVariant.isSelected());
    }

    private void jrbAvailableLocalesActionPerformed(ActionEvent evt) {
        jcbAvailableLocales.setEnabled(true);
        jcbCustomLanguage.setEnabled(false);
        jcbCustomCountry.setEnabled(false);
        jchbCountry.setEnabled(false);
        jchbVariant.setEnabled(false);
        jtfVariant.setEnabled(false);
    }

    private ComboBoxModel getAvailableLocalesCBModel() {
        Arrays.sort(lsdlArray, localeComparator);

        javax.swing.DefaultComboBoxModel cbm =
            new javax.swing.DefaultComboBoxModel(lsdlArray);
        cbm.setSelectedItem(cbmSelectedItem);

        return cbm;
    }

    private ComboBoxModel getCustomLanguageCBModel() {
        return new javax.swing.DefaultComboBoxModel(Locale.getISOLanguages());
    }

    private ComboBoxModel getCustomCountryCBModel() {
        return new javax.swing.DefaultComboBoxModel(Locale.getISOCountries());
    }

    protected void okAction() {
		Locale oldSelectedLocale = selectedLocale;

        super.okAction();

        if (jrbAvailableLocales.isSelected()) {
			cbmSelectedItem = (LSDLocale) jcbAvailableLocales.getSelectedItem();
            selectedLocale = cbmSelectedItem.getLocale();
        } else if (!jchbCountry.isSelected()) {
            selectedLocale = new Locale((String) jcbCustomLanguage.getSelectedItem());
        } else if (!jchbVariant.isSelected() ||
                (jtfVariant.getText().trim().length() == 0)) {
            selectedLocale = new Locale((String) jcbCustomLanguage.getSelectedItem(),
                    (String) jcbCustomCountry.getSelectedItem());
        } else {
            selectedLocale = new Locale((String) jcbCustomLanguage.getSelectedItem(),
                    (String) jcbCustomCountry.getSelectedItem(),
                    jtfVariant.getText());
        }

        dispose();
    }

    /**
     * Listens to property changes in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_LOCALE)) {
			processLocaleChange((Locale) ev.getNewValue());
		}
	}

	/**
	 * Processes a change in the bound property
	 * RegistryBrowser.PROPERTY_LOCALE.
	 */
	protected void processLocaleChange(Locale newLocale) {
		super.processLocaleChange(newLocale);

		updateUIText();

		// Re-order the locales according to the current selected locale.
		jcbAvailableLocales.setModel(getAvailableLocalesCBModel());
		setSize(getPreferredSize());

		invalidate();
		validate();
		getMainPanel().doLayout();
		pack();
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
	protected void updateUIText() {
		super.updateUIText();

        setTitle(resourceBundle.getString("dialog.locale.title"));
        jlSelectLocale.setText(resourceBundle.getString("dialog.locale.select"));
        jrbAvailableLocales.setText(resourceBundle.getString("dialog.locale.available"));
        jrbAvailableLocales.setToolTipText(
            resourceBundle.getString("dialog.locale.available.tip"));
        jrbCustom.setText(resourceBundle.getString("dialog.locale.custom"));
        jrbCustom.setToolTipText(resourceBundle.getString("dialog.locale.custom.tip"));
        jchbCountry.setText(resourceBundle.getString("dialog.locale.customCountry"));
        jchbVariant.setText(resourceBundle.getString("dialog.locale.customVariant"));
	}

    /**
     * Getter for property selectedLocale.
     *
     * @return Locale the selected Locale.
     */
    public Locale getSelectedLocale() {
        return selectedLocale;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame();
        LocaleSelectorDialog localeSelector = new LocaleSelectorDialog(Locale.getDefault(),
                frame, true);
        localeSelector.show();
        System.out.println("Selected Locale was: " +
            localeSelector.getSelectedLocale());
        frame.dispose();
    }
}
