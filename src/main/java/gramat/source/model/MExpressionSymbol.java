package gramat.source.model;

import gramat.source.formatting.SourceWriter;

public class MExpressionSymbol implements MExpression {

    public MSymbol symbol;

    @Override
    public void write(SourceWriter writer) {
        writer.write('\'');
        symbol.write(writer);
        writer.write('\'');
    }
}