package gramat.actions;

import gramat.eval.Context;

import java.io.PrintStream;
import java.util.List;

public class NameHalt extends Action {

    private final int trxID;

    public NameHalt(int trxID) {
        this.trxID = trxID;
    }

    @Override
    public boolean stacks(Action other) {
        return other instanceof NameHalt;
    }

    @Override
    public void printAmCode(PrintStream out) {
        out.print("NAME END");
    }

    @Override
    public void run(Context context) {
        var container = context.popContainer();
        var name = container.popString();

        container.expectEmpty();

        context.peekContainer().pushName(name);
    }

    @Override
    public List<String> getArguments() {
        return List.of(String.valueOf(trxID));
    }
}
