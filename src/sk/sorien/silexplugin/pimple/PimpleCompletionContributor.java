package sk.sorien.silexplugin.pimple;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.SilexProjectComponent;

/**
 * @author Stanislav Turza
 */
public class PimpleCompletionContributor extends CompletionContributor {

    public PimpleCompletionContributor() {
        // $app['<caret>']
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(PhpLanguage.INSTANCE), new ArrayAccessCompletionProvider());
        // $app[''] = $app->extend('<caret>', ...
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(PhpLanguage.INSTANCE), new ExtendsMethodParameterListCompletionProvider());
        // $app->register(, ['<caret>' =>])
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(PhpLanguage.INSTANCE), new RegisterFunctionValuesCompletionProvider());
    }

    private static class ArrayAccessCompletionProvider extends CompletionProvider<CompletionParameters> {
        public void addCompletions(@NotNull CompletionParameters parameters,
                                   ProcessingContext context,
                                   @NotNull CompletionResultSet resultSet) {

            PsiElement element = parameters.getPosition().getParent();
            Project project = element.getProject();

            if(!SilexProjectComponent.isEnabled(project)) {
                return;
            }

            if (!(element instanceof StringLiteralExpression)) {
                return;
            }

            Container container = Utils.findContainerForPimpleArrayAccessLiteral((StringLiteralExpression) element);
            if (container == null) {
                return;
            }

            for (Service service : container.getServices().values()) {
                resultSet.addElement(new ServiceLookupElement(service, project));
            }

            for (Parameter parameter : container.getParameters().values()) {
                resultSet.addElement(new ParameterLookupElement(parameter));
            }

            for (String key : container.getContainers().keySet()) {
                resultSet.addElement(new ContainerLookupElement(key));
            }

            resultSet.stopHere();
        }
    }

    private static class ExtendsMethodParameterListCompletionProvider extends CompletionProvider<CompletionParameters> {
        public void addCompletions(@NotNull CompletionParameters parameters,
                                   ProcessingContext context,
                                   @NotNull CompletionResultSet resultSet) {

            PsiElement element = parameters.getPosition().getParent();
            Project project = element.getProject();

            if(!SilexProjectComponent.isEnabled(project)) {
                return;
            }

            if (!(element instanceof StringLiteralExpression)) {
                return;
            }

            Container container = Utils.findContainerForFirstParameterOfPimpleMethod((StringLiteralExpression) element);
            if (container == null){
                return;
            }

            for (Service service : container.getServices().values()) {
                resultSet.addElement(new ServiceLookupElement(service, project));
            }

            for (Parameter parameter : container.getParameters().values()) {
                resultSet.addElement(new ParameterLookupElement(parameter));
            }

            resultSet.stopHere();
        }
    }

    private static class RegisterFunctionValuesCompletionProvider extends CompletionProvider<CompletionParameters> {
        public void addCompletions(@NotNull CompletionParameters parameters,
                                   ProcessingContext context,
                                   @NotNull CompletionResultSet resultSet) {

            PsiElement stringLiteralExpression = parameters.getPosition().getParent();
            Project project = stringLiteralExpression.getProject();

            if(!SilexProjectComponent.isEnabled(project)) {
                return;
            }

            if (!(stringLiteralExpression instanceof StringLiteralExpression)) {
                return;
            }

            PsiElement arrayKeyElement = stringLiteralExpression.getParent();
            PsiElement element = arrayKeyElement.getParent();

            if (!arrayKeyElement.isEquivalentTo(element.getFirstChild())) {
                return;
            }

            if (!(element instanceof ArrayHashElement)) {
                return;
            }

            element = element.getParent();
            if (!(element instanceof ArrayCreationExpression)) {
                return;
            }

            PsiElement parameterList = element.getParent();
            if (!(parameterList instanceof ParameterList)) {
                return;
            }

            PsiElement[] params = ((ParameterList) parameterList).getParameters();
            if (!(params.length > 1 && params[1].isEquivalentTo(element))) {
                return;
            }

            PsiElement methodReference = parameterList.getParent();
            if (!(methodReference instanceof MethodReference)) {
                return;
            }

            String methodReferenceName = ((MethodReference) methodReference).getName();
            if ((methodReferenceName == null) || !(methodReferenceName.equals("register"))) {
                return;
            }

            Container container = Utils.findContainerForMethodReference((MethodReference) methodReference);
            if (container == null) {
                return;
            }

            for (Parameter parameter : container.getParameters().values()) {
                resultSet.addElement(new ParameterLookupElement(parameter));
            }

            resultSet.stopHere();
        }
    }
}
