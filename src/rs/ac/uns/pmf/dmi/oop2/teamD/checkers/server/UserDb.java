package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.server;

import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.RegistryManager;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user.IUser;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
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
    public int add(IUser user) throws RemoteException {
        if(users.size() <= 1){
            for (IUser u : users) {
                u.onOpponentConnect(user);
            }

            users.add(user);
            System.out.printf("User %s added\n", user.getName());
            return users.size();
        }

        return -1;
    }

    @Override
    public void remove(IUser user) throws RemoteException {
        users.remove(user);
        System.out.printf("User %s removed\n", user.getName());
        send(user,"quit ");
    }

    @Override
    public IUser getOpponent(IUser user) throws RemoteException {

        for(IUser t:users){
            if(!t.equals(user)){
                return t;
            }
        }
        return null;
    }

    @Override
    public void send(IUser sender, String message) throws RemoteException {
       for(IUser t:users){
            if(!t.equals(sender)){
                System.out.printf("User %s sent \"%s\"\n", sender.getName(), message);
                t.onOpponentMove(sender,message);
            }
        }
    }

    public static void main(String[] args) {
        try {
            Registry reg = RegistryManager.get();
            reg.rebind(UserDb.RMI_NAME, new UserDb());
            System.out.println("User DB ready...");
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

    }
}
