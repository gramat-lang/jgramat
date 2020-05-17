package gramat.automata.raw;

import gramat.automata.actions.*;
import gramat.automata.ndfa.Language;
import gramat.automata.ndfa.NAutomaton;

public class RawDynAttribute extends RawAutomaton {

    private final RawAutomaton name;
    private final RawAutomaton content;

    public RawDynAttribute(RawAutomaton name, RawAutomaton content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public RawAutomaton collapse() {
        return new RawDynAttribute(name.collapse(), content.collapse());
    }

    @Override
    public NAutomaton build(Language lang) {
        var amName = name.build(lang);
        var amContent = content.build(lang);

        lang.transition(amName.accepted, amContent.initial, null);

        return lang.automaton(amName.initial, amContent.accepted);
    }
}
