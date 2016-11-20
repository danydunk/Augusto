package src.usi.application;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;

import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.PathsManager;
import src.usi.gui.GuiStateManager;
import src.usi.gui.structure.Window;
import src.usi.pattern.dialogs.Pattern_dialogs;
import src.usi.testcase.GUITestCaseParser;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;
import src.usi.xml.XMLUtil;

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

	public static void reset() throws Exception {

		instance = new ApplicationHelper();
	}

	private ApplicationHelper() throws Exception {

		this.running = false;
		this.root = null;
		if (ConfigurationManager.getInitialActions().length() > 0) {
			final Document doc = XMLUtil.read(ConfigurationManager.getInitialActions());
			this.initial = GUITestCaseParser.parse(doc);
		}
	}

	public void startApplication() throws Exception {

		RationalTestScript.unregisterAll();

		RationalTestScript.shellExecute(PathsManager.getAUTPath());
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
			// this.forceClose();
			return;
		}
		TestObject[] tos = null;

		tos = this.root.find(SubitemFactory.atChild("showing", "true", "enabled", "true"));
		if (tos.length > 0) {
			tos[0].getProcess().kill();
		}
		GuiStateManager.destroy();
		this.running = false;

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