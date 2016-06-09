package sk.sorien.silexplugin.pimple;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Stanislav Turza
 */
public class Utils {

    public static final InsertHandler<LookupElement> CONTAINER_INSERT_HANDLER = new InsertHandler<LookupElement>() {
        @Override
        public void handleInsert(InsertionContext context, LookupElement item) {
            PsiElement element = PsiUtilCore.getElementAtOffset(context.getFile(), context.getStartOffset());
            context.getDocument().deleteString(context.getStartOffset() + item.getLookupString().length(), context.getStartOffset() + element.getText().length());
            // move caret after ]
            context.getEditor().getCaretModel().moveCaretRelatively(2, 0, false, false, true);
        }
    };

    private static Container findContainerForPimpleArrayAccess(ArrayAccessExpression arrayAccessElement, Boolean onlyParentContainers) {

        PsiElement children;
        PsiElement element = arrayAccessElement;
        while ((children = PsiTreeUtil.getChildOfType(element, ArrayAccessExpression.class)) != null) {
            element = children;
        }

        // check if var is pimple container
        Signature signature = new Signature();

        PsiElement signatureElement = PsiTreeUtil.getChildOfAnyType(element, Variable.class, FieldReference.class);
        if (signatureElement == null) {
            return null;
        }

        if (signatureElement instanceof Variable) {
            signature.set(((Variable) signatureElement).getSignature());
        }

        if (signatureElement instanceof FieldReference) {
            signature.set(((FieldReference) signatureElement).getSignature());
        }

        PhpIndex phpIndex = PhpIndex.getInstance(arrayAccessElement.getProject());

        if (!Utils.isPimpleContainerClass(phpIndex, signature.getClassSignature())) {
            return null;
        }

        Container container = ContainerResolver.get(arrayAccessElement.getProject());

        // find proper base container from signature
        for (String parameter : signature.getParameters()) {
            container = container.getContainers().get(getResolvedParameter(phpIndex, parameter));
            if (container == null)
                return null;
        }

        PsiElement lastElement = onlyParentContainers ? arrayAccessElement : arrayAccessElement.getParent();

        // find proper container
        while (!element.isEquivalentTo(lastElement) ) {

            ArrayIndex arrayIndex = ((ArrayAccessExpression)element).getIndex();
            if (arrayIndex == null) {
                return null;
            }

            PsiElement arrayIndexElement = arrayIndex.getValue();
            if (arrayIndexElement == null) {
                return null;
            }

            String containerName;

            if (arrayIndexElement instanceof StringLiteralExpression) {
                containerName = ((StringLiteralExpression) arrayIndexElement).getContents();
            }
            else if (arrayIndexElement instanceof MemberReference) {
                containerName = getResolvedParameter(phpIndex, ((MemberReference) arrayIndexElement).getSignature());
            }
            else return null;

            container = container.getContainers().get(containerName);
            if (container == null) {
                return null;
            }

            element = element.getParent();
        }

        return container;

    }

    public static Container findContainerForPimpleArrayAccessLiteral(StringLiteralExpression stringLiteralExpression) {

        PsiElement element = stringLiteralExpression.getParent();
        if (!(element instanceof ArrayIndex)) {
            return null;
        }

        element = element.getParent();
        if (element instanceof ArrayAccessExpression) {
            return findContainerForPimpleArrayAccess((ArrayAccessExpression) element, true);
        }

        return null;
    }

    public static Container findContainerForFirstParameterOfPimpleMethod(StringLiteralExpression stringLiteralExpression) {
        PsiElement parameterList = stringLiteralExpression.getParent();
        if (!(parameterList instanceof ParameterList)) {
            return null;
        }

        PsiElement[] params = ((ParameterList) parameterList).getParameters();
        if (!(params.length > 0 && params[0].isEquivalentTo(stringLiteralExpression))) {
            return null;
        }

        PsiElement methodReference = parameterList.getParent();
        if (!(methodReference instanceof MethodReference)) {
            return null;
        }

        // we have extend/raw method
        String methodReferenceName = ((MethodReference) methodReference).getName();
        if ((methodReferenceName == null) || !(methodReferenceName.equals("extend") || methodReferenceName.equals("raw"))) {
            return null;
        }

        return findContainerForMethodReference((MethodReference)methodReference);
    }

    public static Container findContainerForMethodReference(MethodReference methodReference) {
        Signature signature = new Signature();

        PsiElement signatureElement = PsiTreeUtil.getChildOfAnyType(methodReference, Variable.class, FieldReference.class, ArrayAccessExpression.class);
        if (signatureElement == null) {
            return null;
        }

        PhpIndex phpIndex = PhpIndex.getInstance(methodReference.getProject());

        Container container;

        if (signatureElement instanceof Variable || signatureElement instanceof FieldReference) {
            signature.set(((PhpReference) signatureElement).getSignature());

            if (!Utils.isPimpleContainerClass(phpIndex, signature.getClassSignature())) {
                return null;
            }

            container = ContainerResolver.get(methodReference.getProject());

            // find proper base container from signature
            for (String parameter : signature.getParameters()) {
                container = container.getContainers().get(getResolvedParameter(phpIndex, parameter));
                if (container == null)
                    return null;
            }

            return container;
        }

        if (signatureElement instanceof ArrayAccessExpression) {
            return findContainerForPimpleArrayAccess((ArrayAccessExpression) signatureElement, false);
        }

        return null;
    }

