package sk.sorien.silexplugin.pimple;

import com.intellij.openapi.project.Project;
import com.intellij.util.SmartFMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Stanislav Turza
 */
public class ContainerResolver {

    private static SmartFMap<Project, Container> containers = SmartFMap.emptyMap();

    public static void putContainer(Project project, Container container) {
        containers = containers.plus(project, container);
    }

    public static void removeContainer(Project project) {
        containers = containers.minus(project);
    }

    @Nullable
    public static Service getService(Project project, String serviceName) {
        return getServices(project).get(serviceName);
    }

    public static Map<String, Service> getServices(Project project) {
        return containers.get(project).getServices();
    }

    @Nullable
    public static Parameter getParameter(Project project, String parameterName) {
        return getParameters(project).get(parameterName);
    }

    public static Map<String, Parameter> getParameters(Project project) {
        return containers.get(project).getParameters();
    }
}
