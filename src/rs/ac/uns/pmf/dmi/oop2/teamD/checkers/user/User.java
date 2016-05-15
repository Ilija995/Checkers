package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.sun.javaws.exceptions.InvalidArgumentException;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.gui.CheckersWindow;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.server.UserDb;


public class User extends UnicastRemoteObject implements IUser {

    private static final long serialVersionUID = -3991786196885452079L;

    private CheckersWindow wnd;
    private String name;
    private String host;

    public User(CheckersWindow wnd,String name,String host) throws RemoteException {
        this.wnd = wnd;
        this.name=name;
        this.host=host;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void onOpponentMove(IUser opponent, String move) throws RemoteException {
        switch (move.substring(0, move.indexOf(' '))) {
            case "quit":
                EventQueue.invokeLater(() -> wnd.onOpponentQuit());
                break;
            case "select":case "move":case "final":
                EventQueue.invokeLater(() -> wnd.onOpponentMove(move));
                break;
            default:
                throw new IllegalArgumentException("Invalid move string");
        }
    }

    @Override
    public void onOpponentConnect(IUser user) throws RemoteException {
        EventQueue.invokeLater(() -> wnd.initMain(user));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int ret = 1;
        ret = prime * ret + getHost().hashCode();
        ret = prime * ret + getName().hashCode();
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        IUser other = (IUser) o;
        if (!getHost().equals(other.getHost())) {
            return false;
        }
        if (!getName().equals(other.getName())) {
            return false;
        }
        return true;
    }

}