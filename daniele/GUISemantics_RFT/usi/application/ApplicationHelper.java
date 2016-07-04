package usi.application;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import usi.rmi.RemoteCoberturaInterface;

import com.rational.test.ft.object.interfaces.RootTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.RationalTestScript;
import com.rational.test.ft.script.SubitemFactory;

public class ApplicationHelper {

	RootTestObject root = null;

	public RootTestObject startApplication() throws Exception {

		RationalTestScript.unregisterAll();

		RationalTestScript.shellExecute("scripts" + System.getProperty("file.separator") + "AUT.bat");
		System.gc();

		long delayTime = System.nanoTime();

		this.root = null;
		try {
			do {

				Thread.sleep(1000);

				this.root = RationalTestScript.getRootTestObject();
			} while (this.root == null && (System.nanoTime() - delayTime) / 1000000 < 30000);
		} catch (Exception e) {
			throw new Exception("ApplicationHelper - startApplication: error, " + e.getMessage());
		}
		return this.root;
	}

	public void closeApplication() {

		if (this.root == null) {
			this.forceClose();
			return;
		}
		TestObject[] tos = null;
		try {
			Registry registry = LocateRegistry.getRegistry(2007);
			RemoteCoberturaInterface rmo = (RemoteCoberturaInterface) registry.lookup("RemoteCobertura");
			rmo.getCoverage();
			tos = this.root.find(SubitemFactory.atChild("showing", "true", "enabled", "true"));
			tos[0].getProcess().kill();
		} catch (Exception e) {
			this.forceClose();
			return;
		}
	}

	public void forceClose() {

		RationalTestScript.shellExecute(System.getProperty("user.dir") + System.getProperty("file.separator")
				+ "AppScript" + System.getProperty("file.separator") + "closeapplication.bat");
	}

	public RootTestObject restartApplication() throws Exception {

		this.closeApplication();
		return this.startApplication();
	}
}
