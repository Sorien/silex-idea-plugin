package sk.sorien.silexplugin.pimple;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import org.jetbrains.annotations.Nullable;
import sk.sorien.silexplugin.SilexProjectComponent;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Stanislav Turza
 */
public class ContainerPhpTypeProvider implements PhpTypeProvider2 {

    @Override
    public char getKey() {
        return 'Š';
    }

    @Nullable
    @Override
    public String getType(PsiElement e) {

        String signature = getTypeForArrayAccess(e);
        if (signature != null) {
            return signature;
        }

        signature = getTypeForParameterOfAnonymousFunction(e);
        if (signature != null) {
            return signature;
        }

        return null;
    }

    private String getTypeForArrayAccess(PsiElement e) {

        ArrayAccessExpression arrayAccessExpression;
        Boolean internalResolve = false;

        if (e instanceof ArrayAccessExpression) {
            arrayAccessExpression = (ArrayAccessExpression)e;
        }
        else if (e instanceof NewExpression)
        {
            ClassReference[] classReferences = PsiTreeUtil.getChildrenOfType(e, ClassReference.class);
            if (classReferences == null || classReferences.length != 1) {
                return null;
            }

            ArrayAccessExpression[] arrayAccessExpressions = PsiTreeUtil.getChildrenOfType(classReferences[0], ArrayAccessExpression.class);
            if (arrayAccessExpressions == null || arrayAccessExpressions.length != 1) {
                return null;
            }

            arrayAccessExpression = arrayAccessExpressions[0];
            internalResolve = true;
        }
        else return null;

        String signature = "";

        PsiElement signatureElement = PsiTreeUtil.getChildOfAnyType(arrayAccessExpression, Variable.class, FieldReference.class);
        if (signatureElement == null) {
            return null;
        }

        if (signatureElement instanceof Variable) {
            signature = ((Variable)signatureElement).getSignature();
        }

        if (signatureElement instanceof FieldReference) {
            signature = ((FieldReference)signatureElement).getSignature();
        }

        // skip simple \array
        if (signature.equals(Utils.ARRAY_SIGNATURE)) {
            return null;
        }

        ArrayIndex arrayIndex = arrayAccessExpression.getIndex();
        if (arrayIndex == null) {
            return null;
        }

        PsiElement element = arrayIndex.getValue();
        String serviceName = "";
        
        if (element instanceof StringLiteralExpression) {
            serviceName = ((StringLiteralExpression) element).getContents();
        }
        else if (element instanceof MemberReference) {
            serviceName = ((MemberReference) element).getSignature();
        }
        else return null;

        return signature + '[' + (internalResolve ? "@" : "") + serviceName + ']';
    }

    private String getTypeForParameterOfAnonymousFunction(PsiElement e) {

        if (!(e instanceof com.jetbrains.php.lang.psi.elements.Parameter)) {
            return null;
        }

        PsiElement element = e.getParent();
        if (!(element instanceof ParameterList)) {
            return  null;
        }

        PsiElement[] anonymousFunctionParams = ((ParameterList) element).getParameters();

        if (anonymousFunctionParams.length == 0) {
            return null;
        }

        element = element.getParent();
        if (!(element instanceof Function)) {
            return null;
        }

        PsiElement closure = element.getParent();
        if (!(closure instanceof PhpExpression)) {
            return null;
        }

        String serviceName = "";

        element = closure.getParent();

        if (element instanceof ParameterList) {

            PsiElement[] methodParams = ((ParameterList) element).getParameters();
            if (methodParams.length == 0) {
                return null;
            }

            element = element.getParent();
            if (!(element instanceof MethodReference)) {
                return null;
            }

            String methodName = ((MethodReference) element).getName();
            if (methodName == null) {
                return null;
            }

            if ((methodName.equals("factory") || methodName.equals("share")) && (methodParams.length == 1) && (methodParams[0].isEquivalentTo(closure)) && (anonymousFunctionParams[0].isEquivalentTo(e))) {
                serviceName = "";

            } else if (methodName.equals("extend") && (methodParams.length == 2) && (methodParams[1].isEquivalentTo(closure))) {
                serviceName = anonymousFunctionParams.length != 2 || anonymousFunctionParams[1].isEquivalentTo(e) ? "" : ((StringLiteralExpression)methodParams[0]).getContents();

            } else return null;

        } else if (element instanceof AssignmentExpression) {

            element = PsiTreeUtil.getChildOfAnyType(element, ArrayAccessExpression.class);
            if (element == null) {
                return null;
            }

        } else return null;

        String signature = "";

        PsiElement signatureElement = PsiTreeUtil.getChildOfAnyType(element, Variable.class, FieldReference.class);
        if (signatureElement == null) {
            return null;
        }

        if (signatureElement instanceof Variable) {
            signature = ((Variable)signatureElement).getSignature();
        }

        if (signatureElement instanceof FieldReference) {
            signature = ((FieldReference)signatureElement).getSignature();
        }

        // skip simple \array
        if (signature.equals(Utils.ARRAY_SIGNATURE)) {
            return null;
        }

        return signature + ( serviceName.isEmpty() ? "" : '[' + serviceName + ']');
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String expression, Project project) {

        if(!SilexProjectComponent.isEnabled(project)) {
            return Collections.emptySet();
        }

        PhpIndex phpIndex = PhpIndex.getInstance(project);

        int openBraceletIndex = expression.lastIndexOf('[');
        int closeBraceletIndex = expression.lastIndexOf(']');

        if ((openBraceletIndex == -1) || (closeBraceletIndex == -1)) {
            return phpIndex.getBySignature(expression);
        }

        String signature = expression.substring(0, openBraceletIndex);
        String parameter = expression.substring(openBraceletIndex + 1, closeBraceletIndex);

        if (Utils.extendsPimpleContainerClass(phpIndex, signature)) {
            String className = getClassNameFromParameter(project, resolveParameter(phpIndex, parameter));

            if (!className.isEmpty()) {
                return phpIndex.getClassesByFQN(className);
            }
        }

        return Collections.emptySet();
    }

    private String getClassNameFromParameter(Project project, String parameter) {

        if (parameter.isEmpty()) {
            return "";
        }

        if (parameter.startsWith("@")) {

            Parameter param = ContainerResolver.getParameter(project, parameter.substring(1));
            return param != null ? param.getValue() : "";
        }

        Service service = ContainerResolver.getService(project, parameter);
        return service != null ? service.getClassName() : "";
    }

    private String resolveParameter(PhpIndex phpIndex, String parameter) {

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

