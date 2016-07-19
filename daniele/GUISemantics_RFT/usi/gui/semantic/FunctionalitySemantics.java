package usi.gui.semantic;

import java.util.ArrayList;
import java.util.List;

import usi.configuration.ConfigurationManager;
import usi.gui.semantic.alloy.Alloy_Model;
import usi.gui.semantic.alloy.entity.Fact;
import usi.gui.semantic.alloy.entity.Function;
import usi.gui.semantic.alloy.entity.Predicate;
import usi.gui.semantic.alloy.entity.Semantic_predicate;
import usi.gui.semantic.alloy.entity.Signature;

public class FunctionalitySemantics extends Alloy_Model {

	protected Predicate click;
	protected Predicate go;
	protected Predicate fill;
	protected Signature window_signature;
	protected Signature input_w_signature;
	protected Signature action_w_signature;
	protected List<String> run_commands;
	protected Semantic_predicate click_semantics;
	protected Semantic_predicate go_semantics;
	protected Semantic_predicate fill_semantics;
	protected List<Signature> windows_extensions;
	protected List<Signature> action_w_extensions;
	protected List<Signature> input_w_extensions;

	public FunctionalitySemantics(final List<Signature> signatures, final List<Fact> facts,
			final List<Predicate> predicates, final List<Function> functions,
			final List<String> open_statments) throws Exception {

		super(signatures, facts, predicates, functions, open_statments);

		for (final Signature sig : signatures) {
			if (sig.isSubset()) {
				continue;
			}
			if ("Window".equals(sig.getIdentifier()) && sig.isAbstract_()) {
				this.window_signature = sig;
			}
			if ("Action_widget".equals(sig.getIdentifier()) && sig.isAbstract_()) {
				this.action_w_signature = sig;
			}
			if ("Input_widget".equals(sig.getIdentifier()) && sig.isAbstract_()) {
				this.input_w_signature = sig;
			}
		}
		for (final Predicate pred : predicates) {
			if ("click".equals(pred.getIdentifier()) && pred.getInputs().keySet().size() == 3) {
				this.click = pred;
			}
			if ("fill".equals(pred.getIdentifier()) && pred.getInputs().keySet().size() == 4) {
				this.fill = pred;
			}
			if ("go".equals(pred.getIdentifier()) && pred.getInputs().keySet().size() == 3) {
				this.go = pred;
			}
		}
		if (this.click == null || this.fill == null || this.go == null
				|| this.window_signature == null || this.action_w_signature == null
				|| this.input_w_signature == null) {
			throw new Exception("FunctionalitySemantics: error in constructor");
		}

		this.windows_extensions = new ArrayList<>();
		this.action_w_extensions = new ArrayList<>();
		this.input_w_extensions = new ArrayList<>();

		for (final Signature sig : signatures) {
			if (sig.isSubset()) {
				continue;
			}
			if (sig.getParent().contains(this.window_signature) && sig.isAbstract_()) {
				this.windows_extensions.add(sig);
			}
			if (sig.getParent().contains(this.action_w_signature) && sig.isAbstract_()) {
				this.action_w_extensions.add(sig);
			}
			if (sig.getParent().contains(this.input_w_signature) && sig.isAbstract_()) {
				this.input_w_extensions.add(sig);
			}
		}

		for (final Predicate pred : predicates) {
			if ("click_semantics".equals(pred.getIdentifier())
					&& pred.getInputs().keySet().size() == 2) {
				this.click_semantics = new Semantic_predicate(pred.getIdentifier(),
						pred.getContent(), pred.getInputs());
			}
			if ("fill_semantics".equals(pred.getIdentifier())
					&& pred.getInputs().keySet().size() == 3) {
				this.fill_semantics = new Semantic_predicate(pred.getIdentifier(),
						pred.getContent(), pred.getInputs());
			}
			if ("go_semantics".equals(pred.getIdentifier())
					&& pred.getInputs().keySet().size() == 2) {
				this.go_semantics = new Semantic_predicate(pred.getIdentifier(), pred.getContent(),
						pred.getInputs());
			}
		}
		if (this.click_semantics == null
				|| this.go_semantics == null
				|| this.fill_semantics == null
				|| (this.windows_extensions.size() + this.action_w_extensions.size() + this.input_w_extensions
						.size()) == 0) {
			throw new Exception("FunctionalitySemantics: error in constructor");
		}
		this.run_commands = new ArrayList<>();
		// this.generate_run_commands();
	}

	public Semantic_predicate getClickSemantics() {

		return this.click_semantics;
	}

	public Semantic_predicate getGoSemantics() {

		return this.go_semantics;
	}

	public Semantic_predicate getFillSemantics() {

		return this.fill_semantics;
	}

