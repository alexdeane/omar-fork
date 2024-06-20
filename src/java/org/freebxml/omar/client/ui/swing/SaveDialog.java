/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/SaveDialog.java,v 1.1 2005/06/24 07:29:21 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;

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
 * The Save Dialog allows configuration of save properties such as Versioning.
 *
 * @author  <a href="mailto:farrukh.najmi@sun.com">Farrukh Najmi</a> / Sun Microsystems
 */
public class SaveDialog extends JBDialog {
    private JCheckBox jchbVersionMetadata;
    private JCheckBox jchbVersionContent;
    private JPanel jPanel1;
    private ButtonGroup bgSelectionMode;
    
    /** Determine if versioning is on or off */
    private boolean versionMetadata = true;
    private boolean versionContent = true;
    
    
    /** Creates new form SaveDialog */
    public SaveDialog(JFrame parent,
            boolean modal) {
        super(parent, modal, true);
        SaveDialog_initialize();
    }
    
    /** Creates new form SaveDialog */
    public SaveDialog(JDialog parent,
            boolean modal) {
        super(parent, modal, true);
        SaveDialog_initialize();
    }
    
    private void SaveDialog_initialize() {
        setEditable(true);
        setTitle(resourceBundle.getString("dialog.save.title"));
        
        jPanel1 = getMainPanel();
        jPanel1.setLayout(new java.awt.GridBagLayout());
        
        GridBagConstraints gridBagConstraints;
        
        bgSelectionMode = new javax.swing.ButtonGroup();
        jchbVersionMetadata = new javax.swing.JCheckBox();
        jchbVersionContent = new javax.swing.JCheckBox();
        
        jchbVersionMetadata.setText(resourceBundle.getString("dialog.save.versionMetaData"));
        jchbVersionMetadata.setEnabled(true);
        jchbVersionMetadata.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jchbVersionMetadataActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 12, 0, 4);
        jPanel1.add(jchbVersionMetadata, gridBagConstraints);
        
        jchbVersionContent.setText(resourceBundle.getString("dialog.save.versionContent"));
        jchbVersionContent.setEnabled(true);
        jchbVersionContent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jchbVersionContentActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 12, 0, 4);
        jPanel1.add(jchbVersionContent, gridBagConstraints);
        
        pack();
    }
    
    private void jchbVersionMetadataActionPerformed(ActionEvent evt) {
        //If not version metadata then MUST NOT version content
        if (!jchbVersionMetadata.isSelected()) {
            jchbVersionContent.setSelected(false);
        }
    }
    
    private void jchbVersionContentActionPerformed(ActionEvent evt) {
        //If versioning content then MUST version metadata
        if (jchbVersionContent.isSelected()) {
            jchbVersionMetadata.setSelected(true);
        }
    }
    
    protected void okAction() {
        super.okAction();                
        
        dispose();
    }
    
    public boolean versionMetadata() {
        return jchbVersionMetadata.isSelected();
    }
    
    public boolean versionContent() {
        return jchbVersionContent.isSelected();
    }
    
    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
    protected void updateUIText() {
        super.updateUIText();
        
        setTitle(resourceBundle.getString("dialog.save.title"));
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame();
        SaveDialog saveDialog = new SaveDialog(frame, true);
        saveDialog.show();
        frame.dispose();
    }
}
