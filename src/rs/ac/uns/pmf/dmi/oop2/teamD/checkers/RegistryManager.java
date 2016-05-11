package rs.ac.uns.pmf.dmi.oop2.teamD.checkers;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RegistryManager {

	/**
	 * Returns RMI registry on host defined inside VM argument.
	 */
	public static Registry get() throws RemoteException {
		String host = System.getProperty("java.rmi.server.hostname");
		if (host == null) {
			System.out.println("VM argument not defined, RMI registry will be available on localhost only.");
			host = "localhost";
		}
		return get(host);
	}

	/**
	 * Returns RMI registry on defined host
	 */
	public static Registry get(String host) throws RemoteException {
		try {
			return LocateRegistry.createRegistry(1099);
		} catch (RemoteException ex) {
			return LocateRegistry.getRegistry(host, 1099);
		}
	}
}