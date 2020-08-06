package gramat.engine.actions.capturing;

import gramat.expressions.capturing.ValueRuntime;

public class AttributeValuePress extends ValueAction {

    public boolean active;

    @Override
    public void run(ValueRuntime runtime) {
        if (!active) {
            runtime.pushAssembler();
            active = true;
        }
    }

    @Override
    public String getDescription() {
        return "PRESS ATTRIBUTE VALUE";
    }

}
