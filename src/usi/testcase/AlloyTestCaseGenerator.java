package src.usi.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import src.usi.configuration.ConfigurationManager;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.semantic.alloy.AlloyUtil;
import src.usi.testcase.structure.GUITestCase;

public class AlloyTestCaseGenerator {

	static private int MIN_TIME = 4;

	public static List<GUITestCase> generateTestCases(final Instance_GUI_pattern instance)
			throws Exception {

		final List<GUITestCase> out = new ArrayList<>();

		final List<RunCommandThread> threads = new ArrayList<>();
		for (int x = 0; x < instance.getSemantics().getRun_commands().size(); x++) {
			final Instance_GUI_pattern newi = instance.clone();
			newi.getSemantics().clearRunCommands();
			newi.getSemantics().addRun_command(instance.getSemantics().getRun_commands().get(x));
			final RunCommandThread t = new RunCommandThread(newi,
					ConfigurationManager.getTestcaseLength());
			System.out.println("ID" + x + ", starting: "
					+ instance.getSemantics().getRun_commands().get(x));
			t.start();
			threads.add(t);
		}

		for (int x = 0; x < threads.size(); x++) {
			final RunCommandThread t = threads.get(x);
			t.join();
			if (t.exception) {
				throw new Exception("ERROR RUNNING ALLOY");
			}
			if (t.timeout) {
				System.out.println("ID" + x + ": TIMEOUT");
				out.add(null);
			} else {
				if (t.tc == null) {
					System.out.println("ID" + x + ": UNSAT");
				} else {
					System.out.println("ID" + x + ": FOUND SOLUTION");
					out.add(t.tc);
				}
			}

		}
		return out;
	}

	public static List<GUITestCase> generateTestCasesMinimal(final Instance_GUI_pattern instance,
			final int max_time) throws Exception {

		final List<GUITestCase> out = new ArrayList<>();

		final List<RunCommandThread[]> threads = new ArrayList<>();
		for (int x = 0; x < instance.getSemantics().getRun_commands().size(); x++) {

			final Instance_GUI_pattern newi = instance.clone();
			newi.getSemantics().clearRunCommands();
			newi.getSemantics().addRun_command(instance.getSemantics().getRun_commands().get(x));
			final RunCommandThread t = new RunCommandThread(newi, max_time);
			System.out.println("ID" + x + ", starting: "
					+ instance.getSemantics().getRun_commands().get(x));
			t.start();
			final RunCommandThread[] ts = new RunCommandThread[2];
			ts[1] = t;
			threads.add(ts);

			final Instance_GUI_pattern newi2 = instance.clone();
			newi2.getSemantics().clearRunCommands();
			newi2.getSemantics().addRun_command(instance.getSemantics().getRun_commands().get(x));
			final RunCommandThreadMinimal tm = new RunCommandThreadMinimal(newi2, MIN_TIME,
					max_time);
			System.out.println("ID" + x + ", starting minimal: "
					+ instance.getSemantics().getRun_commands().get(x));
			tm.start();
			ts[0] = tm;
		}
		for (int x = 0; x < threads.size(); x++) {
			final RunCommandThread[] ts = threads.get(x);
			loop: while (true) {
				if (!ts[0].isAlive()) {
					if (!ts[0].exception && ts[0].tc != null) {
						System.out.println("ID" + x + ": FOUND SOLUTION SIZE "
								+ ts[0].tc.getActions().size());
						out.add(ts[0].tc);
						break loop;
					} else {
						if (!ts[1].isAlive()) {
							if (!ts[1].exception && ts[1].tc != null) {
								System.out.println("ID" + x + ": FOUND SOLUTION SIZE "
										+ ts[1].tc.getActions().size());
								out.add(ts[1].tc);
								break loop;
							}
							System.out.println("ID" + x + ": NOT FOUND SOLUTION");
							break loop;

						}
					}

				} else {
					if (!ts[1].isAlive()) {
						if (!ts[1].exception && ts[1].tc != null) {
							System.out.println("ID" + x + ": FOUND SOLUTION SIZE "
									+ ts[1].tc.getActions().size());
							out.add(ts[1].tc);
							break loop;
						}
						System.out.println("ID" + x + ": NOT FOUND SOLUTION");
						break loop;
					}
				}
				if (ts[0].timeout && ts[1].timeout) {
					System.out.println("ID" + x + ": TIMEOUT");

					out.add(null);
					break loop;
				}
			}
			ts[0].interrupt();
			ts[1].interrupt();
		}
		return out;
	}
}

// inner class used to run threads for parallelism
class RunCommandThread extends Thread {

	public Instance_GUI_pattern inst;
	public GUITestCase tc;
	public boolean exception = false;
	public boolean timeout = false;
	int time;

	public RunCommandThread(final Instance_GUI_pattern inst, final int time) {

		this.inst = inst;
		this.time = time;
	}

	@Override
	public void run() {

		try {
			int time = -1;
			if ((!this.inst.getSemantics().getRun_commands().get(0).contains(" for ") || !this.inst
					.getSemantics().getRun_commands().get(0).split(" for ")[1].contains("Time"))
					&& (!this.inst.getSemantics().getRun_commands().get(0).contains("}for ") || !this.inst
							.getSemantics().getRun_commands().get(0).split("}for ")[1]
							.contains("Time"))
									&& (!this.inst.getSemantics().getRun_commands().get(0).contains(")for ") || !this.inst
											.getSemantics().getRun_commands().get(0).split(")for ")[1]
													.contains("Time"))) {
				time = this.time;
			}
			this.tc = AlloyUtil.getTestcase(this.inst, 0, 0,
					ConfigurationManager.getAlloyRunScope(), time, -1);
		} catch (final TimeoutException ee) {
			this.timeout = true;
		} catch (final Exception e) {
			e.printStackTrace();
			this.exception = true;
			e.printStackTrace();
		}
	}

}

// inner class used to run threads for parallelism
final class RunCommandThreadMinimal extends RunCommandThread {

	public boolean exception = false;
	public boolean timeout = false;
	int min;

	public RunCommandThreadMinimal(final Instance_GUI_pattern inst, final int min, final int max) {

		super(inst, max);
		this.min = min;
	}

	@Override
	public void run() {

		try {

			this.tc = AlloyUtil.getTestcase(this.inst, 0, -1,
					ConfigurationManager.getAlloyRunScope(), this.min, this.time);
		} catch (final TimeoutException ee) {
			this.timeout = true;
		} catch (final InterruptedException e) {
			return;

		} catch (final Exception e) {
			this.exception = true;
			e.printStackTrace();
		}
	}

}
