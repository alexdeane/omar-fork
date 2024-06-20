/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/swing/MappedDocumentListener.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.swing;

import javax.swing.event.DocumentEvent;


/**
 * Implements a document listener for a MappedPanel.
 *
 * The removeUpdate method only invokes update if the mapping is not
 * currently changing. Otherwise, the update might erase the text field,
 * trigger a new DocumentEvent and create an infinite loop.
 *
 * @author Fabian Ritzmann
 */
public abstract class MappedDocumentListener extends RegistryDocumentListener {
    /**
     * Initializes the object.
     *
     * @param p The panel on which the listener operates.
     *
     * @param error A text that is prepended to the exception that may be
     * thrown by the setText method.
     */
    public MappedDocumentListener(RegistryMappedPanel p, String error) {
        super(p, error);
    }

    /**
     * Only invokes update if the current mapping is not changing.
     *
     * @see javax.swing.event.DocumentListener#removeUpdate(DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) {
        if (!((RegistryMappedPanel) this.panel).mappingIsChanging()) {
            update(e);
        }
    }
}
