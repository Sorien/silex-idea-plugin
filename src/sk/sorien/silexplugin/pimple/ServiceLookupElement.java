package sk.sorien.silexplugin.pimple;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.SilexIcons;

/**
 * @author Stanislav Turza
 */
public class ServiceLookupElement extends LookupElement {

    private final Service service;

    public ServiceLookupElement(Service service) {
        this.service = service;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return service.getName();
    }

    public void renderElement(LookupElementPresentation presentation) {

        presentation.setItemText(getLookupString());
        presentation.setTypeText(service.getClassName().substring(1));
        presentation.setIcon(SilexIcons.Service);
    }

    @NotNull
    public Object getObject() {

        PhpClass phpClass = service.getPhpClass();
        return phpClass == null ? this : phpClass;
    }
}
