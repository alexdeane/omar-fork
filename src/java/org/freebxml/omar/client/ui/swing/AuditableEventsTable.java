/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/AuditableEventsTable.java,v 1.5 2005/05/03 17:43:48 dougb62 Exp $
 * ====================================================================
 */

/**
 * $Header:
 */
package org.freebxml.omar.client.ui.swing;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;


/**
 *
 * @author <a href="mailto:nikola.stojanovic@acm.org">Nikola Stojanovic</a>
 */
public class AuditableEventsTable extends JTable {
    protected JavaUIResourceBundle resourceBundle = JavaUIResourceBundle.getInstance();

    final AuditableEventsTableModel tableModel;

    public AuditableEventsTable(AuditableEventsTableModel model) {
        super(model);
        tableModel = model;
        setToolTipText(resourceBundle.getString("tip.auditableEventsTable"));
        setRowHeight(getRowHeight() * 2);
    }

    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
    }

    public void setVisible(boolean makeVisible) {
        if (makeVisible) {
        }

        super.setVisible(makeVisible);
    }
}
