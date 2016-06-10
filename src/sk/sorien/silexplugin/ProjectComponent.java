package sk.sorien.silexplugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sk.sorien.silexplugin.pimple.ContainerResolver;
import sk.sorien.silexplugin.pimple.JsonFileContainer;
import sk.sorien.silexplugin.ui.ContainerStatusBarWidget;

/**
 * @author Stanislav Turza
 */
public class ProjectComponent implements com.intellij.openapi.components.ProjectComponent {

    private final Project project;

    public ProjectComponent(Project project) {
        this.project = project;
    }

    public void projectOpened() {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            ContainerStatusBarWidget containerStatusBarWidget = new ContainerStatusBarWidget(project);
            statusBar.addWidget(containerStatusBarWidget);

            containerStatusBarWidget.setText("");
        }

        ContainerResolver.put(project, new JsonFileContainer(project, Configuration.getInstance(project).containerDefinitionFileName));
    }

    public void projectClosed() {
        ContainerResolver.remove(project);
    }

    public static boolean isEnabled(@Nullable Project project) {
        return project != null && Configuration.getInstance(project).pluginEnabled;
    }

    public static void error(String text, Project project) {
        Notifications.Bus.notify(new Notification("Silex Plugin", "Silex Plugin", text, NotificationType.ERROR), project);
    }

    public static void warning(String text, Project project) {
        Notifications.Bus.notify(new Notification("Silex Plugin", "Silex Plugin", text, NotificationType.WARNING), project);
    }

    public static void configChanged(Project project) {
        ContainerResolver.remove(project);
        ContainerResolver.put(project, new JsonFileContainer(project, Configuration.getInstance(project).containerDefinitionFileName));
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
