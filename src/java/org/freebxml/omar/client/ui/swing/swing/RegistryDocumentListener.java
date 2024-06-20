/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/swing/RegistryDocumentListener.java,v 1.4 2006/04/10 10:59:06 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.swing;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import javax.xml.registry.JAXRException;


/**
 * DocumentListener that factors out some commonly used code. This in an
 * implementation of the Strategy design pattern.
 *
 * The class implements the document listener methods and invokes an
 * abstract method setText. The setText method is intended to set the
 * entered text in the underlying model.
 *
 * @author Fabian Ritzmann
 */
public abstract class RegistryDocumentListener implements DocumentListener {
    protected final JPanel panel;
    protected String errorMessage;

    /**
     * Constructor.
     *
     * @param p Panel to which this listener belongs
     * @param error String that is prepended to error message if
     * setText throws an exception
     */
    public RegistryDocumentListener(JPanel p, String error) {
        this.panel = p;
        this.errorMessage = error;
    }

    /**
     * Invokes update.
     *
     * @see javax.swing.event.DocumentListener#insertUpdate(DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    /**
     * Invokes update.
     *
     * @see javax.swing.event.DocumentListener#removeUpdate(DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    /**
     * Invokes update.
     *
     * @see javax.swing.event.DocumentListener#changedUpdate(DocumentEvent)
     */
    public void changedUpdate(DocumentEvent e) {
        update(e);
    }

    /**
     * Gets the text from the document and calls setText. Displays a message
     * with an error if setText throws an exception.
     *
     * @param ev The event with changed text
     */
    protected void update(DocumentEvent ev) {
        Document doc = ev.getDocument();
        int docLength = doc.getLength();

        try {
            String text = doc.getText(0, docLength);
            setText(text);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.panel,
                this.errorMessage + ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Setter for error message prefix, to be used with updateUIText.
     *
     * @param error String that is prepended to error message if
     * setText throws an exception
     */
    public void setError(String error) {
        this.errorMessage = error;
    }

    /**
     * Should be implemented to set a new text string on the underlying
     * model.
     *
     * @param text The text in a document field
     *
     * @throws JAXRException Should be thrown if an exception in the
     * underlying model is triggered
     */
    protected abstract void setText(String text) throws JAXRException;
}
