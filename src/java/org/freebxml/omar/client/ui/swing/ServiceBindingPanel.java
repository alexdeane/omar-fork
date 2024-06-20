/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ServiceBindingPanel.java,v 1.7 2005/02/13 23:32:01 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ServiceBindingPanel.java,v 1.7 2005/02/13 23:32:01 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.beans.PropertyChangeEvent;

import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ServiceBinding;


/**
 * Panel to edit/inspect a Service.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ServiceBindingPanel extends RegistryObjectPanel {
    JTextField accessURIText = null;
	TitledBorder serviceBindingPanelBorder = null;
	HyperLinkLabel accessURILabel = null;

    /**
     * Creates new ServiceBindingPanel
     */
    public ServiceBindingPanel() {
        super();
        serviceBindingPanelBorder =
			javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("title.serviceBinding"));
        setBorder(serviceBindingPanelBorder);

		accessURILabel =
			new HyperLinkLabel(resourceBundle.getString("label.accessURI"),
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
        gbl.setConstraints(accessURILabel, c);
        add(accessURILabel);

        accessURIText = new JTextField();
        accessURIText.setEditable(editable);
        c.gridx = 0;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(accessURIText, c);
        add(accessURIText);

        accessURILabel.setHyperLinkContainer(new HyperLinkContainer() {
                public String getURL() {
                    return (accessURIText.getText());
                }

                public void setURL(String url) {
                    accessURIText.setText(url);
                }
            });
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, ServiceBinding.class);

        super.setModel(obj);

        ServiceBinding binding = (ServiceBinding) obj;

        try {
            if (binding != null) {
                String accessURIStr = binding.getAccessURI();

                if (accessURIStr != null) {
                    accessURIText.setText(accessURIStr);
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            ServiceBinding serviceBinding = (ServiceBinding) model;

            String accessURITextStr = accessURIText.getText();
            serviceBinding.setAccessURI(accessURITextStr);

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();
    }

    public void clear() throws JAXRException {
        super.clear();
        accessURIText.setText("");
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        accessURIText.setEditable(editable);
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

        serviceBindingPanelBorder.
			setTitle(resourceBundle.getString("title.serviceBinding"));

		accessURILabel.setText(resourceBundle.getString("label.accessURI"));
	}
}
