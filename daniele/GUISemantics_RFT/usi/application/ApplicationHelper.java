package usi.application;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import usi.gui.GuiStateManager;
import usi.rmi.RemoteCoberturaInterface;

import com.rational.test.ft.object.interfaces.RootTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.RationalTestScript;
import com.rational.test.ft.script.SubitemFactory;

public class ApplicationHelper {

	private RootTestObject root;
	private boolean running;

	private static ApplicationHelper instance;

	public static ApplicationHelper getInstance() {

		if (instance == null) {
			instance = new ApplicationHelper();
		}
		return instance;
	}

	private ApplicationHelper() {

		this.running = false;
		this.root = null;
	}

	public void startApplication() throws Exception {

		RationalTestScript.unregisterAll();

		RationalTestScript.shellExecute("scripts" + File.separator + "AUT.bat");
		System.gc();

		final long delayTime = System.nanoTime();

		this.root = null;
		try {
			do {

				Thread.sleep(3000);

				this.root = RationalTestScript.getRootTestObject();
			} while (this.root == null && (System.nanoTime() - delayTime) / 1000000 < 30000);
		} catch (final Exception e) {
			throw new Exception("ApplicationHelper - startApplication: error, " + e.getMessage());
		}
		GuiStateManager.create(this.root);
		this.running = true;
	}

	public void closeApplication() {

		if (this.root == null) {
			this.forceClose();
			return;
		}
		TestObject[] tos = null;
		try {
			final Registry registry = LocateRegistry.getRegistry(2007);
			final RemoteCoberturaInterface rmo = (RemoteCoberturaInterface) registry
					.lookup("RemoteCobertura");
			rmo.getCoverage();
			tos = this.root.find(SubitemFactory.atChild("showing", "true", "enabled", "true"));
			tos[0].getProcess().kill();
			GuiStateManager.destroy();
			this.running = false;
		} catch (final Exception e) {
			this.forceClose();
			GuiStateManager.destroy();
			this.running = false;
		}
	}

	public void forceClose() {

		RationalTestScript.shellExecute(System.getProperty("user.dir") + File.separator
				+ "AppScript" + File.separator + "closeapplication.bat");
	}

	public void restartApplication() throws Exception {

		this.closeApplication();
		this.startApplication();
	}

	public boolean isRunning() {

		return this.running;
	}
}
