package sk.sorien.silexplugin.pimple;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.SilexIcons;

/**
 * @author Stanislav Turza
 */
public class ContainerServiceLookupElement extends LookupElement {

    private final ContainerService service;
    public ContainerServiceLookupElement(ContainerService containerService) {
        service = containerService;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return service.getName();
    }

    public void renderElement(LookupElementPresentation presentation) {

        presentation.setItemText(getLookupString());
        presentation.setTypeText(service.getClassName());
        presentation.setIcon(SilexIcons.Service);
    }
}
