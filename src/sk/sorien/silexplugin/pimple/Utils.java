package sk.sorien.silexplugin.pimple;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Stanislav Turza
 */
public class Utils {

    public static final String ARRAY_SIGNATURE = "#C\\array";

    public static Container getContainerFromArrayAccessLiteral(StringLiteralExpression stringLiteralExpression) {

        PsiElement element = stringLiteralExpression.getParent();
        if (!(element instanceof ArrayIndex)) {
            return null;
        }

        PsiElement baseArrayAccessElement = element.getParent();
        if (!(baseArrayAccessElement instanceof ArrayAccessExpression)) {
            return null;
        }

        // find deepest ArrayAccessExpression
        PsiElement children;
        element = baseArrayAccessElement;
        while ((children = PsiTreeUtil.getChildOfAnyType(element, ArrayAccessExpression.class)) != null) {
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

        if (!Utils.isPimpleContainerClass(PhpIndex.getInstance(stringLiteralExpression.getProject()), signature.getClassSignature())) {
            return null;
        }

        PhpIndex phpIndex = PhpIndex.getInstance(stringLiteralExpression.getProject());
        Container container = ContainerResolver.get(stringLiteralExpression.getProject());

        // find proper base container from signature
        for (String parameter : signature.getParameters()) {
            container = container.getContainers().get(resolveParameter(phpIndex, parameter));
            if (container == null)
                return null;
        }

        // find proper container
        while (!element.isEquivalentTo(baseArrayAccessElement) ) {

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
                containerName = resolveParameter(phpIndex, ((MemberReference) arrayIndexElement).getSignature());
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

    public static Boolean isFirstParameterOfPimpleContainerMethod(StringLiteralExpression stringLiteralExpression) {
        PsiElement parameterList = stringLiteralExpression.getParent();
        if (!(parameterList instanceof ParameterList)) {
            return false;
        }

        PsiElement[] params = ((ParameterList) parameterList).getParameters();
        if (!(params.length > 0 && params[0].isEquivalentTo(stringLiteralExpression))) {
            return false;
        }

        PsiElement methodReference = parameterList.getParent();
        if (!(methodReference instanceof MethodReference)) {
            return false;
        }

        // we have extend/raw method
        String methodReferenceName = ((MethodReference) methodReference).getName();
        if ((methodReferenceName == null) || !(methodReferenceName.equals("extend") || methodReferenceName.equals("raw"))) {
            return false;
        }

        String signature = "";

        PsiElement signatureElement = PsiTreeUtil.getChildOfAnyType(methodReference, Variable.class, FieldReference.class);
        if (signatureElement == null) {
            return false;
        }

        if (signatureElement instanceof Variable) {
            signature = ((Variable)signatureElement).getSignature();
        }

        if (signatureElement instanceof FieldReference) {
            signature = ((FieldReference)signatureElement).getSignature();
        }

        // skip simple \array
        if (signature.equals(ARRAY_SIGNATURE)) {
            return false;
        }

        PhpIndex phpIndex = PhpIndex.getInstance(stringLiteralExpression.getProject());
        return Utils.isPimpleContainerClass(phpIndex, signature);
    }

    public static Boolean isPimpleContainerClass(PhpClass phpClass) {

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

        return false;
    }

    public static String getStringValue(@Nullable PsiElement psiElement) {
        return getStringValue(psiElement, 0);
    }

    @Nullable
    private static String getStringValue(@Nullable PsiElement psiElement, int depth) {

        if (psiElement == null || ++depth > 5) {
            return null;
        }

        if (psiElement instanceof StringLiteralExpression) {
            String resolvedString = ((StringLiteralExpression) psiElement).getContents();
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
                    return ((StringLiteralExpression) resolved).getContents();
                }
            }
        }

        return null;
    }

    public static String resolveParameter(PhpIndex phpIndex, String parameter) {

        // PHP 5.5 class constant: workaround since signature has empty type
        // #K#C\Class\Foo.
        if(parameter.startsWith("#K#C") && parameter.endsWith(".")) {
            return parameter.substring(4, parameter.length() - 1);
        }

        // #P#C\Class\Foo.property
        // #K#C\Class\Foo.CONST
        if(parameter.startsWith("#")) {

            Collection<? extends PhpNamedElement> signTypes = phpIndex.getBySignature(parameter, null, 0);
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
}
