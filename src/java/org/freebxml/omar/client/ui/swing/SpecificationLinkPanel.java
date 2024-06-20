/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/SpecificationLinkPanel.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/SpecificationLinkPanel.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import javax.swing.BorderFactory;


/**
 * Panel to edit/inspect a Service.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class SpecificationLinkPanel extends RegistryObjectPanel {
    /**
     * Creates new ServicePanel
     */
    public SpecificationLinkPanel() {
        super();

        setBorder(BorderFactory.createTitledBorder("Service Details"));
    }
}
