package mcga.brainfuck;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * This class contains a bunch of methods used to parse the file containing the Brainf*ck code
 * and execute the code.
 */
public abstract class Parser {

    protected InputStream stream;
    public static final int SQUARE_SIDE = 3;
    public static final String EMPTY_INSTRUCTION = "000000";

    /**
     * In case a file is specified in the launching command, this constructor is called.
     * @param stream Stream from file
     */
    public Parser(InputStream stream) {
        this.stream = stream;
    }

    /**
     * Default constructor.
     * By default, the input stream is System.in.
     */
    public Parser() {
        this(System.in);
    }

    /**
     * Reads the file containing the Brainfuck code. This method is called in each subclass, with some
     * additions depending on the subclass.
     * @see Check#parseFile()
     * @see Interpreter#parseFile()
     */
    public void parseFile() {
        Scanner scanner = new Scanner(this.stream);
        String str;
        scanner.useDelimiter("\\s*");
        while (scanner.hasNext()) {
            str = scanner.next();
            if (InstructionFactory.isLongSyntax(str)) {
                str += scanner.nextLine();
            }
            try {
                execute(str);
            } catch (InvalidInstructionException e) {
                System.err.println(e.getMessage());
                System.exit(42);
            }
        }
    }

    /**
     * Reads the bitmap image containing the Brainfuck code. This method is called in each subclass, with some
     * additions depending on the subclass.
     * @see Check#readBitmap()
     * @see Interpreter#readBitmap()
     */
    public void readBitmap() {
        try {
            BufferedImage image = ImageIO.read(stream);
            int height = image.getHeight();
            int width = image.getWidth();
            if (height % SQUARE_SIDE != 0 || width % SQUARE_SIDE != 0) {
                throw new InvalidBitmapException();
            }

            String prevColor = "";
            String hexColor = "";
            for (int i = 0 ; i < height ; i += SQUARE_SIDE) {
                for (int j = 0 ; j < width ; j += SQUARE_SIDE) {

                    for (int iSquare = 0 ; iSquare < SQUARE_SIDE ; iSquare++) {
                        for (int jSquare = 0 ; jSquare < SQUARE_SIDE ; jSquare++) {
                            hexColor = "";
                            Color imgColor = new Color(image.getRGB(jSquare + j, iSquare + i));
                            hexColor += String.format("%02X", imgColor.getRed());
                            hexColor += String.format("%02X", imgColor.getGreen());
                            hexColor += String.format("%02X", imgColor.getBlue());
                            if (! prevColor.equals(hexColor) && iSquare != 0 && jSquare != 0) {
                                throw new InvalidBitmapException();
                            }
                            prevColor = hexColor;

                        }
                    }
                    if (! hexColor.equals(EMPTY_INSTRUCTION)) {
                        execute(hexColor);
                    }
                }
            }
        } catch (InvalidInstructionException e) {
            e.printStackTrace();
            System.exit(42);
        } catch (InvalidBitmapException e) {
            e.printStackTrace();
            System.exit(14);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(3);
        }
    }

    /**
     * Defines the actions to execute when a new object from the class is created.
     * Overriden in each subclass.
     * @param str String corresponding to an instruction
     * @throws InvalidInstructionException
     * @see Check#execute(String)
     * @see Interpreter#execute(String)
     * @see Rewrite#execute(String)
     */
    public abstract void execute(String str) throws InvalidInstructionException;


}