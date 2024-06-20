/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/swing/MappedPanel.java,v 1.2 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.swing;


/**
 * A panel that displays a MappedModel. The interface methods help the
 * document listener to decide whether it needs to update a field or not.
 *
 * @author Fabian Ritzmann
 */
public interface MappedPanel {
    /**
     * Used by the MappedDocumentListener when it is processing a changed
     * text field.
     *
     * @param changing Set to true when MappedDocumentListener processes
     * a changed text field; set to false when processing is finished
     */
    void setMappingIsChanging(boolean changing);

    /**
     * Reports whether a text field change is currently processed.
     *
     * @return boolean true, if text field change is processed, false
     * otherwise
     */
    boolean mappingIsChanging();
}
