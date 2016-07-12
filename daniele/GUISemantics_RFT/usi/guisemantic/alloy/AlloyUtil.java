package usi.guisemantic.alloy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import usi.gui.pattern.Cardinality;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_input_widget;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Window;
import usi.guisemantic.FunctionalitySemantics;
import usi.guisemantic.alloy.entity.AlloyEntity;
import usi.guisemantic.alloy.entity.Fact;
import usi.guisemantic.alloy.entity.Function;
import usi.guisemantic.alloy.entity.Predicate;
import usi.guisemantic.alloy.entity.Signature;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.SubsetSig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;

/**
 *
 * This class parsers the "meta-specification" of the CRUDS
 *
 */
public class AlloyUtil {

	static A4Reporter rep = new A4Reporter() {

		@Override
		public void warning(final ErrorWarning msg) {

			System.out.print("Relevance Warning:\n" + (msg.toString().trim()) + "\n\n");
			System.out.flush();
		}
	};

	/**
	 * returns the signature from the list passed as argument which label is the
	 * same as the label passed as param
	 *
	 * @param signatures
	 *            list of signs
	 * @param label
	 *            label of the sig to retrieve
	 * @return
	 */
	public static Signature searchSignatureInList(final List<Signature> signatures,
			final String label) {

		for (final Signature signature : signatures) {
			if (signature.getIdentifier().equals(label)) {
				return signature;
			}
		}
		return null;
	}

	public static AlloyEntity searchElementInList(final List<? extends AlloyEntity> elements,
			final String label) {

		for (final AlloyEntity signature : elements) {
			if (signature.getIdentifier().equals(label)) {
				return signature;
			}
		}
		return null;
	}

	/**
	 * Creates and returns a file that contains the argument model as content.
	 *
	 * @param model
	 * @return
	 * @throws Exception
	 */
	static public File saveModelInFile(final String model, final String filename) throws Exception {

		final File f = new File(filename);
		final BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write(model);
		bw.close();
		return f;
	}

	/**
	 * Compiles a file and returns a compiled alloy model from the Alloy API
	 *
	 * @param model
	 * @return
	 * @throws Exception
	 */
	static public Module compileAlloyModel(final String model) throws Exception {

		try {
			final File tmp = saveModelInTmpFile(model);
			final Module out = CompUtil.parseEverything_fromFile(rep, null, tmp.getAbsolutePath());
			tmp.delete();
			return out;
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("AlloyUtil - compileAlloyModel: compilation error.");
		}
	}

	/**
	 * Compiles a file and returns a compiled alloy model from the Alloy API
	 *
	 * @param model
	 * @return
	 * @throws Exception
	 */
	static public Module compileAlloyModel(final File model) throws Exception {

		try {
			final Module out = CompUtil
					.parseEverything_fromFile(rep, null, model.getAbsolutePath());
			return out;
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("AlloyUtil - compileAlloyModel: compilation error.");
		}
	}

	/**
	 *
	 * @param model
	 *            : compiled alloy model
	 * @param run_command
	 *            : command to run
	 * @return a solution
	 * @throws Exception
	 */
	static public A4Solution runCommand(final Module model, final Command run_command)
			throws Exception {

		final A4Options options = new A4Options();
		options.solver = A4Options.SatSolver.SAT4J;
		A4Solution solution = null;

		try {
			solution = TranslateAlloyToKodkod.execute_command(rep, model.getAllReachableSigs(),
					run_command, options);
		} catch (final Exception e) {
			throw new Exception("AlloyUtil - runCommand: error " + e.getMessage());
		}
		return solution;
	}

