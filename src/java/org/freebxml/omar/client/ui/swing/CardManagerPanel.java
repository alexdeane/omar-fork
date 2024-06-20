/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/CardManagerPanel.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/CardManagerPanel.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 *
 *
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


//import org.oasis.ebxml.registry.bindings.rim.*;

/**
 * A panel that serves as a manager for a card panel.
 * It provides toggle buttons to control which card in card panel is showing.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class CardManagerPanel extends JBPanel {
    private GridBagConstraints c = new GridBagConstraints();
    private CardLayout cardLayout = null;
    protected JPanel selectorPanel = null;
    protected JPanel cardsPanel = null;
    protected String[] cards = null;
    protected JPanel[] cardPanels = null;
    ButtonGroup buttonGroup = null;
    HashMap cardToButtonMap = new HashMap();

    protected CardManagerPanel() {
    }

    public CardManagerPanel(String[] cards, JPanel[] cardPanels) {
        super();

        this.cards = cards;
        this.cardPanels = cardPanels;

        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);

        //The upper panel with radio buttons to select a card in card layout
        selectorPanel = createSelectorPanel();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(selectorPanel, c);
        add(selectorPanel);

        //Next is the panel containing all the cards
        cardsPanel = createCardsPanel();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(cardsPanel, c);
        add(cardsPanel);
    }

    private JPanel createSelectorPanel() {
        //It just has radio buttons that select from among throws cards
        JPanel selectorPanel = new JPanel();
        buttonGroup = new ButtonGroup();

        for (int i = 0; i < cards.length; i++) {
            final String card = cards[i];
            JRadioButton radioButton = new JRadioButton(card);

            if (i == 0) {
                radioButton.setSelected(true);
            }

            radioButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        showCardAction(card);
                    }
                });
            buttonGroup.add(radioButton);
            selectorPanel.add(radioButton);
            cardToButtonMap.put(card, radioButton);
        }

        return selectorPanel;
    }

    public void showCard(String card) {
        JRadioButton radioButton = (JRadioButton) cardToButtonMap.get(card);
        radioButton.doClick();
    }

    protected void showCardAction(String card) {
        cardLayout.show(cardsPanel, card);
    }

    private JPanel createCardsPanel() {
        JPanel cardsPanel = new JPanel();
        cardLayout = new CardLayout();
        cardsPanel.setLayout(cardLayout);

        for (int i = 0; i < cardPanels.length; i++) {
            cardsPanel.add(cardPanels[i], cards[i]);
        }

        return cardsPanel;
    }
}
