package gramat.automata.raw.actuators;

import gramat.automata.ndfa.NContext;
import gramat.automata.ndfa.NStateSet;
import gramat.automata.raw.CollapseContext;
import gramat.automata.raw.RawAutomaton;
import gramat.eval.list.ListCancel;
import gramat.eval.list.ListSave;
import gramat.eval.list.ListStart;
import gramat.eval.object.ObjectCancel;
import gramat.eval.object.ObjectSave;
import gramat.eval.object.ObjectStart;

public class RawList extends RawAutomaton {

    private final Object typeHint;
    private final RawAutomaton content;

    public RawList(RawAutomaton content, Object typeHint) {
        this.content = content;
        this.typeHint = typeHint;
    }

    @Override
    public RawAutomaton collapse() {
        return new RawList(content.collapse(), typeHint);
    }

    @Override
    public void build(NContext context, NStateSet initial, NStateSet accepted) {
        var machine = context.machine(content, initial, accepted);
        var start = new ListStart();
        var save = new ListSave();
        var cancel = new ListCancel();
        context.postBuildHook(() -> TRX.setupActions(machine, start, save, cancel));
    }
}