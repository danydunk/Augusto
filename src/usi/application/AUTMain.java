package src.usi.application;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Arrays;

import mockit.Mock;
import mockit.MockUp;

public class AUTMain {

	protected AUTMain() throws RemoteException {

		super();
	}

	public static void main(final String[] args) {

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
