package sk.sorien.silexplugin.pimple;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

/**
 * @author Stanislav Turza
 */
public class ContainerResolver {

    private static final String CONTAINER_JSON_DUMP = "pimple.json";

    private static long lastModified = 0;
    private static Container container;

    private static void Load(Project project) {

        File file = new File(project.getBaseDir().getPath() + '/' + CONTAINER_JSON_DUMP);

        if (file.exists() && file.lastModified() != lastModified) {
            Container newContainer = JsonDumpParser.parse(file);
            if (newContainer != null) {
                container = newContainer;
            }
            lastModified = file.lastModified();
        }
    }

    @Nullable
    public static ContainerService getService(Project project, String serviceName) {
        return getServices(project).get(serviceName);
    }

    public static Map<String, ContainerService> getServices(Project project) {

        Load(project);
        return container.getServices();
    }

    @Nullable
    public static ContainerParameter getParameter(Project project, String parameterName) {
        return getParameters(project).get(parameterName);
    }

    public static Map<String, ContainerParameter> getParameters(Project project) {

        Load(project);
        return container.getParameters();
    }

}
