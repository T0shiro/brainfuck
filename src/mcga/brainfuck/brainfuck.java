package mcga.brainfuck;

import  java.util.List;
import  java.util.ArrayList;
/**
 * Created by user on 23/09/2016.
 */
public class brainfuck {

    protected int dataPointer = 0; // pointer to the current index of file
    protected int memoryPointer = 0; // pointer to the current index of memory
    public static void main(String[] args) {
        System.out.println("Gaetan");
        System.out.println("salut bg");
        Operation operation = new Operation();
        operation.incrementation();
        operation.incrementation();
        operation.moveR();
        operation.moveR();
        operation.moveR();

    }

}
