package usi.application;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

import usi.rmi.RemoteCoberturaInterface;

import com.rational.test.ft.object.interfaces.RootTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.RationalTestScript;
import com.rational.test.ft.script.SubitemFactory;

public class ApplicationHelper {

	Logger logger = Logger.getLogger("ApplicationHelper");
	RootTestObject root = null;

	public void startApplication() throws Exception {

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
	}

	public void closeApplication() {

		if (this.root == null) {
			this.logger.severe("ApplicationHelper - closeApplication: closing application failed due to: root is null");
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
			this.logger.severe("ApplicationHelper - closeApplication: error, " + e.getLocalizedMessage());
			this.forceClose();
			return;
		}
		this.logger.info("Closing application done");
	}

	public void forceClose() {

		this.logger.info("Force Closing application");
		RationalTestScript.shellExecute(System.getProperty("user.dir") + System.getProperty("file.separator")
				+ "AppScript" + System.getProperty("file.separator") + "closeapplication.bat");
	}

	public void restartApplication() throws Exception {

		this.closeApplication();
		this.startApplication();
	}
}
