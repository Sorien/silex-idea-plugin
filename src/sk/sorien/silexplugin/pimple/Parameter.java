package sk.sorien.silexplugin.pimple;

/**
 * @author Stanislav Turza
 */
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

    public String getFqn() {
        return "\\" + type.toString();
    }

    public String getValue() {
        return value;
    }
}
