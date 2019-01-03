/* Alloy Analyzer 4 -- Copyright (c) 2006-2009, Felix Chang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.mit.csail.sdg.alloy4whole;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4.Version;
import edu.mit.csail.sdg.alloy4.XMLNode;
import edu.mit.csail.sdg.alloy4viz.StaticInstanceReader;
import edu.mit.csail.sdg.alloy4viz.VizGUI;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompUtil;
import edu.mit.csail.sdg.translator.A4Options;
import edu.mit.csail.sdg.translator.A4Options.SatSolver;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.A4SolutionReader;
import edu.mit.csail.sdg.translator.A4SolutionWriter;
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * This class is used by the Alloy developers to drive the regression test
 * suite. For a more detailed guide on how to use Alloy API, please see
 * "ExampleUsingTheCompiler.java"
 */

@CommandLine.Command(
                     mixinStandardHelpOptions = true,
                     version = "CLI version 0.1" )
public final class CLI {

    private enum ESolver {
                          SAT4J,
                          MINISAT,
                          MINISATPROVER,
                          LINGELING,
                          PLINGELING,
                          GLUCOSE,
                          CRYPTOMINISAT
    };

    // CLI options
    @Option(
            names = "--outputDirectory",
            description = "Directory for writting results (default: ${DEFAULT-VALUE})." )
    private String   outputDir            = ".";

    @Option(
            names = "--writeMetaModel",
            description = "Write metamodel or not (default: ${DEFAULT-VALUE})." )
    private boolean  writeMetaModel       = false;

    @Option(
            names = "--writeSolutions",
            description = "Write solutions or not (default: ${DEFAULT-VALUE})." )
    private boolean  writeSolutions       = false;

    @Option(
            names = "--maxSolutions",
            description = "Limit solutions to output. (-1) - no limit. Default: ${DEFAULT-VALUE}" )
    private int      maxSolutions         = 100;

    @Option(
            names = "--solver",
            description = "Selected solver. Valid values: ${COMPLETION-CANDIDATES}. Default: ${DEFAULT-VALUE}" )
    private ESolver  solver               = ESolver.SAT4J;

    @Option(
            names = "--inferPartialInstance",
            description = "Infer partial instance (default: ${DEFAULT-VALUE})." )
    private boolean  inferPartialInstance = true;


    @Option(
            names = "--symmetry",
            description = "Symmetry breaking effort. 0 - favor for likely sat, 100 - favor for likely unsat. Default: ${DEFAULT-VALUE}" )
    private int      symmetry             = 20;

    @Option(
            names = "--skolemDeph",
            description = "Depth of skolemization. 0 - only skolem constants, >= 1 - skolem functions of corresponding arity. Default: ${DEFAULT-VALUE}" )
    private int      skolemDepth          = 0;

    @Option(
            names = "--coreMinimization",
            description = "Core minimization strategy. (0=GuaranteedLocalMinimum 1=FasterButLessAccurate 2=EvenFaster...). Default: ${DEFAULT-VALUE}" )
    private int      coreMinimization     = 2;

    @Option(
            names = "--coreGranularity",
            description = "Unsat core granularity, default is 0 (only top-level conjuncts are " + "considered), 3 expands all quantifiers. Default: ${DEFAULT-VALUE}." )
    private int      coreGranularity      = 0;

    @Option(
            names = "--noOverflow",
            description = "This option specifies whether the solver should report only solutions that " + "don't cause any overflows. Default: ${DEFAULT-VALUE}." )
    private boolean  noOverflow           = false;

    @Option(
            names = "--unrolDepth",
            description = "This option constrols how deep we unroll loops and unroll recursive " + "predicate/function/macros (negative means it's disallowed). " + "Default: ${DEFAULT-VALUE}." )
    private int      unrolls              = -1;

    @Option(
            names = "--outputLog",
            description = "Log file name. Default: ${DEFAULT-VALUE}" )
    private String   logFileName          = "log.txt";

    @Option(
            names = "--viz",
            description = "Start VizGUI to view solutions." )
    private boolean  viz                  = false;

    @Option(
            names = "--run",
            description = "Use specified expression for run `run Expr for ...`" )
    private String   runExpr              = null;

    @Option(
            names = "--check",
            description = "Use specified expression for check `check Expr for ...`" )
    private String   checkExpr            = null;

    @Option(
            names = "--label",
            description = "Use specified label for check/run expression." )
    private String   labelExpr            = null;

    @Option(
            names = "--scope",
            description = "Scope for run/check expressions. Default: ${DEFAULT-VALUE}" )
    private int      scope                = 3;

    @Option(
            names = "--bitwidth",
            description = "Bitwidth for run/check expressions. Default: ${DEFAULT-VALUE}" )
    private int      bitwidth             = 3;

    @Option(
            names = "--seqlength",
            description = "Sequence length for run/check expressions. Default: ${DEFAULT-VALUE}" )
    private int      seqlength            = 3;

    @Option(
            names = "--verbose",
            description = "Verbose mode on/off. Default: ${DEFAULT-VALUE}" )
    private boolean  verbose              = false;

