package org.bakasoft.gramat.parsing;

import org.bakasoft.gramat.CharPredicate;
import org.bakasoft.gramat.Gramat;
import org.bakasoft.gramat.GrammarException;
import org.bakasoft.gramat.Tape;
import org.bakasoft.gramat.elements.Element;
import org.bakasoft.gramat.parsing.elements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

abstract public class GElement {

    abstract public GElement simplify();

    abstract public Element compile(Gramat gramat, Map<String, Element> compiled);

    abstract public boolean isPlain(Gramat gramat);

    abstract public boolean isOptional(Gramat gramat);

    public String stringify() {
        return GStringifier.stringify(this);
    }

    public static Element[] compileAll(GElement[] source, Gramat gramat, Map<String, Element> compiled) {
        return Arrays.stream(source)
                .map(item -> item.compile(gramat, compiled))
                .toArray(Element[]::new);
    }

    public static boolean areAllPlain(GElement[] expressions, Gramat gramat) {
        return Arrays.stream(expressions).allMatch(item -> item.isPlain(gramat));
    }


    public static GElement[] toArray(List<? extends GElement> expressions) {
        return expressions.toArray(new GElement[0]);
    }

    // parsing

    public static GElement expectExpression(Tape tape) {
        ArrayList<GElement> expressions = new ArrayList<>();
        ArrayList<GElement> buffer = new ArrayList<>();
        Runnable flushBuffer = () -> {
            if (buffer.isEmpty()) {
                throw new RuntimeException();
            }
            else if (buffer.size() == 1) {
                expressions.add(buffer.get(0));
            }
            else {
                expressions.add(new GSequence(toArray(buffer)));
            }

            buffer.clear();
        };

        GElement unit;
        boolean createAlternation = false;

        while ((unit = tryExpressionUnit(tape)) != null) {
            if (createAlternation) {
                createAlternation = false;

                flushBuffer.run();
            }

            buffer.add(unit);

            skipVoid(tape);

            if (trySymbol(tape, '|')) {
                skipVoid(tape);

                createAlternation = true;
            }
        }

        if (createAlternation) {
            throw new GrammarException("Expected expression after " + inspect('|'), tape.getLocation());
        }

        flushBuffer.run();

        if (expressions.isEmpty()) {
            throw new GrammarException("Invalid expression", tape.getLocation());
        }
        else if (expressions.size() == 1) {
            return expressions.get(0);
        }

        return new GAlternation(toArray(expressions));
    }

    public static GElement expectExpressionUnit(Tape tape) {
        GElement unit = tryExpressionUnit(tape);

        if (unit == null) {
            throw new GrammarException("Expected expression", tape.getLocation());
        }

        return unit;
    }

    public static GElement tryExpressionUnit(Tape tape) {
        int pos0 = tape.getPosition();

        // check for statements beginning
        if (tryStatementBeginning(tape)) {
            tape.setPosition(pos0);
            return null;
        }
        else if (isLetter(tape)) {
            String name = expectName(tape, "reference or type name");

            skipVoid(tape);

            // check for object
            if (trySymbol(tape, ':')) {
                skipVoid(tape);

                return new GObject(name, expectExpressionUnit(tape));
            }
            // otherwise should be a reference
            else {
                return new GReference(name);
            }
        }
        else if (isChar(tape, '<')) {
            return GProperty.expectProperty(tape);
        }
        else if (isChar(tape, '{')) {
            return GRepetition.expectRepetition(tape);
        }
        else if (isChar(tape, '[')) {
            return GOptional.expectOptional(tape);
        }
        else if (isChar(tape, '!')) {
            return GNegation.expectNegation(tape);
        }
        else if (isChar(tape, '(')) {
            return expectGroup(tape);
        }
        else if (isChar(tape, '"')) {
            return GString.expectString(tape);
        }
        else if (trySymbol(tape, '$')) {
            return new GTerminator();
        }

        // unknown char
        tape.setPosition(pos0);
        return null;
    }

    public static GElement expectGroup(Tape tape) {
        expectSymbol(tape, '(');

        skipVoid(tape);

        GElement expression = expectExpression(tape);

        skipVoid(tape);

        expectSymbol(tape, ')');

        return expression;
    }

    public static boolean trySymbol(Tape tape, char symbol) {
        int pos0 = tape.getPosition();

        if (!tape.alive()) {
            tape.setPosition(pos0);
            return false;
        }

        char actual = tape.peek();

        if (symbol != actual) {
            tape.setPosition(pos0);
            return false;
        }

        tape.moveForward();
        return true;
    }

