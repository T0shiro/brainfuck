package mcga.brainfuck;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This class extends Parser. It rewrites the methods from the superclass to interpret the parsed actions.
 */

public class Interpreter extends Parser {

    /**
     * Default constructor of the class.
     *
     * @see Parser#Parser()
     */
    public static List<Instruction> instructions = new ArrayList<>();
    public static Stack<Integer> jumpIndexStack = new Stack<>();
    public static int backToJumpIndex = 0;

    public Interpreter() {
        super();
    }

    /**
     * Constructor defining the stream in parameter as the input stream.
     *
     * @param stream
     * @see Parser#Parser(InputStream)
     */
    public Interpreter(InputStream stream) {
        super(stream);
    }

    @Override
    public void parseFile() {
        super.parseFile();
        for (int i = 0; i < instructions.size(); i++) {
            try {
                if (i >= backToJumpIndex) {
                    backToJumpIndex=0;
                    instructions.get(i).interpret();
                }
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method overrides Parser#execute called in Parser#parseFile so that it creates the Instruction
     * corresponding to the String in parameter and interprets it.
     *
     * @param str String corresponding to the Instruction
     * @throws InvalidInstructionException
     * @see Parser#execute(String)
     */
    @Override
    public void execute(String str) throws InvalidInstructionException {
        instructions.add(InstructionFactory.createInstruction(str));
    }


}
