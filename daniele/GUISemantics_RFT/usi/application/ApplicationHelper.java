package usi.application;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import usi.gui.GuiStateManager;
import usi.gui.semantic.testcase.GUITestCase;
import usi.rmi.RemoteCoberturaInterface;

import com.rational.test.ft.object.interfaces.RootTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.RationalTestScript;
import com.rational.test.ft.script.SubitemFactory;

public class ApplicationHelper {

	RootTestObject root = null;

	public void startApplication() throws Exception {

		RationalTestScript.unregisterAll();

		RationalTestScript.shellExecute("scripts" + File.separator + "AUT.bat");
		System.gc();

		final long delayTime = System.nanoTime();

		this.root = null;
		try {
			do {

				Thread.sleep(2000);

				this.root = RationalTestScript.getRootTestObject();
			} while (this.root == null && (System.nanoTime() - delayTime) / 1000000 < 30000);
		} catch (final Exception e) {
			throw new Exception("ApplicationHelper - startApplication: error, " + e.getMessage());
		}
		GuiStateManager.create(this.root);
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
		} catch (final Exception e) {
			this.forceClose();
			GuiStateManager.destroy();
			return;
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

	public void runTestCase(final GUITestCase tc) {

	}
}
