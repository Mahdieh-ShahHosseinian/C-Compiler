package Scanner;

public class Symbol {

    public Token token;
    public String lexeme;

    public Symbol(Token token, String lexeme) {
        this.token = token;
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        return "token=" + token +
                ", lexeme='" + lexeme + '\'';
    }
}