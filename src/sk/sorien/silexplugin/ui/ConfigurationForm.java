package sk.sorien.silexplugin.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sk.sorien.silexplugin.Configuration;
import sk.sorien.silexplugin.ProjectComponent;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Stanislav Turza
 */
public class ConfigurationForm implements Configurable {

    private JCheckBox pluginEnabled;
    private JPanel basePanel;
    private JButton containerDumpSearchButton;
    private JTextField containerDumpFileName;
    private final Configuration configuration;
    private Project project;

    public ConfigurationForm(@NotNull final Project project) {
        configuration = Configuration.getInstance(project);
        this.project = project;

        containerDumpSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Pimple Definition File", "json"));
                fileChooser.setCurrentDirectory(new File(project.getBasePath()));

                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    containerDumpFileName.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
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
        return pluginEnabled.isSelected() != configuration.pluginEnabled || !containerDumpFileName.getText().equals(configuration.containerDefinitionFileName);

    }

    @Override
    public void apply() throws ConfigurationException {
        configuration.pluginEnabled = pluginEnabled.isSelected();
        configuration.containerDefinitionFileName = containerDumpFileName.getText();

        ProjectComponent.configChanged(this.project);
    }

    @Override
    public void reset() {
        pluginEnabled.setSelected(configuration.pluginEnabled);
        containerDumpFileName.setText(configuration.containerDefinitionFileName);
    }

    @Override
    public void disposeUIResources() {

    }
}
