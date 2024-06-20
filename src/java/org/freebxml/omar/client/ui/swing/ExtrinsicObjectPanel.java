/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ExtrinsicObjectPanel.java,v 1.11 2005/03/14 23:35:03 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtrinsicObject;

import org.freebxml.omar.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;


/**
 * Panel to edit/inspect an ExtrinsicObject.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ExtrinsicObjectPanel extends RegistryObjectPanel {
    static JFileChooser chooser = new JFileChooser();
    JTextField objectTypeText = null;
    JCheckBox isOpaqueCheckBox = null;
    JComboBox mimeTypeCombo = null;

    JTextField contentVersionNameText = null;
    JTextField contentCommentText = null;
    
    //How to initialize this from browser's mimeTypes
    String[] mimeTypes = { "application/octet-stream", "text/xml", "text/plain" };
    
    /**
     * Creates new ServicePanel
     */
    public ExtrinsicObjectPanel() {
        super();
        
        setBorder(BorderFactory.createTitledBorder(resourceBundle.getString("title.extrinsicObject")));
        
        //contentVersionName and contentComment rows
        JLabel contentVersionNameLabel =
		  new JLabel(resourceBundle.getString("label.contentVersionName"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = row+0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(contentVersionNameLabel, c);
        add(contentVersionNameLabel);
        
        contentVersionNameText = new JTextField();
        contentVersionNameText.setEditable(false);
        c.gridx = 0;
        c.gridy = row+1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(contentVersionNameText, c);
        add(contentVersionNameText);
        
        JLabel contentCommentLabel =
		  new JLabel(resourceBundle.getString("label.contentVersionComment"),
        SwingConstants.LEADING);
        c.gridx = 1;
        c.gridy = row+0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(contentCommentLabel, c);
        add(contentCommentLabel);
        
        contentCommentText = new JTextField();
        contentCommentText.setEditable(editable);
        c.gridx = 1;
        c.gridy = row+1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(contentCommentText, c);
        add(contentCommentText);        
        
        //Mime type
        JLabel mimeTypeLabel =
		  new JLabel(resourceBundle.getString("label.mimeType"),
			     SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(mimeTypeLabel, c);
        add(mimeTypeLabel);
        
        mimeTypeCombo = new JComboBox(new DefaultComboBoxModel(mimeTypes));
        mimeTypeCombo.setEditable(true);
        c.gridx = 0;
        c.gridy = row + 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(mimeTypeCombo, c);
        add(mimeTypeCombo);
        
        //Concept
        JLabel conceptLabel =
		  new JLabel(resourceBundle.getString("label.objectType"),
			     SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = row + 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(conceptLabel, c);
        add(conceptLabel);
        
        objectTypeText = new JTextField();
        objectTypeText.setEditable(false);
        c.gridx = 1;
        c.gridy = row + 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(objectTypeText, c);
        add(objectTypeText);
        
        //Select ClassificationScheme or Concept
        JButton showSchemeButton =
	    new JButton(resourceBundle.getString("button.selectConcept"));
        showSchemeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                getSelectedConcept();
            }
        });
        c.gridx = 1;
        c.gridy = row + 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(showSchemeButton, c);
        add(showSchemeButton);
        
        isOpaqueCheckBox =
		  new JCheckBox(resourceBundle.getString("label.isOpaque"));
        isOpaqueCheckBox.setSelected(false);
        c.gridx = 0;
        c.gridy = row + 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(isOpaqueCheckBox, c);
        add(isOpaqueCheckBox);
        
        JPanel buttonPanel = new JPanel();
        c.gridx = 1;
        c.gridy = row + 5;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(buttonPanel, c);
        add(buttonPanel);
        
        //Select ClassificationScheme or Concept
        JButton chooseRepositoryItemButton =
	    new JButton(resourceBundle.getString("button.chooseRepositoryItem"));
        chooseRepositoryItemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                getRepositoryItemFromFile();
            }
        });
        buttonPanel.add(chooseRepositoryItemButton, BorderLayout.NORTH);
        
        //Select ClassificationScheme or Concept
        JButton removeRepositoryItemButton =
	    new JButton(resourceBundle.getString("button.removeRepositoryItem"));
        removeRepositoryItemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                removeRepositoryItem();
            }
        });
        buttonPanel.add(removeRepositoryItemButton, BorderLayout.SOUTH);
    }
    
    private void getSelectedConcept() {
        ConceptsTreeDialog dialog =
	    ConceptsTreeDialog.showSchemes(this, true, true);

        if (dialog.getStatus() == JBDialog.OK_STATUS) {
            ArrayList selectedObjects = dialog.getSelectedObjects();
            
            int selectionCount = selectedObjects.size();
            
            if (selectionCount == 0) {
                RegistryBrowser.displayError(
                resourceBundle.getString("error.mustSelectConcept"));
            } else {
                if (selectionCount > 1) {
                    RegistryBrowser.displayError(
                    resourceBundle.getString("error.onlyOneSelection"));
                }
                
                ExtrinsicObject eo = (ExtrinsicObject) model;
                Object obj = selectedObjects.get(selectionCount - 1);
                
                try {
                    if (obj instanceof Concept) {
                        Concept concept = (Concept) obj;
                        ((org.freebxml.omar.client.xml.registry.infomodel.ExtrinsicObjectImpl) eo).setObjectType(concept);
                        objectTypeText.setText(concept.getValue());
                    } else {
                        throw new JAXRException(resourceBundle.getString("error.mustSelectConceptOnly"));
                    }
                } catch (JAXRException e) {
                    RegistryBrowser.displayError(e);
                }
            }
        }
    }
    
    private void getRepositoryItemFromFile() {
        if (model != null) {
            ExtrinsicObject eo = (ExtrinsicObject) model;
            
            try {
                int returnVal = chooser.showOpenDialog(this);
                
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File repositoryItemFile = chooser.getSelectedFile();

                    //??Need to encapsulate in a util method and later include in JAXR API 2.0
		    FileDataSource repositoryItemFDS =
			new FileDataSource(repositoryItemFile);
                    DataHandler repositoryItemDH =
			new DataHandler(repositoryItemFDS);
                    eo.setRepositoryItem(repositoryItemDH);
		    String currentMimeType =
			((String) mimeTypeCombo.getSelectedItem());
		    if (currentMimeType == null) {
			mimeTypeCombo.setSelectedItem(repositoryItemFDS.getContentType().toString());
		    }
                }
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        }
    }
    
    private void removeRepositoryItem() {
        if (model != null) {
            ExtrinsicObject eo = (ExtrinsicObject) model;
            
            try {
                eo.setRepositoryItem(null);
		eo.setMimeType(null);
		mimeTypeCombo.setSelectedItem(null);
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        }
    }
    
    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, ExtrinsicObject.class);
        
        super.setModel(obj);
        
        ExtrinsicObjectImpl eo = (ExtrinsicObjectImpl) obj;
        
        try {
            if (eo != null) {
                isOpaqueCheckBox.setSelected(eo.isOpaque());
                
                Concept objectType = eo.getObjectType();
                
                if (objectType != null) {
                    objectTypeText.setText(objectType.getValue());
                }
                
                VersionInfoType contentVersionInfo = eo.getContentVersionInfo();
                if (contentVersionInfo != null) {
                    String contentVersionName = contentVersionInfo.getVersionName();
                    contentVersionNameText.setText(contentVersionName);
                    
                    String contentComment = contentVersionInfo.getComment();
                    contentCommentText.setText(contentComment);
                }
                
                
                String mimeType = eo.getMimeType();
                mimeTypeCombo.insertItemAt(mimeType, 0);
                mimeTypeCombo.setSelectedIndex(0);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }
    
    public Object getModel() throws JAXRException {
        super.getModel();
        
        try {
            if (model != null) {
                ExtrinsicObjectImpl eo = (ExtrinsicObjectImpl) model;

                VersionInfoType contentVersionInfo = eo.getContentVersionInfo();
                if (contentVersionInfo == null) {
                    contentVersionInfo = BindingUtility.getInstance().rimFac.createVersionInfoType();
                }
                String versionName = versionNameText.getText();
                String contentComment = contentCommentText.getText();
                
                if (versionName != null && versionName.length() > 0) {
                    contentVersionInfo.setVersionName(versionName);
                } else {
                    contentVersionInfo.setVersionName(versionName);
                }

                if (contentComment != null && contentComment.length() > 0) {
                    contentVersionInfo.setComment(contentComment);
                } else {
                    contentVersionInfo.setComment(contentComment);
                }
                eo.setContentVersionInfo(contentVersionInfo);

            eo.setOpaque(isOpaqueCheckBox.isSelected());
	    String mimeType = (String) mimeTypeCombo.getSelectedItem();
	    if (mimeType == null || eo.getRepositoryItem() == null) {
		eo.setMimeType(null);
	    } else {
		eo.setMimeType(mimeType);
	    }

                RegistryBrowser.getInstance().getRootPane().updateUI();
            }
        } catch (javax.xml.bind.JAXBException e) {
            throw new JAXRException(e);
        }
        
        return model;
    }
    
    protected void validateInput() throws JAXRException {
        super.validateInput();
        
        ExtrinsicObject eo = (ExtrinsicObject) model;
        String contentVersionName = contentVersionNameText.getText();        
        if (contentVersionName.length() > 16) {
            throw new JAXRException(resourceBundle.getString("error.registryObjectVersionLength"));
        }
        
        String contentComment = contentCommentText.getText();        
        if (contentComment.length() > 256) {
            throw new JAXRException(resourceBundle.getString("error.registryObjectVersionCommentLength"));
        }
        
    }
    
    public void clear() throws JAXRException {
        super.clear();
        isOpaqueCheckBox.setSelected(false);
        contentVersionNameText.setText("");
        contentCommentText.setText("");
    }
    
    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);
        
        contentVersionNameText.setEditable(false); //Only modified by registry
        contentCommentText.setEditable(editable);
    }
}
