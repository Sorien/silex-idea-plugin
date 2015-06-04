package sk.sorien.silexplugin.pimple;

/**
 * @author Stanislav Turza
 */
public class ContainerParameter {

    private final String name;
    private final String type;
    private final String value;

    public ContainerParameter(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getFqn () {
        return "\\" + type;
    }

    public String getValue() {
        return value;
    }
}
