package sk.sorien.silexplugin;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sk.sorien.silexplugin.pimple.ContainerResolver;
import sk.sorien.silexplugin.pimple.JsonFileContainer;

/**
 * @author Stanislav Turza
 */
public class SilexProjectComponent implements com.intellij.openapi.components.ProjectComponent {

    private final Project project;

    public SilexProjectComponent(Project project) {
        this.project = project;
    }

    public void projectOpened() {
        ContainerResolver.putContainer(project, new JsonFileContainer(project));
    }

    public void projectClosed() {
        ContainerResolver.removeContainer(project);
    }

    public static boolean isEnabled(@Nullable Project project) {
        return project != null && Configuration.getInstance(project).pluginEnabled;
    }

    public void initComponent() {

    }

    public void disposeComponent() {

    }

    @NotNull
    public String getComponentName() {
        return "silexPlugin";
    }
}
