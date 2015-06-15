package sk.sorien.silexplugin.pimple;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;

import java.util.Collection;

/**
 * @author Stanislav Turza
 */
public class Utils {

    public static final String ARRAY_SIGNATURE = "#C\\array";

    public static Boolean isArrayAccessLiteralOfPimpleContainer(StringLiteralExpression stringLiteralExpression) {

        PsiElement element = stringLiteralExpression.getParent();
        if (!(element instanceof ArrayIndex)) {
            return false;
        }

        element = element.getParent();
        if (!(element instanceof ArrayAccessExpression)) {
            return false;
        }

        String signature = "";

        PsiElement signatureElement = PsiTreeUtil.getChildOfAnyType(element, Variable.class, FieldReference.class);
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
        if (signature.equals(Utils.ARRAY_SIGNATURE)) {
            return false;
        }

        PhpIndex phpIndex = PhpIndex.getInstance(element.getProject());
        PhpClass phpClass = getPhpClassFromSignature(phpIndex, signature);

        return Utils.extendsPimpleContainerClass(phpClass);
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
        PhpClass phpClass = getPhpClassFromSignature(phpIndex, signature);

        return Utils.extendsPimpleContainerClass(phpClass);
    }

    public static Boolean extendsPimpleContainerClass(PhpClass phpClass) {

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

    public static PhpClass getPhpClassFromSignature(PhpIndex phpIndex, String signature) {

        Collection<? extends PhpNamedElement> classElementCollections = phpIndex.getBySignature(signature, null, 0);
        if (classElementCollections.size() == 0) {
            return null;
        }

        PhpNamedElement element = classElementCollections.iterator().next();

        if (!(element instanceof PhpClass)) {

            if (!(element instanceof Field)) {
                return null;
            }

            classElementCollections = phpIndex.getClassesByFQN(element.getType().toString());
            if (classElementCollections.size() == 0) {
                return null;
            }

            element = classElementCollections.iterator().next();
            if (!(element instanceof PhpClass)) {
                return null;
            }
        }

        return (PhpClass)element;
    }
}
