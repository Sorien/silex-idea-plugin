package sk.sorien.silexplugin.pimple;

/**
 * @author Stanislav Turza
 */
enum ParameterType {
    STRING("string"), INTEGER("int"), BOOLEAN("bool"), FLOAT("float"), ARRAY("array"), CLOSURE("closure"), NULL("null"), UNKNOWN("unknown");

    private final String stringValue;
    ParameterType(final String s) { stringValue = s; }
    public String toString() { return stringValue; }
}

public class Parameter {

    private final String name;
    private final ParameterType type;
    private final String value;

    public Parameter(String name, ParameterType type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public ParameterType getType() {
        return type;
    }

    public String getFqn () {
        return "\\" + type.toString();
    }

    public String getValue() {
        return value;
    }
}
