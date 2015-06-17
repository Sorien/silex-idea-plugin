package sk.sorien.silexplugin.pimple;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.SilexIcons;

public class ContainerLookupElement extends LookupElement {

    private final String name;

    public ContainerLookupElement(String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return this.name;
    }

    public void renderElement(LookupElementPresentation presentation) {

        presentation.setItemText(getLookupString());
        presentation.setIcon(SilexIcons.Container);
    }
}
