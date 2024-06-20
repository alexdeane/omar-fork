/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/StringBufferPanel.java,v 1.5 2004/03/16 14:24:16 tonygraham Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/StringBufferPanel.java,v 1.5 2004/03/16 14:24:16 tonygraham Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;


/**
 * Panel for String
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class StringBufferPanel extends JBPanel {
    JTextField strText = null;

    /**
     * Used for displaying objects
     */
    public StringBufferPanel() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        JLabel strLabel = new JLabel("String value:", SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(strLabel, c);
        add(strLabel);

        strText = new JTextField();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.75;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(strText, c);
        add(strText);
    }

    public StringBuffer getStringBuffer() throws JAXRException {
        StringBuffer str = null;

        if (model != null) {
            str = (StringBuffer) getModel();
        }

        return str;
    }

    public void setStringBuffer(StringBuffer str) throws JAXRException {
        setModel(str);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, StringBuffer.class);

        super.setModel(obj);

        StringBuffer str = (StringBuffer) obj;

        if (str != null) {
            strText.setText(str.toString());
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            StringBuffer str = (StringBuffer) model;

            str.replace(0, str.length(), strText.getText());

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();
    }

    public void clear() throws JAXRException {
        super.clear();
        strText.setText("");
    }
}
