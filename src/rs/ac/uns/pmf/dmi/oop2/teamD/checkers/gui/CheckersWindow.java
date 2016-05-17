package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.gui;

import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.RegistryManager;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.server.IUserDb;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.server.UserDb;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user.IUser;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user.User;

import javax.swing.*;
import java.awt.*;
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

    private Board board;
    private IUserDb userDb;
    private IUser me;

    public CheckersWindow(String dbHost) {

        try {
            userDb = (IUserDb) RegistryManager.get(dbHost).lookup(IUserDb.RMI_NAME);
        } catch (RemoteException | NotBoundException ex) {
            reportError("Initialization error.", true, ex);
        }

        showLoginScreen();
        this.board = new Board(userDb, CheckersWindow.this);
    }

    private void showLoginScreen() {

        getContentPane().removeAll();
        setLayout(new BorderLayout());
        JPanel pnlLoginScreen = new JPanel();
        JTextField txtUserName = new JTextField(30);
        JLabel lblUserName = new JLabel("Enter your name: ");

        JButton btnLogIn = new JButton("LogIn");

        btnLogIn.addActionListener(e -> {
            try {
                String host = System.getProperty("java.rmi.server.hostname");
                if (host == null) {
                    host = "localhost";
                }
                String name = txtUserName.getText();

                Registry reg = RegistryManager.get();
                me = new User(CheckersWindow.this, name, host);
                reg.rebind(name, me);

                // Add me to database
                int playerOrdNum = userDb.add(me);
                if(playerOrdNum == -1) {
                    JOptionPane.showMessageDialog(this,
                            "Two players already play on this host",
                            "Occupied host",
                            JOptionPane.ERROR_MESSAGE
                    );

                    return;
                }

                if (playerOrdNum == 1) {
                    showWaitScreen();
                }
                else {
                    initMain(me);
                }


            } catch (RemoteException ex) {
                reportError("Cannot create User object", true, ex);
            }
        });

        pnlLoginScreen.add(lblUserName);
        pnlLoginScreen.add(txtUserName);
        pnlLoginScreen.add(btnLogIn);

        add(pnlLoginScreen, BorderLayout.NORTH);
    }

	/**
     * Shows wait screen INSTEAD of login with a constantly displayed
     * message in the centre and Quit button at the bottom.
     */
    private void showWaitScreen() {
        getContentPane().removeAll();
        JPanel pnlWaitScreen = new JPanel();
        JLabel lblWait = new JLabel("Waiting for another player...");
        JButton btnQuit = new JButton("Quit");

        btnQuit.addActionListener(e -> {
            JOptionPane.showMessageDialog(null, "You quit the game!");
            dispose();
        });

        pnlWaitScreen.add(lblWait);
        pnlWaitScreen.add(btnQuit);

        add(pnlWaitScreen, BorderLayout.NORTH);

        validate();
        repaint();
    }

	/**
     * Initializes main screen that contains board panel in the centre
     * and players in both of the upper corners. Players are showed
     * in boxes colored with the same color as their pieces
     * @param secondPlayer Second player in a game
     */
    public void initMain(IUser secondPlayer) {
        if (me.equals(secondPlayer)) {
            try {
                board.init(me, userDb.getOpponent(me), false);
            }
            catch (RemoteException ex) {
                reportError("Cannot get opponent from server", true, ex);
            }
        }
        else {
            board.init(me, secondPlayer, true);
        }

        getContentPane().removeAll();

        // TODO: Implement proper players panel
        JPanel playersPanel = new JPanel();
        JLabel lblPlayers = new JLabel("Players");
        playersPanel.add(lblPlayers);

        setLayout(new BorderLayout());
        add(playersPanel, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);


        validate();
        repaint();
    }

    public void onOpponentQuit() {
        JOptionPane.showMessageDialog(this, "Your opponent has quit, you win!" + "\nThe game will now exit.");
        dispose();
    }

	/**
     * Action performed on opponents move.
     * @param move
     */
    public void onOpponentMove(String move) {
        // TODO: Implement this
        switch(move.substring(0,move.indexOf(' '))){
            case "select":
                //setSelected(move.substring(move.indexOf(' ')+1));
                break;
            case "move":
                //doMove(move.substring(move.indexOf(' ')+1));
                break;
            case "final":
                //something...
                break;
            default:
                throw new IllegalArgumentException("Invalid move string");

        }
    }

    public void reportError(String msg, boolean exit, Throwable throwable) {
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
