package org.bakasoft.gramat.elements;

import org.bakasoft.gramat.Tape;

import java.util.Map;
import java.util.Set;

public class Nop extends Element {

    @Override
    public boolean parse(Tape tape) {
        return true;
    }

    @Override
    public Object capture(Tape tape) {
        return null;
    }

    @Override
    public Element link() {
        return this;
    }

    @Override
    public void collectFirstAllowedSymbol(CyclicControl control, Set<String> symbols) {

    }
}
