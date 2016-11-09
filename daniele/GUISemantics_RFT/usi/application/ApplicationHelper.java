package usi.application;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import org.w3c.dom.Document;

import usi.configuration.ConfigurationManager;
import usi.gui.GuiStateManager;
import usi.gui.structure.Window;
import usi.pattern.dialogs.Pattern_dialogs;
import usi.rmi.RemoteCoberturaInterface;
import usi.testcase.GUITestCaseParser;
import usi.testcase.structure.GUIAction;
import usi.testcase.structure.GUITestCase;
import usi.xml.XMLUtil;

import com.rational.test.ft.object.interfaces.RootTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.RationalTestScript;
import com.rational.test.ft.script.SubitemFactory;

public class ApplicationHelper {

	private RootTestObject root;
	private boolean running;
	private GUITestCase initial;

	private static ApplicationHelper instance;

	public static ApplicationHelper getInstance() throws Exception {

		if (instance == null) {
			instance = new ApplicationHelper();
		}
		return instance;
	}

	private ApplicationHelper() throws Exception {

		this.running = false;
		this.root = null;
		if (ConfigurationManager.getInitialActions().length() > 0) {
			final Document doc = XMLUtil.read(new File(ConfigurationManager.getInitialActions())
					.getAbsolutePath());
			this.initial = GUITestCaseParser.parse(doc);
		}
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

		if (this.initial != null) {
			final GuiStateManager gmanager = GuiStateManager.getInstance();
			for (final GUIAction act : this.initial.getActions()) {
				gmanager.readGUI();
				this.dealWithDialogsWindow(gmanager);
				ActionManager.executeAction(act);
			}
			gmanager.readGUI();
			this.dealWithDialogsWindow(gmanager);
		}
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

	// TODO: this code is duplicated
	private void dealWithDialogsWindow(final GuiStateManager gmanager) throws Exception {

		if (gmanager.getCurrentActiveWindows() != null) {
			final Window current = gmanager.getCurrentActiveWindows();
			for (final Pattern_dialogs dialog : Pattern_dialogs.values()) {

				if (dialog.isMatch(current)) {
					final List<GUIAction> acts = dialog.getActionsToGoPast(current);
					for (final GUIAction act : acts) {
						ActionManager.executeAction(act);
						gmanager.readGUI();
					}
				}
			}
		}
	}
}
