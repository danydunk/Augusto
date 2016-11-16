package src.usi.application.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.sourceforge.cobertura.coveragedata.ProjectData;

public interface RemoteCoberturaInterface extends Remote {

	public void getCoverage() throws RemoteException;

	public void println(String s) throws RemoteException;

	public ProjectData getProjectData() throws RemoteException;
}