	/**
	 *
	 * @param model
	 *            : compiled alloy model
	 * @param run_command
	 *            : command to run
	 * @param timeout
	 *            : the maximum time in ms for the command
	 * @return a solution
	 * @throws Exception
	 */
	static public A4Solution runCommand(final Module model, final Command run_command,
			final long timeout) throws Exception {

		final class Run_command_thread extends Thread {

			private A4Solution solution;
			private boolean exception = false;

			public boolean hasException() {

				return this.exception;
			}

			public A4Solution getSolution() {

				return this.solution;
			}

			@Override
			public void run() {

				final A4Options options = new A4Options();
				options.solver = A4Options.SatSolver.SAT4J;
				try {
					final A4Solution app = TranslateAlloyToKodkod.execute_command(rep,
							model.getAllReachableSigs(), run_command, options);
					this.solution = app;
				} catch (final Err e) {
					this.exception = true;
				}
			}
		}

		try {
			final Run_command_thread thread = new Run_command_thread();
			thread.start();
			thread.join(timeout);
			if (thread.isAlive()) {
				thread.interrupt();
			}
			if (!thread.hasException()) {
				return thread.getSolution();
			} else {
				throw new Exception("AlloyUtil - runCommand: error in thread.");
			}
		} catch (final Exception e) {
			throw new Exception("AlloyUtil - runCommand: error.");
		}
	}

