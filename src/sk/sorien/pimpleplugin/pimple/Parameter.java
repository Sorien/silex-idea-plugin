package sk.sorien.pimpleplugin.pimple;

import sk.sorien.pimpleplugin.utils.ContainerMapItem;

/**
 * @author Stanislav Turza
 */
public class Parameter extends ContainerMapItem {

    private final ParameterType type;
    private final String value;

    public Parameter(String name, ParameterType type, String value) {

        super(name);
        this.type = type;
        this.value = value;
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
