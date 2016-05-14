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
    /*private UserDb userss;*/

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
            if(move.startsWith("quit")){
                //wnd.onMessage("player "+opponent.getName()+"has quit the game,you win");
            }else if(move.startsWith("move")){
                //wnd.makeMove(move);
            }else{
                throw new IllegalArgumentException("Invalid command");
            }
    }
    @Override
    public int hashCode() {
        final int prost = 31;
        int rezultat = 1;
        rezultat = prost * rezultat + getHost().hashCode();
        rezultat = prost * rezultat + getName().hashCode();
        return rezultat;
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