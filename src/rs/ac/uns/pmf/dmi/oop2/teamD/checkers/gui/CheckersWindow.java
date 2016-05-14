package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.gui;

import javax.swing.*;
import java.awt.*;


/**
 * Our checkers game window
 * Apart from creating and maintaining gui it responds when other player sends his move
 */

public class CheckersWindow extends JFrame {

    private static final int NUM_BTN = 100;

    private JButton[] fields;
    private Icon bluePawn;
    private Icon orangePawn;
    private Icon blueQueen;
    private Icon orangeQueen;


    public CheckersWindow() {
        bluePawn = new ImageIcon("res\\blue.png");
        orangePawn = new ImageIcon("res\\orange.png");
        blueQueen = new ImageIcon("res\\blueQ.png");
        orangeQueen = new ImageIcon("res\\orangeQ.png");

        initTable();

    }

    private void initTable() {
        getContentPane().removeAll();
        setLayout(new GridLayout(10, 10));

        fields = new JButton[NUM_BTN];

    }

    public static void main(String[] a) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        CheckersWindow chw = new CheckersWindow();
        chw.setDefaultCloseOperation(EXIT_ON_CLOSE);
        chw.setSize(500, 500);
        chw.setTitle("Checkers");
        chw.setVisible(true);
    }


}
