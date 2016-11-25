package src.usi.application.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteCoberturaInterface extends Remote {

	public void saveCoverage() throws RemoteException;

}