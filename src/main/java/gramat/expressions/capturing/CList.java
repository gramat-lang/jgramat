package gramat.expressions.capturing;

import gramat.engine.nodet.NBuilder;
import gramat.engine.nodet.NState;
import gramat.expressions.Expression;

import java.util.List;

public class CList extends Expression {

    private final Expression content;
    private final Object typeHint;

    public CList(Expression content, Object typeHint) {
        this.content = content;
        this.typeHint = typeHint;
    }

    @Override
    public NState build(NBuilder builder, NState initial) {
        var accepted = content.build(builder, initial);
        var begin = new ListBegin();
        var commit = new ListCommit(begin);
        var sustain = new ListSustain(begin);

        TRX2.applyActions(builder, initial, accepted, begin, commit, sustain);

        return accepted;
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(content);
    }

    public class ListBegin extends ValueAction {

        @Override
        public void run(ValueRuntime runtime) {
            // TODO implement
        }

        @Override
        public String getDescription() {
            return "BEGIN LIST";
        }

    }

    public class ListCommit extends ValueAction {

        private final ListBegin begin;

        public ListCommit(ListBegin begin) {
            this.begin = begin;
        }

        @Override
        public void run(ValueRuntime runtime) {
            // TODO implement
        }

        @Override
        public String getDescription() {
            return "COMMIT LIST";
        }
    }

    public class ListSustain extends ValueAction {

        private final ListBegin begin;

        public ListSustain(ListBegin begin) {
            this.begin = begin;
        }

        @Override
        public void run(ValueRuntime runtime) {
            // TODO implement
        }

        @Override
        public String getDescription() {
            return "SUSTAIN LIST";
        }
    }

    public class ListRollback extends ValueAction {

        private final ListBegin begin;

        public ListRollback(ListBegin begin) {
            this.begin = begin;
        }

        @Override
        public void run(ValueRuntime runtime) {
            // TODO implement
        }

        @Override
        public String getDescription() {
            return "ROLLBACK LIST";
        }
    }
}
