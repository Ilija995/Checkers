package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.gui;

import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.RegistryManager;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.server.UserDb;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user.IUser;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Our checkers game window
 * Apart from creating and maintaining gui it responds when other player sends his move
 */

public class CheckersWindow extends JFrame {

    private static final Logger logger = Logger.getLogger(CheckersWindow.class.getName());

    private static final int NUM_BTN = 100;

    private Field[][] board = new Field[10][10];
    private Icon bluePawn;
    private Icon orangePawn;
    private Icon blueQueen;
    private Icon orangeQueen;
    private JTextField txt;
    private JLabel label;
    private UserDb userDb;

    private class Field extends JPanel {
        private int x;
        private int y;
        private boolean hasPawn;
        private boolean hasQueen;
        private JLabel label = new JLabel();
        private IUser user;

        public Field(int x, int y, Color color, IUser user, boolean hasPawn, boolean isBluePawn) {
            this.x = x;
            this.y = y;
            setBackground(color);
            add(label);
            this.user = user;

            if (hasPawn && isBluePawn) {
                setBluePawn();
            }
            else if (hasPawn && !isBluePawn) {
                setOrangePawn();
            }

            if (hasPawn) {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        movePawn();
                    }
                });
            }
            else if (hasQueen) {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        moveQueen();
                    }
                });
            }
        }

        public void setBluePawn(){
            label.setIcon(bluePawn);
        }

        public void setOrangePawn() {
            label.setIcon(orangePawn);
        }

        private void movePawn(){

        }

        public void setBlueQueen(){
            label.setIcon(blueQueen);
        }

        public void setOrangeQueen() {
            label.setIcon(orangeQueen);
        }

        private void moveQueen(){

        }

    }


    public CheckersWindow(String dbHost) {
        bluePawn = new ImageIcon("res\\blue.png");
        orangePawn = new ImageIcon("res\\orange.png");
        blueQueen = new ImageIcon("res\\blueQ.png");
        orangeQueen = new ImageIcon("res\\orangeQ.png");

        try {
            userDb = (UserDb) RegistryManager.get(dbHost).lookup(UserDb.RMI_NAME);
        } catch (RemoteException | NotBoundException ex) {
            reportError("Initialization error.", true, ex);
        }

        logInScreen();

    }

    private void logInScreen() {

        getContentPane().removeAll();
        setLayout(new BorderLayout());
        JPanel panel1 = new JPanel();

        label = new JLabel("Enter your name: ");
        txt = new JTextField(30);

        JButton logIn = new JButton("LogIn");

        logIn.addActionListener(e -> {
            try {
                String host = System.getProperty("java.rmi.server.hostname");
                if (host == null) {
                    host = "localhost";
                }
                String name = txt.getText();

                Registry reg = RegistryManager.get();
                IUser user = new User(CheckersWindow.this, name, host);
                reg.rebind(name, user);

                if(!userDb.add(user)) {
                    JOptionPane.showMessageDialog(this,
                            "Two players already play on this host",
                            "Occupied host",
                            JOptionPane.ERROR_MESSAGE
                    );

                    return;
                }

                initTable();

            } catch (RemoteException ex) {
                reportError("Cannot create User object", true, ex);
            }
        });

        panel1.add(label);
        panel1.add(txt);
        panel1.add(logIn);

        add(panel1, BorderLayout.NORTH);
    }

    private void initTable() {
        getContentPane().removeAll();
        setLayout(new GridLayout(10, 10));


    }

    private void reportError(String msg, boolean exit, Throwable throwable) {
        logger.log(exit ? Level.SEVERE : Level.WARNING, msg, throwable);

        if (exit) {
            msg += "\nProgram EXIT";
        }

        JOptionPane.showMessageDialog(this, msg, "ERROR", JOptionPane.ERROR_MESSAGE);

        if (exit) {
            System.exit(-1);
        }
    }

    public static void main(String[] a) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        String dbHost = JOptionPane.showInputDialog(null, "Host of the RMI registry holding the User DB?");
        if (dbHost == null || dbHost.length() == 0) {
            return;
        }

        CheckersWindow chw = new CheckersWindow(dbHost);
        chw.setDefaultCloseOperation(EXIT_ON_CLOSE);
        chw.setSize(500, 500);
        chw.setTitle("Checkers");
        chw.setVisible(true);
    }


}
