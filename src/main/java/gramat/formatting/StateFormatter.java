package gramat.formatting;

import gramat.machine.State;
import gramat.machine.Transition;
import gramat.util.StringUtils;

import java.util.*;

public class StateFormatter extends AmFormatter {

    public StateFormatter(Appendable output) {
        super(output);
    }

    public void write(State initial) {
        for (var source : list_nodes(initial)) {
            if (source == initial) {
                raw("->");
                sp();
                raw(source.id);
                ln();
            }

            for (var transition : list_transitions(source)) {
                var target = transition.target;
                var symbol = transition.symbol.toString();
                var badge = transition.badge.toString();
                var mode = transition.mode.name();
                var before = StringUtils.join("\n", transition.before);
                var after = StringUtils.join("\n", transition.after);

                if (before.length() > 0) {
                    raw(source.id);
                    sp();
                    raw("->");
                    sp();
                    raw(target.id);
                    sp();
                    raw("!<");
                    sp();
                    amstr(before);
                    ln();
                }

                raw(source.id);
                sp();
                raw("->");
                sp();
                raw(target.id);
                sp();
                raw(":");
                sp();
                amstr(symbol);
                raw("/");
                amstr(badge);
                raw("(");
                amstr(mode);
                raw(")");
                ln();

                if (after.length() > 0) {
                    raw(source.id);
                    sp();
                    raw("->");
                    sp();
                    raw(target.id);
                    sp();
                    raw("!>");
                    sp();
                    amstr(after);
                    ln();
                }
            }

            if (source.isAccepted()) {
                raw(source.id);
                sp();
                raw("<=");
                ln();
            }
        }
    }

    private static List<Transition> list_transitions(State state) {
        var hashLink = new HashMap<String, Transition>();

        for (var link : state) {
            var hash = link.target.id + "/" + link.symbol + "/" + link.hashCode();
            hashLink.put(hash, link);
        }

        var hashes = new ArrayList<>(hashLink.keySet());

        Collections.sort(hashes);

        var result = new ArrayList<Transition>();

        for (var hash : hashes) {
            var link = hashLink.get(hash);

            result.add(link);
        }

        return result;
    }

    private static List<State> list_nodes(State root) {
        var control = new HashSet<State>();
        var queue = new LinkedList<State>();
        var idNodes = new HashMap<String, State>();

        queue.add(root);

        while (queue.size() > 0) {
            var node = queue.remove();

            if (control.add(node)) {
                if (idNodes.containsKey(node.id)) {
                    throw new RuntimeException();
                }

                idNodes.put(node.id, node);

                for (var link : node) {
                    queue.add(link.target);
                }
            }
        }

        var ids = new ArrayList<>(idNodes.keySet());

        Collections.sort(ids);

        var result = new ArrayList<State>();

        for (var id : ids) {
            var node = idNodes.get(id);

            result.add(node);
        }

        return result;
    }

}
