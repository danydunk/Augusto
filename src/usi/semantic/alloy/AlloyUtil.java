package src.usi.semantic.alloy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.PathsManager;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.functionality.instance.Instance_GUI_patternWriter;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Option_input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.gui.structure.Window;
import src.usi.pattern.structure.Cardinality;
import src.usi.pattern.structure.Pattern_action_widget;
import src.usi.pattern.structure.Pattern_input_widget;
import src.usi.pattern.structure.Pattern_selectable_widget;
import src.usi.semantic.FunctionalitySemantics;
import src.usi.semantic.SpecificSemantics;
import src.usi.semantic.alloy.structure.AlloyEntity;
import src.usi.semantic.alloy.structure.Fact;
import src.usi.semantic.alloy.structure.Function;
import src.usi.semantic.alloy.structure.Predicate;
import src.usi.semantic.alloy.structure.Signature;
import src.usi.testcase.GUITestCaseParser;
import src.usi.testcase.inputdata.DataManager;
import src.usi.testcase.structure.Clean;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.Fill;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;
import src.usi.testcase.structure.Select;
import src.usi.xml.XMLUtil;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.SafeList;
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
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;

/**
 *
 * This class parsers the "meta-specification" of the CRUDS
 *
 */
public class AlloyUtil {

	private static long TIMEOUT = 30; // 30 minutes

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

		final A4Reporter rep = new A4Reporter() {

			@Override
			public void warning(final ErrorWarning msg) {

				System.out.print("Relevance Warning:\n" + (msg.toString().trim()) + "\n\n");
				System.out.flush();
			}
		};

		final Map<String, String> modules = new HashMap<>();
		try {
			final File folder = new File(PathsManager.getAlloyModulesFolder());
			final File[] listOfFiles = folder.listFiles();

			for (final File file : listOfFiles) {
				if (file.isFile() && file.getName().contains(".als")) {
					modules.put(file.getName().replace(".als", ""),
							Files.toString(file, Charsets.UTF_8));
				}
			}
		} catch (final Exception e) {}

		try {
			final File tmp = saveModelInTmpFile(model);
			final Module out = CompUtil.parseEverything_fromFile(rep, modules,
					tmp.getAbsolutePath());
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

		final A4Reporter rep = new A4Reporter() {

			@Override
			public void warning(final ErrorWarning msg) {

				System.out.print("Relevance Warning:\n" + (msg.toString().trim()) + "\n\n");
				System.out.flush();
			}
		};

		final Map<String, String> modules = new HashMap<>();
		try {
			final File folder = new File(PathsManager.getAlloyModulesFolder());
			final File[] listOfFiles = folder.listFiles();

			for (final File file : listOfFiles) {
				if (file.isFile() && file.getName().contains(".als")) {
					modules.put(file.getName().replace(".als", ""),
							Files.toString(file, Charsets.UTF_8));
				}
			}
		} catch (final Exception e) {}

		try {
			final Module out = CompUtil.parseEverything_fromFile(rep, modules,
					model.getAbsolutePath());
			return out;
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("AlloyUtil - compileAlloyModel: compilation error.");
		}
	}

