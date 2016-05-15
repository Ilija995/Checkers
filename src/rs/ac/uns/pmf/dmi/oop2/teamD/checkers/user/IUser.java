package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for information about the user and the connection to server
 */
public interface IUser extends Remote, Serializable {
	
	String getName();

	String getHost();

	/**
	 * Receives the opponent move from the server
	 * @param opponent
	 * @param move
	 * @throws RemoteException
	 */
	void onOpponentMove(IUser opponent, String move) throws RemoteException;

	void onOpponentConnect(IUser user) throws RemoteException;

	@Override
	boolean equals(Object anObject);

	@Override
	int hashCode();

}
