package sk.sorien.silexplugin.pimple;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.ui.Icons;

/**
 * @author Stanislav Turza
 */
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
        presentation.setTypeText("Pimple");
        presentation.setIcon(Icons.Container);
    }

    @Override
    public void handleInsert(InsertionContext context) {
        Utils.CONTAINER_INSERT_HANDLER.handleInsert(context, this);
    }
}
