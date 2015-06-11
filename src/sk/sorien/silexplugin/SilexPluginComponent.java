package sk.sorien.silexplugin;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.pimple.ContainerResolver;
import sk.sorien.silexplugin.pimple.JsonFileContainer;

/**
 * @author Stanislav Turza
 */
public class SilexPluginComponent implements com.intellij.openapi.components.ProjectComponent {

    private final Project project;

    public SilexPluginComponent(Project project) {
        this.project = project;
    }

    public void projectOpened() {
        ContainerResolver.putContainer(project, new JsonFileContainer(project));
    }

    public void projectClosed() {
        ContainerResolver.removeContainer(project);
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
