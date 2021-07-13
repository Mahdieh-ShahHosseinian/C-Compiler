package Scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class LexicalScanner {

    private final SymbolTable symbolTable = new SymbolTable();
    private int lineNumber = 1;
    private int pointer = 0;
    private int state = 0;

    //Read the input file line by line
    public void scan(File file) {

        try {

            Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)));

            //Just scan one line to find the number of error line
            while (scanner.hasNextLine()) {
                scanOneLine(scanner.nextLine() + "\0");
                lineNumber++;
            }

            symbolTable.set(Token.END_OF_FILE, "end");
            System.out.println(symbolTable);

        } catch (FileNotFoundException | LexicalException e) {
            e.printStackTrace();
        }
    }

    // reach to the end of file
    private boolean getEOL(String line) {

        if (line.charAt(pointer) == '\0') {
            pointer++;
            return true;
        }
        return false;
    }

    private Token fail() {
        return Token.FAIL;
    }

    /**
     * Scans just one line to find error in that line
     * Checks every Token
     */
    private void scanOneLine(String line) throws LexicalException {

        //Assign pointer = 0 to start from the beginning of the line
        pointer = 0;

        //A loop from start of the line to end of it
        while (pointer != line.length()) {

            //Assign state = 0 to start from beginning of states
            state = 0;

            if (!getEOL(line)) {

                if (getString(line) == fail()) {
                    if (getSeparator(line) == fail()) {
                        if (getOperation(line) == fail()) {
                            state = 9;
                            if (getID(line) == fail()) {
                                state = 12;
                                if (getNum(line) == fail()) {
                                    state = 22;
                                    if (getWhiteSpace(line) == fail()) {
                                        pointer++;
                                        throw new LexicalException("a lexical error in line" + lineNumber);
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    // Check if the input is a string or not
    private Token getString(String line) {

        StringBuilder string = new StringBuilder();
        if (line.charAt(pointer) == 39) {
            pointer++;
            while (line.charAt(pointer) != 39) {
                string.append(line.charAt(pointer++));
            }
            pointer++;
            symbolTable.set(Token.STRING, string.toString());
            return Token.STRING;
        }
        return fail();
    }

    // Check if the input is a separator or not
    private Token getSeparator(String line) throws LexicalException {

        if (pointer >= line.length()) {
            throw new LexicalException("a lexical error in line" + lineNumber);
        } else if (line.charAt(pointer) == '(' || line.charAt(pointer) == ')' ||
                line.charAt(pointer) == '{' || line.charAt(pointer) == '}' ||
                line.charAt(pointer) == ':' || line.charAt(pointer) == 39 ||
                line.charAt(pointer) == ',' || line.charAt(pointer) == ';' ||
                line.charAt(pointer) == '&') {
            symbolTable.set(Token.SEPARATOR, String.valueOf(line.charAt(pointer++)));
            return Token.SEPARATOR;
        }
        return fail();
    }

    /**
     * Check if the input is an operation or not
     * Calls getRelOp function
     */
    private Token getOperation(String line) throws LexicalException {

        if (pointer >= line.length()) {
            throw new LexicalException("a lexical error in line" + lineNumber);
        } else switch (line.charAt(pointer)) {

            case '+':
            case '-':
                if (line.charAt(pointer + 1) == line.charAt(pointer)) {
                    symbolTable.set(Token.OPERATOR, line.charAt(pointer) + String.valueOf(line.charAt(pointer)));
                    pointer += 2;
                    return Token.OPERATOR;
                } else if (line.charAt(pointer + 1) != '=') {
                    symbolTable.set(Token.OPERATOR, String.valueOf(line.charAt(pointer)));
                    pointer++;
                    return Token.OPERATOR;
                }
            case '*':
            case '/':
            case '%':
                if (line.charAt(pointer + 1) == '=') {
                    symbolTable.set(Token.OPERATOR, line.charAt(pointer) + "=");
                    pointer += 2;
                } else {
                    symbolTable.set(Token.OPERATOR, String.valueOf(line.charAt(pointer)));
                    pointer++;
                }
                return Token.OPERATOR;
        }
        return getRelOp(line);
    }

    // Check if the input is a relation operation or not
    private Token getRelOp(String line) throws LexicalException {

        StringBuilder relOp = new StringBuilder();

        while (true) {

            if (pointer >= line.length()) {
                throw new LexicalException("a lexical error in line" + lineNumber);
            } else switch (state) {
                case 0:
                    if (line.charAt(pointer) == '<') {
                        state = 1;
                        relOp.append(line.charAt(pointer));
                        pointer++;
                    } else if (line.charAt(pointer) == '>') {
                        state = 6;
                        relOp.append(line.charAt(pointer));
                        pointer++;
                    } else if (line.charAt(pointer) == '=') {
                        state = 5;
                        relOp.append(line.charAt(pointer));
                        pointer++;
                    } else return fail();
                    break;
                case 1:
                    if (line.charAt(pointer) == '=') {
                        state = 2;
                        relOp.append(line.charAt(pointer));
                        pointer++;
                    } else if (line.charAt(pointer) == '>') {
                        state = 3;
                        relOp.append(line.charAt(pointer));
                        pointer++;
                    } else state = 4;
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                case 7:
                case 8:
                    symbolTable.set(Token.OPERATOR, relOp.toString());
                    return Token.OPERATOR;
                case 6:
                    if (line.charAt(pointer) == '=') {
                        state = 7;
                        relOp.append(line.charAt(pointer));
                        pointer++;
                    } else state = 8;
            }
        }
    }

    // Check if the input is an ID or not
    private Token getID(String line) {

        StringBuilder keyWord = new StringBuilder();

        //To find keywords
        for (int i = pointer; ; i++) {
            if (((line.charAt(i) >= 'a' && line.charAt(i) <= 'z') ||
                    (line.charAt(i) >= 'A' && line.charAt(i) <= 'Z'))) {
                keyWord.append(line.charAt(i));
            } else break;
        }

        switch (keyWord.toString()) {
            case "write" -> {
                symbolTable.set(Token.KEYWORD, "write");
                pointer += 5;
                return Token.KEYWORD;
            }
            case "read" -> {
                symbolTable.set(Token.KEYWORD, "read");
                pointer += 4;
                return Token.KEYWORD;
            }
            case "if" -> {
                symbolTable.set(Token.KEYWORD, "if");
                pointer += 2;
                return Token.KEYWORD;
            }
            case "so" -> {
                symbolTable.set(Token.KEYWORD, "so");
                pointer += 2;
                return Token.KEYWORD;
            }
            case "selector" -> {
                symbolTable.set(Token.KEYWORD, "selector");
                pointer += 8;
                return Token.KEYWORD;
            }
            case "loop" -> {
                symbolTable.set(Token.KEYWORD, "loop");
                pointer += 4;
                return Token.KEYWORD;
            }
            case "until" -> {
                symbolTable.set(Token.KEYWORD, "until");
                pointer += 5;
                return Token.KEYWORD;
            }
            case "int" -> {
                symbolTable.set(Token.KEYWORD, "int");
                pointer += 3;
                return Token.KEYWORD;
            }
            case "float" -> {
                symbolTable.set(Token.KEYWORD, "float");
                pointer += 5;
                return Token.KEYWORD;
            }
            case "char" -> {
                symbolTable.set(Token.KEYWORD, "char");
                pointer += 4;
                return Token.KEYWORD;
            }
            case "return" -> {
                symbolTable.set(Token.KEYWORD, "return");
                pointer += 6;
                return Token.KEYWORD;
            }
            case "void" -> {
                symbolTable.set(Token.KEYWORD, "void");
                pointer += 4;
                return Token.KEYWORD;
            }
            case "select" -> {
                symbolTable.set(Token.KEYWORD, "select");
                pointer += 6;
                return Token.KEYWORD;
            }
            case "other" -> {
                symbolTable.set(Token.KEYWORD, "other");
                pointer += 5;
                return Token.KEYWORD;
            }
            case "else" -> {
                symbolTable.set(Token.KEYWORD, "else");
                pointer += 4;
                return Token.KEYWORD;
            }
            default -> {
                StringBuilder id = new StringBuilder();
                while (true) {
                    switch (state) {
                        case 9:
                            if ((line.charAt(pointer) >= 'a' && line.charAt(pointer) <= 'z') ||
                                    (line.charAt(pointer) >= 'A' && line.charAt(pointer) <= 'Z') ||
                                    line.charAt(pointer) == '_' || line.charAt(pointer) == '$') {
                                id.append(line.charAt(pointer));
                                pointer++;
                                state = 10;
                            } else return fail();
                            break;
                        case 10:
                            if ((line.charAt(pointer) >= 'a' && line.charAt(pointer) <= 'z') ||
                                    (line.charAt(pointer) >= 'A' && line.charAt(pointer) <= 'Z') ||
                                    (line.charAt(pointer) >= '0' && line.charAt(pointer) <= '9') ||
                                    line.charAt(pointer) == '_' || line.charAt(pointer) == '$') {
                                id.append(line.charAt(pointer));
                                pointer++;
                            } else state = 11;
                            break;
                        case 11:
                            symbolTable.set(Token.IDENTIFIER, id.toString());
                            return Token.IDENTIFIER;
                    }
                }
            }
        }
    }

    // Check if the input is a number or not
    private Token getNum(String line) {

        StringBuilder num = new StringBuilder();

        while (true) {
            switch (state) {
                case 12:
                    if ((line.charAt(pointer) >= '0' && line.charAt(pointer) <= '9')) {
                        num.append(line.charAt(pointer));
                        state = 13;
                        pointer++;
                    } else return fail();
                    break;
                case 13:
                    if ((line.charAt(pointer) >= '0' && line.charAt(pointer) <= '9')) {
                        num.append(line.charAt(pointer));
                        pointer++;
                    } else if (line.charAt(pointer) == '.') {
                        num.append(line.charAt(pointer));
                        state = 14;
                        pointer++;
                    } else if (line.charAt(pointer) == 'E') {
                        num.append(line.charAt(pointer));
                        state = 16;
                        pointer++;
                    } else state = 20;
                    break;
                case 14:
                    if ((line.charAt(pointer) >= '0' && line.charAt(pointer) <= '9')) {
                        num.append(line.charAt(pointer));
                        state = 15;
                        pointer++;
                    } else return fail();
                    break;
                case 15:
                    if ((line.charAt(pointer) >= '0' && line.charAt(pointer) <= '9')) {
                        num.append(line.charAt(pointer));
                        pointer++;
                    } else if (line.charAt(pointer) == 'E') {
                        num.append(line.charAt(pointer));
                        state = 16;
                        pointer++;
                    } else state = 21;
                    break;
                case 16:
                    if ((line.charAt(pointer) >= '0' && line.charAt(pointer) <= '9')) {
                        num.append(line.charAt(pointer));
                        pointer++;
                        state = 18;
                    } else if (line.charAt(pointer) == '-' || line.charAt(pointer) == '+') {
                        num.append(line.charAt(pointer));
                        pointer++;
                        state = 17;
                    }
                    break;
                case 17:
                    if ((line.charAt(pointer) >= '0' && line.charAt(pointer) <= '0')) {
                        num.append(line.charAt(pointer));
                        pointer++;
                        state = 18;
                    }
                    break;
                case 18:
                    if ((line.charAt(pointer) >= '0' && line.charAt(pointer) <= '9')) {
                        num.append(line.charAt(pointer));
                        pointer++;
                    } else state = 19;
                    break;
                case 19:
                case 20:
                case 21:
                    symbolTable.set(Token.NUMBER, num.toString());
                    return Token.NUMBER;
            }
        }
    }

    //Remove any white space
    private Token getWhiteSpace(String line) {

        while (true) {
            switch (state) {
                case 22:
                    if (line.charAt(pointer) == '\t' || line.charAt(pointer) == ' ') {
                        state = 23;
                        pointer++;
                    } else return fail();
                    break;
                case 23:
                    if (line.charAt(pointer) == '\t' || line.charAt(pointer) == ' ') {
                        pointer++;
                    } else state = 24;
                    break;
                case 24:
                    return Token.WHITESPACE;
            }
        }
    }

    // getter
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}