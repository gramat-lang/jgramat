package gramat.engine;

public class SymbolWild extends Symbol {
    @Override
    public boolean matches(char chr) {
        return false;
    }

    @Override
    public String toString() {
        return "*";
    }
}