package usi.gui.semantic.testcase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import usi.configuration.ConfigurationManager;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.functionality.mapping.Instance_window;
import usi.gui.semantic.SpecificSemantics;
import usi.gui.semantic.alloy.AlloyUtil;
import usi.gui.semantic.testcase.inputdata.DataManager;
import usi.gui.structure.Action_widget;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Option_input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;

public class AlloyTestCaseGenerator {

	private long RUN_INITIAL_TIMEOUT = 1800000; // 30 minutes
	private int MAX_RUN = 5;

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
		this.MAX_RUN = max_run;
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
					AlloyUtil.getSWScope(this.instance.getSemantics()));
			rc.start();
			threads.add(rc);
		}

		final List<A4Solution> solutions = new ArrayList<>();
		for (final RunCommandThread t : threads) {
			t.join();

			if (!t.hasExceptions() && t.getSolution() != null && t.getSolution().satisfiable()) {
				solutions.add(t.getSolution());
			} else {
				solutions.add(null);
			}
			if (t.isAlive()) {
				t.interrupt();
			}
		}

		final List<GUITestCase> out = new ArrayList<>();
		for (int cont = 0; cont < solutions.size(); cont++) {
			final A4Solution sol = solutions.get(cont);
			if (sol != null) {
				out.add(this.analyzeTuples(sol,
						this.instance.getSemantics().getRun_commands().get(cont)));
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
	public List<GUITestCase> generateMinimalTestCases() throws Exception {

		final SpecificSemantics model = this.instance.getSemantics();

		final String alloy_model = model.toString();
		// System.out.println("START ALLOY MODEL");
		// System.out.println(model);
		// System.out.println("END ALLOY MODEL");

		final Module compiled = AlloyUtil.compileAlloyModel(alloy_model);

		final List<Command> run_commands = compiled.getAllCommands();

		final List<RunCommandThread> threads = new ArrayList<>();
		for (final Command cmd : run_commands) {
			final RunCommandThread rc = new RunCommandThread(cmd, compiled, true,
					AlloyUtil.getWinScope(this.instance.getSemantics()),
					AlloyUtil.getAWScope(this.instance.getSemantics()),
					AlloyUtil.getIWScope(this.instance.getSemantics()),
					AlloyUtil.getSWScope(this.instance.getSemantics()));
			rc.start();
			threads.add(rc);
		}

		final List<A4Solution> solutions = new ArrayList<>();
		for (final RunCommandThread t : threads) {
			t.join();
			if (!t.hasExceptions() && t.getSolution() != null && t.getSolution().satisfiable()) {
				solutions.add(t.getSolution());
			} else {
				solutions.add(null);
			}
			if (t.isAlive()) {
				t.interrupt();
			}
		}

		final List<GUITestCase> out = new ArrayList<>();
		for (int cont = 0; cont < solutions.size(); cont++) {
			final A4Solution sol = solutions.get(cont);
			if (sol != null) {
				out.add(this.analyzeTuples(sol,
						this.instance.getSemantics().getRun_commands().get(cont)));
			}
		}

		return out;
	}

	protected GUITestCase analyzeTuples(final A4Solution solution, final String run)
			throws Exception {

		final Map<String, String> input_data_map = this.elaborateInputData(solution);
		final List<A4Tuple> tracks = AlloyUtil.getTuples(solution, "Track$0");
		final List<A4Tuple> curr_wind = AlloyUtil.getTuples(solution, "Current_window$0");
		if (tracks.size() != curr_wind.size() - 1) {
			throw new Exception(
					"AlloyTestCaseGenerator - analyzeTuples: track and curr window must have same size.");
		}
		final List<GUIAction> actions = new ArrayList<>(tracks.size());
		for (final A4Tuple t : tracks) {
			actions.add(null);
		}

		for (final A4Tuple tuple : tracks) {
			if (tuple.arity() != 3) {
				throw new Exception("AlloyTestCaseGenerator - analyzeTuples: wrong arity of track.");
			}

			final int time_index = this.extractTimeIndex(tuple.atom(2));

			Window source_window = null;
			Window oracle = null;

			for (final A4Tuple curr : curr_wind) {
				// System.out.println("Time$" + (time_index - 1));
				if (this.extractTimeIndex(curr.atom(2)) == (time_index - 1)) {
					String windid = curr.atom(1).split("\\$")[0];
					if (windid.startsWith("General")) {
						// TODO: fix this
						source_window = new Window("General", "General", "General", 0, 0, false);

					} else {
						windid = windid.split("_")[1];
						source_window = this.instance.getGui().getWindow(windid);
					}
				}
				// oracle
				if (this.extractTimeIndex(curr.atom(2)) == (time_index)) {

					String windid = curr.atom(1).split("\\$")[0];
					if (windid.startsWith("General") || windid.startsWith("Undiscovered")) {
						continue;

					} else {
						windid = windid.split("_")[1];
						final Window target = this.instance.getGui().getWindow(windid);
						if (target == null) {
							throw new Exception(
									"AlloyTestCaseGenerator - analyzeTuples: target window not found in GUI.");
						}
						final List<Action_widget> aws = new ArrayList<>();
						final List<Input_widget> iws = new ArrayList<>();
						final List<Selectable_widget> sws = new ArrayList<>();

						final List<A4Tuple> association = AlloyUtil.getTuples(solution,
								curr.atom(1));

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

						iwloop: for (final Input_widget iw : target.getInputWidgets()) {
							final List<A4Tuple> values = AlloyUtil.getTuples(solution,
									"Input_widget_" + iw.getId() + "$0");
							String inputdata = "";
							for (final A4Tuple value : values) {
								if (value.arity() != 3) {
									throw new Exception(
											"AlloyTestCaseGenerator - analyzeTuples: error retriving input widget value.");
								}
								if (value.atom(2).equals(tuple.atom(2))) {

									if (iw instanceof Option_input_widget) {

										final String val = (value.atom(1).split("\\$")[0]);
										// System.out.println("before " + val);
										inputdata = val.split("_value_")[1];

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
										// System.out.println(inputdata);
										final Option_input_widget oiw = (Option_input_widget) iw;

										if (inputdata.length() == 0) {
											inputdata = String.valueOf(oiw.getSelected());
										}
										final Option_input_widget new_oiw = new Option_input_widget(
												oiw.getId(), oiw.getLabel(), oiw.getClasss(),
												oiw.getX(), oiw.getY(), oiw.getSize(),
												Integer.valueOf(inputdata));
										new_oiw.setDescriptor(oiw.getDescriptor());
										iws.add(new_oiw);
									} else {
										final Input_widget new_iw = new Input_widget(iw.getId(),
												iw.getLabel(), iw.getClasss(), iw.getX(),
												iw.getY(), inputdata);
										new_iw.setDescriptor(iw.getDescriptor());
										iws.add(new_iw);
										continue iwloop;
									}

								}
							}
						}

						swloop: for (final Selectable_widget sw : target.getSelectableWidgets()) {
							for (final A4Tuple t : association) {
								// if the aw is associated to the current window
								// we add it
								if (t.arity() == 2
										&& t.atom(1).equals(
												"Selectable_widget_" + sw.getId() + "$0")) {

									final List<A4Tuple> objs = AlloyUtil.getTuples(solution,
											"Selectable_widget_" + sw.getId() + "$0");
									final Map<String, Integer> map = new HashMap<>();
									final List<Integer> to_order = new ArrayList<>();
									String selected = "";

									for (final A4Tuple obj : objs) {
										if (obj.arity() != 3) {
											throw new Exception(
													"AlloyTestCaseGenerator - analyzeTuples: error retriving objects for selectable widget.");
										}

										if (obj.atom(2).equals(tuple.atom(2))) {
											if (!map.containsKey(obj.atom(1))) {
												final List<A4Tuple> appeared = AlloyUtil.getTuples(
														solution, obj.atom(1));
												if (appeared.size() != 1) {
													throw new Exception(
															"AlloyTestCaseGenerator - analyzeTuples: error ordering objects for selectable widget.");
												}
												final int ind = this.extractTimeIndex(appeared.get(
														0).atom(1));
												map.put(obj.atom(1), ind);
												to_order.add(ind);
											} else {
												// if it appears two time it
												// means it is
												// the selected one
												if (selected.length() > 0) {
													throw new Exception(
															"AlloyTestCaseGenerator - analyzeTuples: error retriving selected for selectable widget.");
												}
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
											sw.getY(), sw.getSize() + (map.keySet().size()), sel);
									new_sw.setDescriptor(sw.getDescriptor());
									sws.add(new_sw);
									continue swloop;
								}
							}
						}

						oracle = new Window(target.getId(), target.getLabel(), target.getClasss(),
								target.getX(), target.getY(), target.isModal());
						oracle.setAction_widgets(aws);
						oracle.setInput_widgets(iws);
						oracle.setSelectable_widgets(sws);
					}
				}
			}
			if (source_window == null) {
				throw new Exception(
						"AlloyTestCaseGenerator - analyzeTuples: source window not found.");
			}

			final List<A4Tuple> params = AlloyUtil.getTuples(solution, tuple.atom(1));

			if (tuple.atom(1).startsWith("Go")) {
				if (params.size() != 1) {
					throw new Exception(
							"AlloyTestCaseGenerator - analyzeTuples: wrong number of tuples for go.");
				}
				final A4Tuple wid_tuple = params.get(0);
				if (wid_tuple.atom(1).equals("General$0")) {
					continue;
				}

				if (!wid_tuple.atom(1).startsWith("Window_")) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in go.");
				}
				String window_id = wid_tuple.atom(1).substring(7);
				window_id = window_id.split("\\$")[0];

				Window target_w = null;
				for (final Instance_window w : this.instance.getWindows()) {
					if (w.getInstance().getId().equals(window_id)) {
						target_w = w.getInstance();
						break;
					}
				}

				if (target_w == null) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in go.");
				}

				final Go action = new Go(source_window, oracle, target_w);
				actions.set(time_index - 1, action);
				continue;
			}

			if (tuple.atom(1).startsWith("Fill")) {
				if (params.size() != 2) {
					throw new Exception(
							"AlloyTestCaseGenerator - analyzeTuples: wrong number of tuples for fill.");
				}

				String iw_id = null;
				String value = null;

				for (final A4Tuple t : params) {
					if (t.atom(1).toLowerCase().contains("value")) {
						value = t.atom(1);
						continue;
					} else if (t.atom(1).startsWith("Input_widget_")) {
						iw_id = t.atom(1).substring(13);
						iw_id = iw_id.split("\\$")[0];
						continue;
					}

				}

				if (value == null || iw_id == null) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in fill.");
				}

				Input_widget target_iw = null;
				for (final Input_widget iw : this.instance.getGui().getInput_widgets()) {
					if (iw.getId().equals(iw_id)) {
						// target_iw = iw;
						// TODO: add the current value
						if (iw instanceof Option_input_widget) {
							final Option_input_widget oiw = (Option_input_widget) iw;
							target_iw = new Option_input_widget(oiw.getId(), oiw.getLabel(),
									oiw.getClasss(), oiw.getX(), oiw.getY(), oiw.getSize(),
									oiw.getSelected());
						} else {
							target_iw = new Input_widget(iw.getId(), iw.getLabel(), iw.getClasss(),
									iw.getX(), iw.getY(), iw.getValue());
						}

						target_iw.setDescriptor(iw.getDescriptor());
						break;
					}
				}

				if (target_iw == null) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in fill.");
				}

				// the input data is retrieved
				final String inputdata = input_data_map.get(value);
				if (inputdata == null) {
					throw new Exception(
							"AlloyTestCaseGenerator - analyzeTuples: error getting input data.");
				}
				final Fill action = new Fill(source_window, oracle, target_iw, inputdata);
				actions.set(time_index - 1, action);
				continue;
			}

			if (tuple.atom(1).startsWith("Click")) {
				if (params.size() != 1) {
					throw new Exception(
							"AlloyTestCaseGenerator - analyzeTuples: wrong number of tuples for click.");
				}
				final A4Tuple wid_tuple = params.get(0);
				if (!wid_tuple.atom(1).startsWith("Action_widget_")) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in click.");
				}
				String aw_id = wid_tuple.atom(1).substring(14);
				aw_id = aw_id.split("\\$")[0];

				Action_widget target_aw = null;
				for (final Action_widget aw : this.instance.getGui().getAction_widgets()) {
					if (aw.getId().equals(aw_id)) {
						// target_aw = aw;
						target_aw = new Action_widget(aw.getId(), aw.getLabel(), aw.getClasss(),
								aw.getX(), aw.getY());
						target_aw.setDescriptor(aw.getDescriptor());
						break;
					}
				}

				if (target_aw == null) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in click.");
				}

				final Click action = new Click(source_window, oracle, target_aw);
				actions.set(time_index - 1, action);
				continue;
			}
			if (tuple.atom(1).startsWith("Select")) {
				if (params.size() != 2) {
					throw new Exception(
							"AlloyTestCaseGenerator - analyzeTuples: wrong number of tuples for select.");
				}
				String sw_id = null;
				String sw_name = null;
				String object = null;

				for (final A4Tuple t : params) {
					if (t.atom(1).startsWith("Selectable_widget")) {
						sw_name = t.atom(1);
						sw_id = t.atom(1).substring(18);
						sw_id = sw_id.split("\\$")[0];
						continue;
					}
					if (t.atom(1).startsWith("Object")) {
						object = t.atom(1);
						continue;
					}
				}

				if (object == null || sw_id == null) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in select.");
				}

				// all the tuples connected with the sw
				final List<A4Tuple> sw_tuples = AlloyUtil.getTuples(solution, sw_name);
				final List<String> objects_in_sw_at_t = new ArrayList<>();
				for (final A4Tuple sw_tuple : sw_tuples) {
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
					final List<A4Tuple> obj_tuples = AlloyUtil.getTuples(solution, obj);

					if (obj_tuples.size() != 1) {
						throw new Exception(
								"AlloyTestCaseGenerator - analyzeTuples: error in select.");
					}
					final int appeared = this.extractTimeIndex(obj_tuples.get(0).atom(1));
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
								sw.getClasss(), sw.getX(), sw.getY(), sw.getSize()
										+ objects_in_sw_at_t.size(), 0);
						target_sw.setDescriptor(sw.getDescriptor());
						break;
					}
				}

				if (target_sw == null) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in select.");
				}

				final int select_index = ordered.indexOf(object);

				final Select action = new Select(source_window, oracle, target_sw, select_index);

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
		Sig invalid = null;
		Sig fill = null;
		for (final Sig sig : solution.getAllReachableSigs()) {
			if ("this/Value".equals(sig.label)) {
				value = sig;
			}
			if ("this/Invalid".equals(sig.label)) {
				invalid = sig;
			}
			if ("this/Fill".equals(sig.label)) {
				fill = sig;
			}
		}
		if (invalid == null || value == null || fill == null) {
			throw new Exception(
					"AlloyTestCaseGenerator - elaborateInputData: signatures not found.");
		}

		List<String> fill_atoms = AlloyUtil.getElementsInSet(solution, fill);
		final List<String> value_atoms = AlloyUtil.getElementsInSet(solution, value);
		List<String> invalid_atoms = AlloyUtil.getElementsInSet(solution, invalid);
		List<String> valid_atoms = new ArrayList<>();
		for (final String a : value_atoms) {
			if (!invalid_atoms.contains(a)) {
				valid_atoms.add(a);
			}
		}
		// in the atoms extracted the underscore is substituted with the dollar
		fill_atoms = fill_atoms
				.stream()
				.map(e -> e.substring(0, e.lastIndexOf("_")) + "$"
						+ e.substring(e.lastIndexOf("_") + 1)).collect(Collectors.toList());

		invalid_atoms = invalid_atoms
				.stream()
				.map(e -> e.substring(0, e.lastIndexOf("_")) + "$"
						+ e.substring(e.lastIndexOf("_") + 1)).collect(Collectors.toList());

		valid_atoms = valid_atoms
				.stream()
				.map(e -> e.substring(0, e.lastIndexOf("_")) + "$"
						+ e.substring(e.lastIndexOf("_") + 1)).collect(Collectors.toList());

		final Map<String, List<String>> data_for_value = new HashMap<>();
		// for each valid value we look for all its uses in the solution
		for (final String fill_atom : fill_atoms) {

			final List<A4Tuple> tuples = AlloyUtil.getTuples(solution, fill_atom);
			if (tuples.size() != 2) {
				throw new Exception(
						"AlloyTestCaseGenerator - elaborateInputData: wrong tuples number for fill.");
			}
			String iw = null;
			String v = null;
			for (final A4Tuple tuple : tuples) {
				if (tuple.arity() == 2 && tuple.atom(1).toLowerCase().contains("value")) {
					v = tuple.atom(1);
				} else if (tuple.arity() == 2 && tuple.atom(1).startsWith("Input_widget")) {
					iw = tuple.atom(1);
				}

			}
			if (v == null || iw == null) {
				throw new Exception(
						"AlloyTestCaseGenerator - elaborateInputData: input widget or value not found.");
			}
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
			if (inpw == null) {
				throw new Exception(
						"AlloyTestCaseGenerator - elaborateInputData: input widget not found in instance.");
			}
			if (inpw instanceof Option_input_widget) {
				// for the option input widget we just use the value index
				// provided by alloy
				// this is possible because of the fact added in the model that
				// constrains the possible values associated to the optional
				// input widget
				String val = v.split("\\$")[0].trim();
				val = val.replace("Input_widget_" + inpw.getId() + "_value_", "");
				out.put(v, val);

			} else {
				String metadata = inpw.getLabel() != null ? inpw.getLabel() : "";
				metadata += " ";
				metadata = inpw.getDescriptor() != null ? inpw.getDescriptor() : "";

				List<String> data = null;
				if (valid_atoms.contains(v)) {
					data = dm.getValidData(metadata);
				} else if (invalid_atoms.contains(v)) {
					data = dm.getInvalidData(metadata);
				}
				if (data == null) {
					throw new Exception("AlloyTestCaseGenerator - elaborateInputData: error.");
				}
				if (data_for_value.containsKey(v)) {
					final List<String> new_list = new ArrayList<>();
					// we calculate the intersection between the values already
					// available for this value and the new ones
					for (final String s : data_for_value.get(v)) {
						if (data.contains(s)) {
							new_list.add(s);
						}
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
				List<String> generics = null;
				if (valid_atoms.contains(key)) {
					generics = dm.getValidGenericData();
				} else {
					generics = dm.getInvalidGenericData();
				}
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
			if (out.containsKey(key)) {
				throw new Exception("AlloyTestCaseGenerator - elaborateInputData: error.");
			}
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
		private final boolean minimal;
		private final int win_scope;
		private final int aw_scope;
		private final int iw_scope;
		private final int sw_scope;

		public RunCommandThread(final Command run, final Module alloy, final int win_scope,
				final int aw_scope, final int iw_scope, final int sw_scope) {

			this.run_command = run;
			this.alloy_model = alloy;
			this.exception = false;
			this.minimal = false;
			this.win_scope = win_scope;
			this.aw_scope = aw_scope;
			this.iw_scope = iw_scope;
			this.sw_scope = sw_scope;
		}

		public RunCommandThread(final Command run, final Module alloy, final boolean minimal,
				final int win_scope, final int aw_scope, final int iw_scope, final int sw_scope) {

			this.run_command = run;
			this.alloy_model = alloy;
			this.exception = false;
			this.minimal = minimal;
			this.win_scope = win_scope;
			this.aw_scope = aw_scope;
			this.iw_scope = iw_scope;
			this.sw_scope = sw_scope;
		}

		@Override
		public void run() {

			try {
				int overall = ConfigurationManager.getAlloyRunScope();

				if (this.run_command.overall > 3) {
					overall = this.run_command.overall;
				} else if (this.win_scope == -1 || this.aw_scope == -1 || this.iw_scope == -1
						|| this.sw_scope == -1) {
					overall = overall * 2;
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

				if (this.minimal && time_scope != -1) {
					this.exception = true;
					return;
				}

				if (this.minimal) {
					time_scope = 3;
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

					if (solution == null) {
						// if timeout
						System.out.println("RUN " + (x + 1) + " COMMAND: " + run.toString()
								+ " TIMEOUT!");
						break;
					} else if (this.minimal && !solution.satisfiable()) {

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
