package org.bakasoft.gramat.parsing;

import org.bakasoft.gramat.Gramat;
import org.bakasoft.gramat.LocationRange;
import org.bakasoft.gramat.elements.Element;
import org.bakasoft.gramat.parsing.elements.GReference;
import org.bakasoft.gramat.parsing.util.GControl;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

abstract public class GExpression extends GElement {

  abstract public GExpression simplify();

  abstract public List<GExpression> getChildren();

  abstract public Element compile(Map<String, Element> rules);

  abstract public boolean isOptional_r(GControl control);

  abstract public void validate_r(GControl control);

  abstract public boolean hasWildProducers_r(GControl control);

  abstract public boolean hasWildMutations_r(GControl control);

  public GExpression(LocationRange location, Gramat gramat) {
    super(location, gramat);
  }

  public final boolean isOptional() {
    return isOptional_r(new GControl());
  }

  public boolean hasWildProducers() {
    return hasWildProducers_r(new GControl());
  }

  public boolean hasWildMutations() {
    return hasWildProducers_r(new GControl());
  }

  public int count(Predicate<GExpression> condition) {
    AtomicInteger count = new AtomicInteger(0);

    walk(e -> {
      if (condition.test(e)) {
        count.incrementAndGet();
      }
    });

    return count.get();
  }

  public void walk(Consumer<GExpression> action) {
    Stack<GExpression> stack = new Stack<>();

    stack.push(this);

    while (stack.size() > 0) {
      GExpression current = stack.peek();

      action.accept(current);

      if (current instanceof GReference) {
        GReference ref = (GReference)current;
        GExpression expression = gramat.findExpression(ref.ruleName);

        if (!stack.contains(expression)) {
          stack.push(expression);
        }
      }
      else {
        for (GExpression child : current.getChildren()) {
          if (!stack.contains(child)) {
            stack.push(child);
          }
        }
      }
    }
  }

  public String stringify() {
    return GStringifier.stringify(this);
  }

  public static Element[] compileAll(GExpression[] elements, Map<String, Element> rules) {
    return Arrays.stream(elements)
        .map(item -> item.compile(rules))
        .toArray(Element[]::new);
  }

  public static GExpression[] simplifyAll(GExpression[] elements) {
    ArrayList<GExpression> list = new ArrayList<>();

    for (GExpression element : elements) {
      if (element != null) {
        GExpression simplified = element.simplify();

        if (simplified != null) {
          list.add(simplified);
        }
      }
    }

    return list.toArray(new GExpression[0]);
  }

  public static void validate_r(GControl control, GExpression... expressions) {
    for (GExpression expression : expressions) {
      expression.validate_r(control);
    }
  }

}
