package mcga.brainfuck;


import mcga.brainfuck.exceptions.BrainfuckException;
import mcga.brainfuck.instructions.Input;
import mcga.brainfuck.processing.*;
import mcga.brainfuck.processing.Parser;
import org.apache.commons.cli.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static mcga.brainfuck.Arguments.*;

/**
 * Main class of the project.
 * Contains the main method.
 *
 * @author Team Make Coding Great Again
 */
public class Brainfuck {

    private static Interpreter interpreter;
    private static Memory memory = new Memory();
    private static List<Parser> parsers = new ArrayList<>();
    private static PrintStream mainOutput = System.out;
    public static Memory getMemory() {
        return memory;
    }

    public static void setMemory(Memory memory) {
        Brainfuck.memory = memory;
    }


    /**
     * Main method, which executes the readArguments method and after the execution, displays the values of the
     * memory's cells and the metrics.
     *
     * @param args Different parameters accepted
     */
    public static void main(String[] args) {
        try {
            readArguments(args);
        } catch (BrainfuckException e) {
            System.err.println(e.getMessage());
            System.exit(e.getExitCode());
        }
    }

    /**
     * Reads the provided arguments, determines the actions they correspond to and executes them.
     *
     * @param args list of the arguments passed in parameter
     */

    private static void readArguments(String[] args) throws BrainfuckException {
        Input.stream = System.in;
        Options options = createOptions();
        CommandLineParser commandParser = new DefaultParser();
        try {
            CommandLine line = commandParser.parse(options, args);
            String pValue = line.getOptionValue(P.expression);
            if (line.hasOption(P.expression)) {
                boolean hasTrace = line.hasOption(TRACE.expression);
                boolean hasRewrite = line.hasOption(REWRITE.expression);
                boolean hasCheck = line.hasOption(CHECK.expression);
                boolean hasTranslate = line.hasOption(TRANSLATE.expression);
                boolean hasToC = line.hasOption(TOC.expression);
                if (hasCheck || hasRewrite || hasTranslate) {
                    if (hasRewrite) {
                        parsers.add(new Rewrite(pValue));
                    }
                    if (hasCheck) {
                        parsers.add(new Check(pValue));
                    }
                    if (hasTranslate) {
                        parsers.add(new Translate(pValue));
                    }

                } else if (hasTrace) {
                    int index = pValue.lastIndexOf('.');
                    String bfFile = pValue;
                    if (index > 0) {
                        bfFile = bfFile.substring(0, index);
                    }
                    String logFile = bfFile + ".log";
                    Trace trace = new Trace(pValue, logFile);
                    parsers.add(trace);
                    interpreter = trace;
                } else if (hasToC) {
                    int index = pValue.lastIndexOf(".");
                    String bfFile = pValue;
                    if (index > 0) {
                        bfFile = bfFile.substring(0, index);
                    }
                    String cFile = bfFile + ".c";
                    parsers.add(new ToC(pValue, new PrintStream(cFile)));
                    interpreter=new Interpreter();
                }
                if (parsers.isEmpty()) {
                    Interpreter interpreter = new Interpreter(pValue);
                    parsers.add(interpreter);
                    Brainfuck.interpreter = interpreter;
                }
            }
            if (line.hasOption(INPUT.expression)) {
                Input.stream = new FileInputStream(line.getOptionValue(INPUT.expression));
            }
            if (line.hasOption(OUTPUT.expression)) {
                mainOutput = new PrintStream(line.getOptionValue(OUTPUT.expression));
            }
            if (parsers.isEmpty()) {
                Interpreter interpreter = new Interpreter();
                parsers.add(interpreter);
                Brainfuck.interpreter = interpreter;
            }
            for (Parser parser : parsers) {
                parser.parseFile();
            }
        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Error : " + exp.getMessage());
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(3);
        }
    }
    
    public static PrintStream getMainOutput() {
        return mainOutput;
    }
    
    public static Interpreter getInterpreter() {
        return interpreter;
    }
}