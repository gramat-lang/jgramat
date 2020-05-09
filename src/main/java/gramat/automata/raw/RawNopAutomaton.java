package gramat.automata.raw;

import gramat.automata.builder.AutomatonBuilder;
import gramat.automata.builder.Segment;
import gramat.automata.builder.StateReference;
import gramat.automata.nondet.NAutomaton;
import gramat.automata.nondet.NLanguage;

public class RawNopAutomaton extends RawAutomaton {

    @Override
    public RawAutomaton collapse() {
        return this;
    }

    @Override
    public NAutomaton build(NLanguage lang) {
        var state = lang.state();

        return lang.automaton(state, state, state);
    }
}
