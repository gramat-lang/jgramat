package gramat.pipeline.compiling;

import gramat.scheme.data.expressions.ExpressionData;
import gramat.scheme.data.expressions.ReferenceData;
import gramat.util.NameMap;

import java.util.*;

public class Stats {

    public final Set<String> recursiveReferences;
    public final int count;

    private Stats(Set<String> recursiveReferences, int count) {
        this.recursiveReferences = recursiveReferences;
        this.count = count;
    }

    public static Stats compute(ExpressionData main, NameMap<ExpressionData> expressions) {
        var references = new LinkedHashSet<String>();
        var control = new HashSet<ExpressionData>();
        var stack = new ArrayDeque<String>();

        compute(main, expressions, stack, references, control);

        return new Stats(references, control.size());
    }

    private static void compute(ExpressionData main, NameMap<ExpressionData> expressions, Deque<String> stack, Set<String> references, Set<ExpressionData> control) {
        if (control.add(main)) {
            if (main instanceof ReferenceData) {
                var reference = (ReferenceData) main;

                if (!references.contains(reference.name)) {
                    if (stack.contains(reference.name)) {
                        references.add(reference.name);
                    } else {
                        var ref = expressions.find(reference.name);

                        compute(ref, expressions, stack, references, control, reference.name);
                    }
                }
            }

            for (var child : main.getChildren()) {
                compute(child, expressions, stack, references, control);
            }
        }
    }

    private static void compute(ExpressionData main, NameMap<ExpressionData> expressions, Deque<String> stack, Set<String> result, Set<ExpressionData> control, String name) {
        stack.push(name);

        compute(main, expressions, stack, result, control);

        stack.pop();
    }

}
