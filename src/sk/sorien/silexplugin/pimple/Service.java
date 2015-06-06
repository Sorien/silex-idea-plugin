package sk.sorien.silexplugin.pimple;

/**
 * @author Stanislav Turza
 */
public class Service {

    private final String name;
    private final String className;

    public Service(String name, String className) {
        this.name = name;

        if (!className.startsWith("\\")) {
            className = "\\" + className;
        }

        this.className = className;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }
}
