package mcga.brainfuck;

/**
 * Class defining the action corresponding to the beginning of a loop.
 * This class, as well as the Back class, extends Loop, as they both define a loop in a Brainf*ck program.
 */
public class Jump extends Loop {

    public Jump() {
        Interpreter.jumpIndexStack.add(Interpreter.instructions.size());
    }

    /**
     * Overrides the method defined in the Instruction interface to execute the jump action.
     * @throws InvalidValueException
     * @throws IndexOutOfBoundsException
     */
    @Override
    public void interpret() throws InvalidValueException, IndexOutOfBoundsException {
        jump();
    }

    /**
     * Defines the actions of a jump instruction.
     */
    private void jump() {
        if(Brainfuck.memory.getCurrentCellValue()==0){
            Interpreter.backToJumpIndex=boundLoopIndex;
        }
    }
}
