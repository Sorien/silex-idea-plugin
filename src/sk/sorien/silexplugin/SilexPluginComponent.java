package sk.sorien.silexplugin;

import org.jetbrains.annotations.NotNull;

/**
 * @author Stanislav Turza
 */
public class SilexPluginComponent implements com.intellij.openapi.components.ProjectComponent {
    public void projectOpened() {

    }

    public void projectClosed() {

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
