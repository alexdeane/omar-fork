/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/HyperLinkLabel.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;


/*
 * Based upon code from:
 * http://forum.java.sun.com/thread.jsp?thread=328882&forum=57&message=1337973
 *
 */
public class HyperLinkLabel extends JLabel implements HyperLinkContainer {
    Color stdFG = Color.BLACK;
    Font stdFont;
    Font urlFont;
    Color urlNormalFG = Color.BLUE;
    Color urlHiliteFG = Color.RED;
    String url;
    MouseListener mouseListener;
    HyperLinkContainer linkContainer = null;

    public HyperLinkLabel() {
        this(null, null, 0);
    }

    public HyperLinkLabel(Icon image) {
        this(null, image, 0);
    }

    public HyperLinkLabel(Icon image, int horizontalAlignment) {
        this(null, image, horizontalAlignment);
    }

    public HyperLinkLabel(String text) {
        this(text, null, 0);
    }

    public HyperLinkLabel(String text, int horizontalAlignment) {
        this(text, null, horizontalAlignment);
    }

    public HyperLinkLabel(String text, Icon icon, int horizontalAlignment) {
        super(icon, horizontalAlignment);

        stdFG = this.getForeground();
        stdFont = this.getFont();

        //Make urlFont be bold and underlined
        urlFont = stdFont.deriveFont(Font.BOLD);

        //java.util.HashMap textAttributes = new java.util.HashMap(stdFont.getAttributes());
        //textAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        //urlFont = urlFont.deriveFont(textAttributes);
        setText(text);

        //Create mouse listener
        mouseListener = new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        String _url = getURL();

                        if ((_url != null) && (_url.length() > 0)) {
                            HyperLinker.displayURL(_url);
                        }
                    }

                    public void mouseEntered(MouseEvent e) {
                        String _url = getURL();

                        if ((_url != null) && (_url.length() > 0)) {
                            HyperLinkLabel.this.setCursor(new Cursor(
                                    Cursor.HAND_CURSOR));
                            HyperLinkLabel.this.setForeground(urlHiliteFG);
                        }
                    }

                    public void mouseExited(MouseEvent e) {
                        String _url = getURL();

                        if ((_url != null) && (_url.length() > 0)) {
                            HyperLinkLabel.this.setForeground(urlNormalFG);
                            HyperLinkLabel.this.setCursor(new Cursor(
                                    Cursor.DEFAULT_CURSOR));
                        }
                    }
                };
    }

    public String getURL() {
        if (this.linkContainer != null) {
            url = this.linkContainer.getURL();
        }

        return url;
    }

    public void setURL(String urlString) throws MalformedURLException {
        removeMouseListener(mouseListener);
        this.url = null;
        linkContainer = null;

        setForeground(stdFG);

        //setFont(stdFont);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        //Check if URL is valid
        URL _url = null;

        if (urlString != null) {
            _url = new URL(urlString);
        }

        if (_url != null) {
            //Valid URL. Use it.
            this.url = urlString;
            linkContainer = null;

            setForeground(urlNormalFG);

            //setFont(urlFont);
            addMouseListener(mouseListener);
        }
    }

    public HyperLinkContainer getHyperLinkContainer() {
        HyperLinkContainer _linkContainer = linkContainer;

        if (_linkContainer == null) {
            _linkContainer = this;
        }

        return _linkContainer;
    }

    public void setHyperLinkContainer(HyperLinkContainer linkContainer) {
        this.linkContainer = linkContainer;

        setForeground(urlNormalFG);

        //setFont(urlFont);
        addMouseListener(mouseListener);
    }

    public void setText(String text) {
        super.setText(text);

        //Check if URL is valid
        URL _url = null;

        try {
            _url = new URL(text);

            //setURL if text is a valid URL
            setURL(text);
        } catch (MalformedURLException e) {
            //No need to do anything. It is normal for tetx to not be a URL
        }
    }

    public static void main(String[] args) throws MalformedURLException {
        JFrame f = new JFrame("HyperLinkLabel Tester");
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        Container c = f.getContentPane();
        HyperLinkLabel lb = new HyperLinkLabel("click me");
        lb.setURL("http://ebxmlrr.sourceforge.net");

        c.add(lb);
        f.pack();
        f.show();
    }
}
