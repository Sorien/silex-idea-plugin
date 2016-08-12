package sk.sorien.pimpleplugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sk.sorien.pimpleplugin.Configuration;
import sk.sorien.pimpleplugin.ProjectComponent;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.MouseEvent;
import java.io.File;

public class ContainerStatusBarWidget extends EditorBasedWidget implements com.intellij.openapi.wm.StatusBarWidget.TextPresentation, com.intellij.openapi.wm.StatusBarWidget {

    private StatusBar statusBar;
    private String text;
    private Project project;

    public ContainerStatusBarWidget(Project project) {
        super(project);
        this.project = project;
    }

    @NotNull
    @Override

    public String ID() {
        return "silex.statusbar.widget";
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType platformType) {
        return this;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    @Override
    public void dispose() {

    }

    public void setText(String text) {
        this.text = text;
        if (statusBar != null) {
            statusBar.updateWidget(this.ID());
        }
    }

    @NotNull
    @Override
    public String getText() {
        return "Pimple Dump: " + text;
    }

    @NotNull
    @Override
    public String getMaxPossibleText() {
        return "";
    }

    @Override
    public float getAlignment() {
        return 0;
    }

    @Nullable
    @Override
    public String getTooltipText() {
        return null;
    }

    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return new Consumer<MouseEvent>() {
            @Override
            public void consume(MouseEvent mouseEvent) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Pimple Definition File", "json"));
                fileChooser.setCurrentDirectory(new File(project.getBasePath()));

                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    Configuration.getInstance(project).containerDefinitionFileName = fileChooser.getSelectedFile().getAbsolutePath();
                    ProjectComponent.configChanged(project);
                }
            }
        };
    }
}
