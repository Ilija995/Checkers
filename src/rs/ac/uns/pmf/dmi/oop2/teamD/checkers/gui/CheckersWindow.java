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

    private JTextField txt;
    private JLabel label;
    private Board board;
    private IUserDb userDb;
    private IUser me;

    public CheckersWindow(String dbHost) {

        try {
            userDb = (UserDb) RegistryManager.get(dbHost).lookup(UserDb.RMI_NAME);
        } catch (RemoteException | NotBoundException ex) {
            reportError("Initialization error.", true, ex);
        }

        showLoginScreen();
        this.board = new Board(userDb, CheckersWindow.this);
    }

    private void showLoginScreen() {

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

        panel1.add(label);
        panel1.add(txt);
        panel1.add(logIn);

        add(panel1, BorderLayout.NORTH);
    }

	/**
     * Show wait window while waiting for other player
     */
    private void showWaitScreen() {
        // TODO: Implement this
    }

	/**
     * Method for main window creation
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

        // TODO: Implement rest
    }

    public void onOpponentQuit() {
        // TODO: Implement this
    }

    public void onOpponentMove(String move) {
        // TODO: Implement this
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
