/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/RegistryObjectsTransferable.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */
/*
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/RegistryObjectsTransferable.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.Serializable;

import java.util.ArrayList;


/**
 * An object that represents the clipboard contents for a ArrayList of RegistryObjects selection.
 *
 * The object has two representations:
 * <p>
 * 1. Richer: Object representtaion
 * 2. Plain: plain text representation.
 *
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 *
 */
public class RegistryObjectsTransferable
    extends com.jgraph.plaf.basic.BasicTransferable implements Serializable,
        ClipboardOwner {
    /** Local Machine Reference Data Flavor. */
    public static DataFlavor dataFlavor;

    /* Local Machine Reference Data Flavor. */
    static {
        DataFlavor localDataFlavor;

        try {
            localDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                    "; class=org.freebxml.omar.client.ui.swing.RegistryObjectsTransferable");
        } catch (ClassNotFoundException cnfe) {
            localDataFlavor = null;
        }

        dataFlavor = localDataFlavor;
    }

    private ArrayList registryObjects = null;

    /**
     * Constructs a new transferable selection for <code>cells</code>,
     * <code>cs</code>and <code>attrMap</code>.
     */
    public RegistryObjectsTransferable(ArrayList registryObjects) {
        this.registryObjects = registryObjects;
    }

    /**
     * Returns the <code>registryObjects</code> that represent the selection.
     */
    public ArrayList getRegistryObjects() {
        return registryObjects;
    }

    // from ClipboardOwner
    public void lostOwnership(Clipboard clip, Transferable contents) {
        // do nothing
    }

    // --- Richer ----------------------------------------------------------

    /**
     * Returns the jvm-localreference flavors of the transferable.
     */
    public DataFlavor[] getRicherFlavors() {
        return new DataFlavor[] { dataFlavor };
    }

    /**
     * Fetch the data in a jvm-localreference format.
     */
    public Object getRicherData(DataFlavor flavor)
        throws UnsupportedFlavorException {
        if (flavor.equals(dataFlavor)) {
            return this;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    // --- Plain ----------------------------------------------------------

    /**
     * Returns true if the transferable support a text/plain format.
     */
    public boolean isPlainSupported() {
        return true;
    }

    /**
     * Fetch the data in a text/plain format.
     */
    public String getPlainData() {
        return "have'nt implemented this yet";
    }
}