    @Parameters(
                index = "0",
                arity = "0..*",
                description = "input files" )
    private String[] files;


    // Reporter
    private static final class SimpleReporter extends A4Reporter {

        public final class EchoingStringBuilder {

            private final StringBuilder sb   = new StringBuilder();

            boolean                     echo = false;

            public boolean echo(boolean e) {
                boolean prev = echo;
                echo = e;
                return prev;
            }

            public EchoingStringBuilder delete(int start, int end) {
                sb.delete(start, end);
                return this;
            }

            public int length() {
                return sb.length();
            }

            public EchoingStringBuilder append(String s) {
                sb.append(s);
                if (echo) {
                    System.out.append(s);
                    System.out.flush();
                }
                return this;
            }

            @Override
            public String toString() {
                return sb.toString();
            }
        }

        private final EchoingStringBuilder sb       = new EchoingStringBuilder();

        private final List<ErrorWarning>   warnings = new ArrayList<ErrorWarning>();

        private final RandomAccessFile     os;

        public SimpleReporter(String filename) throws IOException {
            os = new RandomAccessFile(filename, "rw");
            os.setLength(0);
        }

        public void flush() throws IOException {
            if (sb.length() > 65536) {
                os.write(sb.toString().getBytes("UTF-8"));
                sb.delete(0, sb.length());
            }
        }

        public void close() throws IOException {
            if (sb.length() > 0) {
                os.write(sb.toString().getBytes("UTF-8"));
                sb.delete(0, sb.length());
            }
            os.close();
        }

        @Override
        public void debug(String msg) {
            sb.append(msg);
        }

        @Override
        public void parse(String msg) {
            sb.append(msg);
        }

        @Override
        public void typecheck(String msg) {
            sb.append(msg);
        }

        @Override
        public void warning(ErrorWarning msg) {
            warnings.add(msg);
        }

        @Override
        public void scope(String msg) {
            sb.append("   ");
            sb.append(msg);
        }

        @Override
        public void bound(String msg) {
            sb.append("   ");
            sb.append(msg);
        }

        @Override
        public void translate(String solver, int bitwidth, int maxseq, int skolemDepth, int symmetry) {
            sb.append("   Solver=" + solver + " Bitwidth=" + bitwidth + " MaxSeq=" + maxseq + " Symmetry=" + (symmetry > 0 ? ("" + symmetry) : "OFF") + "\n");
        }

        @Override
        public void solve(int primaryVars, int totalVars, int clauses) {
            sb.append("   " + totalVars + " vars. " + primaryVars + " primary vars. " + clauses + " clauses.\n");
        }

        @Override
        public void resultCNF(String filename) {
        }

        @Override
        public void resultSAT(Object command, long solvingTime, Object solution) {
            if (!(command instanceof Command))
                return;
            Command cmd = (Command) command;
            sb.append(cmd.check ? "   Counterexample found. " : "   Instance found. ");
            if (cmd.check)
                sb.append("Assertion is invalid");
            else
                sb.append("Predicate is consistent");
            if (cmd.expects == 0)
                sb.append(", contrary to expectation");
            else if (cmd.expects == 1)
                sb.append(", as expected");
            sb.append(". " + solvingTime + "ms.\n\n");
        }

        @Override
        public void resultUNSAT(Object command, long solvingTime, Object solution) {
            if (!(command instanceof Command))
                return;
            Command cmd = (Command) command;
            sb.append(cmd.check ? "   No counterexample found." : "   No instance found.");
            if (cmd.check)
                sb.append(" Assertion may be valid");
            else
                sb.append(" Predicate may be inconsistent");
            if (cmd.expects == 1)
                sb.append(", contrary to expectation");
            else if (cmd.expects == 0)
                sb.append(", as expected");
            sb.append(". " + solvingTime + "ms.\n\n");
        }
    }

    private CLI() {
    }

    private A4Options GetOptions() {
        SatSolver satSolver;
        switch (solver) {
            case MINISAT :
                satSolver = A4Options.SatSolver.MiniSatJNI;
                break;
            case MINISATPROVER :
                satSolver = A4Options.SatSolver.MiniSatProverJNI;
                break;
            case LINGELING :
                satSolver = A4Options.SatSolver.LingelingJNI;
                break;
            case PLINGELING :
                satSolver = A4Options.SatSolver.PLingelingJNI;
                break;
            case GLUCOSE :
                satSolver = A4Options.SatSolver.GlucoseJNI;
                break;
            case CRYPTOMINISAT :
                satSolver = A4Options.SatSolver.CryptoMiniSatJNI;
                break;
            default :
                satSolver = A4Options.SatSolver.SAT4J;
        }

        A4Options options = new A4Options();

        options.tempDirectory = Environment.alloyTempPath();
        options.solverDirectory = Environment.alloyBinaryPath();
        options.solver = satSolver;
        options.inferPartialInstance = inferPartialInstance;
        options.symmetry = symmetry;
        options.skolemDepth = skolemDepth;
        options.coreMinimization = coreMinimization;
        options.coreGranularity = coreGranularity;
        options.noOverflow = noOverflow;
        options.unrolls = unrolls;

        return options;
    }

