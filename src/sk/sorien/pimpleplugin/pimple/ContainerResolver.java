package sk.sorien.pimpleplugin.pimple;

import com.intellij.openapi.project.Project;
import com.intellij.util.SmartFMap;

/**
 * @author Stanislav Turza
 */
public class ContainerResolver {

    private static SmartFMap<Project, Container> containers = SmartFMap.emptyMap();

    public static void put(Project project, Container container) {
        containers = containers.plus(project, container);
    }

    public static void remove(Project project) {
        containers = containers.minus(project);
    }

    public static Container get(Project project) {
        return containers.get(project);
    }
}
