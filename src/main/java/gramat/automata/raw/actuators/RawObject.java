package gramat.automata.raw.actuators;

import gramat.automata.ndfa.Language;
import gramat.automata.ndfa.NAutomaton;
import gramat.automata.raw.RawAutomaton;
import gramat.eval.Action;
import gramat.eval.object.ObjectCancel;
import gramat.eval.object.ObjectSave;
import gramat.eval.object.ObjectStart;
import gramat.eval.value.ValueCancel;
import gramat.eval.value.ValueSave;
import gramat.eval.value.ValueStart;

public class RawObject extends RawAutomaton {

    private final Object typeHint;
    private final RawAutomaton content;

    public RawObject(RawAutomaton content, Object typeHint) {
        this.content = content;
        this.typeHint = typeHint;
    }

    @Override
    public RawAutomaton collapse() {
        return new RawObject(content.collapse(), typeHint);
    }

    @Override
    public NAutomaton build(Language lang) {
        var am = content.build(lang);
        var start = new ObjectStart();
        var save = new ObjectSave();
        var cancel = new ObjectCancel();
        lang.postBuild(() -> TRX.setupActions(am, start, save, cancel));
        return am;
    }
}
