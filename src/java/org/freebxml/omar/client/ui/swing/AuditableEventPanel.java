/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/AuditableEventPanel.java,v 1.1 2004/10/25 23:45:04 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import javax.swing.BorderFactory;


/**
 * Panel to edit/inspect an AuditableEvent.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class AuditableEventPanel extends RegistryObjectPanel {
    /**
     * Creates new ServicePanel
     */
    public AuditableEventPanel() {
        super();

        setBorder(BorderFactory.createTitledBorder("AuditableEvent Details"));
    }
}
