package org.gramat.elements;

import java.util.Map;
import java.util.Set;

public class ListElement extends Element {

    private final Class<?> type;
    private Element element;

    public ListElement(Class<?> type, Element element) {
        this.type = type;
        this.element = element;
    }

    @Override
    protected boolean parseImpl(Context ctx) {
        ctx.builder.openList(type);

        if (element.parse(ctx)) {
            ctx.builder.pushList();
            return true;
        }

        return false;
    }

    @Override
    public boolean isOptional(Set<Element> control) {
        return control.add(element) && element.isOptional(control);
    }

    @Override
    public void collectFirstAllowedSymbol(Set<Element> control, Set<String> symbols) {
        if (control.add(element)) {
            element.collectFirstAllowedSymbol(control, symbols);
        }
    }

    @Override
    public void resolveInto(Map<String, Element> rules, Set<Element> control) {
        if (control.add(this)) {
            element = resolveInto(rules, control, element);
        }
    }
}
