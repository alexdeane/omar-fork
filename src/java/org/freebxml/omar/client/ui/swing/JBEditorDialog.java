/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/JBEditorDialog.java,v 1.7 2005/03/10 14:17:41 vikram_blr Exp $
 * ====================================================================
 */

/**
 * $Header:
 *
 *
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;

import java.beans.PropertyChangeListener;

import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import javax.xml.registry.JAXRException;


/**
 * A JBDialog that has an object at its model and serves as a UI editor for that model object.
 */
public class JBEditorDialog extends JBDialog {
    JBPanel panel = null;

    public JBEditorDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        JBEditorDialog_initialize();
    }

    public JBEditorDialog(JDialog parent, boolean modal) {
        super(parent, modal);
        JBEditorDialog_initialize();
    }

    private void JBEditorDialog_initialize() {
    }

    public Object getModel() throws JAXRException {
        Object model = null;

        if (panel != null) {
            model = panel.getModel();
        }

        return model;
    }

    public void setModel(Object model) throws JAXRException {
        status = JBDialog.CANCEL_STATUS;

        Class oldClass = null;
        Class newClass = null;

        Object oldModel = getModel();

        if ((model != null)) {
            if (oldModel != null) {
                oldClass = oldModel.getClass();
            }

            newClass = model.getClass();

            if (newClass != oldClass) {
                try {
                    setTitle(getDialogTitleFromModelClass(newClass));
                    
                    Class panelClass = null;
                    
                    while (true) {
                        try {
                            //First see if panelClass defined for this modelClass
                            panelClass = getPanelClassFromModelClass(newClass);
                            break;
                        } catch (ClassNotFoundException e) {
                            newClass = newClass.getSuperclass();
                            if (newClass.getName().startsWith("java.")) {
                                throw e;
                            }
                        }
                    }

                    //Now create an instance of the panelClass
                    panel = (JBPanel) panelClass.newInstance();
                    panel.setModel(model);
                    panel.setEditable(editable);

                    //Add panel to dialog
                    JScrollPane scrPane = new JScrollPane(panel);
                    mainPanel.removeAll();
                    mainPanel.add(scrPane, BorderLayout.CENTER);
                    pack();
                } catch (ClassNotFoundException e) {
                    throw new JAXRException(e);
                } catch (InstantiationException e) {
                    throw new JAXRException(e);
                } catch (IllegalAccessException e) {
                    throw new JAXRException(e);
                }
            }
        }
    }
    
    private String getDialogTitleFromModelClass(Class modelClass) {
        String title = "";
        
        String modelClassName = modelClass.getName();
        String packagePrefix = modelClassName.substring(0,
                modelClassName.lastIndexOf(".") + 1);
        modelClassName = modelClassName.substring(modelClassName.lastIndexOf(
                    ".") + 1);

        if (modelClassName.endsWith("Impl")) {
            //Remove Impl suffix for JAXR provider Impl classes
            modelClassName = modelClassName.substring(0,
                    modelClassName.length() - 4);
        }

        title = modelClassName;
        return title;
    }
    
    private Class getPanelClassFromModelClass(Class modelClass) throws ClassNotFoundException {
        Class panelClass = null;
        
        //Need to get <modelClass>Panel from <modelClass>.
        String modelClassName = modelClass.getName();
        String packagePrefix = modelClassName.substring(0,
                modelClassName.lastIndexOf(".") + 1);
        modelClassName = modelClassName.substring(modelClassName.lastIndexOf(
                    ".") + 1);

        if (modelClassName.endsWith("Impl")) {
            //Remove Impl suffix for JAXR provider Impl classes
            modelClassName = modelClassName.substring(0,
                    modelClassName.length() - 4);
        }

        String panelClassName = null;

        try {
            //First try same package as model class for panel class's package
                                    // I18N: Do not localize next statement.
            panelClassName = packagePrefix + modelClassName +
                "Panel";
            panelClass = Class.forName(panelClassName);
        } catch (ClassNotFoundException e) {
            //Next try default package as a fallback for panel class's package
                                    // I18N: Do not localize next statement.
            panelClassName = "org.freebxml.omar.client.ui.swing." +
                modelClassName + "Panel";
            panelClass = Class.forName(panelClassName);
        }
        
        return panelClass;
    }

    protected void okAction() {
        super.okAction();

        if (status == JBDialog.OK_STATUS) {
            try {
				// I18N: Do not localize next statement.
                firePropertyChange(PROPERTY_DIALOG_OK, "OK", getModel());
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        }
    }

    public static JBEditorDialog showObjectDetails(Component parent,
        Object obj, boolean modal, boolean editable) {
        JBEditorDialog dialog = null;

        try {
            dialog = null;

            Window window = (Window) (SwingUtilities.getRoot(parent));

            if (window instanceof JFrame) {
                dialog = new JBEditorDialog((JFrame) window, modal);
            } else if (window instanceof JDialog) {
                dialog = new JBEditorDialog((JDialog) window, modal);
            }

            dialog.setModel(obj);
            dialog.setEditable(editable);

            if (obj instanceof PropertyChangeListener) {
                dialog.addPropertyChangeListener((PropertyChangeListener) obj);
            }

            dialog.pack();

			if (dialog.getComponentOrientation().isLeftToRight()) {
				dialog.setLocation((int) (window.getLocation().getX() + 30),
								   (int) (window.getLocation().getY() + 30));
			} else {
				dialog.setLocation((int) (window.getWidth() -
										  dialog.getWidth() - 30),
								   (int) (window.getLocation().getY() + 30));
			}

            dialog.setVisible(true);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return dialog;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        if (panel != null) {
            panel.validateInput();
        }
    }

    public void clear() throws JAXRException {
        super.clear();

        if (panel != null) {
            panel.clear();
        }
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        if (panel != null) {
            panel.setEditable(editable);
        }
    }
}
