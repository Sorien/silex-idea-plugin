package sk.sorien.silexplugin.pimple;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Stanislav Turza
 */
public class ServiceReference implements PsiReference {
    private final PsiElement element;
    private final TextRange textRange;
    private final String text;
    private final PhpClass phpClass;

    public ServiceReference(@NotNull PhpClass phpClass, StringLiteralExpression element) {
        this.element = element;
        this.phpClass = phpClass;
        this.text = element.getContents();
        this.textRange = new TextRange(1, element.getTextLength() - 1);
    }

    @Override
    public String toString() {
        return getCanonicalText();
    }

    public PsiElement getElement() {
        return this.element;
    }

    public TextRange getRangeInElement() {
        return textRange;
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    public boolean isReferenceTo(PsiElement element) {
        return resolve() == element;
    }

    @NotNull
    public Object[] getVariants() {
        return new Object[0];
    }

    public boolean isSoft() {
        return false;
    }

    @Nullable
    public PsiElement resolve() {
        return phpClass;
    }

    @NotNull
    @Override
    public String getCanonicalText() {
        return text;
    }
}