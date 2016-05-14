package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.gui.CheckersWindow;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.server.UserDb;

/**
 * Created by UserPC on 14.05.2016..
 */
public class User extends UnicastRemoteObject implements IUser {

    private static final long serialVersionUID = -3991786196885452079L;

    private CheckersWindow wnd;
    /*private UserDb userss;*/

    public User(CheckersWindow wnd) throws RemoteException {
        this.wnd = wnd;
    }


    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public void onOpponentMove(IUser opponent, String move) throws RemoteException {

    }

    @Override
    public void onOpponentQuit(IUser opponent) throws RemoteException {
            /*userss.remove(opponent);*/
    }
}