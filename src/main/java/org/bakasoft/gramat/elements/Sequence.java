package org.bakasoft.gramat.elements;

import java.util.Set;

public class Sequence extends Element {

    private final Element[] elements;

    public Sequence(Element[] elements) {
        this.elements = elements;
    }

    @Override
    protected boolean parseImpl(Context ctx) {
        int pos0 = ctx.tape.getPosition();

        for (Element element : elements) {
            if (!element.parse(ctx)) {
                // did not match!
                ctx.tape.setPosition(pos0);
                return false;
            }
        }

        // perfect match!
        return true;
    }

    @Override
    public boolean isOptional(Set<Element> control) {
        for (Element element : elements) {
            if (control.add(element) && !element.isOptional(control)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void collectFirstAllowedSymbol(Set<Element> control, Set<String> symbols) {
        for (Element element : elements) {
            if (control.add(element)) {
                element.collectFirstAllowedSymbol(control, symbols);
            }

            if (!element.isOptional()) {
                break;
            }
        }
    }

    @Override
    public Element link() {
        return new Sequence(linkAll(elements));
    }

}
