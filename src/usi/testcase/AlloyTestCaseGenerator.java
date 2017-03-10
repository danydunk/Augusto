package src.usi.testcase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import src.usi.configuration.ConfigurationManager;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Option_input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.gui.structure.Window;
import src.usi.semantic.SpecificSemantics;
import src.usi.semantic.alloy.AlloyUtil;
import src.usi.testcase.inputdata.DataManager;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.Fill;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;
import src.usi.testcase.structure.Select;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;

public class AlloyTestCaseGenerator {

	private long RUN_INITIAL_TIMEOUT = 1200000; // 20 minutes
	private int MAX_RUN;
	private final int INITAL_TIME_SIZE = 3;

	final Instance_GUI_pattern instance;

	/**
	 * Function that generates GUI test cases running the run commands contained
	 * in a specific semantics. Each command is run until completion or timeout.
	 *
	 * @param initial_timout
	 * @return
	 * @throws Exception
	 */

	public AlloyTestCaseGenerator(final Instance_GUI_pattern instance, final int max_run,
			final long initial_timout) {

		this.instance = instance;
		this.RUN_INITIAL_TIMEOUT = initial_timout;
	}

	public AlloyTestCaseGenerator(final Instance_GUI_pattern instance) {

		this.instance = instance;
	}

	/**
	 * Function that generates GUI test cases running the run commands contained
	 * in a specific semantics. Each command is run until completion or timeout.
	 * The scope general scope is the one specified in the configuration.
	 * Additional scopes are added for each structural signature. If one of the
	 * structural signatures has a not definite scope (for instance during
	 * refinement) the general scope is doubled. If in the original command a
	 * general or a time scope are set they are kept.
	 *
	 * @return
	 * @throws Exception
	 */
	public List<GUITestCase> generateTestCases() throws Exception {

		this.MAX_RUN = 1;
		final SpecificSemantics model = this.instance.getSemantics();
		final String alloy_model = model.toString();
		// System.out.println("START ALLOY MODEL");
		// System.out.println(model);
		// System.out.println("END ALLOY MODEL");

		final Module compiled = AlloyUtil.compileAlloyModel(alloy_model);

		final List<Command> run_commands = compiled.getAllCommands();

		final List<RunCommandThread> threads = new ArrayList<>();
		for (final Command cmd : run_commands) {
			final RunCommandThread rc = new RunCommandThread(cmd, compiled,
					AlloyUtil.getWinScope(this.instance.getSemantics()),
					AlloyUtil.getAWScope(this.instance.getSemantics()),
					AlloyUtil.getIWScope(this.instance.getSemantics()),
					AlloyUtil.getSWScope(this.instance.getSemantics()),
					AlloyUtil.getValueScope(this.instance.getSemantics()));
			rc.start();
			threads.add(rc);
		}

		final List<A4Solution> solutions = new ArrayList<>();

		for (final RunCommandThread t : threads) {
			t.join();

			if (!t.hasExceptions() && t.getSolution() != null && t.getSolution().satisfiable()) {
				solutions.add(t.getSolution());
			} else {

				solutions.add(t.getSolution());
			}
			if (t.isAlive()) {
				t.interrupt();
			}
		}

		final List<GUITestCase> out = new ArrayList<>();

		for (int cont = 0; cont < solutions.size(); cont++) {
			final A4Solution sol = solutions.get(cont);
			if (sol != null) {
				if (sol.satisfiable()) {
					out.add(this.analyzeTuples(sol, this.instance.getSemantics().getRun_commands()
							.get(cont)));
				}

			} else {
				out.add(null);
			}
		}

		return out;
	}

