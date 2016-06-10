package sk.sorien.pimpleplugin.pimple;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import sk.sorien.pimpleplugin.ui.Icons;

import java.util.Collection;

/**
 * @author Stanislav Turza
 */
public class ServiceLookupElement extends LookupElement {

    private final Service service;
    private final Project project;

    public ServiceLookupElement(Service service, Project project) {
        this.service = service;
        this.project = project;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return service.getName();
    }

    public void renderElement(LookupElementPresentation presentation) {

        presentation.setItemText(getLookupString());
        presentation.setTypeText(service.getClassName().substring(1));
        presentation.setIcon(Icons.Service);
    }

    @NotNull
    public Object getObject() {

        Collection<PhpClass> classes = PhpIndex.getInstance(project).getClassesByFQN(service.getClassName());
        return !classes.isEmpty() ? classes.iterator().next() : this;
    }

    @Override
    public void handleInsert(InsertionContext context) {
        Utils.CONTAINER_INSERT_HANDLER.handleInsert(context, this);
    }
}