    public static void expectSymbol(Tape tape, char c) {
        if (!trySymbol(tape, c)) {
            throw new GrammarException("Expected character " + inspect(c), tape.getLocation());
        }
    }

    public static boolean trySymbols(Tape tape, String symbol) {
        int pos0 = tape.getPosition();
        int index = 0;

        while (index < symbol.length()) {
            char expected = symbol.charAt(index);

            if (tape.alive()) {
                char actual = tape.peek();
                if (expected == actual) {
                    index++;
                    tape.moveForward();
                }
                else {
                    tape.setPosition(pos0);
                    return false;
                }
            }
            else {
                tape.setPosition(pos0);
                return false;
            }
        }

        return true;
    }

    public static String expectName(Tape tape, String description) {
        String name = tryName(tape);

        if (name == null) {
            throw new GrammarException("Expected " + description, tape.getLocation());
        }

        return name;
    }

    public static String tryName(Tape tape) {
        int pos0 = tape.getPosition();
        if (isLetter(tape)) {
            StringBuilder name = new StringBuilder();

            while (isLetter(tape) || isDigit(tape)) {
                name.append(tape.peek());
                tape.moveForward();
            }

            return name.toString();
        }

        tape.setPosition(pos0);
        return null;
    }

    public static Integer expectInteger(Tape tape) {
        Integer value = tryInteger(tape);

        if (value == null) {
            throw new GrammarException("Expected integer", tape.getLocation());
        }

        return value;
    }

    public static Integer tryInteger(Tape tape) {
        StringBuilder digits = new StringBuilder();

        while (isDigit(tape)) {
            digits.append(tape.peek());
            tape.moveForward();
        }

        if (digits.length() == 0) {
            return null;
        }

        return Integer.parseInt(digits.toString());
    }

    public static void skipVoid(Tape tape) {
        while (isWhitespace(tape)) {
            tape.moveForward();
        }
    }

    public static boolean tryStatementBeginning(Tape tape) {
        int pos0 = tape.getPosition();
        boolean result;

        if (tryName(tape) != null) {
            skipVoid(tape);

            // if there is a name followed by assignment symbols, it's a rule!
            result = trySymbols(tape, ":=") || trySymbol(tape, '=');
        }
        else if (trySymbol(tape, '@')) {
            // if there is an @ followed by a name, it's a directive!
            result = tryName(tape) != null;
        }
        else {
            skipVoid(tape);

            result = !tape.alive();
        }

        tape.setPosition(pos0);
        return result;
    }

    public static boolean isLetter(Tape tape) {
        return is(tape, c -> c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == '-');
    }

    public static boolean isDigit(Tape tape) {
        return is(tape, c -> c >= '0' && c <= '9');
    }

    public static boolean isWhitespace(Tape tape) {
        return is(tape, c -> c == ' ' || c == '\t' || c == '\r' || c == '\n');
    }

    public static boolean isChar(Tape tape, char expected) {
        if (tape.alive()) {
            char actual = tape.peek();

            return actual == expected;
        }

        return false;
    }

    public static boolean is(Tape tape, CharPredicate predicate) {
        if (tape.alive()) {
            char c = tape.peek();

            return predicate.test(c);
        }

        return false;
    }

    public static String inspect(Object obj) {
        if (obj instanceof Character) {
            // TODO escape string
            return "'" + obj + "'";
        }

        return obj.toString();
    }

    public static String expectQuotedToken(Tape tape) {
        String literal = tryQuotedToken(tape);

        if (literal == null) {
            throw new RuntimeException("Expected string literal");
        }

        return literal;
    }

    public static String tryQuotedToken(Tape tape) {
        int pos0 = tape.getPosition();
        StringBuilder content = new StringBuilder();

        if (!trySymbol(tape, '\"')) {
            tape.setPosition(pos0);
            return null;
        }

        while (!isChar(tape, '\"')) {
            char c = tape.peek();
            tape.moveForward();

            if (c == '\\') {
                String escaped = readEscaped(tape);

                content.append(escaped);
            }
            else {
                // TODO allow only accepted characters
                content.append(c);
            }
        }

        if (!trySymbol(tape, '\"')) {
            tape.setPosition(pos0);
            return null;
        }

        return content.toString();
    }

    private static String readEscaped(Tape tape) {
        char c = tape.peek();
        tape.moveForward();

        switch (c) {
            case '"':
            case '\\': return "\\";
            case '/': return "/";
            case 'b': return "\b";
            case 'f': return "\f";
            case 'n': return "\n";
            case 'r': return "\r";
            case 't': return "\t";
            default:
                throw new GrammarException("Invalid escape sequence: " + inspect(c), tape.getLocation());
        }
    }

}