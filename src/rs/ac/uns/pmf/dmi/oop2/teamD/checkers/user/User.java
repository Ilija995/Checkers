package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
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

    }

    @Override
    public void onOpponentQuit(IUser opponent) throws RemoteException {
            /*userss.remove(opponent);*/
    }
}