	/**
	 * Function that generates GUI test cases running the run commands contained
	 * in a specific semantics. Each run command is run for a maximum of max_run
	 * times. The timeout of each run command is initial_timeout. When a command
	 * goes in timeout is rerun with a double timeout. The general scope is the
	 * one specified in the configuration. Additional scopes are added for each
	 * structural signature. If one of the structural signatures has a not
	 * definite scope (for instance during refinement) the general scope is
	 * doubled. If in the original command a general scope is set they are
	 * kept.If a time scope is set an exception is raised.
	 *
	 * @return
	 * @throws Exception
	 */
	public List<GUITestCase> generateMinimalTestCases(final int max_time) throws Exception {

		this.MAX_RUN = max_time - this.INITAL_TIME_SIZE;
		final SpecificSemantics model = this.instance.getSemantics();

		final String alloy_model = model.toString();
		// System.out.println("START ALLOY MODEL");
		// System.out.println(model);
		// System.out.println("END ALLOY MODEL");

		final Module compiled = AlloyUtil.compileAlloyModel(alloy_model);

		final List<Command> run_commands = compiled.getAllCommands();

		final List<RunCommandThread[]> threads = new ArrayList<>();
		for (final Command cmd : run_commands) {
			final RunCommandThread[] pair = new RunCommandThread[2];

			final RunCommandThread rc1 = new RunCommandThread(cmd, compiled, 0,
					AlloyUtil.getWinScope(this.instance.getSemantics()),
					AlloyUtil.getAWScope(this.instance.getSemantics()),
					AlloyUtil.getIWScope(this.instance.getSemantics()),
					AlloyUtil.getSWScope(this.instance.getSemantics()),
					AlloyUtil.getValueScope(this.instance.getSemantics()));
			rc1.start();
			final RunCommandThread rc2 = new RunCommandThread(cmd, compiled, -1,
					AlloyUtil.getWinScope(this.instance.getSemantics()),
					AlloyUtil.getAWScope(this.instance.getSemantics()),
					AlloyUtil.getIWScope(this.instance.getSemantics()),
					AlloyUtil.getSWScope(this.instance.getSemantics()),
					AlloyUtil.getValueScope(this.instance.getSemantics()));
			rc2.start();
			pair[0] = rc1;
			pair[1] = rc2;
			threads.add(pair);
		}

		final List<A4Solution> solutions = new ArrayList<>();
		for (final RunCommandThread[] ts : threads) {
			loop: while (true) {
				if (!ts[0].isAlive()) {
					if (!ts[0].hasExceptions() && ts[0].getSolution() != null
							&& ts[0].getSolution().satisfiable()) {
						solutions.add(ts[0].getSolution());
						break loop;
					} else {
						if (!ts[1].isAlive()) {
							if (!ts[1].hasExceptions() && ts[1].getSolution() != null
									&& ts[1].getSolution().satisfiable()) {
								solutions.add(ts[1].getSolution());
								break loop;
							} else {
								solutions.add(ts[0].getSolution());
								break loop;
							}
						} else {
							continue loop;
						}
					}

				} else {
					if (!ts[1].isAlive()) {
						solutions.add(ts[1].getSolution());
						break loop;
					}
				}
			}
			ts[0].interrupt();
			ts[1].interrupt();
		}

		final List<GUITestCase> out = new ArrayList<>();
		for (int cont = 0; cont < solutions.size(); cont++) {
			final A4Solution sol = solutions.get(cont);
			if (sol != null) {
				if (sol.satisfiable()) {
					out.add(this.analyzeTuples(sol, this.instance.getSemantics().getRun_commands()
							.get(cont)));
				}
			} else {
				out.add(null);
			}
		}

		return out;
	}

