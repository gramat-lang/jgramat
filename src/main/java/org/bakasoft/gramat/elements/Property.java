package org.bakasoft.gramat.elements;

import org.bakasoft.gramat.*;
import org.bakasoft.gramat.parsing.PropertyData;
import org.bakasoft.gramat.parsing.PropertyMode;

import java.util.Set;

public class Property extends Element implements WrappedElement {

    private final String propertyName;
    private final boolean appendMode;

    private Element element;

    public Property(String propertyName, boolean appendMode, Element element) {
        this.propertyName = propertyName;
        this.appendMode = appendMode;
        this.element = element;
    }

    public Property(Grammar grammar, PropertyData data) {
        grammar.addElement(data, this);

        this.propertyName = data.getName();
        this.element = grammar.settle(data.getExpression());

        if (data.getMode() == PropertyMode.SET) {
            appendMode = false;
        } else if (data.getMode() == PropertyMode.ADD) {
            appendMode = true;
        } else {
            throw new RuntimeException("missing property mode");
        }
    }

    @Override
    public void replace(CyclicControl control, Element older, Element newer) {
        control.enter(this, () -> {
            if (element == older) {
                element = newer;
            }
            else {
                element.replace(control, older, newer);
            }
        });
    }

    @Override
    public boolean isCyclic(CyclicControl control) {
        return control.isCyclic(element);
    }

    @Override
    public void optimize(OptimizationControl control) {
        control.enter(this, () -> {
            control.next(element);
        });
    }

    @Override
    public boolean parse(Tape tape) {
        int pos0 = tape.getPosition();
        Location loc0 = tape.getLocation();
        Object value = element.capture(tape);

        if (value == null) {
            // did not match!
            tape.setPosition(pos0);
            return tape.no(this);
        }

        ObjectHandle entity = tape.peekCapture();

        if (value instanceof GrammarElement) {
            ((GrammarElement) value).setBeginLocation(loc0);
            ((GrammarElement) value).setEndLocation(tape.getLocation());
        }

//        System.out.println(" SET " + propertyName + ": <" + Json.stringify(value, 2) + ">");

        if (appendMode) {
            entity.addValue(propertyName, value);
        }
        else {
            entity.setValue(propertyName, value);
        }

        // perfect match!
        return tape.ok(this);
    }

    @Override
    public Object capture(Tape tape) {
        return captureText(tape);
    }

    @Override
    public void codify(CodifyControl control, boolean grouped) {
        control.codify(this, grouped, output -> {
            output.append('<');
            output.append(propertyName);
            if (appendMode) {
                output.append('+');
            }
            output.append(':');
            element.codify(control, true);
            output.append('>');
        });
    }

    @Override
    public void collectFirstAllowedSymbol(CyclicControl control, Set<String> symbols) {
        control.enter(this, () -> {
            element.collectFirstAllowedSymbol(control, symbols);
        });
    }
}