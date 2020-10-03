package gramat.pipeline;

import gramat.actions.ActionStore;
import gramat.framework.Component;
import gramat.framework.DefaultComponent;
import gramat.graph.*;
import gramat.util.NameMap;

import java.util.*;

public class SegmentFlattener extends DefaultComponent {

    private final NameMap<Segment> segments;

    public SegmentFlattener(Component parent, NameMap<Segment> segments) {
        super(parent);
        this.segments = segments;
    }

    public LineGraph flatten(Segment segment) {
        var recursive = new LinkedHashSet<String>();

        compute_recursive_dependencies(segment, new Stack<>(), recursive);

        var main = make_line(segment, recursive);
        var dependencies = new NameMap<Line>();

        flatten_segment(dependencies, recursive);

        return new LineGraph(gramat, main, dependencies);
    }

    public void flatten_segment(NameMap<Line> result, Set<String> recursive) {
        for (var name : recursive) {
            if (!result.containsKey(name)) {
                var segment = segments.find(name);

                result.put(name, make_line(segment, recursive));
            }
        }
    }

    private Line make_line(Segment segment, Set<String> recursive) {
        var graph = new Graph();
        var line = graph.createLine();

        copy_segment(graph, segment, line.source, line.target, new ActionStore(), new ActionStore(), recursive);

        return line;
    }

    private void copy_segment(Graph graph, Segment segment, Node rootSource, Node rootTarget, ActionStore beforeActions, ActionStore afterActions, Set<String> recursive) {
        var copies = new HashMap<Node, Node>();

        for (var source : segment.sources) {
            copies.put(source, rootSource);
        }

        for (var target : segment.targets) {
            copies.put(target, rootTarget);
        }

        for (var link : segment.graph.walkLinksFrom(segment.sources)) {
            var sourceCopy = copies.computeIfAbsent(link.source, graph::createNodeFrom);
            var targetCopy = copies.computeIfAbsent(link.target, graph::createNodeFrom);

            if (link.token.isSymbol() || recursive.contains(link.token.getReference())) {
                var linkCopy = graph.createLink(sourceCopy, targetCopy, link.token);

                linkCopy.afterActions.append(link.afterActions);
                linkCopy.beforeActions.append(link.beforeActions);
            }
            else {
                var refSegment = segments.find(link.token.getReference());

                copy_segment(
                        graph,
                        refSegment,
                        sourceCopy, targetCopy,
                        link.beforeActions, link.afterActions, recursive);
            }
        }

        // Apply wrapping actions

        for (var entry : copies.entrySet()) {
            var orig = entry.getKey();
            var copy = entry.getValue();

            if (segment.sources.contains(orig)) {
                for (var link : graph.findOutgoingLinks(copy)) {
                    // If the link belongs to the section being copied...
                    if (copies.containsValue(link.target)) {
                        link.beforeActions.append(beforeActions); // TODO double-check order
                    }
                }
            }

            if (segment.targets.contains(orig)) {
                for (var link : graph.findIncomingLinks(copy)) {
                    // If the link belongs to the section being copied...
                    if (copies.containsValue(link.source)) {
                        link.afterActions.append(afterActions); // TODO double-check order
                    }
                }
            }
        }
    }

    private void compute_recursive_dependencies(String name, Segment segment, Stack<String> stack, Set<String> result) {
        stack.push(name);

        compute_recursive_dependencies(segment, stack, result);

        stack.pop();
    }

    private void compute_recursive_dependencies(Segment segment, Stack<String> stack, Set<String> result) {
        for (var link : segment.graph.walkLinksFrom(segment.sources)) {
            if (link.token.isReference()) {
                var refName = link.token.getReference();

                if (stack.contains(refName)) {
                    result.add(refName);
                }
                else {
                    var refSegment = segments.find(refName);

                    compute_recursive_dependencies(refName, refSegment, stack, result);
                }
            }
        }
    }

}
