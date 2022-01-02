package su.nexmedia.engine.utils.evaluation.javaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A String tokenizer that accepts delimiters that are greater than one
 * character.
 *
 * @author Jean-Marc Astesana
 * @see <a href="../../../license.html">License information</a>
 */
public class Tokenizer {

    protected final Map<String, Function>       functions;
    protected final Map<String, List<Operator>> operators;
    protected final Map<String, Constant>       constants;
    protected final String                      functionArgumentSeparator;
    protected final Map<String, BracketPair>    functionBrackets;
    protected final Map<String, BracketPair>    expressionBrackets;
    private Pattern pattern;
    // private String tokenDelimiters;
    private boolean trimTokens;

    /**
     * Constructor. <br>
     * By default, this tokenizer trims all the tokens.
     *
     * @param delimiters                the delimiters of the tokenizer, usually,
     *                                  the operators symbols, the brackets and the
     *                                  function argument separator are used as
     *                                  delimiter in the string.
     * @param functions
     * @param expressionBrackets
     * @param operators
     * @param functionBrackets
     * @param constants
     * @param functionArgumentSeparator
     */
    public Tokenizer(List<String> delimiters, Map<String, Function> functions, Map<String, List<Operator>> operators, Map<String, Constant> constants, String functionArgumentSeparator, Map<String, BracketPair> functionBrackets, Map<String, BracketPair> expressionBrackets) {
        this.pattern = delimitersToRegexp(delimiters);
        trimTokens = true;
        this.functions = functions;
        this.operators = operators;
        this.constants = constants;
        this.functionArgumentSeparator = functionArgumentSeparator;
        this.functionBrackets = functionBrackets;
        this.expressionBrackets = expressionBrackets;
    }

    protected static Pattern delimitersToRegexp(List<String> delimiters) {
        // First, create a regular expression that match the union of the delimiters
        // Be aware that, in case of delimiters containing others (example && and &),
        // the longer may be before the shorter (&& should be before &) or the regexpr
        // parser will recognize && as two &.
        Collections.sort(delimiters, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return -o1.compareTo(o2);
            }
        });
        // Build a string that will contain the regular expression
        StringBuilder result = new StringBuilder();
        result.append('(');
        for (String delim : delimiters) {
            // For each delimiter
            if (result.length() != 1) {
                // Add it to the union
                result.append('|');
            }
            // Quote the delimiter as it could contain some regexpr reserved characters
            result.append("\\Q").append(delim).append("\\E");
        }
        result.append(')');
        return Pattern.compile(result.toString());
    }

    /**
     * Tests whether this tokens trims the tokens returned by
     * {@link #tokenize(String)} method.
     *
     * @return true if tokens are trimmed.
     */
    public boolean isTrimTokens() {
        return trimTokens;
    }

    /**
     * Sets the trimTokens attribute.
     *
     * @param trimTokens true to have the tokens returned by
     *                   {@link #tokenize(String)} method trimmed. <br>
     *                   Note that empty tokens are always omitted by this class.
     */
    public void setTrimTokens(boolean trimTokens) {
        this.trimTokens = trimTokens;
    }

    /*
     * private void addToTokens (List<Token> tokens, String token) { token =
     * token.trim(); if (!token.isEmpty()) { Token previous = tokens.isEmpty() ?
     * null : tokens.get(tokens.size()-1); Token theToken = toToken(previous,token);
     * } }
     */

    /**
     * Converts a string into tokens. <br>
     * Example: The result for the expression "<i>-1+min(10,3)</i>" evaluated for a
     * DoubleEvaluator is an iterator on "-", "1", "+", "min", "(", "10", ",", "3",
     * ")".
     *
     * @param string The string to be split into tokens
     * @return The tokens
     */
    public Collection<Token> tokenize(String string) {
        List<Token> res = new ArrayList<>();
        String lines[] = string.split("\\r?\\n");
        Token previous = null;
        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum];
            Matcher m = pattern.matcher(lines[lineNum]);
            int pos = 0;
            Token token;
            while (m.find()) {
                // While there's a delimiter in the string
                if (pos != m.start()) {
                    // If there's something between the current and the previous delimiter
                    // Add to the tokens list
                    token = toToken(previous, string.substring(pos, m.start()).trim());
                    if (token != null) {
                        token.setLineInfo(line, lineNum, pos, m.start() - 1);
                        res.add(token);
                        previous = token;
                    }
                }
                token = toToken(previous, m.group().trim());
                if (token != null) {
                    token.setLineInfo(line, lineNum, m.start(), m.end() - 1);
                    res.add(token);
                    previous = token;
                }
                pos = m.end(); // Remember end of delimiter
            }
            if (pos != string.length()) {
                // If it remains some characters in the string after last delimiter
                token = toToken(previous, string.substring(pos).trim());
                if (token != null) {
                    token.setLineInfo(line, lineNum, pos, string.length());
                    res.add(token);
                    previous = token;
                }
            }
        }
        // Return the result
        return res;
    }

    public Token toToken(Token previous, String strToken) {
        strToken = strToken.trim();
        if (strToken.isEmpty()) {
            return null;
        }
        if (strToken.equals(functionArgumentSeparator)) {
            return Token.buildArgumentSeparator(strToken);
        }
        else if (functions.containsKey(strToken)) {
            return Token.buildFunction(functions.get(strToken));
        }
        else if (operators.containsKey(strToken)) {
            List<Operator> list = operators.get(strToken);
            return (list.size() == 1) ? Token.buildOperator(list.get(0)) : Token.buildOperator(guessOperator(previous, list));
        }
        else {
            final BracketPair brackets = getBracketPair(strToken);
            if (brackets != null) {
                if (brackets.getOpen().equals(strToken)) {
                    return Token.buildOpenToken(brackets);
                }
                else {
                    return Token.buildCloseToken(brackets);
                }
            }
            else {
                return Token.buildLiteral(strToken);
            }
        }
    }

    protected BracketPair getBracketPair(String token) {
        BracketPair result = expressionBrackets.get(token);
        return result == null ? functionBrackets.get(token) : result;
    }

    /**
     * When a token can be more than one operator (homonym operators), this method
     * guesses the right operator. <br>
     * A very common case is the - sign in arithmetic computation which can be an
     * unary or a binary operator, depending on what was the previous token. <br>
     * <b>Warning:</b> maybe the arguments of this function are not enough to deal
     * with all the cases. So, this part of the evaluation is in alpha state (method
     * may change in the future).
     *
     * @param previous   The last parsed tokens (the previous token in the infix
     *                   expression we are evaluating).
     * @param candidates The candidate tokens.
     * @return A token
     * @see #validateHomonyms(List)
     */
    protected Operator guessOperator(Token previous, List<Operator> candidates) {
        final int argCount = ((previous != null) && (previous.isCloseBracket() || previous.isLiteral())) ? 2 : 1;
        for (Operator operator : candidates) {
            if (operator.getOperandCount() == argCount) {
                return operator;
            }
        }
        return null;
    }

}
