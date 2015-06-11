package sk.sorien.silexplugin;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Stanislav Turza
 */
@State(
        name = "SilexPluginSettings",
        storages = {
                @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
                @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/silex-plugin.xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class Configuration implements PersistentStateComponent<Configuration> {

    public boolean pluginEnabled = true;

    public static Configuration getInstance(Project project) {
        return ServiceManager.getService(project, Configuration.class);
    }

    @Nullable
    @Override
    public Configuration getState() {
        return this;
    }

    @Override
    public void loadState(Configuration configuration) {
        XmlSerializerUtil.copyBean(configuration, this);
    }
}