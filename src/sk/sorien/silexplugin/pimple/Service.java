package sk.sorien.silexplugin.pimple;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Stanislav Turza
 */
public class Service {

    private final String name;
    private final String className;
    private final Project project;
    private PhpClass phpClass;

    public Service(String name, String className, Project project) {
        this.name = name;

        if (!className.startsWith("\\")) {
            className = "\\" + className;
        }

        this.className = className;
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    @Nullable
    public PhpClass getPhpClass() {
        if (phpClass == null) {
            Collection<PhpClass> classes = PhpIndex.getInstance(project).getClassesByFQN(className);
            if (!classes.isEmpty()) {
                phpClass = classes.iterator().next();
            }
        }

        return phpClass;
    }
}
