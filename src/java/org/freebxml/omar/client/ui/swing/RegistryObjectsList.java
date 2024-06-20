/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/RegistryObjectsList.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/RegistryObjectsList.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import javax.xml.registry.JAXRException;


/**
 * Specialized JList for showing RegistryObjects.
 * Supports drag&drop of Classification objects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryObjectsList extends JBList {
    public RegistryObjectsList(String interfaceName) {
        super(interfaceName);
    }

    /**
     *
     */
    public RegistryObjectsList(String interfaceName,
        RegistryObjectsListModel model) {
        super(interfaceName, model);

        //setDragEnabled(true);
        //setTransferHandler(new RegistryObjectsListTransferHandler());

        /*
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JComponent c = (JComponent)e.getSource();
                TransferHandler th = c.getTransferHandler();
                th.exportAsDrag(c, e, TransferHandler.COPY);
            }
        };
        addMouseListener(ml);
        */
    }

    /**
     * TransferHandler for RegistryObjectsList
     *
     */
    public class RegistryObjectsListTransferHandler extends TransferHandler {
        boolean isCut = false;

        public boolean canImport(JComponent comp, DataFlavor[] flavors) {
            return true;
        }

        protected final Transferable createTransferable(JComponent c) {
            if (c instanceof RegistryObjectsList) {
                RegistryObjectsList roList = (RegistryObjectsList) c;

                try {
                    ArrayList registryObjects = ((RegistryObjectsListModel) roList.getModel()).getModels();

                    if ((registryObjects != null) &&
                            (registryObjects.size() > 0)) {
                        return create(registryObjects);
                    }
                } catch (JAXRException e) {
                    RegistryBrowser.displayError(e);
                }
            }

            return null;
        }

        protected RegistryObjectsTransferable create(ArrayList registrObjects) {
            ArrayList registryObjects = null;

            try {
                registryObjects = ((RegistryObjectsListModel) RegistryObjectsList.this.getModel()).getModels();
            } catch (JAXRException e) {
                registryObjects = new ArrayList();
                RegistryBrowser.displayError(e);
            }

            return new RegistryObjectsTransferable(registryObjects);
        }

        protected void exportDone(JComponent comp, Transferable data, int action) {
            if (comp instanceof RegistryObjectsList &&
                    data instanceof RegistryObjectsTransferable) {
                /*
                Object[]    cells = ((GraphTransferable) data).getCells();
                JGraph      graph = (JGraph) comp;
                Point       p = insertionLocation;

                if (p == null && action == TransferHandler.MOVE) {
                removeCells(graph, cells);
                }
                else if (p != null && handle != null) {
                int mod = (action == TransferHandler.COPY)
                      ? InputEvent.CTRL_MASK : 0;

                handle.mouseReleased(new MouseEvent(comp, 0, 0, mod, p.x, p.y,
                                                1, false));
                }
                insertionLocation = null;
                    */
            }
        }

        public void exportToClipboard(JComponent compo, Clipboard clip,
            int action) {
            isCut = (action == TransferHandler.MOVE);
            super.exportToClipboard(compo, clip, action);
        }

        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        public boolean importData(JComponent comp, Transferable t) {
            try {
                if (comp instanceof RegistryObjectsList) {
                    RegistryObjectsList list = (RegistryObjectsList) comp;

                    if (t.isDataFlavorSupported(
                                RegistryObjectsTransferable.dataFlavor)) {
                        Object obj = t.getTransferData(RegistryObjectsTransferable.dataFlavor);
                        RegistryObjectsTransferable roTransferrable = (RegistryObjectsTransferable) obj;

                        ((RegistryObjectsListModel) RegistryObjectsList.this.getModel()).setModels(roTransferrable.getRegistryObjects());

                        return true;
                    }
                }
            } catch (Exception exception) {
            } finally {
                isCut = false;
            }

            return false;
        }
    }
}