	protected GUITestCase analyzeTuples(final A4Solution solution, final String run)
			throws Exception {

		Map<String, String> input_data_map = null;
		input_data_map = this.elaborateInputData(solution);

		final List<A4Tuple> tracks = AlloyUtil
				.getTuplesRel(solution, "Track$0", "this/Track", "op");
		final List<A4Tuple> curr_wind = AlloyUtil.getTuplesRel(solution, "Current_window$0",
				"this/Current_window", "is_in");

		assert (tracks.size() == curr_wind.size() - 1);

		final List<GUIAction> actions = new ArrayList<>(tracks.size());
		for (final A4Tuple t : tracks) {
			actions.add(null);
		}

		for (final A4Tuple tuple : tracks) {

			assert (tuple.arity() == 3);

			final int time_index = this.extractTimeIndex(tuple.atom(2));

			Window source_window = null;
			Window oracle = null;

			for (final A4Tuple curr : curr_wind) {

				if (this.extractTimeIndex(curr.atom(2)) == (time_index - 1)) {
					String windid = curr.atom(1).split("\\$")[0];
					windid = windid.split("_")[1];
					source_window = this.instance.getGui().getWindow(windid);
				}
				// oracle
				if (this.extractTimeIndex(curr.atom(2)) == (time_index)) {

					String windid = curr.atom(1).split("\\$")[0];
					if (windid.startsWith("Undiscovered")) {
						continue;

					} else {
						windid = windid.split("_")[1];
						final Window target = this.instance.getGui().getWindow(windid);
						if (target == null) {
							// oracle window not found
							continue;
						}
						final List<Action_widget> aws = new ArrayList<>();
						final List<Input_widget> iws = new ArrayList<>();
						final List<Selectable_widget> sws = new ArrayList<>();

						List<A4Tuple> association = AlloyUtil.getTuplesRel(solution, curr.atom(1),
								"this/Window", "aws");

						awloop: for (final Action_widget aw : target.getActionWidgets()) {

							for (final A4Tuple t : association) {
								// if the aw is associated to the current window
								// we add it
								if (t.arity() == 2
										&& t.atom(1).equals("Action_widget_" + aw.getId() + "$0")) {
									aws.add(aw);
									continue awloop;
								}
							}
						}
						association = AlloyUtil.getTuplesRel(solution, curr.atom(1), "this/Window",
								"iws");
						iwloop: for (final Input_widget iw : target.getInputWidgets()) {
							final List<A4Tuple> values = AlloyUtil.getTuplesRel(solution,
									"Input_widget_" + iw.getId() + "$0", "this/Input_widget",
									"content");
							String inputdata = "";
							for (final A4Tuple value : values) {

								assert (value.arity() <= 3);

								if (value.arity() == 3 && value.atom(2).equals(tuple.atom(2))) {

									if (iw instanceof Option_input_widget) {

										assert (value.atom(1).toString()
												.startsWith("Option_value_"));
										inputdata = value.atom(1).replace("Option_value_", "")
												.replace("$0", "");

									} else {
										// the input data is retrieved
										inputdata = input_data_map.get(value.atom(1));
									}
								}
							}

							for (final A4Tuple t : association) {
								// if the iw is associated to the current window
								// we add it
								if (t.arity() == 2
										&& t.atom(1).equals("Input_widget_" + iw.getId() + "$0")) {
									if (iw instanceof Option_input_widget) {
										final Option_input_widget oiw = (Option_input_widget) iw;

										if (inputdata.length() == 0) {
											inputdata = String.valueOf(oiw.getSelected());
										}
										final Option_input_widget new_oiw = new Option_input_widget(
												oiw.getId(), oiw.getLabel(), oiw.getClasss(),
												oiw.getX(), oiw.getY(), oiw.getWidth(),
												oiw.getHeight(), oiw.getSize(),
												Integer.valueOf(inputdata));
										new_oiw.setDescriptor(oiw.getDescriptor());
										iws.add(new_oiw);
									} else {
										final Input_widget new_iw = new Input_widget(iw.getId(),
												iw.getLabel(), iw.getClasss(), iw.getX(),
												iw.getY(), iw.getWidth(), iw.getHeight(), inputdata);
										new_iw.setDescriptor(iw.getDescriptor());
										iws.add(new_iw);
										continue iwloop;
									}

								}
							}
						}
						association = AlloyUtil.getTuplesRel(solution, curr.atom(1), "this/Window",
								"sws");
						swloop: for (final Selectable_widget sw : target.getSelectableWidgets()) {
							for (final A4Tuple t : association) {
								// if the aw is associated to the current window
								// we add it
								if (t.arity() == 2
										&& t.atom(1).equals(
												"Selectable_widget_" + sw.getId() + "$0")) {

									final List<A4Tuple> objs = AlloyUtil.getTuplesRel(solution,
											"Selectable_widget_" + sw.getId() + "$0",
											"this/Selectable_widget", "list");
									final Map<String, Integer> map = new HashMap<>();
									final List<Integer> to_order = new ArrayList<>();
									String selected = "";

									for (final A4Tuple obj : objs) {

										assert (obj.arity() == 3);

										if (obj.atom(2).equals(tuple.atom(2))) {
											if (!map.containsKey(obj.atom(1))) {
												final List<A4Tuple> appeared = AlloyUtil
														.getTuplesRel(solution, obj.atom(1),
																"this/Object", "appeared");

												assert (appeared.size() > 0);

												int ind = -1;
												for (final A4Tuple tt : appeared) {
													if (tt.arity() == 2
															&& tt.atom(1).startsWith("Time")) {

														ind = this.extractTimeIndex(tt.atom(1));
														break;
													}
												}

												assert (ind != -1);
												map.put(obj.atom(1), ind);
												to_order.add(ind);
											} else {
												// if it appears two time it
												// means it is
												// the selected one

												assert (selected.length() == 0);

												selected = obj.atom(1);
											}
										}
									}
									Collections.sort(to_order);
									int sel = -1;
									if (selected.length() > 0) {
										sel = to_order.indexOf(map.get(selected));
									}
									final Selectable_widget new_sw = new Selectable_widget(
											sw.getId(), sw.getLabel(), sw.getClasss(), sw.getX(),
											sw.getY(), sw.getWidth(), sw.getHeight(), sw.getSize()
											+ (map.keySet().size()), sel);
									new_sw.setDescriptor(sw.getDescriptor());
									sws.add(new_sw);
									continue swloop;
								}
							}
						}

						oracle = new Window(target.getId(), target.getLabel(), target.getClasss(),
								target.getX(), target.getY(), target.getWidth(),
								target.getHeight(), target.isModal());
						oracle.setAction_widgets(aws);
						oracle.setInput_widgets(iws);
						oracle.setSelectable_widgets(sws);
					}
				}
			}

			assert (source_window != null);

			// final List<A4Tuple> params = AlloyUtil.getTuples(solution,
			// tuple.atom(1));

			if (tuple.atom(1).startsWith("Fill")) {

				final List<A4Tuple> values = AlloyUtil.getTuplesRel(solution, tuple.atom(1),
						"this/Fill", "with");
				final List<A4Tuple> targetiw = AlloyUtil.getTuplesRel(solution, tuple.atom(1),
						"this/Fill", "filled");

				assert (values.size() <= 1);
				assert (targetiw.size() == 1);

				String value = null;

				String iw_id = targetiw.get(0).atom(1).substring(13);

				iw_id = iw_id.split("\\$")[0];

				if (values.size() == 1) {
					value = values.get(0).atom(1);

				}
				assert (iw_id != null);

				Input_widget target_iw = null;
				for (final Input_widget iw : this.instance.getGui().getInput_widgets()) {
					if (iw.getId().equals(iw_id)) {
						// target_iw = iw;
						// TODO: add the current value
						if (iw instanceof Option_input_widget) {
							final Option_input_widget oiw = (Option_input_widget) iw;
							target_iw = new Option_input_widget(oiw.getId(), oiw.getLabel(),
									oiw.getClasss(), oiw.getX(), oiw.getY(), oiw.getWidth(),
									oiw.getHeight(), oiw.getSize(), oiw.getSelected());
						} else {
							target_iw = new Input_widget(iw.getId(), iw.getLabel(), iw.getClasss(),
									iw.getX(), iw.getY(), iw.getWidth(), iw.getHeight(),
									iw.getValue());
						}

						target_iw.setDescriptor(iw.getDescriptor());
						break;
					}
				}

				assert (target_iw != null);

				// the input data is retrieved
				String inputdata = null;
				if (value != null) {
					if (target_iw instanceof Option_input_widget) {

						assert (value.startsWith("Option_value_"));
						inputdata = value.replace("Option_value_", "").replace("$0", "");
					} else {
						inputdata = input_data_map.get(value);

					}

					assert (inputdata != null);
				}
				final Fill action = new Fill(source_window, oracle, target_iw, inputdata);
				actions.set(time_index - 1, action);
				continue;
			}

			if (tuple.atom(1).startsWith("Click")) {

				final List<A4Tuple> targetaw = AlloyUtil.getTuplesRel(solution, tuple.atom(1),
						"this/Click", "clicked");
				assert (targetaw.size() == 1);

				final A4Tuple wid_tuple = targetaw.get(0);

				assert (wid_tuple.atom(1).startsWith("Action_widget_"));

				String aw_id = wid_tuple.atom(1).substring(14);
				aw_id = aw_id.split("\\$")[0];

				Action_widget target_aw = null;
				for (final Action_widget aw : this.instance.getGui().getAction_widgets()) {
					if (aw.getId().equals(aw_id)) {
						// target_aw = aw;
						target_aw = new Action_widget(aw.getId(), aw.getLabel(), aw.getClasss(),
								aw.getX(), aw.getY(), aw.getWidth(), aw.getHeight());
						target_aw.setDescriptor(aw.getDescriptor());
						break;
					}
				}

				assert (target_aw != null);

				final Click action = new Click(source_window, oracle, target_aw);
				actions.set(time_index - 1, action);
				continue;
			}
			if (tuple.atom(1).startsWith("Select")) {

				final List<A4Tuple> targetsw = AlloyUtil.getTuplesRel(solution, tuple.atom(1),
						"this/Select", "wid");
				final List<A4Tuple> which = AlloyUtil.getTuplesRel(solution, tuple.atom(1),
						"this/Select", "which");
				assert (targetsw.size() == 1);
				assert (which.size() == 1);

				final String sw_name = targetsw.get(0).atom(1);
				String sw_id = targetsw.get(0).atom(1).substring(18);
				sw_id = sw_id.split("\\$")[0];

				final String object = which.get(0).atom(1);
				;

				assert (object != null && sw_id != null);

				// all the tuples connected with the sw
				final List<A4Tuple> sw_list = AlloyUtil.getTuplesRel(solution, sw_name,
						"this/Selectable_widget", "list");
				final List<String> objects_in_sw_at_t = new ArrayList<>();
				for (final A4Tuple sw_tuple : sw_list) {
					if (this.extractTimeIndex(sw_tuple.atom(2)) == (time_index - 1)) {
						if (!objects_in_sw_at_t.contains(sw_tuple.atom(1))
								&& sw_tuple.atom(1).startsWith("Object")) {
							objects_in_sw_at_t.add(sw_tuple.atom(1));
						}
					}
				}
				// now we order the objects in the selectable widget wrt their
				// addition time
				final List<Integer> to_order = new ArrayList<>();
				final Map<String, Integer> map = new HashMap<>();
				for (final String obj : objects_in_sw_at_t) {
					final List<A4Tuple> obj_tuples = AlloyUtil.getTuplesRel(solution, obj,
							"this/Object", "appeared");

					assert (obj_tuples.size() == 1);
					final int appeared = this.extractTimeIndex(obj_tuples.get(0).atom(1));

					assert (appeared != -1);
					if (!map.containsKey(obj)) {
						to_order.add(appeared);
						map.put(obj, appeared);
					}
				}
				Collections.sort(to_order);
				final List<String> ordered = new ArrayList<>();
				for (final Integer s : to_order) {
					ordered.add(null);
				}
				for (final String obj : map.keySet()) {
					ordered.set(to_order.indexOf(map.get(obj)), obj);
				}

				Selectable_widget target_sw = null;
				for (final Selectable_widget sw : this.instance.getGui().getSelectable_widgets()) {
					if (sw.getId().equals(sw_id)) {
						// target_sw = sw;
						// TODO: add the correct selected
						target_sw = new Selectable_widget(sw.getId(), sw.getLabel(),
								sw.getClasss(), sw.getX(), sw.getY(), sw.getWidth(),
								sw.getHeight(), objects_in_sw_at_t.size(), 0);
						target_sw.setDescriptor(sw.getDescriptor());
						break;
					}
				}

				assert (target_sw != null);

				final int select_index = ordered.indexOf(object);

				final Select action = new Select(source_window, oracle, target_sw, select_index,
						true);
				actions.set(time_index - 1, action);

				continue;
			}
		}

		final GUITestCase test = new GUITestCase(solution, actions, run);
		return test;
	}

