package sk.sorien.silexplugin.pimple;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Stanislav Turza
 */
public class ServiceReference extends PsiReferenceBase<PsiElement> {

    private final String className;

    public ServiceReference(String className, StringLiteralExpression element) {
        super(element);
        this.className = className;
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @NotNull
    public Object[] getVariants() {
        return new Object[0];
    }

    @Nullable
    public PsiElement resolve() {
        Collection<PhpClass> classes = PhpIndex.getInstance(getElement().getProject()).getClassesByFQN(className);

        if (classes.isEmpty()) {
            return null;
        }

        return classes.iterator().next();
    }
}