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
import mockit.MockUp;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;

public class AUTMain extends UnicastRemoteObject implements RemoteCoberturaInterface {

	protected AUTMain() throws RemoteException {

		super();
	}

	private static final long serialVersionUID = 1L;

	public static void main(final String[] args) {

		System.out.println("Creazione e registrazione dell'oggetto");
		try {
			LocateRegistry.createRegistry(2007);
			final Registry registry = LocateRegistry.getRegistry(2007);
			registry.rebind("RemoteCobertura", new AUTMain());
		} catch (final RemoteException e) {
			e.printStackTrace();
			System.out.println("Impossibile creare o registrare l'oggetto");
			System.exit(1);
		}
		new SystemMock();

		// TODO: fix final time mock
		// System.out.println(SystemMock.currentTimeMillis());

		try {
			final Class<?> autClass = Class.forName(args[0]);
			final String methodName = "main";
			final Class<?>[] argsTypes = new Class[] { String[].class };
			final Method mainMethod = autClass.getDeclaredMethod(methodName, argsTypes);
			final String[] mainArgs = Arrays.copyOfRange(args, 1, args.length);
			mainMethod.invoke(null, (Object) mainArgs);
		} catch (final SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final InvocationTargetException e) {
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
		} catch (final Throwable t) {
			System.err.println("Cobertura RMI - Write failed");
		}
	}

	@Override
	public void println(final String s) {

		System.err.println(s);
	}

	@Override
	public ProjectData getProjectData() throws RemoteException {

		try {
			ProjectData.saveGlobalProjectData();
			// Thread.sleep(200); // necessario perchè i projectdata vengano
			// scritti su file
			final ProjectData projectData = CoverageDataFileHandler.loadCoverageData(new File("lib"
					+ File.separator + "cobertura" + File.separator + "cobertura.ser"));
			System.out.println("Cobertura RMI - Get ProjectData Write done");
			return projectData;
		} catch (final Exception t) {
			System.out.println("Cobertura RMI - Get ProjectData failed");
			t.printStackTrace();
			return null;
		}
	}

	public final static class SystemMock extends MockUp<System> {

		private static final long startT = System.currentTimeMillis();

		private static final long referenceT = 1477564991731L;

		public SystemMock() {

			super();
			System.out.println(startT);
		}

		@Mock
		public static long currentTimeMillis() {

			return referenceT + (System.nanoTime() - startT) / 1000000L;
		}
	}
}
