package sk.sorien.silexplugin.pimple;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.SilexProjectComponent;

import java.util.Collection;

/**
 * @author Stanislav Turza
 */
public class PimpleReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
                PlatformPatterns.psiElement(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE), new ServiceReferenceProvider()
        );
    }

    private class ServiceReferenceProvider extends PsiReferenceProvider {
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

            Collection<PhpClass> classes = PhpIndex.getInstance(psiElement.getProject()).getClassesByFQN(service.getClassName());

            if (classes.isEmpty()) {
                return new PsiReference[0];
            }

            ServiceReference psiReference = new ServiceReference(classes.iterator().next(), (StringLiteralExpression) psiElement);
            return new PsiReference[]{psiReference};
        }
    }
}