	/**
	 * Function that reads a solution and prepares all the input values for the
	 * input widgets
	 *
	 * @param solution
	 * @return
	 * @throws Exception
	 */
	private Map<String, String> elaborateInputData(final A4Solution solution) throws Exception {

		final Map<String, String> out = new HashMap<>();
		final DataManager dm = DataManager.getInstance();
		Sig value = null;
		Sig fill = null;
		for (final Sig sig : solution.getAllReachableSigs()) {
			if ("this/Value".equals(sig.label)) {
				value = sig;
			}
			if ("this/Fill".equals(sig.label)) {
				fill = sig;
			}
		}

		assert (value != null && fill != null);

		List<String> fill_atoms = AlloyUtil.getElementsInSet(solution, fill);
		List<String> value_atoms = AlloyUtil.getElementsInSet(solution, value);

		// in the atoms extracted the underscore is substituted with the dollar
		fill_atoms = fill_atoms
				.stream()
				.map(e -> e.substring(0, e.lastIndexOf("_")) + "$"
						+ e.substring(e.lastIndexOf("_") + 1)).collect(Collectors.toList());

		value_atoms = value_atoms
				.stream()
				.map(e -> e.substring(0, e.lastIndexOf("_")) + "$"
						+ e.substring(e.lastIndexOf("_") + 1)).collect(Collectors.toList());

		final Map<String, List<String>> data_for_value = new HashMap<>();

		// we deal with the initial values
		for (final Input_widget iw : this.instance.getGui().getInput_widgets()) {
			if (iw instanceof Option_input_widget) {
				final Option_input_widget oiw = (Option_input_widget) iw;
				final List<A4Tuple> tups = AlloyUtil.getTuplesRel(solution,
						"Input_widget_" + iw.getId() + "$0", "this/Input_widget", "content");
				String first = null;
				for (final A4Tuple tup : tups) {
					if (tup.arity() == 3 && tup.atom(2).startsWith("Time$0")) {
						first = tup.atom(1);
						break;
					}
				}
				if (first != null) {

					out.put(first + "_option", String.valueOf(oiw.getSelected()));
					continue;
				}
			}
		}

		for (final String fill_atom : fill_atoms) {

			final List<A4Tuple> vals = AlloyUtil.getTuplesRel(solution, fill_atom, "this/Fill",
					"with");
			final List<A4Tuple> targetiw = AlloyUtil.getTuplesRel(solution, fill_atom, "this/Fill",
					"filled");

			final String iw = targetiw.get(0).atom(1);
			String v = null;

			if (vals.size() == 1) {
				v = vals.get(0).atom(1);
			}

			final List<String> invalid_values = new ArrayList<String>();
			final List<A4Tuple> inv_tuples = AlloyUtil.getTuplesRel(solution, iw,
					"this/Input_widget", "invalid");
			for (final A4Tuple tup : inv_tuples) {
				assert (tup.arity() == 2 && tup.atom(1).startsWith("Value"));
				invalid_values.add(tup.atom(1));
			}

			assert (iw != null && invalid_values != null);

			String iw_id = iw.substring(13);
			iw_id = iw_id.split("\\$")[0];
			Input_widget inpw = null;
			// the corresponding iw is searched in the instance
			for (final Input_widget i_w : this.instance.getGui().getInput_widgets()) {
				if (i_w.getId().equals(iw_id)) {
					inpw = i_w;
					break;
				}
			}
			// options values are handled differently
			if (v != null && v.startsWith("Option_value_")) {
				continue;
			}

			assert (inpw != null);
			if (v != null) {

				assert (!(inpw instanceof Option_input_widget));

				String metadata = inpw.getLabel() != null ? inpw.getLabel() : "";
				metadata += inpw.getDescriptor() != null && metadata.length() == 0 ? inpw
						.getDescriptor() : "";

						List<String> data = null;
						if (invalid_values.contains(v)) {
							data = dm.getInvalidData(metadata);

							assert (data.size() > 0);
						} else {
							data = dm.getValidData(metadata);
						}

						assert (data != null);

						if (data_for_value.containsKey(v)) {
							List<String> new_list = new ArrayList<>();
							// we calculate the intersection between the values
							// already
							// available for this value and the new ones

							if (data.size() == 0) {
								new_list = data_for_value.get(v);
							} else {
								for (final String s : data_for_value.get(v)) {
									if (data.contains(s)) {
										new_list.add(s);
									}
								}
							}
							if (invalid_values.contains(v) && new_list.size() == 0) {
								throw new Exception(
										"AlloyTestCaseGeneration - not enough invalid input data.");
							}

							data_for_value.put(v, new_list);
						} else {
							data_for_value.put(v, data);
						}

			}
		}

		// we keep track of the values so that we do not use twice the same
		// input data for different values
		final List<String> used_values = new ArrayList<>();
		for (final String key : data_for_value.keySet()) {
			final List<String> possible_values = new ArrayList<>();
			for (final String s : data_for_value.get(key)) {
				if (!used_values.contains(s)) {

					possible_values.add(s);
				}
			}
			if (possible_values.isEmpty()) {
				final List<String> generics = dm.getGenericData();

				for (final String s : generics) {
					if (!used_values.contains(s)) {
						possible_values.add(s);
					}
				}
			}
			if (possible_values.isEmpty()) {
				throw new Exception(
						"AlloyTestCaseGenerator - elaborateInputData: not enough inputdata.");
			}
			final Random r = new Random();
			final int index = r.nextInt(possible_values.size());
			final String val = possible_values.get(index);
			used_values.add(val);

			assert (!out.containsKey(key));

			out.put(key, val);
		}

		return out;
	}

