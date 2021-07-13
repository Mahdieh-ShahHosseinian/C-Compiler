package Scanner;

import java.util.ArrayList;

public class SymbolTable {

    private final ArrayList<Symbol> symbols = new ArrayList<>();
    private int index = 0;

    public void set(Token token, String lexeme) {
        symbols.add(new Symbol(token, lexeme));
    }

    public Symbol next() {
        return symbols.get(index++);
    }

    public boolean hasNext() {
        return symbols.get(index).token != Token.END_OF_FILE;
    }

    public Symbol current() {
        return symbols.get(index - 1);
    }

    @Override
    public String toString() {
        return "SymbolTable{" +
                symbols +
                '}';
    }

    public void back() {
        index--;
    }
}