/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/AssociationPanel.java,v 1.10 2007/08/15 23:10:23 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/AssociationPanel.java,v 1.10 2007/08/15 23:10:23 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.beans.PropertyChangeEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.infomodel.AssociationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.RegistryObjectRef;
import org.freebxml.omar.common.BindingUtility;


/**
 * Panel to edit/inspect an Association.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class AssociationPanel extends RegistryObjectPanel {
	TitledBorder panelBorder = null;
	JLabel sourceObjectLabel = null;
    JTextField sourceObjectText = null;
	JLabel targetObjectLabel = null;
    JTextField targetObjectText = null;
    JCheckBox confirmedBySourceCheckBox = null;
    JCheckBox confirmedByTargetCheckBox = null;
    JCheckBox isExtramuralCheckBox = null;
	JLabel assocTypeLabel = null;
    JComboBox assocTypeCombo = null;
    String[] assocTypes = { "PartnerOf" };

    /**
     * Creates new ServicePanel
     */
    public AssociationPanel() {
        super();

        panelBorder =
			BorderFactory.createTitledBorder(resourceBundle.getString("title.associationDetails"));
        setBorder(panelBorder);

        //Source Object
		sourceObjectLabel = new JLabel(resourceBundle.getString("label.sourceObject"),
									   SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(sourceObjectLabel, c);
        add(sourceObjectLabel);

        sourceObjectText = new JTextField();
        sourceObjectText.setEditable(editable);
        c.gridx = 0;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(sourceObjectText, c);
        add(sourceObjectText);

        //targetObject
        targetObjectLabel = new JLabel(resourceBundle.getString("label.targetObject"),
									   SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = row + 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(targetObjectLabel, c);
        add(targetObjectLabel);

        targetObjectText = new JTextField();
        targetObjectText.setEditable(editable);
        c.gridx = 1;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(targetObjectText, c);
        add(targetObjectText);

        confirmedBySourceCheckBox =
			new JCheckBox(resourceBundle.getString("label.confirmedBySourceOwner"));
        confirmedBySourceCheckBox.setSelected(false);
        confirmedBySourceCheckBox.setEnabled(false);
        c.gridx = 0;
        c.gridy = row + 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(confirmedBySourceCheckBox, c);
        //add(confirmedBySourceCheckBox);

        confirmedByTargetCheckBox =
			new JCheckBox(resourceBundle.getString("label.confirmedByTargetOwner"));
        confirmedByTargetCheckBox.setSelected(false);
        confirmedByTargetCheckBox.setEnabled(false);
        c.gridx = 1;
        c.gridy = row + 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(confirmedByTargetCheckBox, c);
        //add(confirmedByTargetCheckBox);

		assocTypeLabel = new JLabel(resourceBundle.getString("label.associationType"),
									SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(assocTypeLabel, c);
        add(assocTypeLabel);

        assocTypeCombo = new JComboBox(getAssociationTypes());
        assocTypeCombo.setEditable(editable);
        c.gridx = 0;
        c.gridy = row + 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(assocTypeCombo, c);
        add(assocTypeCombo);

        isExtramuralCheckBox =
			new JCheckBox(resourceBundle.getString("label.isExtramural"));
        isExtramuralCheckBox.setSelected(false);
        isExtramuralCheckBox.setEnabled(false);
        c.gridx = 1;
        c.gridy = row + 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(isExtramuralCheckBox, c);
        add(isExtramuralCheckBox);
    }

    public static String[] getAssociationTypes() {
        String[] assocTypes = null;

        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl bqm = (org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl) (client.getBusinessQueryManager());
            Collection concepts = bqm.findConceptsByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/%");

            int size = concepts.size();
            assocTypes = new String[size];

            Iterator iter = concepts.iterator();
            int i = 0;

            while (iter.hasNext()) {
                Concept concept = (Concept) iter.next();
                assocTypes[i++] = concept.getValue();
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return assocTypes;
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, Association.class);

        super.setModel(obj);

        Association ass = (Association) obj;

        try {
            if (ass != null) {
                RegistryObject src = ass.getSourceObject();
                RegistryObject target = ass.getTargetObject();
                boolean srcConfirmed = ass.isConfirmedBySourceOwner();
                boolean targetConfirmed = ass.isConfirmedByTargetOwner();
                boolean isExtramural = ass.isExtramural();
                Concept assType = ass.getAssociationType();

                if (src != null) {
                    sourceObjectText.setText(src.getKey().getId());
                }

                if (target != null) {
                    targetObjectText.setText(target.getKey().getId());
                }

                confirmedBySourceCheckBox.setSelected(srcConfirmed);
                confirmedByTargetCheckBox.setSelected(targetConfirmed);
                isExtramuralCheckBox.setSelected(isExtramural);

                if (assType != null) {
                    String path = assType.getPath();

                    /* //??new concepts do not have path set
                    if (!(path.startsWith("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/"))) {
                        RegistryBrowser.displayError("Association type must be a Concept under the canonical AssociationType ClassificationScheme");
                    }
                     */
                    assocTypeCombo.setSelectedItem(assType.getValue());
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            AssociationImpl ass = (AssociationImpl) model;

            JAXRClient client = RegistryBrowser.getInstance().getClient();
            org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl bqm = (org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl) (client.getBusinessQueryManager());

            Concept assocType = bqm.findConceptByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                    (String) (assocTypeCombo.getSelectedItem()));
            
            if (assocType == null) {
                //Following is needed in case concept is not an immediate first level child.
                assocType = bqm.findConceptByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/%/" +
                    (String) (assocTypeCombo.getSelectedItem()));
            }
            ass.setAssociationType(assocType);

            ass.setSourceObjectRef(new RegistryObjectRef((LifeCycleManagerImpl)ass.getLifeCycleManager(), sourceObjectText.getText()));
            ass.setTargetObjectRef(new RegistryObjectRef((LifeCycleManagerImpl)ass.getLifeCycleManager(), targetObjectText.getText()));
            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        Association ass = (Association) model;
    }

    public void clear() throws JAXRException {
        super.clear();
        sourceObjectText.setText("");
        targetObjectText.setText("");
        confirmedBySourceCheckBox.setSelected(false);
        confirmedByTargetCheckBox.setSelected(false);
        isExtramuralCheckBox.setSelected(false);

        //assocTypeCombo = null;
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        sourceObjectText.setEditable(editable);
        targetObjectText.setEditable(editable);
        assocTypeCombo.setEditable(editable);
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
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
	protected void updateUIText() {
		super.updateUIText();

        panelBorder.setTitle(resourceBundle.getString("title.associationDetails"));
		sourceObjectLabel.setText(resourceBundle.getString("label.sourceObject"));
        targetObjectLabel.setText(resourceBundle.getString("label.targetObject"));
        confirmedBySourceCheckBox.setText(resourceBundle.getString("label.confirmedBySourceOwner"));
        confirmedByTargetCheckBox.setText(resourceBundle.getString("label.confirmedByTargetOwner"));
		assocTypeLabel.setText(resourceBundle.getString("label.associationType"));
        isExtramuralCheckBox.setText(resourceBundle.getString("label.isExtramural"));
	}
}
