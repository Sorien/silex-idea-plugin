package sk.sorien.silexplugin.pimple;

import sk.sorien.silexplugin.utils.ContainerMapItem;

/**
 * @author Stanislav Turza
 */
public class Service extends ContainerMapItem {

    private final String className;

    public Service(String name, String className) {

        super(name);

        if (!className.startsWith("\\")) {
            className = "\\" + className;
        }

        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