	static public FunctionalitySemantics instantiate(final Alloy_Model in) throws Exception {

		final List<Signature> sigs = new ArrayList<>(in.getSignatures());
		final List<Fact> facts = new ArrayList<>(in.getFacts());
		final List<Predicate> predicates = new ArrayList<>(in.getPredicates());
		final List<Function> functions = new ArrayList<>(in.getFunctions());
		final List<String> imports = new ArrayList<>(in.getOpenStatements());

		return new FunctionalitySemantics(sigs, facts, predicates, functions, imports);
	}

	public List<Signature> getWindows_extensions() {

		return new ArrayList<>(this.windows_extensions);
	}

	public List<Signature> getAction_w_extensions() {

		return new ArrayList<>(this.action_w_extensions);
	}

	public List<Signature> getInput_w_extensions() {

		return new ArrayList<>(this.input_w_extensions);
	}

	public void generate_run_commands() throws Exception {

		final String click = "one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and";
		for (final String prec : this.click_semantics.getCases().keySet()) {
			final String pred = click + " (" + prec + ") and (";
			// the number of possible combinations
			final List<String> cases = this.click_semantics.getCases().get(prec);
			final double n = Math.pow(2, cases.size());

			for (int cont = 0; cont < n; cont++) {
				String binary = Integer.toBinaryString(cont);
				for (int c = 0; c < (cases.size() - binary.length()); c++) {
					binary = "0" + binary;
				}
				String sem_pred = "";
				for (int cont2 = cases.size() - 1; cont2 >= 0; cont2--) {
					if (cont2 > (binary.length() - 1) || binary.charAt(cont2) == '0') {
						sem_pred = sem_pred + "not (" + cases.get(cont2) + ")";
					} else {
						sem_pred = sem_pred + cases.get(cont2);
					}
					if (cont2 > 0) {
						sem_pred = sem_pred + " and ";
					}
				}
				final String run_command = "run {System and {" + pred + sem_pred + ")} } for "
						+ ConfigurationManager.getAlloyRunScope();
				this.run_commands.add(run_command);
			}
		}

		final String fill = "one t: Time, iw: Input_widget, v: Value, f: Fill | fill [iw, t, T/next[t], v, f] and";
		for (final String prec : this.fill_semantics.getCases().keySet()) {
			final String pred = fill + " (" + prec + ") and (";
			// the number of possible combinations
			final List<String> cases = this.fill_semantics.getCases().get(prec);
			final double n = Math.pow(2, cases.size());

			for (int cont = 0; cont < n; cont++) {
				String binary = Integer.toBinaryString(cont);
				for (int c = 0; c < (cases.size() - binary.length()); c++) {
					binary = "0" + binary;
				}
				String sem_pred = "";
				for (int cont2 = cases.size() - 1; cont2 >= 0; cont2--) {
					if (cont2 > (binary.length() - 1) || binary.charAt(cont2) == '0') {
						sem_pred = sem_pred + "not (" + cases.get(cont2) + ")";
					} else {
						sem_pred = sem_pred + cases.get(cont2);
					}
					if (cont2 > 0) {
						sem_pred = sem_pred + " and ";
					}
				}
				final String run_command = "run {System and {" + pred + sem_pred + ")} } for "
						+ ConfigurationManager.getAlloyRunScope();
				this.run_commands.add(run_command);
			}
		}

		final String go = "one t: Time, w: Window, g: Go | go [w, t, T/next[t], g] and";
		for (final String prec : this.go_semantics.getCases().keySet()) {
			final String pred = go + " (" + prec + ") and (";
			// the number of possible combinations
			final List<String> cases = this.go_semantics.getCases().get(prec);
			final double n = Math.pow(2, cases.size());

			for (int cont = 0; cont < n; cont++) {
				String binary = Integer.toBinaryString(cont);
				for (int c = 0; c < (cases.size() - binary.length()); c++) {
					binary = "0" + binary;
				}
				String sem_pred = "";
				for (int cont2 = cases.size() - 1; cont2 >= 0; cont2--) {
					if (cont2 > (binary.length() - 1) || binary.charAt(cont2) == '0') {
						sem_pred = sem_pred + "not (" + cases.get(cont2) + ")";
					} else {
						sem_pred = sem_pred + cases.get(cont2);
					}
					if (cont2 > 0) {
						sem_pred = sem_pred + " and ";
					}
				}
				final String run_command = "run {System and {" + pred + sem_pred + ")} } for "
						+ ConfigurationManager.getAlloyRunScope();
				this.run_commands.add(run_command);
			}
		}
	}

	@Override
	public String toString() {

		String s = super.toString() + System.getProperty("line.separator");

		for (final String rc : this.getRun_commands()) {
			s = s + rc + System.getProperty("line.separator");
		}
		return s;
	}

	public List<String> getRun_commands() {

		return new ArrayList<>(this.run_commands);
	}

	public void addRun_command(final String run) {

		this.run_commands.add(run);
	}

	public Signature getWindow_signature() {

		return this.window_signature;
	}

	public Signature getInput_w_signature() {

		return this.input_w_signature;
	}

	public Signature getAction_w_signature() {

		return this.action_w_signature;
	}
}
