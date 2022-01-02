package su.nexmedia.engine.utils.evaluation.javaluator;

import java.util.Collections;

/**
 * A token. <br>
 * When evaluating an expression, it is first split into tokens. These tokens
 * can be operators, constants, etc ...
 *
 * @author Jean-Marc Astesana
 * @see <a href="../../../license.html">License information</a>
 */
public class Token {

    static final Token FUNCTION_ARG_SEPARATOR = new Token(Kind.FUNCTION_SEPARATOR, null);
    private static final int LINE_CHARS_TO_SHOW = 30;
    private Kind   kind;
    private Object content;

    // For better error messages:
    private String line;
    private int    lineNum;
    private int    startPosition;
    // private int endPosition;

    protected Token(Kind kind, Object content) {
        super();
        if ((kind.equals(Kind.OPERATOR) && !(content instanceof Operator)) || (kind.equals(Kind.FUNCTION) && !(content instanceof Function)) || (kind.equals(Kind.LITERAL) && !(content instanceof String))) {
            throw new IllegalArgumentException();
        }
        this.kind = kind;
        this.content = content;
    }

    public static Token buildLiteral(String literal) {
        return new Token(Kind.LITERAL, literal);
    }

    public static Token buildOperator(Operator ope) {
        return new Token(Kind.OPERATOR, ope);
    }

    public static Token buildFunction(Function function) {
        return new Token(Kind.FUNCTION, function);
    }

    public static Token buildOpenToken(BracketPair pair) {
        return new Token(Kind.OPEN_BRACKET, pair);
    }

    public static Token buildCloseToken(BracketPair pair) {
        return new Token(Kind.CLOSE_BRACKET, pair);
    }

    public static Token buildArgumentSeparator(String strToken) {
        return new Token(Kind.FUNCTION_SEPARATOR, strToken);
    }

    public String appendTokenInfo(String msg) {
        msg += "\n";
        if (line.length() > LINE_CHARS_TO_SHOW) {

        }
        else {
            msg += String.format("At position %s of line number %s\n", startPosition, lineNum);
            msg += "   " + line + "\n";
            msg += "   " + String.join("", Collections.nCopies(startPosition, " ")) + "^";
        }

        return msg + "\n";
    }

    public Token setLineInfo(String line, int lineNum, int startPosition, int endPosition) {
        this.line = line;
        this.lineNum = lineNum;
        this.startPosition = startPosition;
        // this.endPosition = endPosition;
        return this;
    }

    public BracketPair getBrackets() {
        return (BracketPair) this.content;
    }

    public Operator getOperator() {
        return (Operator) this.content;
    }

    public Function getFunction() {
        return (Function) this.content;
    }

    public Kind getKind() {
        return kind;
    }

    public String getString() {
        if (kind.equals(Kind.OPEN_BRACKET)) {
            return ((BracketPair) content).getOpen();
        }
        else if (kind.equals(Kind.CLOSE_BRACKET)) {
            return ((BracketPair) content).getClose();
        }
        return content.toString();
    }

    /**
     * Tests whether the token is an operator.
     *
     * @return true if the token is an operator
     */
    public boolean isOperator() {
        return kind.equals(Kind.OPERATOR);
    }

    /**
     * Tests whether the token is a function.
     *
     * @return true if the token is a function
     */
    public boolean isFunction() {
        return kind.equals(Kind.FUNCTION);
    }

    /**
     * Tests whether the token is an open bracket.
     *
     * @return true if the token is an open bracket
     */
    public boolean isOpenBracket() {
        return kind.equals(Kind.OPEN_BRACKET);
    }

    /**
     * Tests whether the token is a close bracket.
     *
     * @return true if the token is a close bracket
     */
    public boolean isCloseBracket() {
        return kind.equals(Kind.CLOSE_BRACKET);
    }

    /**
     * Tests whether the token is a function argument separator.
     *
     * @return true if the token is a function argument separator
     */
    public boolean isFunctionArgumentSeparator() {
        return kind.equals(Kind.FUNCTION_SEPARATOR);
    }

    /**
     * Tests whether the token is a literal or a constant or a variable name.
     *
     * @return true if the token is a literal, a constant or a variable name
     */
    public boolean isLiteral() {
        return kind.equals(Kind.LITERAL);
    }

    public Operator.Associativity getAssociativity() {
        return getOperator().getAssociativity();
    }

    public int getPrecedence() {
        return getOperator().getPrecedence();
    }

    public String getLiteral() {
        if (!this.kind.equals(Kind.LITERAL)) {
            throw new IllegalArgumentException();
        }
        return this.getString();
    }

    @Override
    public String toString() {
        return "Token [kind=" + kind + ", content=" + content + "]";
    }

    public enum Kind {
        OPEN_BRACKET, CLOSE_BRACKET, FUNCTION_SEPARATOR, FUNCTION, OPERATOR, LITERAL
    }

}
