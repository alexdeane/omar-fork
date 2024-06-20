/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2006 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/ConceptsTreeModelSemaphore.java,v 1.1 2006/08/24 20:41:48 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import javax.swing.SwingUtilities;
import org.freebxml.omar.client.ui.swing.ConceptsTreeModel;

/**
 * Helper class used to hold a ConceptsTreeModel 'objectTypesTreeModel',
 * synchronizing its initialization and access to it.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
class ConceptsTreeModelSemaphore {
    /** The instance, used for synchronization. */
    private static ConceptsTreeModelSemaphore instance;
    /** The object we want to store. */
    private static ConceptsTreeModel objectTypesTreeModel;

    /** The Constructor, private. */
    private ConceptsTreeModelSemaphore() {
    }

    /** Getter for instance, initializes it on first call. */
    private synchronized static ConceptsTreeModelSemaphore getInstance() {
        if (instance == null) instance = new ConceptsTreeModelSemaphore();
        return instance;
    }

    /** Setter for 'objectTypesTreeModel'. Notifies all waiting threads. */
    public static void setObjectTypesTreeModel(ConceptsTreeModel
					       objectTypesTreeModel) {
        ConceptsTreeModelSemaphore.objectTypesTreeModel = objectTypesTreeModel;
        getInstance().doNotifyAll();
    }

    /** Getter for 'objectTypesTreeModel'. Put thread on wait() if not set yet. */
    public static ConceptsTreeModel getObjectTypesTreeModel() {
        // Do not hold Event Dispatch Thread!
        if (ConceptsTreeModelSemaphore.objectTypesTreeModel == null &&
            !SwingUtilities.isEventDispatchThread()) {
            getInstance().doWait();
        }
        return ConceptsTreeModelSemaphore.objectTypesTreeModel;
    }

    /** Synchronized method to put the calling thread on wait. */
    private synchronized void doWait() {
        try {
            wait();
        } catch (InterruptedException e) {
        }
    }

    /** Synchronized method to notify all waiting threads. */
    private synchronized void doNotifyAll() {
        notifyAll();
    }
}
