package org.bakasoft.gramat.elements;

import org.bakasoft.gramat.Tape;

import java.util.Set;
import java.util.function.Function;

public class ValueElement extends Element implements WrappedElement {

    private final Function<String, ?> parser;
    private Element element;

    public ValueElement(Function<String, ?> parser, Element element) {
        this.element = element;
        this.parser = parser;
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
    public boolean parse(Tape tape) {
        return element.parse(tape);
    }

    @Override
    public Object capture(Tape tape) {
        String raw = element.captureText(tape);

        if (raw == null) {
            return null; // TODO improve how to handle null
        }

        return parser.apply(raw);
    }

    @Override
    public void codify(CodifyControl control, boolean grouped) {
        control.codify(this, grouped, output -> {

            output.append('=');
            element.codify(control, true);
            output.append(';');
        });
    }

    @Override
    public void collectFirstAllowedSymbol(CyclicControl control, Set<String> symbols) {
        control.enter(this, () -> {
            element.collectFirstAllowedSymbol(control, symbols);
        });
    }
}


