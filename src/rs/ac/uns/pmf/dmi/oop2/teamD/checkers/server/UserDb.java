package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.server;

import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user.IUser;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by UserPC on 14.05.2016..
 */
public class UserDb extends UnicastRemoteObject implements IUserDb {


    private static final long serialVersionUID = 2513243613133876083L;

    private final List<IUser> users;

    protected UserDb() throws RemoteException{
        users=new ArrayList<>();
    }
    @Override
    public void add(IUser user) throws RemoteException {
        if(users.size()<=1){
            users.add(user);
        } else System.out.println("Maksimalno 2 igraca");
    }

    @Override
    public void remove(IUser user) throws RemoteException {
        users.remove(user);
        send(user,"protivnik je napustio igru");
    }

    @Override
    public IUser getOpponent(IUser user) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(IUser sender, String message) throws RemoteException {
        /*CheckersWindow.opponentExited(sender,message);
        * User.onMeassage(sender,message)*/
    }
    /*public List<UserInfo> get() throws RemoteException {
            return users;
    }*/
}
