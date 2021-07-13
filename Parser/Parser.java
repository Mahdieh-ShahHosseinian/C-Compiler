package Parser;

import Scanner.*;
import javafx.scene.control.TreeItem;

public class Parser {

    private SymbolTable symbolTable;
    private TreeItem<String> root;

    public void pars(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        program();
    }

    //    Program → int|void main ( ) Compound-stmt
    private void program() {

        root = new TreeItem<>("Program");
        root.setExpanded(true);

        try {
            if ((symbolTable.next().token.equals(Token.KEYWORD) && (symbolTable.current().lexeme.equals("int") || symbolTable.current().lexeme.equals("void")))) {

                root.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                if ((symbolTable.next().token.equals(Token.IDENTIFIER) && symbolTable.current().lexeme.equals("main")) &&
                        (symbolTable.next().token.equals(Token.SEPARATOR) && symbolTable.current().lexeme.equals("(")) &&
                        (symbolTable.next().token.equals(Token.SEPARATOR) && symbolTable.current().lexeme.equals(")"))) {

                    root.getChildren().add(new TreeItem<>("main"));
                    root.getChildren().add(new TreeItem<>("("));
                    root.getChildren().add(new TreeItem<>(")"));
                    compoundStmt(root);
                } else {
                    throw new ParserException("invalid");
                }
            } else {
                throw new ParserException("invalid");
            }
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }

    //    Compound-stmt → { InnerCompound-stmt }
    private void compoundStmt(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("CompoundStmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        if (symbolTable.next().lexeme.equals("{")) {
            treeItem.getChildren().add(new TreeItem<>("{"));

            innerCompoundStmt(treeItem);
            if (!(symbolTable.next().lexeme.equals("}"))) {
                throw new ParserException(" } expected");
            } else {
                treeItem.getChildren().add(new TreeItem<>("}"));
            }
        } else {
            throw new ParserException("{ expected");
        }
    }

    //    InnerCompound-stmt → Variable  statements
    private void innerCompoundStmt(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("InnerCompoundStmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        variable(treeItem);
        statements(treeItem);
    }

    //    Variable → type id S; Variable | ε
    private void variable(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("Variable");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        if (symbolTable.next().token.equals(Token.KEYWORD)) {
            switch (symbolTable.current().lexeme) {
                case "int", "float", "char" -> {
                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

                    if (symbolTable.next().token.equals(Token.IDENTIFIER)) {

                        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                        s(treeItem);
                        if (symbolTable.next().lexeme.equals(";")) {
                            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                            variable(treeItem);
                        } else {
                            throw new ParserException("; expected");
                        }
                    } else {
                        throw new ParserException("id expected");
                    }
                }
                default -> {
                    treeItem.getChildren().add(new TreeItem<>("ε"));
                    symbolTable.back();
                }
            }
        } else {
            treeItem.getChildren().add(new TreeItem<>("ε"));
            symbolTable.back();
        }
    }

    //    S → , id S | = (Num | ID) Expression S | ε
    private void s(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("S");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        switch (symbolTable.next().lexeme) {
            case "," -> {
                treeItem.getChildren().add(new TreeItem<>(","));
                if (symbolTable.next().token.equals(Token.IDENTIFIER)) {
                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

                    s(treeItem);
                } else throw new ParserException("id expected");
            }
            case "=" -> {
                treeItem.getChildren().add(new TreeItem<>("="));
                switch (symbolTable.next().token) {
                    case NUMBER, IDENTIFIER -> {
                        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

                        expression(treeItem);
                        s(treeItem);
                    }
                    default -> throw new ParserException("id or number expected");
                }
            }
            default -> {
                treeItem.getChildren().add(new TreeItem<>("ε"));
                symbolTable.back();
            }
        }
    }

    //    Expression → Operator Operand | ε
    private void expression(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("Expression");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        if (symbolTable.next().token.equals(Token.OPERATOR)) {
            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
            switch (symbolTable.current().lexeme) {
                case "++", "--" -> { // TODO Unary operator
                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                    if (!symbolTable.next().lexeme.equals(";")) throw new ParserException("; expected");
                    else treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                }
                case "=", "+=", "-=", "*=", "/=", "%=" -> { // TODO Binary operation
                    symbolTable.back();
                    if (!symbolTable.current().token.equals(Token.IDENTIFIER)) throw new ParserException("left operand expected");
                    else treeItem.getChildren().add(new TreeItem<>(symbolTable.next().lexeme));
                    symbolTable.next();
                }
                default -> {
                    switch (symbolTable.next().token) {
                        case NUMBER, IDENTIFIER -> treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                        default -> throw new ParserException("id or number expected");
                    }
                }
            }
        } else {
            treeItem.getChildren().add(new TreeItem<>("ε"));
            symbolTable.back();
        }
    }

    //    statements → Stmt statements | ε
    private void statements(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("Statements");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        switch (symbolTable.next().token) {
            case IDENTIFIER, KEYWORD -> {
                stmt(treeItem);
                statements(treeItem);
            }
            default -> {
                treeItem.getChildren().add(new TreeItem<>("ε"));
                symbolTable.back();
            }
        }
    }

    //    Stmt → ID OPERATOR [+|-|ε (Num | ID)] Expression ; | InnerCompound-stmt | If-Stmt | selector-Stmt | until-stmt | loop-stmt | write-stmt | read-stmt
    private void stmt(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("Stmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        switch (symbolTable.current().token) {
            case IDENTIFIER -> {
                treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                if (symbolTable.next().token.equals(Token.OPERATOR)) {
                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                    switch (symbolTable.current().lexeme) {
                        case "++", "--" -> {
                            if (!symbolTable.next().lexeme.equals(";")) throw new ParserException("; expected");
                            else treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                        }
                        default -> {
                            switch (symbolTable.next().lexeme) {
                                case "+", "-":
                                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                                    symbolTable.next();
                                default: {
                                    symbolTable.back();
                                    switch (symbolTable.next().token) {
                                        case NUMBER, IDENTIFIER -> {
                                            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                                            expression(treeItem);
                                            if (!symbolTable.next().lexeme.equals(";"))
                                                throw new ParserException("; expected");
                                            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                                        }
                                        default -> throw new ParserException("id or number expected");
                                    }
                                }
                            }
                        }
                    }
                } else {
                    throw new ParserException("expression expected");
                }
            }
            case KEYWORD -> {
                switch (symbolTable.current().lexeme) {
                    case "int", "char", "float" -> {
                        symbolTable.back();
                        innerCompoundStmt(treeItem);
                    }
                    case "if" -> ifStmt(treeItem);
                    case "selector" -> selectorStmt(treeItem);
                    case "until" -> untilStmt(treeItem);
                    case "loop" -> loopStmt(treeItem);
                    case "write" -> write(treeItem);
                    case "read" -> read(treeItem);
                    default -> throw new ParserException("unexpected order");
                }
            }
            default -> {
                treeItem.getChildren().add(new TreeItem<>("ε"));
                symbolTable.back();
            }
        }
    }

    //    If-stmt → if ((Num | ID)  Expression) so Compound-stmt Else-stmt
    private void ifStmt(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("IfStmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

        if (symbolTable.next().lexeme.equals("(")) {
            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
            switch (symbolTable.next().token) {
                case NUMBER, IDENTIFIER -> {
                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                    expression(treeItem);
                    if (symbolTable.next().lexeme.equals(")")) {
                        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                        if (symbolTable.next().lexeme.equals("so")) {
                            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                            compoundStmt(treeItem);
                            elseStmt(treeItem);
                        } else {
                            throw new ParserException("wrong if");
                        }
                    } else {
                        throw new ParserException(") expected");
                    }
                }
                default -> throw new ParserException("expression expected");
            }
        } else {
            throw new ParserException("( expected");
        }
    }

    //    Else-stmt  else Compound-stmt | ε
    private void elseStmt(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("ElseStmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        if (symbolTable.next().lexeme.equals("else")) {
            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
            compoundStmt(treeItem);
        } else {
            treeItem.getChildren().add(new TreeItem<>("ε"));
            symbolTable.back();
        }
    }

    //    Selector-Stmt  selector : ID { selectStmt otherStmt}
    private void selectorStmt(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("SelectorStmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

        if (symbolTable.next().lexeme.equals(":")) {
            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

            if (symbolTable.next().token.equals(Token.IDENTIFIER)) {
                treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

                if (symbolTable.next().lexeme.equals("{")) {
                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

                    selectStmt(treeItem);
                    otherStmt(treeItem);
                    if (!(symbolTable.next().lexeme.equals("}"))) {
                        throw new ParserException(" } expected");
                    } else {
                        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                    }
                }
            } else {
                throw new ParserException("id expected");
            }
        } else {
            throw new ParserException(": expected");
        }
    }

    //    selectStmt  select NUM : compoundStmt selectStmt | ε
    private void selectStmt(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("SelectStmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        if (symbolTable.next().lexeme.equals("select")) {
            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

            if (symbolTable.next().token.equals(Token.NUMBER)) {
                treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

                if (symbolTable.next().lexeme.equals(":")) {
                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

                    compoundStmt(treeItem);
                    selectStmt(treeItem);
                } else {
                    throw new ParserException(": expected");
                }
            } else {
                throw new ParserException("number expected");
            }
        } else {
            treeItem.getChildren().add(new TreeItem<>("ε"));
            symbolTable.back();
        }
    }

    //    otherStmt  other : compoundStmt | ε
    private void otherStmt(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("OtherStmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        if (symbolTable.next().lexeme.equals("other")) {
            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

            if (symbolTable.next().lexeme.equals(":")) {
                treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                compoundStmt(treeItem);
            } else {
                throw new ParserException(": expected");
            }
        } else {
            treeItem.getChildren().add(new TreeItem<>("ε"));
            symbolTable.back();
        }
    }

    //    until-stmt → until (((Num | ID)   Expression ) Compound-stmt
    private void untilStmt(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("UntilStmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

        if (symbolTable.next().lexeme.equals("(")) {
            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
            switch (symbolTable.next().token) {
                case NUMBER, IDENTIFIER -> {
                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                    expression(treeItem);
                    if (symbolTable.next().lexeme.equals(")")) {
                        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

                        compoundStmt(treeItem);
                    } else {
                        throw new ParserException(") expected");
                    }
                }
                default -> throw new ParserException("expression expected");
            }
        } else {
            throw new ParserException("( expected");
        }
    }

    //    loopStmt  ( ID & (Num | ID)  expression & ID[(++ | --) | (+= ID)]) compound_stmt
    private void loopStmt(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("LoopStmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

        if (symbolTable.next().lexeme.equals("(")) {
            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
            if (symbolTable.next().token.equals(Token.IDENTIFIER)) {
                treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                if (symbolTable.next().lexeme.equals("&")) {
                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                    switch (symbolTable.next().token) {
                        case IDENTIFIER, NUMBER -> {
                            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                            expression(treeItem);
                            if (symbolTable.next().lexeme.equals("&")) {
                                treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                                if (symbolTable.next().token.equals(Token.IDENTIFIER)) {
                                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                                    switch (symbolTable.next().lexeme) {
                                        case "+=", "-=", "*=", "/=", "%=" -> {
                                            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                                            if (symbolTable.next().lexeme.equals(")")) {
                                                treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                                                switch (symbolTable.next().token) {
                                                    case IDENTIFIER, NUMBER -> {
                                                        compoundStmt(treeItem);
                                                        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                                                    }
                                                    default -> throw new ParserException("id expected");
                                                }
                                            } else {
                                                throw new ParserException(") expected");
                                            }
                                        }
                                        case "++", "--" -> {
                                            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                                            if (symbolTable.next().lexeme.equals(")")) {
                                                treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                                                compoundStmt(treeItem);
                                            } else {
                                                throw new ParserException(") expected");
                                            }
                                        }
                                        default -> throw new ParserException("invalid step");
                                    }
                                } else {
                                    throw new ParserException("id expected");
                                }
                            } else {
                                throw new ParserException("& expected");
                            }
                        }
                        default -> throw new ParserException("id or number expected");
                    }
                } else {
                    throw new ParserException("& expected");
                }
            } else {
                throw new ParserException("id expected");
            }
        } else {
            throw new ParserException("( expected");
        }
    }

    //    writeStmt  write (string stringValue);
    private void write(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("WriteStmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

        if (symbolTable.next().lexeme.equals("(")) {
            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
            if (symbolTable.next().token.equals(Token.STRING)) {
                treeItem.getChildren().add(new TreeItem<>((char) 39 + symbolTable.current().lexeme + (char) 39));
                stringValue(treeItem);
                if (symbolTable.next().lexeme.equals(")")) {
                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                    if (!symbolTable.next().lexeme.equals(";")) {
                        throw new ParserException("; expected");
                    } else {
                        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                    }
                } else {
                    throw new ParserException(") expected");
                }
            } else {
                throw new ParserException("String expected");
            }
        } else {
            throw new ParserException("( expected");
        }
    }

    //    stringValue  , id | ε
    private void stringValue(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("StringValueStmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        if (symbolTable.next().lexeme.equals(",")) {
            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
            if (!symbolTable.next().token.equals(Token.IDENTIFIER)) {
                throw new ParserException("id expected");
            } else {
                treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
            }
        } else {
            treeItem.getChildren().add(new TreeItem<>("ε"));
            symbolTable.back();
        }
    }

    //    readStmt  read (int|char|float , id);
    private void read(TreeItem<String> parent) throws ParserException {

        TreeItem<String> treeItem = new TreeItem<>("ReadStmt");
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);

        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));

        if (symbolTable.next().lexeme.equals("(")) {
            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
            switch (symbolTable.next().lexeme) {
                case "int", "float", "char" -> {
                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                    if (symbolTable.next().lexeme.equals(",")) {
                        treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                        if (symbolTable.next().token.equals(Token.IDENTIFIER)) {
                            treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                            if (symbolTable.next().lexeme.equals(")")) {
                                treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                                if (!symbolTable.next().lexeme.equals(";")) {
                                    throw new ParserException("; expected");
                                } else {
                                    treeItem.getChildren().add(new TreeItem<>(symbolTable.current().lexeme));
                                }
                            } else {
                                throw new ParserException(") expected");
                            }
                        } else {
                            throw new ParserException("id expected");
                        }
                    } else {
                        throw new ParserException(", expected");
                    }
                }
                default -> throw new ParserException("type expected");
            }
        } else {
            throw new ParserException("( expected");
        }
    }

    public TreeItem<String> getTreeRoot() {
        return root;
    }
}