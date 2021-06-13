package main;

import grammar.PDLGrammar;
import grammar.syntaxtree.Start;
import type.ExpTreeNode;
import type.VarTable;
import visitor.ExpTreeBuilderVisitor;

import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import generator.CodeGenerator;

import java.io.File;

public class PDLSolver {

	private static class Options {
		@Option(name = "-i", aliases = "-input", usage = "process the PDL file <INPUT>", metaVar = "INPUT")
		public File file = null;
		@Option(name = "-o", aliases = "-output", usage = "place the generated program into <OUTPUT>", metaVar = "OUTPUT")
		public File output = null;
		@Option(name = "-d", aliases = "-directory", usage = "process all PDL files in <DIRECTORY>", metaVar = "DIRECTORY")
		public File directory = null;
		@Option(name = "-t", aliases = "-iteration", usage = "maximum iteration time for bound tightening")
		public int iteration = 100;
		@Option(name = "-l", aliases = "-memlimit", usage = "memory limitation for dynamic programming table")
		public String memlimit = "10000000";
		@Option(name = "-v", aliases = "-variable", usage = "display the variable table")
		public boolean displayVar = false;
		@Option(name = "-e", aliases = "-exptree", usage = "display the expression trees")
		public boolean displayExpTree = false;
		@Option(name = "-p", aliases = "-depend", usage = "display the dependencies between variables")
		public boolean displayDepend = false;
		@Option(name = "-m", aliases = "-message", usage = "display messages")
		public boolean displayMessage = false;
	};

	private static Options options = new Options();

	private static void process(File infile, File outfile) {
		if (outfile == null) {
			String infilename = infile.getPath();
			outfile = new File(infilename.substring(0, infilename.lastIndexOf('.')) + ".c");
		}
		if (options.displayMessage) {
			System.out.println();
			System.out.println("====================");
			System.out.println("Processing " + infile + ", OUTPUT: " + outfile);
			System.out.println();
		}
		Start start;
		long startTime = System.currentTimeMillis();
		try {
			PDLGrammar pdlParser = new PDLGrammar(new FileInputStream(infile));
			start = pdlParser.Start();
		} catch (Throwable e) {
			System.out.println("Parse Error!");
			System.out.println(e.getMessage());
			System.exit(1);
			return;
		}
		VarTable varTable = new VarTable();
		ArrayList<ExpTreeNode> expTrees = new ArrayList<ExpTreeNode>();
		try {
			start.accept(new ExpTreeBuilderVisitor(varTable, expTrees));
		} catch (Exception e) {
			System.out.println("Analyse Error!");
			System.out.println(e.getMessage());
			System.exit(1);
			return;
		}
		if (varTable.getOutputs().size() == 0) {
			System.out.println("No Output Error!");
			System.exit(1);
			return;
		}
		if (options.displayExpTree) {
			System.out.println("[ Expression Trees ]");
			for (ExpTreeNode node : expTrees)
				System.out.println(node);
			System.out.println();
		}
		BoundTightener boundTightener = new BoundTightener(expTrees);
		for (int count = 0; count < options.iteration; count++)
			try {
				if (!boundTightener.tighten())
					break;
			} catch (Exception e) {
				System.out.println("Type Error!");
				e.printStackTrace();
				System.out.println(e.getMessage());
				System.exit(1);
				return;
			}
		new DepExtracter().extract(expTrees);
		if (options.displayDepend) {
			System.out.println("[ Dependencies ]");
			varTable.printDepend();
			System.out.println();
		}
		if (!varTable.chooseIndependent()) {
			System.out.println("Cannot be enumerated!");
			String vs = varTable.getInfiniteVars();
			if (vs != null)
				System.out.println("Unbounded variables: " + vs);
			if (options.displayVar) {
				System.out.println("[ Identifier Table ]");
				System.out.println(varTable);
			}
			System.exit(1);
			return;
		}
		if (options.displayVar) {
			System.out.println("[ Identifier Table ]");
			System.out.println(varTable);
		}
		try {
			new CodeGenerator().generate(outfile, varTable, expTrees, options.memlimit);
		} catch (IOException e) {
			System.err.println("Failed to save code to [" + outfile.getPath() + "]!\n");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Cannot be enumerated!");
			String vs = varTable.getInfiniteVars();
			if (vs != null)
				System.out.println("Unbounded variables: " + vs);
			System.exit(1);
		}
		if (options.displayMessage)
			System.out.println("Time Elapsed: " + (System.currentTimeMillis() - startTime) + "ms");
	}

	public static void main(String[] args) {
		CmdLineParser cmdline = new CmdLineParser(options);
		try {
			cmdline.parseArgument(args);
			if (options.file != null)
				process(options.file, options.output);
			else if (options.directory != null) {
				File[] files = options.directory.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".pdl");
					}

				});
				for (File file : files)
					process(file, null);
			}
		} catch (CmdLineException e) {
			System.err.println("PDLSolver [options...]    arguments...");
			cmdline.printUsage(System.err);
			System.exit(1);
			return;
		}
	}

}
