package cluedo_game;

import javax.swing.*;

public class testAccuseMenu {
    public static void main(String[] args) {
        Deck deck = new Deck();
        GameLogic.deck = deck;
        deck.fillMurderEnvelope();
        Token white = new Token(0, 9, "White", 0);
        Token green = new Token(0, 14, "Green", 1);

        new AccuseMenu(new JFrame(), new JPanel(), white);
    }
}
