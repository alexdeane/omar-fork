/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ConceptsTreeDialog.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ConceptsTreeDialog.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


/**
 * Base class for all JAXR Browser dialogs.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ConceptsTreeDialog extends JBDialog {
    public static final String PROPERTY_SELECTED_CONCEPTS = "selectedConcepts";
    private ConceptsTree conceptsTree = null;

    public ConceptsTreeDialog(JDialog parent, boolean modal) {
        super(parent, modal);
        ConceptsTreeDialog_initialize();
    }

    public ConceptsTreeDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        ConceptsTreeDialog_initialize();
    }

    void ConceptsTreeDialog_initialize() {
        setTitle("Classification Schemes");

        GridBagConstraints c = new GridBagConstraints();
        GridBagLayout gbl1 = new GridBagLayout();

        //Top level panel
        JPanel panel = mainPanel;
        panel.setLayout(gbl1);

        //The conceptsTree
        conceptsTree = new ConceptsTree(true);

        JScrollPane conceptsTreePane = new JScrollPane(conceptsTree);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl1.setConstraints(conceptsTreePane, c);
        panel.add(conceptsTreePane);

        pack();
    }

    ConceptsTree getModel() {
        return conceptsTree;
    }

    protected void okAction() {
        super.okAction();

        if (conceptsTree != null) {
            String test = new String();
            firePropertyChange(PROPERTY_SELECTED_CONCEPTS, test,
                getSelectedObjects());

            //System.err.println("firePropertyChange");
        } else {
            //System.err.println("Missed firePropertyChange " + conceptsTree);
        }

        dispose();
    }

    public ArrayList getSelectedObjects() {
        ArrayList selectedObjects = null;

        if (conceptsTree != null) {
            selectedObjects = conceptsTree.getSelectedObjects();
        }

        return selectedObjects;
    }

    public ArrayList getSelectedConcepts() {
        ArrayList selectedObjects = null;

        if (conceptsTree != null) {
            selectedObjects = conceptsTree.getSelectedConcepts();
        }

        return selectedObjects;
    }

    public ArrayList getSelectedClassificationSchemes() {
        ArrayList selectedObjects = null;

        if (conceptsTree != null) {
            selectedObjects = conceptsTree.getSelectedClassificationSchemes();
        }

        return selectedObjects;
    }

    public static ConceptsTreeDialog showSchemes(Component parent,
        boolean modal, boolean editable) {
        ConceptsTreeDialog dialog = null;
        RegistryBrowser.setWaitCursor();

        Window window = (Window) (SwingUtilities.getRoot(parent));

        if (window instanceof JFrame) {
            dialog = new ConceptsTreeDialog((JFrame) window, modal);
        } else if (window instanceof JDialog) {
            dialog = new ConceptsTreeDialog((JDialog) window, modal);
        }

        dialog.setEditable(editable);
        dialog.setLocation((int) (window.getLocation().getX() + 30),
            (int) (window.getLocation().getY() + 30));
        dialog.setVisible(true);

        RegistryBrowser.setDefaultCursor();

        return dialog;
    }

    public static void clearCache() {
        ConceptsTree.clearCache();
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        if (this.editable != editable) {
            super.setEditable(editable);
            conceptsTree.setEditable(editable);
        }
    }
}
