package src.usi.semantic.alloy;

import java.util.ArrayList;
import java.util.List;

import src.usi.semantic.alloy.structure.Fact;
import src.usi.semantic.alloy.structure.Function;
import src.usi.semantic.alloy.structure.Predicate;
import src.usi.semantic.alloy.structure.Signature;

public class Alloy_Model {

	protected final List<String> open_statements;
	protected final List<Signature> signatures;
	protected final List<Fact> facts;
	protected final List<Predicate> predicates;
	protected final List<Function> functions;
	protected List<String> run_commands;

	public Alloy_Model(final List<Signature> signatures, final List<Fact> facts,
			final List<Predicate> predicates, final List<Function> functions,
			final List<String> open_statements) throws Exception {

		if (signatures == null || signatures.size() == 0 || facts == null || predicates == null
				|| functions == null || open_statements == null) {
			throw new Exception("AlloyModel: constructor error");
		}
		this.signatures = new ArrayList<>(signatures);
		this.facts = new ArrayList<>(facts);
		this.predicates = new ArrayList<>(predicates);
		this.functions = new ArrayList<>(functions);
		this.open_statements = new ArrayList<>(open_statements);
		this.run_commands = new ArrayList<>();

	}

	@Override
	public String toString() {

		String s = "";
		for (final String open : this.open_statements) {
			s += open + System.getProperty("line.separator");
		}
		for (final Signature signature : this.signatures) {
			s += signature.toString() + System.getProperty("line.separator");
		}
		for (final Fact fact : this.facts) {
			s += fact.toString() + System.getProperty("line.separator");
		}
		for (final Predicate predicate : this.predicates) {
			s += predicate.toString() + System.getProperty("line.separator");
		}
		for (final Function function : this.functions) {
			s += function.toString() + System.getProperty("line.separator");
		}

		s = s + System.getProperty("line.separator");

		for (final String rc : this.getRun_commands()) {
			s = s + rc + System.getProperty("line.separator");
		}
		return s;
	}

	public List<Signature> getSignatures() {

		return new ArrayList<>(this.signatures);
	}

	public List<Fact> getFacts() {

		return new ArrayList<>(this.facts);
	}

	public List<Predicate> getPredicates() {

		return new ArrayList<>(this.predicates);
	}

	public List<Function> getFunctions() {

		return new ArrayList<>(this.functions);
	}

	public List<String> getOpenStatements() {

		return this.open_statements;
	}

	public List<String> getRun_commands() {

		return new ArrayList<>(this.run_commands);
	}

	public void addRun_command(final String run) {

		this.run_commands.add(run);
	}

	public void clearRunCommands() {

		this.run_commands = new ArrayList<>();
	}
}
