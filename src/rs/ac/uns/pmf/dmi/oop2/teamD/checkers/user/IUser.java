package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for information about the user and the connection to server
 */
public interface IUser extends Remote, Serializable {
	
	String getName() throws RemoteException;

	String getHost() throws RemoteException;

	/**
	 * Receives the opponent move from the server
	 * @param opponent
	 * @param move
	 * @throws RemoteException
	 */
	void onOpponentMove(IUser opponent, String move) throws RemoteException;

	void onOpponentConnect(IUser user) throws RemoteException;
	/*
	@Override
	boolean equals(Object anObject) throws RemoteException ;

	@Override
	int hashCode() throws RemoteException;
	*/
}
