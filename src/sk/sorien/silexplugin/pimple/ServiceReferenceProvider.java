package sk.sorien.silexplugin.pimple;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.SilexProjectComponent;

import java.util.Collection;

/**
 * @author Stanislav Turza
 */
public class ServiceReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

        String serviceName = ((StringLiteralExpression) psiElement).getContents();

        if(!SilexProjectComponent.isEnabled(psiElement.getProject())) {
            return new PsiReference[0];
        }

        if (!Utils.isArrayAccessLiteralOfPimpleContainer((StringLiteralExpression) psiElement)) {
            if (!Utils.isFirstParameterOfPimpleContainerMethod((StringLiteralExpression) psiElement)) {
                return new PsiReference[0];
            }
        }

        Service service = ContainerResolver.getService(psiElement.getProject(), serviceName);
        if (service == null) {
            return new PsiReference[0];
        }

        PhpIndex phpIndex = PhpIndex.getInstance(psiElement.getProject());
        Collection<PhpClass> phpClasses = phpIndex.getClassesByFQN(service.getClassName());

        for (PhpClass phpClass : phpClasses) {
            ServiceReference psiReference = new ServiceReference(phpClass, (StringLiteralExpression) psiElement);
            return new PsiReference[]{psiReference};
        }

        return new PsiReference[0];
    }
}
