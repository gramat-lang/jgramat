package gramat.automata.raw.units;

import gramat.automata.ndfa.NContext;
import gramat.automata.raw.RawAutomaton;


public class RawNopAutomaton extends RawAutomaton {

    @Override
    public RawAutomaton collapse() {
        return this;
    }

    @Override
    public void build(NContext context) {
        context.initialAccepted();
    }
}
