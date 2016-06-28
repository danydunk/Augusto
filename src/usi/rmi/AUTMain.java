package usi.rmi;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;

public class AUTMain extends UnicastRemoteObject implements RemoteCoberturaInterface {

	protected AUTMain() throws RemoteException {

		super();
	}

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {

		System.out.println("Creazione e registrazione dell'oggetto");
		try {
			LocateRegistry.createRegistry(2007);
			Registry registry = LocateRegistry.getRegistry(2007);
			registry.rebind("RemoteCobertura", new AUTMain());
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("Impossibile creare o registrare l'oggetto");
			System.exit(1);
		}

		Mockit.setUpMock(SystemMock.class);

		try {
			Class<?> autClass = Class.forName(args[0]);
			String methodName = "main";
			Class<?>[] argsTypes = new Class[] { String[].class };
			Method mainMethod = autClass.getDeclaredMethod(methodName, argsTypes);
			String[] mainArgs = Arrays.copyOfRange(args, 1, args.length);
			mainMethod.invoke(null, (Object) mainArgs);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void getCoverage() throws RemoteException {

		try {
			/*
			 * String className =
			 * "net.sourceforge.cobertura.coveragedata.ProjectData"; String
			 * methodName = "saveGlobalProjectData"; Class<?> saveClass =
			 * Class.forName(className); java.lang.reflect.Method saveMethod =
			 * saveClass.getDeclaredMethod(methodName, new Class[0]);
			 * saveMethod.invoke(null, new Object[0]);
			 */

			ProjectData.saveGlobalProjectData();
			// Thread.sleep(1000);
			System.out.println("Cobertura RMI - Write done");
		} catch (Throwable t) {
			System.err.println("Cobertura RMI - Write failed");
		}
	}

	@Override
	public void println(String s) {

		System.err.println(s);
	}

	@Override
	public ProjectData getProjectData() throws RemoteException {

		try {
			ProjectData.saveGlobalProjectData();
			// Thread.sleep(200); // necessario perchè i projectdata vengano
			// scritti su file
			ProjectData projectData = CoverageDataFileHandler.loadCoverageData(new File("lib"
					+ System.getProperty("file.separator") + "cobertura" + System.getProperty("file.separator")
					+ "cobertura.ser"));
			System.out.println("Cobertura RMI - Get ProjectData Write done");
			return projectData;
		} catch (Exception t) {
			System.out.println("Cobertura RMI - Get ProjectData failed");
			t.printStackTrace();
			return null;
		}
	}

	@MockClass(realClass = System.class)
	public static class SystemMock {

		private static final long startT = System.nanoTime();
		private static final long referenceT = 1433356618285L;

		@Mock
		public static long currentTimeMillis() {

			return referenceT + (System.nanoTime() - startT) / 1000000L;
		}
	}
}
