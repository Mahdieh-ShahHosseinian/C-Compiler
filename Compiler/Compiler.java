package Compiler;

import Parser.Parser;
import Scanner.LexicalScanner;
import javafx.scene.control.TreeItem;

import java.io.File;

public class Compiler {

    private final File file = new File(System.getProperty("user.home") + "\\Documents\\input.txt");
    private final LexicalScanner lScanner = new LexicalScanner();
    private final Parser parser = new Parser();

    public Compiler() {
    }

    public void compile() {
        lScanner.scan(file);
        parser.pars(lScanner.getSymbolTable());
    }

    public TreeItem<String> getParserTreeRoot() {
        return parser.getTreeRoot();
    }
}