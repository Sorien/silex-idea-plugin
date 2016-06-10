package sk.sorien.silexplugin;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;

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

    private static final String CONTAINER_JSON_DUMP = "pimple.json";

    public boolean pluginEnabled = true;
    public String containerDefinitionFileName = "";

    public static Configuration getInstance(Project project) {

        Configuration config = ServiceManager.getService(project, Configuration.class);

        if (config.containerDefinitionFileName.equals("")) {
            config.containerDefinitionFileName =  project.getBasePath() + File.separator + CONTAINER_JSON_DUMP;
        }

        return config;
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