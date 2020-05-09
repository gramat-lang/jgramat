package gramat.automata.raw;

import gramat.automata.nondet.NAutomaton;
import gramat.automata.nondet.NLanguage;

public class RawWildAutomaton extends RawAutomaton {

    @Override
    public RawAutomaton collapse() {
        return this;
    }

    @Override
    public NAutomaton build(NLanguage lang) {
        var state = lang.state();

        state.linkWild(state);

        return lang.automaton(state, state, state);
    }

}