	// inner class used to run threads for parallelism
	final class RunCommandThread extends Thread {

		final private Command run_command;
		final private Module alloy_model;
		private A4Solution solution;
		private boolean exception;
		// 1 normal, 0 minimal, -1 max
		private final int type;
		private final int win_scope;
		private final int aw_scope;
		private final int iw_scope;
		private final int sw_scope;
		private final int value_scope;

		public RunCommandThread(final Command run, final Module alloy, final int win_scope,
				final int aw_scope, final int iw_scope, final int sw_scope, final int value_scope) {

			this.run_command = run;
			this.alloy_model = alloy;
			this.exception = false;
			this.type = 1;
			this.win_scope = win_scope;
			this.aw_scope = aw_scope;
			this.iw_scope = iw_scope;
			this.sw_scope = sw_scope;
			this.value_scope = value_scope;
		}

		public RunCommandThread(final Command run, final Module alloy, final int type,
				final int win_scope, final int aw_scope, final int iw_scope, final int sw_scope,
				final int value_scope) {

			this.run_command = run;
			this.alloy_model = alloy;
			this.exception = false;
			this.type = type;
			this.win_scope = win_scope;
			this.aw_scope = aw_scope;
			this.iw_scope = iw_scope;
			this.sw_scope = sw_scope;
			this.value_scope = value_scope;
		}