    private static Boolean isPimpleContainerClass(PhpClass phpClass) {

        if (phpClass == null) {
            return false;
        } else if (isPimpleContainerBaseClass(phpClass.getFQN())) {
            return true;
        } else {
            Integer counter = 0;

            while ((phpClass = phpClass.getSuperClass()) != null && counter < 5) {

                if (isPimpleContainerBaseClass(phpClass.getFQN())) {
                    return true;
                }

                counter++;
            }
            return false;
        }
    }

    private static Boolean isPimpleContainerBaseClass(String className) {
        return className != null && (className.equals("\\Silex\\Application") || className.equals("\\Pimple\\Container") || className.equals("\\Pimple"));
    }

    public static Boolean isPimpleContainerClass(PhpIndex phpIndex, String signature) {
        return isPimpleContainerClass(phpIndex, signature, 0);
    }

    private static Boolean isPimpleContainerClass(PhpIndex phpIndex, String signature, int depth) {

        if (++depth > 3) {
            return false;
        }

        Collection<? extends PhpNamedElement> collection = phpIndex.getBySignature(signature, null, 0);
        if (collection.size() == 0) {
            return false;
        }

        PhpNamedElement element = collection.iterator().next();

        if (element instanceof PhpClass) {
            return isPimpleContainerClass((PhpClass) element);
        }

        if ((element instanceof Field) || (element instanceof Method)) {

            for (String type : element.getType().getTypes()) {

                if (type.startsWith("#") && isPimpleContainerClass(phpIndex, type, depth)) {
                    return true;
                } else {
                    collection = phpIndex.getClassesByFQN(type);
                    if (collection.size() == 0) {
                        continue;
                    }

                    element = collection.iterator().next();

                    if (element instanceof PhpClass) {
                        if (isPimpleContainerClass((PhpClass) element)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static String normalizedString(StringLiteralExpression text) {
        return  text.isSingleQuote() ? text.getContents(): text.getContents().replace("\\\\", "\\");
    }

    private static String getStringValue(@Nullable PsiElement psiElement) {
        return getStringValue(psiElement, 0);
    }

    @Nullable
    private static String getStringValue(@Nullable PsiElement psiElement, int depth) {

        if (psiElement == null || ++depth > 5) {
            return null;
        }

        if (psiElement instanceof StringLiteralExpression) {
            String resolvedString = normalizedString((StringLiteralExpression) psiElement);
            if (StringUtils.isEmpty(resolvedString)) {
                return null;
            }

            return resolvedString;
        }

        if (psiElement instanceof Field) {
            return getStringValue(((Field) psiElement).getDefaultValue(), depth);
        }

        if (psiElement instanceof PhpReference) {

            PsiReference psiReference = psiElement.getReference();
            if (psiReference == null) {
                return null;
            }

            PsiElement ref = psiReference.resolve();
            if (ref instanceof PhpReference) {
                return getStringValue(psiElement, depth);
            }

            if (ref instanceof Field) {
                PsiElement resolved = ((Field) ref).getDefaultValue();

                if (resolved instanceof StringLiteralExpression) {
                    return normalizedString((StringLiteralExpression) resolved);
                }
            }
        }

        return null;
    }

    public static String getResolvedParameter(PhpIndex phpIndex, String parameter) {

        // PHP 5.5 class constant: "Class\Foo::class"
        if(parameter.startsWith("#K#C")) {
            // PhpStorm9: #K#C\Class\Foo.class
            if(parameter.endsWith(".class")) {
                return parameter.substring(5, parameter.length() - 6);
            }

            // PhpStorm8: #K#C\Class\Foo.
            // workaround since signature has empty type
            if(parameter.endsWith(".")) {
                return parameter.substring(5, parameter.length() - 1);
            }
        }

        // #P#C\Class\Foo.property
        // #K#C\Class\Foo.CONST
        if(parameter.startsWith("#")) {

            Collection<? extends PhpNamedElement> signTypes = phpIndex.getBySignature(parameter);
            if(signTypes.size() == 0) {
                return "";
            }

            parameter = Utils.getStringValue(signTypes.iterator().next());
            if(parameter == null) {
                return "";
            }
        }

        return parameter;
    }

    public static Boolean isParameter(PsiElement element, PsiElement[] parameters, Integer index) {
        return (parameters.length > index) && (parameters[index].isEquivalentTo(element));
    }
}
