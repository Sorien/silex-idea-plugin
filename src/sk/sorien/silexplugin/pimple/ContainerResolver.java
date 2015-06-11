package sk.sorien.silexplugin.pimple;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Stanislav Turza
 */
public class ContainerResolver {

    private static final IdentityHashMap<Project, Container> containers = new IdentityHashMap<Project, Container>();

    public static void putContainer(Project project, Container container) {
        containers.put(project, container);
    }

    public static void removeContainer(Project project) {
        containers.remove(project);
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
