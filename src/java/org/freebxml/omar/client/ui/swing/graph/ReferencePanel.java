/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/graph/ReferencePanel.java,v 1.4 2004/09/17 14:47:24 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.graph;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryObject;


/**
 * A panel that serves as an editor for setting a reference link between two RegistryObjects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ReferencePanel extends JPanel {
    private RegistryObject src = null;
    private RegistryObject target = null;
    private GridBagConstraints c = new GridBagConstraints();
    String[] refAttributes = {  };
    JComboBox refAttributeCombo = null;
    boolean isCollectionRef = false;

    /**
     * Class Constructor.
     */
    public ReferencePanel(RegistryObject src, RegistryObject target) {
        this.src = src;
        this.target = target;

        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);

        JLabel refAttributeLabel = new JLabel("Reference Attribute:",
                SwingConstants.LEFT);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(refAttributeLabel, c);
        add(refAttributeLabel);

        refAttributeCombo = new JComboBox(getReferenceAttributes());
        refAttributeCombo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                }
            });

        refAttributeCombo.setEditable(true);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(refAttributeCombo, c);
        add(refAttributeCombo);
    }

    public void setReferenceAttributeOnSourceObject() throws JAXRException {
        String referenceAttribute = (String) refAttributeCombo.getSelectedItem();

        //Now use Refelection API to add target to src
        try {
            Class srcClass = src.getClass();
            Class targetClass = target.getClass();
            Class registryObjectClass = null;

            String targetInterfaceName = targetClass.getName();
            targetInterfaceName = targetInterfaceName.substring(targetInterfaceName.lastIndexOf(
                        ".") + 1);

            if (targetInterfaceName.endsWith("Impl")) {
                //Remove Impl suffix for JAXR provider Impl classes
                targetInterfaceName = targetInterfaceName.substring(0,
                        targetInterfaceName.length() - 4);
            }

            targetInterfaceName = "javax.xml.registry.infomodel." +
                targetInterfaceName;

            ClassLoader classLoader = srcClass.getClassLoader();

            try {
                targetClass = classLoader.loadClass(targetInterfaceName);
                registryObjectClass = classLoader.loadClass(
                        "javax.xml.registry.infomodel.RegistryObject");
            } catch (ClassNotFoundException e) {
                throw new JAXRException("No JAXR interface found by name " +
                    targetInterfaceName);
            }

            String suffix = referenceAttribute.substring(0, 1).toUpperCase() +
                referenceAttribute.substring(1, referenceAttribute.length());
            Method method = null;
            Class[] paramTypes = new Class[1];

            //See if there is a simple attribute of this name using type of targetObject
            try {
                paramTypes[0] = targetClass;
                method = srcClass.getMethod("set" + suffix, paramTypes);

                Object[] params = new Object[1];
                params[0] = target;
                method.invoke(src, params);
                isCollectionRef = false;

                return;
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
            }

            //See if there is a simple attribute of this name using base type RegistryObject
            try {
                paramTypes[0] = registryObjectClass;
                method = srcClass.getMethod("set" + suffix, paramTypes);

                Object[] params = new Object[1];
                params[0] = target;
                method.invoke(src, params);
                isCollectionRef = false;

                return;
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
            }

            //See if there is a addCXXX method for suffix of XXX ending in "s" for plural
            if (suffix.endsWith("s")) {
                suffix = suffix.substring(0, suffix.length() - 1);
            }

            try {
                paramTypes[0] = targetClass;
                method = srcClass.getMethod("add" + suffix, paramTypes);

                Object[] params = new Object[1];
                params[0] = target;
                method.invoke(src, params);
                isCollectionRef = true;

                return;
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
            }

            //See if there is a addCXXX method for suffix of XXX ending in "es" for plural
            if (suffix.endsWith("e")) {
                suffix = suffix.substring(0, suffix.length() - 1);
            }

            try {
                paramTypes[0] = targetClass;
                method = srcClass.getMethod("add" + suffix, paramTypes);

                Object[] params = new Object[1];
                params[0] = target;
                method.invoke(src, params);
                isCollectionRef = true;

                return;
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
            }

            throw new JAXRException("No method found for reference attribute " +
                referenceAttribute + " for src object of type " +
                srcClass.getName());
        } catch (IllegalArgumentException e) {
            throw new JAXRException(e);
        } catch (InvocationTargetException e) {
            throw new JAXRException(e.getCause());
        } catch (ExceptionInInitializerError e) {
            throw new JAXRException(e);
        }
    }

    public String getReferenceAttribute() {
        String refAttribute = (String) refAttributeCombo.getSelectedItem();

        return refAttribute;
    }

    public String[] getReferenceAttributes() {
        return refAttributes;
    }

    public void setReferenceAttributes(String[] refAttributes) {
        this.refAttributes = refAttributes;
        refAttributeCombo.setModel(new DefaultComboBoxModel(refAttributes));
    }
}
