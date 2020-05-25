package gramat.automata.raw;

import gramat.automata.ndfa.NContext;
import gramat.automata.ndfa.NStateSet;
import gramat.automata.raw.units.RawLiteralAutomaton;
import gramat.automata.raw.units.RawNopAutomaton;
import gramat.util.ListTool;

import java.util.ArrayList;
import java.util.List;


public class RawSeriesAutomaton extends RawCompositeAutomaton {

    public RawSeriesAutomaton(List<RawAutomaton> items) {
        super(items);
    }

    public RawSeriesAutomaton() {
        super(new ArrayList<>());
    }

    @Override
    public void build(NContext context, NStateSet initial, NStateSet accepted) {
        // -> q1 -> q2 : c[1]
        //    q2 -> q3 : c[I]
        //    q3 => q4 : c[N]
        var last = initial;

        for (var item : items) {
            var next = new NStateSet();

            item.build(context, last, next);

            last = next;
        }

        accepted.add(last);
    }

    @Override
    public RawAutomaton collapse() {
        collapseAll(items);

        if (items.isEmpty()) {
            return new RawNopAutomaton();
        }
        else if (items.size() == 1) {
            return items.get(0);
        }

        return join_literals(join_series(this));
    }

    private static RawAutomaton join_literals(RawSeriesAutomaton series) {
        var items = new ArrayList<RawAutomaton>();

        ListTool.collapse(
                series.items,
                RawStringAutomaton.class,
                items::add,
                strs -> {
                    StringBuilder str = new StringBuilder();

                    for (var s : strs) {
                        str.append(s.getStringValue());
                    }

                    items.add(new RawLiteralAutomaton(str.toString()));
                });

        return new RawSeriesAutomaton(items);
    }

    private static RawSeriesAutomaton join_series(RawSeriesAutomaton series) {
        var items = new ArrayList<RawAutomaton>();

        ListTool.collapse(
                series.items,
                RawSeriesAutomaton.class,
                items::add,
                items::addAll);

        return new RawSeriesAutomaton(items);
    }
}
