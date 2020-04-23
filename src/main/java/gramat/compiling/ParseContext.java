package gramat.compiling;

import gramat.util.parsing.Location;

public interface ParseContext {

    ValueParser getParser(String name);

    Class<?> getType(String name);

    void warning(String message, Location location);

}
