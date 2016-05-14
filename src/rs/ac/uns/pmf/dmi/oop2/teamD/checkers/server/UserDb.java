package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.server;

import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.gui.CheckersWindow;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user.IUser;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;


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
        } else System.out.println("Only two players allowed per game");
    }

    @Override
    public void remove(IUser user) throws RemoteException {
        users.remove(user);
        send(user,"quit");

    }

    @Override
    public void send(IUser sender, String message) throws RemoteException {
       for(IUser t:users){
            if(!t.equals(sender)){
                t.onOpponentMove(sender,message);
            }
        }
    }
    /*public List<UserInfo> get() throws RemoteException {
            return users;
    }*/
}
