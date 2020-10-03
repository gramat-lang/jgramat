package gramat.pipeline;

import gramat.am.ExpressionFactory;
import gramat.am.expression.AmExpression;
import gramat.formatting.NodeFormatter;
import gramat.formatting.StateFormatter;
import gramat.framework.Component;
import gramat.graph.Line;
import gramat.graph.Segment;
import gramat.machine.State;
import gramat.util.NameMap;

public class Pipeline {

    public static State compile(Component component, String name, NameMap<AmExpression> grammar) {
        var rule = new RuleExpression(component, ExpressionFactory.reference(name), grammar);

        return compile(rule);
    }

    public static State compile(Component component, AmExpression expression, NameMap<AmExpression> grammar) {
        var rule = new RuleExpression(component, expression, grammar);

        return compile(rule);
    }

    public static State compile(RuleExpression rule) {
        var step1 = compileStep1(rule);
        var step2 = compileStep2(step1);
        var step3 = compileStep3(step2);
        return compileStep4(rule.parent, step3);
    }

    public static SegmentGraph compileStep1(RuleExpression rule) {
        var dependencies = new NameMap<Segment>();
        var compiler = new ExpressionCompiler(rule.parent);
        var main = compiler.compile(rule.main);

        for (var name : rule.dependencies.keySet()) {
            var expr = rule.dependencies.find(name);
            var segment = compiler.compile(expr);

            System.out.println("========== SEGMENT " + name);
            new NodeFormatter(System.out).write(segment);

            dependencies.set(name, segment);
        }

        return new SegmentGraph(main, dependencies);
    }

    public static LineGraph compileStep2(SegmentGraph graph) {
        var flattener = new SegmentFlattener(graph.dependencies);
        var lines = flattener.flatten(graph.main);

        for (var entry : lines.dependencies.entrySet()) {
            System.out.println("========== LINE " + entry.getKey());

            new NodeFormatter(System.out).write(entry.getValue());
        }

        return lines;
    }

    public static Line compileStep3(LineGraph graph) {
        var resolver = new LineReducer(graph.dependencies);
        var line = resolver.resolve(graph.main);

        System.out.println("========== RESOLVED");
        new NodeFormatter(System.out).write(line);

        return line;
    }

    public static State compileStep4(Component parent, Line line) {
        var stateCompiler = new StateCompiler(parent, line.graph);
        var state = stateCompiler.compile(line.graph.segment(line.source, line.target));

        System.out.println("========== STATE");
        new StateFormatter(System.out).write(state);

        return state;
    }

}