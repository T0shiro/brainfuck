package mcga.brainfuck.processing;

import mcga.brainfuck.Macro;
import mcga.brainfuck.Metrics;
import mcga.brainfuck.ProcedureStruct;
import mcga.brainfuck.exceptions.InvalidBitmapException;
import mcga.brainfuck.exceptions.InvalidCodeException;
import mcga.brainfuck.exceptions.InvalidInstructionException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains a bunch of methods used to parse the file containing the Brainf*ck code
 * and execute the code.
 *
 * @author Team Make Coding Great Again
 */
public abstract class Parser {
    public static final String PROC_PARAM_SEP = ";";
    public static final String CALL_PATTERN = "([^(]*)(?:\\(((?:.+" + PROC_PARAM_SEP + "?)*?)\\))?";
    static final int SQUARE_SIDE = 3;
    private static final String FILE_FORMAT = ".bmp";
    private static final String PROCEDURE = "@";
    private static final String FUNCTION = "§";
    private static final String MACRO = "$";
    private static final String COM = "#";
    private static final String EMPTY_INSTRUCTION = "000000";
    static Map<String, ProcedureStruct> procedureMap = new HashMap<>();
    private Map<String, Macro> macroMap = new HashMap<>();
    private InputStream stream;
    private String fileName;


    public Parser(String fileName) throws FileNotFoundException {
        this(new FileInputStream(fileName));
        this.fileName = fileName;

    }

    /**
     * In case a file is specified in the launching command, this constructor is called.
     *
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
     * Tests the first character to determine if the String is made of several short syntax instructions
     * or a single long syntax instruction.
     *
     * @param str String of command
     * @return true if it is a long syntax, false otherwise
     */
    private static boolean isLetter(String str) {
        char firstChar = str.charAt(0);
        return Character.isLetter(firstChar);
    }

    /**
     * Tests if the String is a macro declaration beginning with a '$'
     *
     * @param str String to test
     * @return true if it is a macro declaration, false otherwise
     */
    private static boolean isMacroDeclaration(String str) {
        return str.startsWith(MACRO);
    }

    /**
     * Tests if the String is the start of a comment
     *
     * @param str String to test
     * @return true if it is a '#', false otherwise
     */
    private static boolean isComments(String str) {
        return str.equals(COM);
    }

    private static boolean isProcedureDeclaration(String str) {
        return str.equals(PROCEDURE);
    }

    private static boolean isFunctionDeclaration(String str) {
        return str.equals(FUNCTION);
    }

    public static ProcedureStruct getProcedure(String key) {
        return procedureMap.get(key);
    }

    /**
     * Reads the file containing the Brainf*ck code. This method is called in each subclass, with some
     * additions depending on the subclass.
     *
     * @see Check#parseFile()
     * @see Interpreter#parseFile()
     */
    public void parseFile() throws InvalidCodeException {
        Metrics.setProgSize(0);
        if (fileName != null && fileName.endsWith(FILE_FORMAT)) {
            readBitmap();
        } else {
            readText();
        }

    }

    /**
     * Reads the bitmap image containing the Brainfuck code. This method is called in each subclass, with some
     * additions depending on the subclass.
     *
     * @see Check#readBitmap()
     * @see Interpreter#readBitmap()
     */
    private void readBitmap() throws InvalidCodeException {
        BufferedImage image;
        try {
            image = ImageIO.read(stream);
            int height = image.getHeight();
            int width = image.getWidth();
            if (height % SQUARE_SIDE != 0 || width % SQUARE_SIDE != 0) {
                throw new InvalidBitmapException();
            }
            String prevColor;
            String hexColor = "";
            for (int i = 0; i < height; i += SQUARE_SIDE) {
                for (int j = 0; j < width; j += SQUARE_SIDE) {
                    prevColor = colorToHex(new Color(image.getRGB(j, i))); // hexadecimal code of the color of the upper left pixel of the square
                    for (int iSquare = 0; iSquare < SQUARE_SIDE; iSquare++) {
                        for (int jSquare = 0; jSquare < SQUARE_SIDE; jSquare++) {
                            Color imgColor = new Color(image.getRGB(jSquare + j, iSquare + i));
                            hexColor = colorToHex(imgColor);
                            if (!prevColor.equals(hexColor)) {
                                throw new InvalidBitmapException();
                            }
                        }
                    }
                    if (!hexColor.equals(EMPTY_INSTRUCTION)) {
                        execute(hexColor);
                        Metrics.incrProgSize();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readText() throws InvalidCodeException {
        Scanner scanner = new Scanner(this.stream);
        scanFile(scanner);
    }

    protected void readText(String str) throws InvalidCodeException {
        Scanner scanner = new Scanner(str);
        scanFile(scanner);
    }

    private void scanFile(Scanner scanner) throws InvalidCodeException {
        String str;
        scanner.useDelimiter("\\s*");
        while (scanner.hasNext()) {
            str = scanner.next();
            if (isLetter(str)) {
                str += scanner.nextLine();
                str = str.replaceAll("\\s*#.*", "");
                Pattern pattern = Pattern.compile(CALL_PATTERN);
                Matcher matcher = pattern.matcher(str);
                if (matcher.matches()) {
                    Macro macro;
                    String macroName = matcher.group(1);
                    if ((macro = macroMap.get(macroName)) != null) {
                        str = macro.callMacro(macroName, matcher.group(2));
                        readText(str);
                    } else {
                        Metrics.incrProgSize();
                        execute(str);
                    }
                }
            } else if (isComments(str)) {
                scanner.nextLine();
            } else if (isMacroDeclaration(str)) {
                declaration(scanner, (name, code, params) -> macroMap.put(name, new Macro(name, code, params)));
            } else if (isProcedureDeclaration(str)) {
                declaration(scanner, declareFunction(false));
            } else if (isFunctionDeclaration(str)) {
                declaration(scanner, declareFunction(true));
            } else {
                Metrics.incrProgSize();
                execute(str);
            }
        }

    }

    public IDeclaration declareFunction(boolean function) {
        return new FunctionDeclaration(function);
    }

    private void declaration(Scanner scanner, IDeclaration declaration) throws InvalidCodeException {
        String[] tab;
        String code;
        String name;
        tab = scanner.nextLine().split("=");
        Pattern pat = Pattern.compile(CALL_PATTERN);
        Matcher matcher = pat.matcher(tab[0]);
        code = tab[1];
        String params[] = {};
        if (matcher.find()) {
            String paramsString = matcher.group(2);
            if (paramsString != null) {
                params = paramsString.split(PROC_PARAM_SEP);
            }
            name = matcher.group(1);
        } else {
            name = tab[0];
        }
        if (!macroMap.containsKey(name) && !procedureMap.containsKey(name)) {
            declaration.action(name, code, params);
        } else {
            throw new InvalidCodeException(name + " is already defined");
        }
    }

    /**
     * Converts a Color object to its hexadecimal value.
     *
     * @param color Color to convert.
     * @return String corresponding hexadecimal value
     */
    private String colorToHex(Color color) {
        return Integer.toHexString(color.getRGB()).substring(2);
    }

    /**
     * This method is overriden in all subclasses.
     *
     * @param str string value of the argument to interpret
     * @throws InvalidInstructionException instructions invalid
     * @see Check#execute(String)
     * @see Interpreter#execute(String)
     * @see Rewrite#execute(String)
     * @see Translate#execute(String)
     */
    public abstract void execute(String str) throws InvalidCodeException;

}
