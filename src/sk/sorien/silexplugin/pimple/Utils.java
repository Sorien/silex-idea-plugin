package sk.sorien.silexplugin.pimple;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;

import java.util.Collection;
import java.util.List;

/**
 * @author Stanislav Turza
 */
public class Utils {

    public static Boolean isArrayAccessLiteralOfPimpleContainer(StringLiteralExpression stringLiteralExpression) {

        PsiElement element = stringLiteralExpression.getParent();
        if (!(element instanceof ArrayIndex)) {
            return false;
        }

        element = element.getParent();
        if (!(element instanceof ArrayAccessExpression)) {
            return false;
        }

        Variable[] variables = PsiTreeUtil.getChildrenOfType(element, Variable.class);
        if (variables == null || variables.length != 1) {
            return false;
        }

        Variable variable = variables[0];

        // skip simple \array
        if (variable.getSignature().equals("#C\\array")) {
            return false;
        }

        PhpIndex phpIndex = PhpIndex.getInstance(variable.getProject());

        Collection<? extends PhpNamedElement> classElementCollections = phpIndex.getBySignature(variable.getSignature(), null, 0);
        if (classElementCollections.size() == 0) {
            return false;
        }

        PhpNamedElement phpNamedElement = classElementCollections.iterator().next();
        if (!(phpNamedElement instanceof PhpClass) || !Utils.extendsPimpleContainerClass((PhpClass) phpNamedElement)) {
            return false;
        }

        return true;
    }

    public static Boolean isArgumentOfPimpleContainerMethod(StringLiteralExpression stringLiteralExpression, String methodName, Integer parameterIndex) {
        PsiElement parameterList = stringLiteralExpression.getParent();
        if (!(parameterList instanceof ParameterList)) {
            return false;
        }

        PsiElement[] params = ((ParameterList) parameterList).getParameters();
        if (!(params.length > parameterIndex && params[parameterIndex].isEquivalentTo(stringLiteralExpression))) {
            return false;
        }

        PsiElement methodReference = parameterList.getParent();
        if (!(methodReference instanceof MethodReference)) {
            return false;
        }

        // we have extend method
        String methodReferenceName = ((MethodReference) methodReference).getName();
        if ((methodReferenceName == null) || (!methodReferenceName.equals(methodName))) {
            return false;
        }

        Variable[] variables = PsiTreeUtil.getChildrenOfType(methodReference, Variable.class);
        if (variables == null || variables.length != 1) {
            return false;
        }

        Variable variable = variables[0];

        // skip simple \array
        if (variable.getSignature().equals("#C\\array")) {
            return false;
        }

        PhpIndex phpIndex = PhpIndex.getInstance(variable.getProject());

        Collection<? extends PhpNamedElement> classElementCollections = phpIndex.getBySignature(variable.getSignature(), null, 0);
        if (classElementCollections.size() == 0) {
            return false;
        }

        PhpNamedElement phpNamedElement = classElementCollections.iterator().next();
        if (!(phpNamedElement instanceof PhpClass) || !Utils.extendsPimpleContainerClass((PhpClass) phpNamedElement)) {
            return false;
        }

        return true;
    }

    public static Boolean extendsPimpleContainerClass(PhpClass phpClass) {

        if (isPimpleContainerBaseClass(phpClass.getFQN())) {
            return true;
        } else {

            ExtendsList extendList = phpClass.getExtendsList();

            List<ClassReference> classReferences = extendList.getReferenceElements();
            if (classReferences != null) {

                for (ClassReference classReference : classReferences) {

                    if (isPimpleContainerBaseClass(classReference.getFQN())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static Boolean isPimpleContainerBaseClass(String className) {
        return className != null && (className.equals("\\Silex\\Application") || className.equals("\\Pimple\\Container") || className.equals("\\Pimple"));
    }
}
