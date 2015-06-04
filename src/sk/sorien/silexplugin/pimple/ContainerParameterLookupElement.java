package sk.sorien.silexplugin.pimple;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.SilexIcons;

public class ContainerParameterLookupElement extends LookupElement {

    private final ContainerParameter parameter;
    public ContainerParameterLookupElement(ContainerParameter containerParameter) {
        parameter = containerParameter;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return parameter.getName();
    }

    public void renderElement(LookupElementPresentation presentation) {

        presentation.setItemText(getLookupString());
        presentation.setTypeText(parameter.getType());

        if (!parameter.getValue().isEmpty())
            presentation.appendTailText("(" + parameter.getValue() + ")", true);

        presentation.setIcon(SilexIcons.Parameter);
    }
}