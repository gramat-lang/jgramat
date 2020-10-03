package gramat.actions;

import gramat.eval.Context;
import gramat.parsers.ValueParser;

import java.util.List;

import static gramat.util.Validations.tryCastAndTest;

public class ValueEnd extends Action {

    private final int trxID;
    private final ValueParser parser;

    public ValueEnd(int trxID, ValueParser parser) {
        this.trxID = trxID;
        this.parser = parser;
    }

    @Override
    public boolean contains(Action other) {
        return tryCastAndTest(
                ValueEnd.class,
                other,
                a -> a.trxID == this.trxID && this.parser == a.parser
        );
    }

    @Override
    public void run(Context context) {
        var id = context.transactionID(trxID);

        context.transaction().commit(id);
    }

    @Override
    public List<String> getArguments() {
        if (parser == null || parser.getName() == null) {// TODO
            return List.of();
        }
        return List.of(String.valueOf(trxID), parser.getName());
    }
}