	static public String getSemProp(final SpecificSemantics model, final int command_index)
			throws Exception {

		final File f = File.createTempFile("alloymodel", ".als");
		final BufferedWriter output = new BufferedWriter(new FileWriter(f));
		output.write(model.toString());
		output.close();

		final List<String> cmds = new ArrayList<>();
		cmds.add("java");
		cmds.add("-Xmx2g");
		cmds.add("-Xss512m");
		cmds.add("-cp");
		cmds.add(PathsManager.getBINSPath() + ";" + System.getProperty("java.class.path"));
		cmds.add("src.usi.semantic.alloy.AlloyRunner");
		cmds.add(f.getAbsolutePath());
		cmds.add("1");
		cmds.add(String.valueOf(command_index));
		final ProcessBuilder pb = new ProcessBuilder(cmds);
		final Process process = pb.start();
		int code = -1;
		try {
			code = process.waitFor();
		} catch (final InterruptedException iec) {
			process.destroyForcibly();
			Thread.currentThread().interrupt();
			return null;
		}
		if (code != 0) {
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));
			String err = "";
			String line = null;
			while ((line = br.readLine()) != null) {
				err += line + System.getProperty("line.separator");

			}
			System.err.println(err);
			throw new Exception();
		} else {
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String out = "";
			String line = null;
			while ((line = br.readLine()) != null) {
				out = line + System.getProperty("line.separator");

			}
			out = out.trim();
			if (out.startsWith("sat:")) {
				return out.replace("sat:", "").trim();
			}
			if (out.equals("unsat")) {
				return null;
			}
			throw new Exception("unrecongised output");
		}
	}

	static public GUITestCase getTestcase(final Instance_GUI_pattern model,
			final int command_index, final int type, final int scope, final int t1, final int t2)
			throws Exception {

		final String path = XMLUtil.saveTMP(Instance_GUI_patternWriter
				.writeInstanceGUIPattern(model));

		final List<String> cmds = new ArrayList<>();
		cmds.add("java");
		cmds.add("-Xmx2g");
		cmds.add("-Xss512m");
		cmds.add("-cp");
		cmds.add(PathsManager.getBINSPath() + ";" + System.getProperty("java.class.path"));
		cmds.add("src.usi.semantic.alloy.AlloyRunner");
		cmds.add(path);
		cmds.add(String.valueOf(type));
		cmds.add(String.valueOf(command_index));
		cmds.add(String.valueOf(scope));
		if (t1 != -1) {
			cmds.add(String.valueOf(t1));
		}
		if (t2 != -1) {
			cmds.add(String.valueOf(t2));
		}

		final ProcessBuilder pb = new ProcessBuilder(cmds);
		Process process = null;
		try {
			process = pb.start();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		boolean terminated = false;
		try {
			terminated = process.waitFor(TIMEOUT, TimeUnit.MINUTES);
		} catch (final InterruptedException iec) {
			process.destroyForcibly();
			Thread.currentThread().interrupt();
			return null;
		}
		if (!terminated) {
			process.destroyForcibly();
			throw new TimeoutException();
		} else {
			if (process.exitValue() != 0) {
				final BufferedReader br = new BufferedReader(new InputStreamReader(
						process.getErrorStream()));
				String err = "";
				String line = null;
				while ((line = br.readLine()) != null) {
					err += line + System.getProperty("line.separator");

				}
				throw new Exception(err);
			} else {
				final BufferedReader br = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
				String out = "";
				String line = null;
				while ((line = br.readLine()) != null) {
					out += line + System.getProperty("line.separator");
				}
				out = out.trim();
				if (out.equals("unsat")) {
					return null;
				} else {

					final GUITestCase tc = GUITestCaseParser.parse(XMLUtil.read(out));
					return tc;
				}
			}
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

		String source = "";
		for (final File file : files) {

			// the source file is read
			final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

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
		for (final String run : retrieveRunCommands(source)) {
			model.addRun_command(run);
		}
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

		for (final String run : retrieveRunCommands(model)) {
			out.addRun_command(run);
		}
		return out;
	}

	private static List<String> retrieveRunCommands(final String source) throws Exception {

		final String separator = System.getProperty("line.separator");
		final String[] lines = source.split(separator);
		final List<String> out = new ArrayList<>();
		for (final String line : lines) {
			if (line.trim().startsWith("run {") || line.trim().startsWith("run{")
					|| line.trim().startsWith("run(") || line.trim().startsWith("run (")) {
				out.add(line);
			}
		}

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
		// /System.out.println(sig.label);
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

	// static public List<A4Tuple> getTuples(final A4Solution solution, final
	// String labelToMatch) {
	//
	// final List<A4Tuple> tuples = getAllTuples(solution);
	// final List<A4Tuple> result = new ArrayList<>();
	//
	// final Iterator<A4Tuple> tupi = tuples.iterator();
	// while (tupi.hasNext()) {
	// final A4Tuple tup = tupi.next();
	// if (tup.atom(0).equals(labelToMatch)) {
	// result.add(tup);
	// }
	// }
	// return result;
	// }

	static public List<A4Tuple> getTuplesRel(final A4Solution solution, final String labelToMatch,
			final String sig, final String rel) {

		final List<A4Tuple> tuples = getAllTuplesSigRel(solution, sig, rel);
		final List<A4Tuple> result = new ArrayList<>();

		final Iterator<A4Tuple> tupi = tuples.iterator();
		while (tupi.hasNext()) {
			final A4Tuple tup = tupi.next();
			if (tup.atom(0).equals(labelToMatch)) {
				result.add(tup);
			}
		}
		return result;
	}

	static public List<String> getElementsInSet(final A4Solution solution, final Sig sig) {

		final String[] lines = solution.toString().split("\\r?\\n");
		for (String line : lines) {
			if (line.startsWith(sig.label + "={")) {
				line = line.substring(2 + sig.label.length());
				line = line.substring(0, line.length() - 1);
				if (line.length() == 0) {
					return new ArrayList<String>();
				}
				final String[] atoms = line.split(",");
				final List<String> out = new ArrayList<>();
				for (final String ss : atoms) {
					out.add(ss.trim().replace("$", "_"));
				}
				return out;
			}
		}

		return null;
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

	static public List<A4Tuple> getAllTuplesSigRel(final A4Solution solution, final String sig,
			final String rel) {

		final List<A4Tuple> allTuples = new ArrayList<>();
		// We collect all tuples
		for (final Sig s : solution.getAllReachableSigs()) {
			if (!s.label.equals(sig)) {
				continue;
			}
			for (final Field f : s.getFields()) {
				if (!f.label.equals(rel)) {
					continue;
				}
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
	 * @throws Exception
	 */
	static public Fact createFactsForInputWidget(final Map<Input_widget, Signature> iws,
			final Signature window, final boolean invalid) throws Exception {

		final Fact initial_fact = createFactsForElement(iws.values(), window, "iws");
		String content = initial_fact.getContent();
		final DataManager dm = DataManager.getInstance();

		// to fix the problem of the view window
		// TODO: find another solution
		// final List<Input_widget> to_order = new ArrayList<>();

		for (final Input_widget iw : iws.keySet()) {

			String metadata = iw.getLabel() != null ? iw.getLabel() : "";
			metadata += iw.getDescriptor() != null && metadata.length() == 0 ? iw.getDescriptor()
					: "";

			// to_order.add(iw);
			if (invalid) {

				String unvalid = iws.get(iw).getIdentifier() + ".invalid = (";
				String vals = iws.get(iw).getIdentifier() + ".val = (";

				if (iw instanceof Option_input_widget) {

					final Option_input_widget oiw = (Option_input_widget) iw;

					if ((dm.getInvalidItemizedData(metadata).size() + dm.getValidItemizedData(
							metadata).size()) > 0) {
						// options.addAll(dm.getInvalidItemizedData(metadata));
						// options.addAll(dm.getValidItemizedData(metadata));
						for (final Integer i : dm.getInvalidItemizedData(metadata)) {
							// we limit to only 10 values
							if (i > 4) {
								continue;
							}
							unvalid += "Option_value_" + i + "+";
							vals += "Option_value_" + i + "+";

						}
						for (final Integer i : dm.getValidItemizedData(metadata)) {
							// we limit to only 10 values
							if (i > 4) {
								continue;
							}
							vals += "Option_value_" + i + "+";
						}

					} else {
						for (int x = 0; x < oiw.getSize(); x++) {
							if (x > 4) {
								continue;
							}
							vals += "Option_value_" + x + "+";
						}
					}

					if (unvalid.equals(iws.get(iw).getIdentifier() + ".invalid = (")) {
						unvalid = "#(" + iws.get(iw).getIdentifier() + ".invalid) = 0";
					} else {
						unvalid = unvalid.substring(0, unvalid.length() - 1) + ")";
					}
					if (vals.equals(iws.get(iw).getIdentifier() + ".valid = (")) {
						vals = "#(" + iws.get(iw).getIdentifier() + ".valid) = 0";
					} else {
						vals = vals.substring(0, vals.length() - 1) + ")";
					}

					content += System.getProperty("line.separator");
					content += vals;
					content += System.getProperty("line.separator");
					content += unvalid;
					content += System.getProperty("line.separator");

					if (oiw.getSelected() == -1 || oiw.getSelected() > 9) {
						content += iws.get(iw).getIdentifier()
								+ ".content.(T/first) = To_be_cleaned";
					} else {
						content += iws.get(iw).getIdentifier()
								+ ".content.(T/first) = Option_value_" + oiw.getSelected();
					}
				} else {
					if ((dm.getInvalidData(metadata).size() + dm.getValidData(metadata).size()) > 0) {

						content += System.getProperty("line.separator");
						content += "#" + iws.get(iw).getIdentifier() + ".invalid < "
								+ (dm.getInvalidData(metadata).size() + 1);
						if (dm.getInvalidData(metadata).size() > 0) {
							content += " and #" + iws.get(iw).getIdentifier() + ".invalid > 0";
						}

						content += System.getProperty("line.separator");
						content += "#"
								+ iws.get(iw).getIdentifier()
								+ ".val < "
								+ (dm.getInvalidData(metadata).size()
										+ dm.getValidData(metadata).size() + 1);
					} else {
						content += System.getProperty("line.separator");
						content += "#" + iws.get(iw).getIdentifier() + ".invalid = 0";

					}
					content += System.getProperty("line.separator");
					if (iw.getValue().length() > 0) {
						content += "#" + iws.get(iw).getIdentifier()
								+ ".content.(T/first) = 1 and not(" + iws.get(iw).getIdentifier()
								+ ".content.(T/first) in " + iws.get(iw).getIdentifier()
								+ ".invalid + To_be_cleaned)";
					} else {
						content += iws.get(iw).getIdentifier()
								+ ".content.(T/first) = To_be_cleaned";
					}
					content += System.getProperty("line.separator");
					content += "#((";
					for (int x = 0; x < 5; x++) {
						content += "Option_value_" + x;
						if (x != 4) {
							content += "+";
						} else {
							content += ")";
						}
					}
					content += " & " + iws.get(iw).getIdentifier() + ".val) = 0";
				}

			} else {
				// if there are not invalid values
				if (iw instanceof Option_input_widget) {

					final Option_input_widget oiw = (Option_input_widget) iw;

					final List<Integer> options = new ArrayList<>();
					String vs = "filled." + iws.get(iw).getIdentifier() + ".with in (";

					for (int x = 0; x < oiw.getSize(); x++) {
						options.add(x);
					}

					for (final Integer i : options) {
						// we limit to only 10 values
						if (i > 4) {
							continue;
						}
						vs += "Option_value_" + i + "+";
					}
					if (vs.equals("filled." + iws.get(iw).getIdentifier() + ".with in (")) {
						vs = "#(filled." + iws.get(iw).getIdentifier() + ".with) = 0";
					} else {
						vs = vs.substring(0, vs.length() - 1) + ")";
					}

					content += System.getProperty("line.separator");
					content += vs;
					content += System.getProperty("line.separator");

					if (oiw.getSelected() == -1 || oiw.getSelected() > 9) {
						content += "(" + iws.get(iw).getIdentifier()
								+ ".content.(T/first)) = To_be_cleaned";
					} else {
						content += iws.get(iw).getIdentifier()
								+ ".content.(T/first) = Option_value_" + oiw.getSelected();
					}

				} else {

					content += System.getProperty("line.separator");
					if (iw.getValue().length() > 0) {
						content += "#" + iws.get(iw).getIdentifier()
								+ ".content.(T/first) = 1 and not(" + iws.get(iw).getIdentifier()
								+ ".content.(T/first) = To_be_cleaned)";
					} else {
						content += iws.get(iw).getIdentifier()
								+ ".content.(T/first) = To_be_cleaned";
					}
					content += System.getProperty("line.separator");
					content += "#(filled." + iws.get(iw).getIdentifier() + ".with & (";
					for (int x = 0; x < 5; x++) {
						content += "Option_value_" + x;
						if (x != 4) {
							content += "+";
						} else {
							content += ")) = 0";

						}
					}
				}
			}
		}
		content += System.getProperty("line.separator");
		// Collections.sort(to_order);
		// for (int cont = 0; cont < (to_order.size() - 1); cont++) {
		// if (cont != 0) {
		// content += " and ";
		// }
		// content += "IW/next[" + iws.get(to_order.get(cont)).getIdentifier() +
		// "]="
		// + iws.get(to_order.get(cont + 1)).getIdentifier();
		// }

		final Fact fact = new Fact(window.getIdentifier() + "_iws", content);
		return fact;
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
			for (final Window w : gui.getDynamicForwardLinks(aw.getId())) {
				if (ws.containsKey(w)) {
					edges.add(w);
				}
			}

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
			} else {
				content += System.getProperty("line.separator") + "#" + aws.get(aw).getIdentifier()
						+ ".goes = 0";
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
	public static Fact createFactsForSelectableWidget(final Map<Selectable_widget, Signature> sws,
			final Signature window) {

		return createFactsForElement(sws.values(), window, "sws");
	}

	public static Fact createFactsForElement(final Collection<Signature> widgets,
			final Signature window, final String fieldToRelated) {

		if (widgets.isEmpty()) {
			return new Fact(window.getIdentifier() + "_" + fieldToRelated, window.getIdentifier()
					+ "." + fieldToRelated + " = none");
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

	public static Signature searchForParent(final FunctionalitySemantics func_semantics,
			final Pattern_selectable_widget psw) throws Exception {

		Signature psw_sig;
		if (psw.getAlloyCorrespondence() != null && psw.getAlloyCorrespondence().length() > 0) {
			// the pattern input widget signature is retrieved
			final List<Signature> to_search = new ArrayList<>(
					func_semantics.getSelectable_w_extensions());
			to_search.add(func_semantics.getSelectable_w_signature());
			psw_sig = AlloyUtil.searchSignatureInList(to_search, psw.getAlloyCorrespondence());
			// System.out.println(piw.getAlloyCorrespondence());
			if (psw_sig == null) {
				throw new Exception("SpecificSemantics - generate: wrong alloy corrispondence "
						+ psw.getAlloyCorrespondence());
			}
		} else {
			psw_sig = func_semantics.getSelectable_w_signature();
		}
		return psw_sig;
	}

	public static Signature searchForParent(final FunctionalitySemantics func_semantics,
			final Pattern_input_widget piw) throws Exception {

		Signature piw_sig;
		if (piw.getAlloyCorrespondence() != null && piw.getAlloyCorrespondence().length() > 0) {
			// the pattern input widget signature is retrieved
			final List<Signature> to_search = new ArrayList<>(
					func_semantics.getInput_w_extensions());
			to_search.add(func_semantics.getInput_w_signature());
			piw_sig = AlloyUtil.searchSignatureInList(to_search, piw.getAlloyCorrespondence());
			// System.out.println(piw.getAlloyCorrespondence());
			if (piw_sig == null) {
				throw new Exception("SpecificSemantics - generate: wrong alloy corrispondence "
						+ piw.getAlloyCorrespondence());
			}
		} else {
			piw_sig = func_semantics.getInput_w_signature();
		}
		return piw_sig;
	}

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

	/*
	 * Function that extracts the semantic property from a solution.
	 */
	public static String extractProperty(final A4Solution sol, final SpecificSemantics sem)
			throws Exception {

		if (!sem.hasSemanticProperty()) {
			return "";
		}
		// we retrieve the signature that start with property
		final Map<Sig, List<String>> properties = new HashMap<>();
		for (final Sig sig : sol.getAllReachableSigs()) {
			if (sig.label.startsWith("this/Property_")) {
				properties.put(sig, new ArrayList<>());
			}
		}

		// first part of constraint
		String exists = "";
		// the constraint
		String constraint = "";

		// we retrieve all the atoms related to this sigs
		// map used to create the exists part of the fact
		final Map<String, List<String>> map_sig_atoms = new HashMap<>();

		int cont = 0;
		for (final Sig sig : properties.keySet()) {
			final String signame = sig.label.substring(5);
			if (!map_sig_atoms.containsKey(signame)) {
				map_sig_atoms.put(signame, new ArrayList<String>());
			}

			final List<String> sig_atom = AlloyUtil.getElementsInSet(sol, sig);
			properties.get(sig).addAll(sig_atom);
			// String new_exist = "";
			String new_constraint = "";
			for (final String ss : sig_atom) {

				// if (new_exist.length() > 0) {
				// new_exist += ",";
				// }
				// new_exist += ss;
				map_sig_atoms.get(signame).add(ss);

				new_constraint += ss;

				if (sig_atom.indexOf(ss) != sig_atom.size() - 1) {
					new_constraint += "+";
				}

			}

			// if (new_exist.length() > 0) {
			// new_exist += ":" + signame;
			// if (!exists.equals("one ")) {
			// exists += ",";
			// }
			// exists += new_exist;
			// }
			if (new_constraint.length() == 0) {
				constraint += "#" + signame + " = 0";
			} else {
				constraint += signame + " = (" + new_constraint + ")";
			}
			if (cont < properties.size() - 1) {
				constraint += " and ";
			}
			cont++;
		}

		for (final String sign : map_sig_atoms.keySet()) {
			if (map_sig_atoms.get(sign).size() == 0) {
				continue;
			}
			exists += "one ";
			for (int c = 1; c < map_sig_atoms.get(sign).size() + 1; c++) {
				final String atom = map_sig_atoms.get(sign).get(c - 1);
				if ((c % 5) == 0) {
					exists = exists.substring(0, exists.length() - 1) + ":" + sign + "|one ";
				}
				exists += atom + ",";
			}
			exists = exists.substring(0, exists.length() - 1) + ":" + sign + "|";
		}

		// for all the atoms found
		for (final Sig s : properties.keySet()) {

			final List<Field> fields_appoggio = new ArrayList<Field>();
			for (final Field f : s.getFields()) {
				fields_appoggio.add(f);
			}

			if (s instanceof SubsetSig) {
				final SubsetSig sub = (SubsetSig) s;
				for (final Sig ss : sub.parents) {
					for (final Field f : ss.getFields()) {
						fields_appoggio.add(f);
					}
				}
			}
			final List<Field> fields = new ArrayList<Field>();
			for (final Field field : fields_appoggio) {
				if (field.decl().expr instanceof ExprUnary) {
					final ExprUnary unary = (ExprUnary) field.decl().expr;
					final String fieldRef = unary.sub.toString().replaceAll("this/", "");
					if (!fieldRef.equals("Time") && isPartOfStructuralSigs(fieldRef, sem)) {
						fields.add(field);
					}
					// TODO: deal with other signatures
				} else if (field.decl().expr instanceof ExprBinary) {
					final ExprBinary bin = (ExprBinary) field.decl().expr;
					final String left = bin.left.toString().replaceAll("this/", "");
					final String right = bin.right.toString().replaceAll("this/", "");
					// TODO: deal with ternary rel
				}
			}

			final String signame = s.label.replace("this/", "");
			for (final String atom : map_sig_atoms.get(signame)) {

				for (final Field f : fields) {
					final A4TupleSet ts = sol.eval(f);
					final Iterator<A4Tuple> tupi = ts.iterator();
					String new_constraint = "";

					while (tupi.hasNext()) {
						final A4Tuple tup = tupi.next();
						if (!tup.atom(0).replace("$", "_").equals(atom)) {
							continue;
						}
						switch (tup.arity()) {
						case 2:
							final String sig = extractSigNameFromAtoms(tup.atom(1));

							new_constraint += sig + "+";
							// TODO: deal with other sigs that are not part of
							// the
							// structure
							break;
						case 3:
							// TODO: deal with ternry relations
							// sig = extractSigNameFromAtoms(tup.atom(1));
							// if (sig.equals("Time")) {
							// continue;
							// }
							// final String sig2 =
							// extractSigNameFromAtoms(tup.atom(2));
							// if (sig.equals("Time")) {
							// continue;
							// }
							// if (isPartOfStructuralSigs(sig, sem) &&
							// isPartOfStructuralSigs(sig2, sem)) {
							// new_constraint += sig;
							// }

							break;
						default:
							throw new Exception("AlloyUtil - extractProperty: wrong arity.");
						}

					}
					if (new_constraint.length() > 0) {
						new_constraint = new_constraint.substring(0, new_constraint.length() - 1);
						new_constraint = " and " + atom + "." + f.label + " = (" + new_constraint
								+ ")";
					} else {
						new_constraint = " and #" + atom + "." + f.label + " = 0";
					}
					constraint += new_constraint;

				}
			}
		}
		if (exists.equals("")) {
			return constraint;
		}
		return exists + constraint;
	}

	private static String extractSigNameFromAtoms(final String atom) {

		return atom.split("\\$")[0];
	}

	private static boolean isPartOfStructuralSigs(final String sig, final SpecificSemantics sem) {

		if (sem.getWindow_signature().getIdentifier().equals(sig)) {
			return true;
		}

		for (final Signature s : sem.getWindows_extensions()) {
			if (s.getIdentifier().equals(sig)) {
				return true;
			}
		}

		for (final Signature s : sem.getConcrete_windows()) {
			if (s.getIdentifier().equals(sig)) {
				return true;
			}
		}

		if (sem.getAction_w_signature().getIdentifier().equals(sig)) {
			return true;
		}

		for (final Signature s : sem.getAction_w_extensions()) {
			if (s.getIdentifier().equals(sig)) {
				return true;
			}
		}

		for (final Signature s : sem.getConcrete_action_w()) {
			if (s.getIdentifier().equals(sig)) {
				return true;
			}
		}

		if (sem.getInput_w_signature().getIdentifier().equals(sig)) {
			return true;
		}

		for (final Signature s : sem.getInput_w_extensions()) {
			if (s.getIdentifier().equals(sig)) {
				return true;
			}
		}

		for (final Signature s : sem.getConcrete_input_w()) {
			if (s.getIdentifier().equals(sig)) {
				return true;
			}
		}

		if (sem.getSelectable_w_signature().getIdentifier().equals(sig)) {
			return true;
		}

		for (final Signature s : sem.getSelectable_w_extensions()) {
			if (s.getIdentifier().equals(sig)) {
				return true;
			}
		}

		for (final Signature s : sem.getConcrete_selectable_w()) {
			if (s.getIdentifier().equals(sig)) {
				return true;
			}
		}

		return false;
	}

	static public int getWinScope(final SpecificSemantics in) {

		int cont = 0;
		for (final Signature s : in.getConcrete_windows()) {
			if (s.getCardinality() == null || s.getCardinality().getMax() > 1) {
				return -1;
			}
			cont++;
		}
		return cont;
	}

	static public int getValueScope(final SpecificSemantics in) {

		return 5 + 1;
	}

	static public int getAWScope(final SpecificSemantics in) {

		int cont = 0;
		for (final Signature s : in.getConcrete_action_w()) {

			if (s.getCardinality() == null || s.getCardinality().getMax() > 1) {

				return -1;
			}
			cont++;
		}
		return cont;
	}

	static public int getIWScope(final SpecificSemantics in) {

		int cont = 0;
		for (final Signature s : in.getConcrete_input_w()) {
			if (s.getCardinality() == null || s.getCardinality().getMax() > 1) {
				return -1;
			}
			cont++;
		}
		return cont;
	}

	static public int getSWScope(final SpecificSemantics in) {

		int cont = 0;
		for (final Signature s : in.getConcrete_selectable_w()) {
			if (s.getCardinality() == null || s.getCardinality().getMax() > 1) {
				return -1;
			}
			cont++;
		}
		return cont;
	}

	/**
	 * Method that returns an alloy model that cannot run the test case in input
	 *
	 * @param mod
	 * @param tcr
	 * @return
	 * @throws Exception
	 */
	static public Alloy_Model getTCaseModelOpposite(final SpecificSemantics mod,
			final List<GUIAction> acts) throws Exception {

		final List<Signature> sigs = mod.getSignatures();
		final List<Fact> facts = mod.getFacts();
		final List<Function> funcs = mod.getFunctions();
		final List<Predicate> preds = mod.getPredicates();
		final List<String> opens = mod.getOpenStatements();

		boolean invalid = false;
		for (final Signature sig : sigs) {
			if (sig.getIdentifier().equals("Invalid")) {
				invalid = true;
			}
		}

		final String fact = getFactForTC(acts, invalid);

		final Fact new_fact = new Fact("testcase", "not(" + fact + ")");
		facts.add(new_fact);

		final Alloy_Model out = new Alloy_Model(sigs, facts, preds, funcs, opens);
		return out;
	}

	/**
	 * Method that returns an alloy model with one run command that if run
	 * reproduces the test case in input
	 *
	 * @param mod
	 * @param tcr
	 * @return
	 * @throws Exception
	 */
	static public SpecificSemantics getTCaseModel(final SpecificSemantics mod,
			final List<GUIAction> acts, final Window reached, final Instance_GUI_pattern in)
			throws Exception {

		final Random r = new Random();
		final List<Signature> sigs = mod.getSignatures();
		final List<Fact> facts = mod.getFacts();
		final List<Function> funcs = mod.getFunctions();
		final List<Predicate> preds = mod.getPredicates();
		final List<String> opens = mod.getOpenStatements();

		boolean invalid = false;
		for (final Signature sig : sigs) {
			if (sig.getIdentifier().equals("Input_widget") && sig.getBinary_relations().size() == 2) {
				invalid = true;
			}
		}

		String fact = getFactForTC(acts, invalid);

		if (reached != null) {
			fact += " and Current_window.is_in.(T/last)=Window_" + reached.getId();

			if (acts.get(acts.size() - 1) instanceof Click) {
				final Click c = (Click) acts.get(acts.size() - 1);

				final List<Window> winds = in.getGui()
						.getDynamicForwardLinks(c.getWidget().getId());
				boolean selflink = true;
				for (final Window www : winds) {
					if (!www.getId().equals(c.getWindow().getId())) {
						selflink = false;
					}
				}

				if (selflink) {

					// for (final Input_widget iw : reached.getInputWidgets()) {
					// if (in.getPIW_for_IW(iw.getId()) != null) {
					// // final Input_widget iiw = (Input_widget)
					// // in.getGui()
					// // .getWindow(reached.getId()).getWidget(iw.getId());
					// // if (iiw.getValue() == null ||
					// // iiw.getValue().length() == 0) {
					// if (iw.getValue() == null || iw.getValue().length() == 0)
					// {
					// fact += " and #Input_widget_" + iw.getId()
					// + ".content.(T/last) = 0";
					// } else {
					// fact += " and #Input_widget_" + iw.getId()
					// + ".content.(T/last) = 1";
					// }
					// // }
					// }
					// }
					for (final Selectable_widget sw : reached.getSelectableWidgets()) {
						if (in.getPSW_for_SW(sw.getId()) != null) {
							final Selectable_widget ssw = (Selectable_widget) in.getGui()
									.getWindow(reached.getId()).getWidget(sw.getId());
							final int size = sw.getSize() - ssw.getSize();

							fact += " and #Selectable_widget_" + sw.getId() + ".list.(T/last) = "
									+ size;
							// if (sw.getSelected() == -1) {
							// fact += " and #Selectable_widget_" + sw.getId() +
							// ".selected.(T/last) = 0";
							// } else {
							// fact += " and #Selectable_widget_" + sw.getId() +
							// ".selected.(T/last) = 1";
							// }
						}
					}
				}
			}
		}

		final Fact new_fact = new Fact("testcase", fact);
		facts.add(new_fact);

		final List<GUIAction> actual_acts = acts.stream().filter(e -> !(e instanceof Clean))
				.collect(Collectors.toList());

		final int time_size = actual_acts.size() + 1;
		final int op_size = time_size - 1;

		int winscope = AlloyUtil.getWinScope(mod);
		int awscope = AlloyUtil.getAWScope(mod);
		int vscope = AlloyUtil.getValueScope(mod);
		int iwscope = AlloyUtil.getIWScope(mod);
		int swscope = AlloyUtil.getSWScope(mod);

		int totalscope = ConfigurationManager.getAlloyRunScope();

		if (r.nextBoolean()) {
			totalscope = totalscope + 1;
		}
		if (r.nextBoolean()) {
			winscope = winscope + 1;
		}
		if (r.nextBoolean()) {
			awscope = awscope + 1;
		}
		if (r.nextBoolean()) {
			vscope = vscope + 1;
		}
		if (r.nextBoolean()) {
			iwscope = iwscope + 1;
		}
		if (r.nextBoolean()) {
			swscope = swscope + 1;
		}

		if (winscope == -1 || awscope == -1 || iwscope == -1 || swscope == -1) {

			totalscope = totalscope * 2;
		}

		String runCom = "run {System} for " + totalscope + " but " + time_size + " Time, "
				+ op_size + " Operation";

		if (winscope > -1) {
			runCom += "," + winscope + " Window ";
		}
		if (awscope > -1) {
			runCom += "," + awscope + " Action_widget ";
		}
		if (vscope > -1) {

			runCom += "," + (vscope + (op_size * 2 / 3)) + " Value ";
		}
		if (iwscope > -1) {
			runCom += "," + iwscope + " Input_widget ";
		}
		if (swscope > -1) {
			runCom += "," + swscope + " Selectable_widget ";
		}

		final Alloy_Model out = new Alloy_Model(sigs, facts, preds, funcs, opens);
		out.addRun_command(runCom);

		return SpecificSemantics.instantiate(out);

	}

	/**
	 * Method that returns an alloy model that cannot run the test case in input
	 *
	 * @param mod
	 * @param tcr
	 * @return
	 * @throws Exception
	 */
	static private String getFactForTC(final List<GUIAction> acts, final boolean invalid)
			throws Exception {

		final DataManager dm = DataManager.getInstance();
		String fact = "";
		String t = "";
		final Map<String, List<String>> values_used = new HashMap<>();
		final Map<String, List<Input_widget>> values_used_iw = new HashMap<>();

		for (int cont = 0; cont < acts.size(); cont++) {
			final GUIAction act = acts.get(cont);
			if (act instanceof Clean) {
				continue;
			}
			if (cont == 0) {
				t = "t";

			} else {
				t = "T/next[" + t + "]";
				fact += " and ";
			}

			if (act instanceof Click) {
				final Click c = (Click) act;

				fact += "Track.op.(" + t + ") in Click";

				fact += " and Track.op.(" + t + ").clicked=Action_widget_" + c.getWidget().getId();
			}

			if (act instanceof Fill) {
				final Fill f = (Fill) act;

				fact += "Track.op.(" + t + ") in Fill";

				fact += " and Track.op.(" + t + ").filled=Input_widget_" + f.getWidget().getId();

				if (!(f.getWidget() instanceof Option_input_widget)) {

					String new_value = "";
					if (f.getInput() != null) {
						new_value += f.getInput();
					}
					if (!values_used_iw.containsKey(new_value)) {
						values_used_iw.put(new_value, new ArrayList<Input_widget>());
					}
					values_used_iw.get(new_value).add((Input_widget) f.getWidget());

					if (!values_used.containsKey(new_value)) {
						values_used.put(new_value, new ArrayList<String>());
					}
					values_used.get(new_value).add("Track.op.(" + t + ").with");
				} else {
					if (f.getInput() == null || f.getInput().length() == 0) {
						fact += " and Track.op.(" + t + ").with=none";
					} else {
						fact += " and Track.op.(" + t + ").with=Option_value_" + f.getInput();
					}
				}
			}

			if (act instanceof Select) {
				final Select s = (Select) act;
				final Selectable_widget sw = (Selectable_widget) s.getWidget();

				fact += "Track.op.(" + t + ") in Select";

				final String obj = "Track.op.(" + t + ")" + ".which";

				fact += " and Track.op.(" + t + ").wid=Selectable_widget_" + s.getWidget().getId();
				// fact += " and #Selectable_widget_" + s.getWidget().getId() +
				// ".list.(" + t + ")="
				// + sw.getSize();
				fact += " and #(T/prevs[(" + obj + ".appeared)] & Selectable_widget_"
						+ s.getWidget().getId() + ".list.(" + t + ").appeared) = " + s.getIndex();
				// fact += " and #(T/nexts[(" + obj +
				// ".appeared)] & Selectable_widget_"
				// + s.getWidget().getId() + ".list.(" + t + ").appeared) = "
				// + ((sw.getSize() - 1) - s.getIndex());
			}
		}
		// we deal with the values
		final List<String> keys = values_used.keySet().stream().collect(Collectors.toList());
		for (int c = 0; c < keys.size(); c++) {
			final String s = keys.get(c);
			// we deal with the equals
			if (s != null && s.length() > 0) {
				final List<String> fills = values_used.get(s);

				fact += " and #" + fills.get(0) + " = 1";

				if (fills.size() > 1) {
					String prev = fills.get(0);
					for (int cont = 1; cont < fills.size(); cont++) {
						fact += " and " + prev + "=" + fills.get(cont);
						prev = fills.get(cont);
					}
				}
				// we deal with the differents
				for (int cc = c + 1; cc < keys.size(); cc++) {
					final String ss = keys.get(cc);
					fact += " and " + values_used.get(s).get(0) + "!=" + values_used.get(ss).get(0);
				}

				// we check if the value is valid or invalid
				for (final Input_widget iw : values_used_iw.get(s)) {

					String metadata = iw.getLabel() != null ? iw.getLabel() : "";
					metadata += iw.getDescriptor() != null && metadata.length() == 0 ? iw
							.getDescriptor() : "";

							if (dm.getInvalidData(metadata).contains(s)) {
								assert (invalid);
								fact += " and " + values_used.get(s).get(0) + " in Input_widget_"
										+ iw.getId() + ".invalid";

							} else {
								if (invalid) {
									fact += " and not(" + values_used.get(s).get(0) + " in Input_widget_"
											+ iw.getId() + ".invalid)";
								}
							}
				}
			} else {
				final List<String> fills = values_used.get(s);
				for (int cont = 0; cont < fills.size(); cont++) {
					fact += " and " + fills.get(cont) + "=none";
				}

			}

		}

		fact = "one t: Time | " + fact;
		return fact;

	}
}
