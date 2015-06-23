package sk.sorien.silexplugin.pimple;

/**
 * @author Stanislav Turza
 */
public enum ParameterType {
    STRING("string"), INTEGER("int"), BOOLEAN("bool"), FLOAT("float"), ARRAY("array"), CLOSURE("closure"), NULL("null"), UNKNOWN("unknown");

    private final String stringValue;

    ParameterType(final String s) {
        stringValue = s;
    }

    public String toString() {
        return stringValue;
    }
}
