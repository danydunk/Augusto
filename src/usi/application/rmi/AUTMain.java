package src.usi.application.rmi;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

import mockit.Mock;
import mockit.MockUp;
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

		try {
			final Class<?> autClass = Class.forName(args[0]);
			final String methodName = "main";
			final Class<?>[] argsTypes = new Class[] { String[].class };
			final Method mainMethod = autClass.getDeclaredMethod(methodName, argsTypes);
			final String[] mainArgs = Arrays.copyOfRange(args, 1, args.length);
			mainMethod.invoke(null, (Object) mainArgs);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveCoverage() throws RemoteException {

		try {

			ProjectData.saveGlobalProjectData();
			System.out.println("Cobertura RMI - Write done");
		} catch (final Throwable t) {
			System.err.println("Cobertura RMI - Write failed");
		}
	}

	public final static class SystemMock extends MockUp<System> {

		private static final long startT = System.currentTimeMillis();

		private static final long referenceT = 1477564991731L;

		public SystemMock() {

			super();
		}

		@Mock
		public static long currentTimeMillis() {

			return referenceT + (System.nanoTime() - startT) / 1000000L;
		}
	}
}
