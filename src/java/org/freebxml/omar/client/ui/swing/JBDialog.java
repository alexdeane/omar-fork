/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/JBDialog.java,v 1.11 2005/09/02 07:24:54 selswannes Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/JBDialog.java,v 1.11 2005/09/02 07:24:54 selswannes Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import javax.xml.registry.JAXRException;


/**
 * Base class for all JAXR Browser dialogs.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBDialog extends JDialog implements PropertyChangeListener {
    public static final int CANCEL_STATUS = 0;
    public static final int OK_STATUS = 1;
    public static final int ERROR_STATUS = 2;
    public static final String PROPERTY_DIALOG_OK = "DIALOG_OK";

    protected JavaUIResourceBundle resourceBundle =
		JavaUIResourceBundle.getInstance();

    //Parent for JBPanel
    protected JPanel mainPanel = null;

    //Parent for OK / Cancel buttons
    protected JPanel buttonPanel = null;
    protected int status = CANCEL_STATUS;
    protected boolean editable = false;
    protected GridBagConstraints c = new GridBagConstraints();
    protected GridBagLayout gbl = new GridBagLayout();
    private JButton okButton = null;
    private JButton cancelButton = null;

    public JBDialog(JDialog parent, boolean modal) {
        this(parent, modal, null);
    }

    public JBDialog(JFrame parent, boolean modal) {
        this(parent, modal, null);
    }

    public JBDialog(JDialog parent, boolean modal, boolean editable) {
        this(parent, modal, editable, null);
    }

    public JBDialog(JFrame parent, boolean modal, boolean editable) {
        this(parent, modal, editable, null);
    }

    public JBDialog(JDialog parent, boolean modal, JPanel mainPanel) {
        this(parent, modal, RegistryBrowser.getInstance().isAuthenticated(), mainPanel);
    }

    public JBDialog(JFrame parent, boolean modal, JPanel mainPanel) {
        this(parent, modal, RegistryBrowser.getInstance().isAuthenticated(), mainPanel);
    }

    public JBDialog(JDialog parent, boolean modal, boolean editable, JPanel mainPanel) {
        super(parent, modal);
        this.editable = editable;
        this.mainPanel = mainPanel;
		this.setLocale(parent.getLocale());
		//this.applyComponentOrientation(ComponentOrientation.getOrientation(getLocale()));
		this.setComponentOrientation(ComponentOrientation.getOrientation(getLocale()));
        jbDialog_initialize();
    }

    public JBDialog(JFrame parent, boolean modal, boolean editable, JPanel mainPanel) {
        super(parent, modal);
        this.editable = editable;
        this.mainPanel = mainPanel;
		this.setLocale(parent.getLocale());
		// Should be unnecessary:
		//this.applyComponentOrientation(ComponentOrientation.getOrientation(getLocale()));
		this.setComponentOrientation(ComponentOrientation.getOrientation(getLocale()));
        jbDialog_initialize();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void jbDialog_initialize() {
        Container contentPane = getContentPane();
        contentPane.setLayout(gbl);

        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
        }

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(mainPanel, c);
        contentPane.add(mainPanel);

        //The buttonPanel       
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

        okButton = new JButton(resourceBundle.getString("button.ok"));
        okButton.setVisible(editable);
        okButton.setEnabled(editable);
        okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    okAction();
                }
            });

        cancelButton = new JButton(resourceBundle.getString("button.cancel"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelAction();
                }
            });
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        if (editable) {
            getRootPane().setDefaultButton(okButton);
        } else {
            getRootPane().setDefaultButton(cancelButton);
        }

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(buttonPanel, c);
        contentPane.add(buttonPanel);

        Point lastDialogLocation = getParent().getLocation();
        lastDialogLocation.x += 40;
        lastDialogLocation.y += 40;
        setLocation(lastDialogLocation);

        pack();

        //add listener for 'locale' bound property
        RegistryBrowser.getInstance().
			addPropertyChangeListener(RegistryBrowser.PROPERTY_LOCALE,
									  this);

        //add listener for 'authenticated' bound property
        RegistryBrowser.getInstance().
			addPropertyChangeListener(RegistryBrowser.PROPERTY_AUTHENTICATED,
									  this);
    }

    public int getStatus() {
        return status;
    }

    protected void okAction() {
        try {
            validateInput();
            status = OK_STATUS;
            dispose();
        } catch (Exception e) {
            status = ERROR_STATUS;
            RegistryBrowser.displayError(e);
        }
    }

    protected void cancelAction() {
        status = CANCEL_STATUS;
        dispose();
    }

    protected void validateInput() throws JAXRException {
    }

    protected void clear() throws JAXRException {
        cancelAction();
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
        okButton.setVisible(editable);
        okButton.setEnabled(editable);

        if (editable) {
            getRootPane().setDefaultButton(okButton);
        } else {
            getRootPane().setDefaultButton(cancelButton);
        }
    }

    /**
     * Gets whether this dialog is read-only or editable.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Listens to property changes in the bound property RegistryBrowser.PROPERTY_AUTHENTICATED.
     * Calls setEditable according to user authentication state.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_AUTHENTICATED)) {
            boolean authenticated = ((Boolean) ev.getNewValue()).booleanValue();

            setEditable(authenticated);
		} else if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_LOCALE)) {
			processLocaleChange((Locale) ev.getNewValue());
		}
    }

	/**
	 * Processes a change in the bound property
	 * RegistryBrowser.PROPERTY_LOCALE.
	 */
	protected void processLocaleChange(Locale newLocale) {
		resourceBundle = JavaUIResourceBundle.getInstance();

		setLocale(newLocale);
		applyComponentOrientation(ComponentOrientation.getOrientation(newLocale));

		this.updateUIText();
		//invalidate();
		//validate();
		//getMainPanel().doLayout();
		//pack();
		//setSize(getPreferredSize());
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
	protected void updateUIText() {
        okButton.setText(resourceBundle.getString("button.ok"));
        cancelButton.setText(resourceBundle.getString("button.cancel"));
	}
}
