package sk.sorien.silexplugin;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Stanislav Turza
 */
public class ConfigurationForm implements Configurable {

    private JCheckBox pluginEnabled;
    private JPanel basePanel;
    private Configuration configuration;

    public ConfigurationForm(@NotNull Project project) {
        configuration = Configuration.getInstance(project);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Silex Plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return basePanel;
    }

    @Override
    public boolean isModified() {
        return pluginEnabled.isSelected() != configuration.pluginEnabled;
    }

    @Override
    public void apply() throws ConfigurationException {
        configuration.pluginEnabled = pluginEnabled.isSelected();
    }

    @Override
    public void reset() {
        pluginEnabled.setSelected(configuration.pluginEnabled);
    }

    @Override
    public void disposeUIResources() {

    }
}
