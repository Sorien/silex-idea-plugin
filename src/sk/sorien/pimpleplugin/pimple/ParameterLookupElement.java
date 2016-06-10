package sk.sorien.pimpleplugin.pimple;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import org.jetbrains.annotations.NotNull;
import sk.sorien.pimpleplugin.ui.Icons;

public class ParameterLookupElement extends LookupElement {

    private final Parameter parameter;

    public ParameterLookupElement(Parameter parameter) {
        this.parameter = parameter;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return parameter.getName();
    }

    public void renderElement(LookupElementPresentation presentation) {

        presentation.setItemText(getLookupString());
        presentation.setTypeText(parameter.getType().toString());

        if (!parameter.getValue().isEmpty())
            presentation.appendTailText("(" + parameter.getValue() + ")", true);

        presentation.setIcon(Icons.Parameter);
    }

    @Override
    public void handleInsert(InsertionContext context) {
        Utils.CONTAINER_INSERT_HANDLER.handleInsert(context, this);
    }
}