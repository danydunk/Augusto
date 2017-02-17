package src.usi.semantic;

import java.util.ArrayList;
import java.util.List;

import src.usi.semantic.alloy.Alloy_Model;
import src.usi.semantic.alloy.structure.Fact;
import src.usi.semantic.alloy.structure.Function;
import src.usi.semantic.alloy.structure.Predicate;
import src.usi.semantic.alloy.structure.Semantic_predicate;
import src.usi.semantic.alloy.structure.Signature;

public class FunctionalitySemantics extends Alloy_Model {

	protected boolean semanticproperty;
	protected Predicate click;
	protected Predicate fill;
	protected Predicate select;
	protected Signature window_signature;
	protected Signature input_w_signature;
	protected Signature action_w_signature;
	protected Signature selectable_w_signature;
	protected Semantic_predicate click_semantics;
	protected Semantic_predicate fill_semantics;
	protected Semantic_predicate select_semantics;
	protected List<Signature> windows_extensions;
	protected List<Signature> action_w_extensions;
	protected List<Signature> input_w_extensions;
	protected List<Signature> selectable_w_extensions;

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
			if ("Selectable_widget".equals(sig.getIdentifier()) && sig.isAbstract_()) {
				this.selectable_w_signature = sig;
			}
			if (sig.getIdentifier().startsWith("Property_") && !sig.isAbstract_()) {
				this.semanticproperty = true;
			}
		}
		for (final Predicate pred : predicates) {
			if ("click".equals(pred.getIdentifier()) && pred.getInputs().keySet().size() == 3) {
				this.click = pred;
			}
			if ("fill".equals(pred.getIdentifier()) && pred.getInputs().keySet().size() == 4) {
				this.fill = pred;
			}
			if ("select".equals(pred.getIdentifier()) && pred.getInputs().keySet().size() == 4) {
				this.select = pred;
			}
		}
		if (this.click == null || this.fill == null || this.select == null
				|| this.window_signature == null || this.action_w_signature == null
				|| this.input_w_signature == null || this.selectable_w_signature == null) {
			throw new Exception("FunctionalitySemantics: error in constructor");
		}

		this.windows_extensions = new ArrayList<>();
		this.action_w_extensions = new ArrayList<>();
		this.input_w_extensions = new ArrayList<>();
		this.selectable_w_extensions = new ArrayList<>();

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
			if (sig.getParent().contains(this.selectable_w_signature) && sig.isAbstract_()) {
				this.selectable_w_extensions.add(sig);
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
			if ("select_semantics".equals(pred.getIdentifier())
					&& pred.getInputs().keySet().size() == 3) {
				this.select_semantics = new Semantic_predicate(pred.getIdentifier(),
						pred.getContent(), pred.getInputs());
			}
		}
		if (this.click_semantics == null
				|| this.fill_semantics == null
				|| this.select_semantics == null
				|| (this.windows_extensions.size() + this.action_w_extensions.size()
						+ this.input_w_extensions.size() + this.selectable_w_extensions.size()) == 0) {
			throw new Exception("FunctionalitySemantics: error in constructor");
		}
		// this.generate_run_commands();
	}

	public Semantic_predicate getClickSemantics() {

		return this.click_semantics;
	}

	public Semantic_predicate getFillSemantics() {

		return this.fill_semantics;
	}

	public Semantic_predicate getSelectSemantics() {

		return this.select_semantics;
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

	public List<Signature> getSelectable_w_extensions() {

		return new ArrayList<>(this.selectable_w_extensions);
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

	public Signature getSelectable_w_signature() {

		return this.selectable_w_signature;
	}

	public boolean hasSemanticProperty() {

		return this.semanticproperty;
	}
}