		@Override
		public void run() {

			try {
				int overall = ConfigurationManager.getAlloyRunScope();

				if (this.run_command.overall > 3) {
					overall = this.run_command.overall;
				} else if (this.win_scope == -1 || this.aw_scope == -1 || this.iw_scope == -1
						|| this.sw_scope == -1) {
					overall = overall * 4;
				}

				Sig win = null;
				for (final Sig s : this.alloy_model.getAllSigs()) {
					if (s.label.equals("this/Window")) {
						win = s;
					}
				}
				if (win == null) {
					this.exception = true;
					return;
				}

				Sig v = null;
				for (final Sig s : this.alloy_model.getAllSigs()) {
					if (s.label.equals("this/Value")) {
						v = s;
					}
				}
				if (v == null) {
					this.exception = true;
					return;
				}

				Sig aw = null;
				for (final Sig s : this.alloy_model.getAllSigs()) {
					if (s.label.equals("this/Action_widget")) {
						aw = s;
					}
				}
				if (aw == null) {
					this.exception = true;
					return;
				}

				Sig iw = null;
				for (final Sig s : this.alloy_model.getAllSigs()) {
					if (s.label.equals("this/Input_widget")) {
						iw = s;
					}
				}
				if (iw == null) {
					this.exception = true;
					return;
				}

				Sig sw = null;
				for (final Sig s : this.alloy_model.getAllSigs()) {
					if (s.label.equals("this/Selectable_widget")) {
						sw = s;
					}
				}
				if (sw == null) {
					this.exception = true;
					return;
				}
				Sig time = null;
				for (final Sig s : this.alloy_model.getAllSigs()) {
					if (s.label.equals("this/Time")) {
						time = s;
					}
				}
				if (time == null) {
					this.exception = true;
					return;
				}

				Sig operation = null;
				for (final Sig s : this.alloy_model.getAllSigs()) {
					if (s.label.equals("this/Operation")) {
						operation = s;
					}
				}
				if (operation == null) {
					this.exception = true;
					return;
				}

				int time_scope = -1;

				final ConstList<CommandScope> set_scopes = this.run_command.scope;
				for (final CommandScope scope : set_scopes) {
					if (scope.sig == time) {
						time_scope = scope.endingScope;
					}
				}

				if ((this.type == 0 || this.type == -1) && time_scope != -1) {
					this.exception = true;
					return;
				}

				if (this.type == 0) {
					time_scope = AlloyTestCaseGenerator.this.INITAL_TIME_SIZE;
				}
				if (this.type == -1) {
					time_scope = AlloyTestCaseGenerator.this.INITAL_TIME_SIZE
							+ AlloyTestCaseGenerator.this.MAX_RUN;
				} else {
					if (time_scope == -1) {
						time_scope = ConfigurationManager.getTestcaseLength();
					}
				}

				final long timeout = AlloyTestCaseGenerator.this.RUN_INITIAL_TIMEOUT;

				for (int x = 0; x < AlloyTestCaseGenerator.this.MAX_RUN; x++) {

					final List<CommandScope> scopes = new ArrayList<>();
					final CommandScope timescope = new CommandScope(time, false, time_scope);
					final CommandScope opscope = new CommandScope(operation, false, time_scope - 1);
					scopes.add(timescope);
					scopes.add(opscope);
					if (this.win_scope > -1) {
						final CommandScope winscope = new CommandScope(win, false, this.win_scope);
						scopes.add(winscope);
					}
					if (this.aw_scope > -1) {
						final CommandScope awscope = new CommandScope(aw, false, this.aw_scope);
						scopes.add(awscope);
					}
					if (this.value_scope > -1) {
						final CommandScope vscope = new CommandScope(v, false,
								(this.value_scope + ((time_scope - 1) * 1 / 2)));
						scopes.add(vscope);
					}
					if (this.iw_scope > -1) {
						final CommandScope iwscope = new CommandScope(iw, false, this.iw_scope);
						scopes.add(iwscope);
					}
					if (this.sw_scope > -1) {
						final CommandScope swscope = new CommandScope(sw, false, this.sw_scope);
						scopes.add(swscope);
					}

					final ConstList<CommandScope> scope_list = this.run_command.scope.make(scopes);

					final Command run = new Command(this.run_command.pos, this.run_command.label,
							this.run_command.check, overall, this.run_command.bitwidth,
							this.run_command.maxseq, this.run_command.expects, scope_list,
							this.run_command.additionalExactScopes, this.run_command.formula,
							this.run_command.parent);

					System.out.println("STARTING COMMAND: " + run.toString() + " RUN " + (x + 1));
					final A4Solution solution = AlloyUtil
							.runCommand(this.alloy_model, run, timeout);

					if (Thread.currentThread().isInterrupted()) {
						System.out.println("RUN " + (x + 1) + " COMMAND: " + run.toString()
								+ " INTERRUPTED!");
						return;
					}
					if (solution == null) {
						// if timeout
						System.out.println("RUN " + (x + 1) + " COMMAND: " + run.toString()
								+ " TIMEOUT!");
						break;
					} else if (this.type == 0 && !solution.satisfiable()) {

						time_scope++;
						if (x + 1 < AlloyTestCaseGenerator.this.MAX_RUN) {
							System.out.println("RUN " + (x + 1) + " COMMAND: " + run.toString()
									+ " unsat. Time scope = " + time_scope);
						}
					} else {
						this.solution = solution;
						if (solution.satisfiable()) {
							System.out.println("RUN " + (x + 1) + " COMMAND: " + run.toString()
									+ " found solution.");
						} else {
							System.out.println("RUN " + (x + 1) + " COMMAND: " + run.toString()
									+ " solution not found.");
						}
						break;
					}
				}
			} catch (final InterruptedException ee) {
				System.out.println("COMMAND: " + this.run_command.toString() + " INTERRUPTED!");
				return;
			} catch (final Exception e) {
				// e.printStackTrace();
				this.exception = true;
			}
		}

		public A4Solution getSolution() {

			return this.solution;
		}

		public boolean hasExceptions() {

			return this.exception;
		}
	}

	private int extractTimeIndex(final String atom) throws Exception {

		if (!atom.startsWith("Time")) {
			throw new Exception("AlloyUtil - extractTimeIndex: atom should start with Time.");
		}
		try {
			return Integer.valueOf(atom.split("\\$")[1]);
		} catch (final Exception e) {
			throw new Exception("AlloyUtil - extractTimeIndex: error.");
		}
	}

}
