package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.gui;

import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.RegistryManager;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.server.IUserDb;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user.IUser;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

    public CheckersWindow() {

        showLoginScreen();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    userDb.remove(me);
                }
                catch (RemoteException re) {
                    reportError("Couldn't remove player", true, re);
                }
                finally {
                    dispose();
                }
            }
        });

    }

    private void showLoginScreen() {

        getContentPane().removeAll();
        setLayout(new BorderLayout());
        JPanel pnlLoginScreen = new JPanel();
        JTextField txtUserName = new JTextField(30);
        JLabel lblUserName = new JLabel("Username: ");
        JPanel pnlHost = new JPanel();
        JTextField txtHostEntry = new JTextField(30);
        JLabel lblHostName = new JLabel("Host: ");

        JButton btnLogIn = new JButton("LogIn");

        btnLogIn.addActionListener(e -> {
            String dbHost = txtHostEntry.getText();

            try {
                userDb = (IUserDb) RegistryManager.get(dbHost).lookup(IUserDb.RMI_NAME);
            } catch (RemoteException | NotBoundException ex) {
                reportError("Initialization error.", true, ex);
            }

            this.board = new Board(userDb, CheckersWindow.this);

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

        pnlHost.add(lblHostName);
        pnlHost.add(txtHostEntry);

        add(pnlHost, BorderLayout.NORTH);
        add(pnlLoginScreen, BorderLayout.CENTER);
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
            board.calculateValidFields();
            board.setMyMove(true);
        }

        getContentPane().removeAll();

        JPanel playersPanel = new JPanel();
        JLabel lblBluePlayer = new JLabel();
        JLabel lblVersus = new JLabel("versus");
        JLabel lblOrangePlayer = new JLabel();

        String myName = null;
        String opponentName = null;
        try {
            myName = me.getName();
            opponentName = userDb.getOpponent(me).getName();
        } catch (RemoteException re) {
            reportError("Cannot get user name", false, re);
        }

        lblBluePlayer.setOpaque(true);
        if (board.isBlue()) {
            lblBluePlayer.setBackground(Color.BLUE);
        }
        lblBluePlayer.setText(board.isBlue() ? myName : opponentName);

        lblOrangePlayer.setOpaque(true);
        if (!board.isBlue()) {
            lblOrangePlayer.setBackground(Color.ORANGE);
        }
        lblOrangePlayer.setText(board.isBlue() ? opponentName : myName);

        playersPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        playersPanel.add(lblBluePlayer);
        playersPanel.add(lblVersus);
        playersPanel.add(lblOrangePlayer);

        setLayout(new BorderLayout());
        add(playersPanel, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);


        validate();
        repaint();
    }

    public void onOpponentQuit() {
        try {
            JOptionPane.showMessageDialog(this, "Your opponent has quit, you win!" + "\nThe game will now exit.");
            userDb.remove(me);
        }
        catch (RemoteException re) {
            reportError("Couldn't remove player", true, re);
        }
        finally {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }

	/**
     * Action performed on opponents move.
     * @param move
     */
    public void onOpponentMove(String move) {
        switch(move.substring(0,move.indexOf(' '))){
            case "select":
                setOpponentsSelection(move.substring(move.indexOf(' ') + 1));
                break;
            case "move":
                makeOpponentsMove(move.substring(move.indexOf(' ') + 1), false);
                break;
            case "final":
                makeOpponentsMove(move.substring(move.indexOf(' ') + 1), true);
                break;
            default:
                throw new IllegalArgumentException("Invalid move string");

        }
    }

	/**
     * You either captured all of the opponents pieces, or he/she cannot make a move,
     * thus you won!
     */
    public void onOpponentLoss() {
        try {
            JOptionPane.showMessageDialog(this, "Your opponent cannot make a move, you win!" + "\nThe game will now exit.");
            userDb.remove(me);
        }
        catch (RemoteException ex) {
            reportError("Cannot remove player", true, ex);
        }
        finally {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }

    public void setOpponentsSelection(String strId){
        board.selectPiece(Integer.parseInt(strId));
    }

    public void makeOpponentsMove(String move, boolean isFinal){
        String[] s=move.split(" ");
        if(s.length <= 3){
            int startField = Integer.parseInt(s[0].trim());
            int endField = Integer.parseInt(s[1].trim());
            int capturedField = -1;
            if (s.length == 3) {
                capturedField = Integer.parseInt(s[2].trim());
            }
            board.movePiece(startField, endField, capturedField);
        }
        else {
            throw new IllegalArgumentException("Illegal move");
        }

        if (isFinal) {
            if (board.calculateValidFields()) {
                board.setMyMove(true);
            }
            else {
                try {
                    userDb.send(me, "lost");
                    userDb.remove(me);
                    JOptionPane.showMessageDialog(this, "You cannot make any move, you lost!" + "\nThe game will now exit.");
                }
                catch (RemoteException ex) {
                    reportError("Cannot send message or remove player", true, ex);
                }
                finally {
                    dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                }
            }
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

        CheckersWindow chw = new CheckersWindow();
        chw.setDefaultCloseOperation(EXIT_ON_CLOSE);
        chw.setSize(500, 500);
        //chw.setResizable(false);
        chw.setTitle("Checkers");
        chw.setVisible(true);
    }


}