    private void WriteMetaModel(String filename, Module top) throws Exception {
        if (writeMetaModel) {
            StringWriter metasb = new StringWriter();
            PrintWriter meta = new PrintWriter(metasb);
            Util.encodeXMLs(meta, "\n<alloy builddate=\"", Version.buildDate(), "\">\n\n");
            A4SolutionWriter.writeMetamodel(top.getAllReachableSigs(), filename, meta);
            Util.encodeXMLs(meta, "\n</alloy>");
            meta.flush();
            metasb.flush();
            String metaxml = metasb.toString();
            A4SolutionReader.read(new ArrayList<Sig>(), new XMLNode(new StringReader(metaxml)));
            StaticInstanceReader.parseInstance(new StringReader(metaxml));
            RandomAccessFile of = new RandomAccessFile(Util.FilePath.makePath(outputDir, filename, "xml"), "rw");
            of.setLength(0);
            of.write(metaxml.getBytes("UTF-8"));
            of.close();
        }
    }

    public void WriteSolution(int num, String filename, Command cmd, A4Solution solution) throws Exception {
        String fname = Util.FilePath.basenameWithoutExesnion(filename) + "_" + cmd.label + "_" + String.valueOf(num);

        solution.writeXML(Util.FilePath.makePath(outputDir, fname, "xml"));
    }

    private static void EnumerateSolutions(SimpleReporter rep, CLI cli, String filename, Command c, A4Solution s) throws Exception {
        Set<String> latestKodkods = new LinkedHashSet<String>();
        if (cli.writeSolutions && s.satisfiable()) {
            latestKodkods.add(s.toString());
            int idx = 0;
            cli.WriteSolution(idx++, filename, c, s);
            int tries = 0;
            if (s.isIncremental()) {
                s = s.next();
                while (s.satisfiable() && (idx < cli.maxSolutions || cli.maxSolutions == -1) && tries < 100) {
                    if (latestKodkods.add(s.toString())) {
                        cli.WriteSolution(idx++, filename, c, s);
                    } else {
                        tries++;
                    }
                    s = s.next();
                }
            }
            rep.sb.append("Enumerating solutions: " + String.valueOf(idx) + " instances found.\n");
        }
    }

    private static Module parseFile(final SimpleReporter rep, String filename) throws IOException {
        rep.sb.append("\n\nMain file = " + filename + "\n");
        Module world = CompUtil.parseEverything_fromFile(rep, null, filename);

        for (ErrorWarning msg : rep.warnings) {
            rep.sb.append("Relevance Warning:\n" + (msg.toString().trim()) + "\n\n");
        }
        rep.warnings.clear();
        rep.flush();
        return world;
    }

    private static void executeCommand(CLI cli, A4Options options, final SimpleReporter rep, String filename, Module world, Command c) throws Exception, IOException {
        rep.sb.append("Executing \"" + c.toString() + "\"\n");
        A4Solution s = TranslateAlloyToKodkod.execute_commandFromBook(rep, world.getAllReachableSigs(), c, options);
        if (s.satisfiable()) {
            EnumerateSolutions(rep, cli, filename, c, s);
        }
        rep.flush();
    }

    public static void main(String[] args) throws Exception {

        CLI cli = new CLI();

        CommandLine commandLine = new CommandLine(cli);
        commandLine.parse(args);
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        } else if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            return;
        }

        if (cli.viz) {
            if (cli.files != null) {
                VizGUI viz = new VizGUI(true, cli.files[0], null);
            } else {
                VizGUI viz = new VizGUI(true, null, null);
            }
            return;
        }

        if (cli.files == null) {
            commandLine.usage(System.out);
            return;
        }

        A4Options options = cli.GetOptions();
        Environment.copyFromJAR();

        final SimpleReporter rep = new SimpleReporter(cli.logFileName);
        final SimpleReporter.EchoingStringBuilder sb = rep.sb;
        sb.echo(cli.verbose);

        for (String filename : cli.files) {

            options.originalFilename = filename;

            try {
                Module world = parseFile(rep, filename);

                List<Command> cmds = world.getAllCommands();

                cli.WriteMetaModel(filename, world);

                if (cli.runExpr != null || cli.checkExpr != null) {
                    Expr expr = CompUtil.parseOneExpression_fromString(world, cli.runExpr != null ? cli.runExpr : cli.checkExpr);
                    Command c = new Command(cli.runExpr == null, cli.scope, cli.bitwidth, cli.seqlength, expr);
                    executeCommand(cli, options, rep, filename, world, c);
                } else {
                    for (int i = 0; i < cmds.size(); i++) {
                        Command c = cmds.get(i);
                        executeCommand(cli, options, rep, filename, world, c);
                    }
                }
            } catch (Throwable ex) {
                rep.sb.append("\n\nException: " + ex + "\n\n");
            }
        }
        rep.close();
        Environment.iterateTemp(Environment.alloyHome(), true);
    }

}
