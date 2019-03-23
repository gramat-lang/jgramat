package org.bakasoft.gramat.elements;

import org.bakasoft.gramat.Grammar;
import org.bakasoft.gramat.Tape;
import org.bakasoft.gramat.parsing.NegationData;

import java.util.Set;

public class Negation extends Element {

    private Element element;

    public Negation(Element element) {
        this.element = element;
    }

    public Negation(Grammar grammar, NegationData data) {
        grammar.addElement(data, this);

        this.element = grammar.settle(data.getExpression());
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
            if (element instanceof Negation) {
                control.apply("remove double negation", this, ((Negation) element).element);
            }
            else {
                control.next(element);
            }
        });
    }

    @Override
    public boolean parse(Tape tape) {
        int pos0 = tape.getPosition();

        if (element.parse(tape)) {
            // did not match!
            tape.setPosition(pos0);
            return tape.no(this);
        }

        // perfect match!
        tape.moveForward();
        return tape.ok(this);
    }

    @Override
    public Object capture(Tape tape) {
        return captureText(tape);
    }

    @Override
    public void codify(CodifyControl control, boolean grouped) {
        control.codify(this, grouped, output -> {
            output.append("!");
            element.codify(control, false);
        });
    }

    @Override
    public void collectFirstAllowedSymbol(CyclicControl control, Set<String> symbols) {
        // no symbols to collect
    }

}