	/**
	 * Create the Alloy model from the File.
	 *
	 * @param abstractModelPath
	 *            path of the file to analyze
	 * @return alloy model created from the file
	 * @throws Exception
	 */
	static public Alloy_Model loadAlloyModelFromFile(final File... files) throws Exception {

		final List<String> open_statements = new ArrayList<>();
		final List<Signature> signatures = new ArrayList<>();
		final List<Fact> facts = new ArrayList<>();
		final List<Predicate> predicates = new ArrayList<>();
		final List<Function> functions = new ArrayList<>();

		for (final File file : files) {

			// the source file is read
			final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String source = "";
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				source += line + System.getProperty("line.separator");
			}
			bufferedReader.close();

			final Module alloyFromFile = compileAlloyModel(file);

			open_statements.addAll(retrieveOpenStatements(source));
			signatures.addAll(retrieveSignatures(alloyFromFile));
			facts.addAll(retrieveFacts(alloyFromFile, source));
			predicates.addAll(retrievePredicates(alloyFromFile, signatures, source));
			functions.addAll(retrieveFunctions(alloyFromFile, signatures, source));

		}
		final Alloy_Model model = new Alloy_Model(signatures, facts, predicates, functions,
				open_statements);
		return model;
	}

	/**
	 * Create the Alloy model from a String.
	 *
	 * @param string
	 *            representation of the alloy model
	 * @return alloy model created from the file
	 * @throws Exception
	 */
	static public Alloy_Model loadAlloyModelFromString(final String model) throws Exception {

		final Module alloyFromFile = compileAlloyModel(model);

		final List<String> open_statements = retrieveOpenStatements(model);
		final List<Signature> signatures = retrieveSignatures(alloyFromFile);
		final List<Fact> facts = retrieveFacts(alloyFromFile, model);
		final List<Predicate> predicates = retrievePredicates(alloyFromFile, signatures, model);
		final List<Function> functions = retrieveFunctions(alloyFromFile, signatures, model);

		final Alloy_Model out = new Alloy_Model(signatures, facts, predicates, functions,
				open_statements);
		return out;
	}

	private static List<Function> retrieveFunctions(final Module alloyFromFile,
			final List<Signature> signatures, final String source) throws Exception {

		final List<Function> functions = new ArrayList<>();
		final String separator = System.getProperty("line.separator");
		final String[] lines = source.split(separator);

		for (final Func func : alloyFromFile.getAllFunc()) {

			if (!func.isPred) {
				final int pos = func.pos.y - 1;
				// check that the position is correct
				if (lines.length < pos || !lines[pos].trim().startsWith("fun")
						|| !lines[pos].trim().contains(func.label.replaceAll("this/", ""))) {
					throw new Exception("AlloyUtil: error retriving functions.");
				}
				// we get the content of the predicate from the source code
				final String content = getBody(lines, pos);
				final Function function = new Function(func.label.replaceAll("this/", ""), content,
						func.returnDecl.toString(), getDeclarationParameters(func, signatures));
				functions.add(function);
			}
		}
		return functions;
	}

	private static String getBody(final String[] lines, final int pos) throws Exception {

		String content = "";
		// if predicate has a body
		if (!lines[pos].trim().endsWith("{}") && !lines[pos].trim().endsWith("{ }")) {
			boolean end_found = false;
			for (int x = pos + 1; x < lines.length; x++) {
				// check that the content is does not contain any other alloy
				// entity
				if (lines[x].trim().startsWith("pred") || lines[x].trim().startsWith("abstract")
						|| lines[x].trim().startsWith("sig") || lines[x].trim().startsWith("fun")
						|| lines[x].trim().startsWith("fact")
						|| lines[x].trim().startsWith("assert")
						|| lines[x].trim().startsWith("run")
						|| lines[x].trim().startsWith("one sig")) {
					System.out.println();
					throw new Exception("AlloyUtil: error retriving body.");
				}
				if (lines[x].trim().equals("}")) {
					end_found = true;
					break;
				}

				content += System.getProperty("line.separator") + lines[x];
			}
			if (!end_found) {
				throw new Exception("AlloyUtil: error retriving body.");
			}
			// the first new line is removed
			content = content.replaceFirst(System.getProperty("line.separator"), "");
		}
		return content;
	}

	// function that retrives the predicates in the module. It needs the source
	// code because the compiled model
	// modifies the structure of the expressions
	private static List<Predicate> retrievePredicates(final Module alloyFromFile,
			final List<Signature> signatures, final String source) throws Exception {

		final ArrayList<Predicate> predicates = new ArrayList<>();
		final String separator = System.getProperty("line.separator");
		final String[] lines = source.split(separator);

		for (final Func func : alloyFromFile.getAllFunc()) {
			if (func.isPred && !func.label.contains("run$")) {
				final int pos = func.pos.y - 1;
				// check that the position is correct
				if (lines.length < pos || !lines[pos].trim().startsWith("pred")
						|| !lines[pos].trim().contains(func.label.replaceAll("this/", ""))) {
					throw new Exception("AlloyUtil: error retriving predicates.");
				}
				// we get the content of the predicate from the source code
				final String content = getBody(lines, pos);
				final Predicate predicate = new Predicate(func.label.replaceAll("this/", ""),
						content, getDeclarationParameters(func, signatures));
				predicates.add(predicate);
			}
		}
		return predicates;
	}

	private static LinkedHashMap<Signature, List<String>> getDeclarationParameters(final Func func,
			final List<Signature> signatures) throws Exception {

		final LinkedHashMap<Signature, List<String>> inputs = new LinkedHashMap<>();

		final List<Decl> pars = func.decls;
		for (final Decl par : pars) {
			final Signature sig = searchSignatureInList(signatures,
					par.expr.toString().replace("this/", ""));
			final List<String> labels = new ArrayList<>();
			for (final ExprHasName lab : par.names) {
				labels.add(lab.toString());
			}
			inputs.put(sig, labels);

		}
		return inputs;
	}

	// function that retrieves the facts in the module.
	private static ArrayList<Fact> retrieveFacts(final Module alloyFromFile, final String source)
			throws Exception {

		final ArrayList<Fact> facts = new ArrayList<>();
		final String separator = System.getProperty("line.separator");
		final String[] lines = source.split(separator);

		final SafeList<Pair<String, Expr>> factsFromModule = alloyFromFile.getAllFacts();
		for (final Pair<String, Expr> pair : factsFromModule) {
			// the fact declaration is at the previous line
			final int pos = pair.b.pos.y - 1;
			// check that the position is correct
			if (lines.length < pos || !lines[pos].trim().startsWith("fact")
					|| !lines[pos].trim().contains(pair.a.replaceAll("this/", ""))) {
				// throw new Exception("AlloyUtil: error retriving facts.");
			}
			// we get the content of the predicate from the source code
			final String content = getBody(lines, pos);
			final Fact ourFact = new Fact(pair.a.replace("$", ""), content);
			facts.add(ourFact);
		}
		return facts;
	}

	private static List<String> retrieveOpenStatements(final String source) {

		final String separator = System.getProperty("line.separator");
		final String[] lines = source.split(separator);
		final List<String> out = new ArrayList<>();
		for (final String line : lines) {
			if (line.trim().startsWith("open ")) {
				out.add(line);
			}
		}
		return out;
	}

	private static ArrayList<Signature> retrieveSignatures(final Module alloyFromFile)
			throws Exception {

		final ArrayList<Signature> signatures = new ArrayList<>();
		final SafeList<Sig> sigFromModule = alloyFromFile.getAllSigs();

		List<Sig> sigs = new ArrayList<>();
		for (final Sig sig : sigFromModule) {
			sigs.add(sig);
		}

		while (true) {
			final List<Sig> to_process = new ArrayList<>();

			loop: for (final Sig signature : sigs) {

				Signature ourSig = null;
				if (signature instanceof PrimSig) {
					final PrimSig sig = (PrimSig) signature;
					final List<Signature> parent = new ArrayList<>();

					if (sig.parent != null && sig.parent != PrimSig.UNIV
							&& sig.parent != PrimSig.NONE) {
						final Signature par = searchSignatureInList(signatures,
								sig.parent.label.replaceAll("this/", ""));
						if (par == null) {
							to_process.add(signature);
							continue;
						}
						parent.add(par);
					}

					// We create the signature from the data of PrimSig
					ourSig = new Signature(sig.label.replaceAll("this/", ""), getCardinality(sig),
							sig.isAbstract != null, parent, false);

				} else {
					final SubsetSig sig = (SubsetSig) signature;
					final List<Signature> parents = new ArrayList<>();

					if (sig.parents == null || sig.parents.size() == 0) {
						throw new Exception(
								"AlloyUtil - retrieveSignatures: error parsing subset signature.");
					}

					for (final Sig par_sig : sig.parents) {
						if (par_sig == SubsetSig.NONE || par_sig == SubsetSig.UNIV) {
							continue;
						}
						final Signature par = searchSignatureInList(signatures,
								par_sig.label.replaceAll("this/", ""));
						parents.add(par);
						if (par == null) {
							to_process.add(signature);
							continue loop;
						}

					}

					// We create the signature from the data of SubsetSig
					ourSig = new Signature(sig.label.replaceAll("this/", ""), getCardinality(sig),
							sig.isAbstract != null, parents, true);
				}
				signatures.add(ourSig);
			}
			if (to_process.size() == 0) {
				break;
			}
			if (to_process.size() == sigs.size()) {
				throw new Exception("AlloyUtil - retrieveSignatures: error parsing signatures.");
			}
			sigs = to_process;
		}

		// now we add the fields
		for (final Sig signature : sigFromModule) {
			for (final Field field : signature.getFields()) {
				final Signature ourSig = searchSignatureInList(signatures,
						signature.label.replaceAll("this/", ""));

				if (field.decl().expr instanceof ExprUnary) {
					final ExprUnary unary = (ExprUnary) field.decl().expr;
					final String fieldRef = unary.sub.toString().replaceAll("this/", "");
					final Signature co_dom = searchSignatureInList(signatures, fieldRef);
					if (ourSig == null || co_dom == null) {
						throw new Exception(
								"AlloyUtil - retrieveSignatures: error parsing relations.");
					}
					ourSig.addBinaryRelation(field.label.replaceAll("this/", ""), co_dom,
							getCardinality(unary.op));

				} else if (field.decl().expr instanceof ExprBinary) {
					final ExprBinary bin = (ExprBinary) field.decl().expr;
					final String left = bin.left.toString().replaceAll("this/", "");
					final String right = bin.right.toString().replaceAll("this/", "");
					final Signature mid_dom = searchSignatureInList(signatures, left);
					final Signature co_dom = searchSignatureInList(signatures, right);

					if (ourSig == null || co_dom == null || mid_dom == null) {
						throw new Exception(
								"AlloyUtil - retrieveSignatures: error parsing relations.");
					}
					final Cardinality[] cards = getCardinality(bin.op);

					ourSig.addTernaryRelation(field.label.replaceAll("this/", ""), co_dom, mid_dom,
							cards[0], cards[1]);
				}
			}
		}
		return signatures;

	}

	private static Cardinality getCardinality(final ExprUnary.Op in) throws Exception {

		switch (in) {
		case LONEOF:
			return Cardinality.LONE;
		case SOMEOF:
			return Cardinality.SOME;
		case ONEOF:
			return Cardinality.ONE;
		case SETOF:
			return Cardinality.SET;
		default:
			throw new Exception("AlloyUtil: error in compiling multiplicities.");
		}
	}

	private static Cardinality[] getCardinality(final ExprBinary.Op in) throws Exception {

		final Cardinality[] out = new Cardinality[2];
		switch (in) {
		case ANY_ARROW_LONE:
			out[0] = Cardinality.SET;
			out[1] = Cardinality.LONE;
			break;
		case ANY_ARROW_ONE:
			out[0] = Cardinality.SET;
			out[1] = Cardinality.ONE;
			break;
		case ANY_ARROW_SOME:
			out[0] = Cardinality.SET;
			out[1] = Cardinality.SOME;
			break;
		case ARROW:
			out[0] = Cardinality.SET;
			out[1] = Cardinality.SET;
			break;
		case LONE_ARROW_ANY:
			out[0] = Cardinality.LONE;
			out[1] = Cardinality.SET;
			break;
		case LONE_ARROW_LONE:
			out[0] = Cardinality.LONE;
			out[1] = Cardinality.LONE;
			break;
		case LONE_ARROW_ONE:
			out[0] = Cardinality.LONE;
			out[1] = Cardinality.ONE;
			break;
		case LONE_ARROW_SOME:
			out[0] = Cardinality.LONE;
			out[1] = Cardinality.SOME;
			break;
		case ONE_ARROW_ANY:
			out[0] = Cardinality.ONE;
			out[1] = Cardinality.SET;
			break;
		case ONE_ARROW_LONE:
			out[0] = Cardinality.ONE;
			out[1] = Cardinality.LONE;
			break;
		case ONE_ARROW_ONE:
			out[0] = Cardinality.ONE;
			out[1] = Cardinality.ONE;
			break;
		case ONE_ARROW_SOME:
			out[0] = Cardinality.ONE;
			out[1] = Cardinality.SOME;
			break;
		case SOME_ARROW_ANY:
			out[0] = Cardinality.SOME;
			out[1] = Cardinality.SET;
			break;
		case SOME_ARROW_LONE:
			out[0] = Cardinality.SOME;
			out[1] = Cardinality.LONE;
			break;
		case SOME_ARROW_ONE:
			out[0] = Cardinality.SOME;
			out[1] = Cardinality.ONE;
			break;
		case SOME_ARROW_SOME:
			out[0] = Cardinality.SOME;
			out[1] = Cardinality.SOME;
			break;
		default:
			throw new Exception("AlloyUtil: error in compiling multiplicities.");
		}
		return out;
	}

	/**
	 * Returns the cardinality of the PrimSing
	 *
	 * @param sig
	 * @return
	 */
	private static Cardinality getCardinality(final Sig sig) {

		if (sig.isLone != null) {
			return Cardinality.LONE;
		}
		if (sig.isOne != null) {
			return Cardinality.ONE;
		}
		if (sig.isSome != null) {
			return Cardinality.SOME;
		}
		if (sig.isSubset != null) {
			return Cardinality.SET;
		}
		// default null
		return null;

	}

	/**
	 * Creates and returns a temporary file that contains the argument model as
	 * content.
	 *
	 * @param model
	 * @return
	 * @throws Exception
	 */
	static private File saveModelInTmpFile(final String model) throws Exception {

		final File temp = File.createTempFile("tempfile", ".tmp");
		final BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
		bw.write(model);
		bw.close();
		return temp;
	}

	static public List<A4Tuple> getTuples(final A4Solution solution, final String labelToMatch) {

		final List<A4Tuple> tuples = getAllTuples(solution);
		final List<A4Tuple> result = new ArrayList<>();

		final Iterator<A4Tuple> tupi = tuples.iterator();
		while (tupi.hasNext()) {
			final A4Tuple tup = tupi.next();
			if (tup.atom(0).startsWith(labelToMatch)) {
				result.add(tup);
			}
		}
		return result;
	}

	static public List<A4Tuple> getAllTuples(final A4Solution solution) {

		final List<A4Tuple> allTuples = new ArrayList<>();
		// We collect all tuples
		for (final Sig s : solution.getAllReachableSigs()) {
			for (final Field f : s.getFields()) {
				final A4TupleSet ts = solution.eval(f);
				final Iterator<A4Tuple> tupi = ts.iterator();
				while (tupi.hasNext()) {
					final A4Tuple tup = tupi.next();
					allTuples.add(tup);
				}
			}
		}
		return allTuples;
	}

	/**
	 * Method that links Windows signatures with Input Widget Signatures
	 * according to the structure of the GUI
	 *
	 * @param iws
	 * @param window
	 */
	static public Fact createFactsForInputWidget(final Map<Input_widget, Signature> iws,
			final Signature window) {

		return createFactsForElement(iws.values(), window, "iws");
	}

	/**
	 * Method that links Windows signatures with Action Widget Signatures
	 * according to the structure of the GUI. it requires also the gui and the
	 * list of added signatures to add fact about the edges
	 *
	 * @param aws
	 * @param window
	 * @throws Exception
	 */
	public static Fact createFactsForActionWidget(final Map<Action_widget, Signature> aws,
			final Signature window, final Map<Window, Signature> ws, final GUI gui)
			throws Exception {

		final Fact initial_fact = createFactsForElement(aws.values(), window, "aws");
		String content = initial_fact.getContent();

		for (final Action_widget aw : aws.keySet()) {
			final List<Window> edges = new ArrayList<>();
			for (final Window w : gui.getStaticForwardLinks(aw.getId())) {
				if (ws.containsKey(w)) {
					edges.add(w);
				}
			}
			if (edges.size() > 0) {
				content += System.getProperty("line.separator") + aws.get(aw).getIdentifier()
						+ ".goes = ";
				int i = 0;
				for (final Window edge : edges) {
					content += ws.get(edge).getIdentifier();
					content += (i + 1 == edges.size()) ? "" : " + ";
					i++;
				}
			}
		}

		final Fact fact = new Fact(window.getIdentifier() + "_aws", content);
		return fact;
	}

	/**
	 * Method that links Windows signatures with Selectable Widget Signatures
	 * according to the structure of the GUI
	 *
	 * @param sws
	 * @param window
	 */
	public static Fact createFactsForSelectableWidget(final List<Signature> sws,
			final Signature window) {

		return createFactsForElement(sws, window, "aws");
	}

	public static Fact createFactsForElement(final Collection<Signature> widgets,
			final Signature window, final String fieldToRelated) {

		if (widgets.isEmpty()) {
			return new Fact(window.getIdentifier() + "_" + fieldToRelated, "");
		}

		String content = window.getIdentifier() + "." + fieldToRelated + " = ";
		int i = 0;
		for (final Signature widget : widgets) {
			content += widget.getIdentifier();
			content += (i + 1 == widgets.size()) ? "" : " + ";
			i++;
		}

		return new Fact(window.getIdentifier() + "_" + fieldToRelated, content);
	}

	// TODO: maybe we should move this method to, for instance, AlloyModel.
	public static Signature searchForParent(final FunctionalitySemantics func_semantics,
			final Pattern_input_widget piw) throws Exception {

		Signature piw_sig;
		if (piw.getAlloyCorrespondence() != null && piw.getAlloyCorrespondence().length() > 0) {
			// the pattern input widget signature is retrieved
			final List<Signature> to_search = new ArrayList<>(
					func_semantics.getInput_w_extensions());
			to_search.add(func_semantics.getInput_w_signature());
			piw_sig = AlloyUtil.searchSignatureInList(to_search, piw.getAlloyCorrespondence());
			System.out.println(piw.getAlloyCorrespondence());
			if (piw_sig == null) {
				throw new Exception("SpecificSemantics - generate: wrong alloy corrispondence "
						+ piw.getAlloyCorrespondence());
			}
		} else {
			piw_sig = func_semantics.getInput_w_signature();
		}
		return piw_sig;
	}

	// TODO: maybe we should move this method to, for instance, AlloyModel.
	public static Signature searchForParent(final FunctionalitySemantics func_semantics,
			final Pattern_action_widget paw) throws Exception {

		Signature paw_sig = null;
		if (paw.getAlloyCorrespondence() != null && paw.getAlloyCorrespondence().length() > 0) {

			// the pattern action widget signature is retrieved
			final List<Signature> to_search = new ArrayList<>(
					func_semantics.getAction_w_extensions());
			to_search.add(func_semantics.getAction_w_signature());
			paw_sig = AlloyUtil.searchSignatureInList(to_search, paw.getAlloyCorrespondence());

			if (paw_sig == null) {
				throw new Exception("SpecificSemantics - generate: wrong alloy corrispondence "
						+ paw.getAlloyCorrespondence());
			}
		} else {
			paw_sig = func_semantics.getAction_w_signature();
		}
		return paw_sig;
	}
}
