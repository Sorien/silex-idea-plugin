package sk.sorien.silexplugin.pimple;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.completion.PhpVariantsUtil;
import com.jetbrains.php.completion.UsageContext;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Stanislav Turza
 */
public class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {

    public CompletionContributor() {
        // $app['<caret>']
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(PhpLanguage.INSTANCE), new ArrayAccessCompletionProvider());
        // $app['']-><caret>
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().afterLeaf("->").withLanguage(PhpLanguage.INSTANCE), new FieldReferenceCompletionProvider());
        // $app[''] = $app->extend('<caret>', ...
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(PhpLanguage.INSTANCE), new ExtendsMethodParameterListCompletionProvider());
    }

    private static class ArrayAccessCompletionProvider extends CompletionProvider<CompletionParameters> {
        public void addCompletions(@NotNull CompletionParameters parameters,
                                   ProcessingContext context,
                                   @NotNull CompletionResultSet resultSet) {

            PsiElement position = parameters.getPosition().getParent();

            if(!(position instanceof StringLiteralExpression)) {
                return;
            }

            position = position.getParent();
            if (!(position instanceof ArrayIndex)) {
                return;
            }

            position = position.getParent();
            if (!(position instanceof ArrayAccessExpression)) {
                return;
            }

            Variable[] variables = PsiTreeUtil.getChildrenOfType(position, Variable.class);
            if (variables == null || variables.length != 1) {
                return;
            }

            Variable variable = variables[0];

            // skip simple \array
            if (variable.getSignature().equals("#C\\array")) {
                return;
            }

            PhpIndex phpIndex = PhpIndex.getInstance(variable.getProject());

            Collection<? extends PhpNamedElement> classElementCollections = phpIndex.getBySignature(variable.getSignature(), null, 0);
            if(classElementCollections.size() == 0) {
                return;
            }

            PhpNamedElement phpNamedElement = classElementCollections.iterator().next();
            if (!(phpNamedElement instanceof PhpClass) || !Utils.extendsPimpleContainerClass((PhpClass) phpNamedElement)) {
                return;
            }

            for (Service element : ContainerResolver.getServices(variable.getProject()).values()){
                resultSet.addElement(new ServiceLookupElement(element));
            }

            for (Parameter element : ContainerResolver.getParameters(variable.getProject()).values()){
                resultSet.addElement(new ParameterLookupElement(element));
            }
        }
    }

    private static class FieldReferenceCompletionProvider extends CompletionProvider<CompletionParameters> {
        public void addCompletions(@NotNull CompletionParameters parameters,
                                   ProcessingContext context,
                                   @NotNull CompletionResultSet resultSet) {

            PsiElement position = parameters.getPosition().getOriginalElement().getParent();

            if(position instanceof FieldReference) {

                ArrayAccessExpression[] arrayAccesses = PsiTreeUtil.getChildrenOfType(position, ArrayAccessExpression.class);
                if (arrayAccesses == null || arrayAccesses.length != 1) {
                    return;
                }

                Variable[] variables = PsiTreeUtil.getChildrenOfType(arrayAccesses[0], Variable.class);
                if (variables == null || variables.length != 1) {
                    return;
                }

                Variable variable = variables[0];

                // skip simple \array
                if (variable.getSignature().equals("#C\\array")) {
                    return;
                }

                // get variable['<stringLiteralExpression>']
                ArrayIndex arrayIndex = arrayAccesses[0].getIndex();
                if (arrayIndex == null) {
                    return;
                }

                PsiElement stringLiteralExpression = arrayIndex.getValue();
                if ((stringLiteralExpression == null) || !(stringLiteralExpression instanceof StringLiteralExpression)) {
                    return;
                }

                PhpIndex phpIndex = PhpIndex.getInstance(variable.getProject());

                // variable is pimple container
                Collection<? extends PhpNamedElement> classElementCollections = phpIndex.getBySignature(variable.getSignature(), null, 0);
                if(classElementCollections.size() == 0) {
                    return;
                }

                PhpNamedElement phpNamedElement = classElementCollections.iterator().next();
                if (!(phpNamedElement instanceof PhpClass) || !Utils.extendsPimpleContainerClass((PhpClass) phpNamedElement)) {
                    return;
                }

                // resolve original class name from service definition
                Service service = ContainerResolver.getService(variable.getProject(), ((StringLiteralExpression) stringLiteralExpression).getContents());
                if (service == null) {
                    return;
                }

                UsageContext usageContext = new UsageContext(PhpModifier.State.DYNAMIC);

                Collection classes = phpIndex.getAnyByFQN(service.getClassName());

                for (Object aClass : classes) {
                    PhpClass phpClass = (PhpClass) aClass;
                    usageContext.setTargetObjectClass(phpClass);

                    resultSet.addAllElements(PhpVariantsUtil.getLookupItems(phpClass.getMethods(), false, usageContext));
                    resultSet.addAllElements(PhpVariantsUtil.getLookupItems(phpClass.getFields(), false, usageContext));
                }
            }
        }
    }

    private static class ExtendsMethodParameterListCompletionProvider extends CompletionProvider<CompletionParameters> {
        public void addCompletions(@NotNull CompletionParameters parameters,
                                   ProcessingContext context,
                                   @NotNull CompletionResultSet resultSet) {

            PsiElement position = parameters.getPosition().getParent();

            if(!(position instanceof StringLiteralExpression)) {
                return;
            }

            PsiElement parameterList = position.getParent();
            if (!(parameterList instanceof ParameterList)) {
                return;
            }

            PsiElement[] params = ((ParameterList)parameterList).getParameters();
            if (params.length == 0 || !params[0].isEquivalentTo(position)) {
                return;
            }

            PsiElement methodReference = parameterList.getParent();
            if(!(methodReference instanceof MethodReference)) {
                return;
            }

            // we have extend method
            String methodName = ((MethodReference) methodReference).getName();
            if ((methodName == null ) || (!methodName.equals("extend"))) {
                return;
            }

            Variable[] variables = PsiTreeUtil.getChildrenOfType(methodReference, Variable.class);
            if (variables == null || variables.length != 1) {
                return;
            }

            Variable variable = variables[0];

            // skip simple \array
            if (variable.getSignature().equals("#C\\array")) {
                return;
            }

            PhpIndex phpIndex = PhpIndex.getInstance(variable.getProject());

            Collection<? extends PhpNamedElement> classElementCollections = phpIndex.getBySignature(variable.getSignature(), null, 0);
            if(classElementCollections.size() == 0) {
                return;
            }

            PhpNamedElement phpNamedElement = classElementCollections.iterator().next();
            if (!(phpNamedElement instanceof PhpClass) || !Utils.extendsPimpleContainerClass((PhpClass) phpNamedElement)) {
                return;
            }

            for (Service element : ContainerResolver.getServices(variable.getProject()).values()){
                resultSet.addElement(new ServiceLookupElement(element));
            }

            for (Parameter element : ContainerResolver.getParameters(variable.getProject()).values()){
                resultSet.addElement(new ParameterLookupElement(element));
            }
        }
    }

}
