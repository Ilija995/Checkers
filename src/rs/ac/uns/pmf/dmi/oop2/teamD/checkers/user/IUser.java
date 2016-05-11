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

	/**
	 * Gets notified from the server when the opponent quit
	 * @param opponent
	 * @throws RemoteException
	 */
	void onOpponentQuit(IUser opponent) throws RemoteException;
}
