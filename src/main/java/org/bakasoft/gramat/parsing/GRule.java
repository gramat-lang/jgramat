package org.bakasoft.gramat.parsing;

import org.bakasoft.gramat.Gramat;
import org.bakasoft.gramat.elements.Element;
import org.bakasoft.gramat.parsing.elements.templates.*;
import org.bakasoft.gramat.parsing.elements.producers.GList;
import org.bakasoft.gramat.parsing.elements.producers.GObject;
import org.bakasoft.gramat.parsing.elements.producers.GValue;
import org.bakasoft.gramat.parsing.elements.transforms.GTransform;
import org.bakasoft.gramat.parsing.util.GControl;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class GRule {

  public final String name;
  public final GExpression expression;

  public GRule(String name, GExpression expression) {
    this.name = Objects.requireNonNull(name);
    this.expression = Objects.requireNonNull(expression);
  }

  public GRule simplify() {
    GExpression simplified = expression.simplify();

    if (simplified == null) {
      return null;
    }

    return new GRule(name, resolveNames(name, simplified));
  }

  private static GExpression resolveNames(String name, GExpression expression) {
    if (expression instanceof GObject) {
      GObject gObj = (GObject) expression;

      if (gObj.typeName == null) {
        return new GObject(gObj.location, name, gObj.expression);
      }
    } else if (expression instanceof GList) {
      GList gList = (GList) expression;

      if (gList.typeName == null) {
        return new GList(gList.location, name, gList.expression);
      }
    } else if (expression instanceof GValue) {
      GValue gValue = (GValue) expression;

      if (gValue.typeName == null) {
        return new GValue(gValue.location, name, gValue.expression);
      }
    } else if (expression instanceof GTransform) {
      GTransform gTran = (GTransform) expression;

      if (gTran.name == null) {
        return new GTransform(gTran.location, name, gTran.expression);
      }
    } else if (expression instanceof GFunction) {
      GFunction gFunc = (GFunction) expression;

      if (gFunc.name == null) {
        return new GFunction(gFunc.location, name, gFunc.arguments, gFunc.expression);
      }
    }

    return expression;
  }

  public Element compile(Map<String, Element> rules) {
    return expression.compile(rules);
  }

  public void validate() {
    expression.validate_r(new GControl());
  }
}
