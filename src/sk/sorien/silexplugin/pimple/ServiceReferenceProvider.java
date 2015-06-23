package sk.sorien.silexplugin.pimple;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.SilexProjectComponent;

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

        Container container = Utils.findContainerForPimpleArrayAccessLiteral((StringLiteralExpression) psiElement);
        if (container == null) {

            container = Utils.findContainerForFirstParameterOfPimpleMethod((StringLiteralExpression) psiElement);
            if (container == null) {
                return new PsiReference[0];
            }

            // we cant detect if we are triggering CTRL+Click from some SubContainer or not so fallback to top most.
            container = ContainerResolver.get(psiElement.getProject());
        }

        Service service = container.getServices().get(serviceName);
        if (service == null) {
            return new PsiReference[0];
        }

        PhpClass phpClass = service.getPhpClass();

        if (phpClass == null) {
            return new PsiReference[0];
        }

        ServiceReference psiReference = new ServiceReference(phpClass, (StringLiteralExpression) psiElement);
        return new PsiReference[]{psiReference};
    }
}
