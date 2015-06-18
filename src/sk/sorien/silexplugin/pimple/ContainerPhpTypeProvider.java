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
import java.util.List;

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


        PsiElement signatureElement = PsiTreeUtil.getChildOfAnyType(arrayAccessExpression, Variable.class, FieldReference.class, ArrayAccessExpression.class);
        if (signatureElement == null) {
            return null;
        }

        Signature signature = new Signature();

        if (signatureElement instanceof Variable) {
            // skip simple \array
            signature.set(((Variable) signatureElement).getSignature());
            if (signature.getClassSignature().equals(Utils.ARRAY_SIGNATURE)) {
                return null;
            }
        }

        if (signatureElement instanceof FieldReference) {
            signature.set(((FieldReference)signatureElement).getSignature());
        }

        if (signatureElement instanceof ArrayAccessExpression) {
            signature.set(getTypeForArrayAccess(signatureElement));
        }

        if (signature.getClassSignature().isEmpty()) {
            return null;
        }

        ArrayIndex arrayIndex = arrayAccessExpression.getIndex();
        if (arrayIndex == null) {
            return null;
        }

        PsiElement element = arrayIndex.getValue();
        String serviceName;
        
        if (element instanceof StringLiteralExpression) {
            serviceName = ((StringLiteralExpression) element).getContents();
        }
        else if (element instanceof MemberReference) {
            serviceName = ((MemberReference) element).getSignature();
        }
        else return null;

        return signature.toString() + '[' + (internalResolve ? "@" : "") + serviceName + ']';
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

        String serviceName = null;

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
                serviceName = null;

            } else if (methodName.equals("extend") && (methodParams.length == 2) && (methodParams[1].isEquivalentTo(closure))) {
                serviceName = anonymousFunctionParams.length != 2 || anonymousFunctionParams[1].isEquivalentTo(e) ? null : ((StringLiteralExpression)methodParams[0]).getContents();

            } else return null;

        } else if (element instanceof AssignmentExpression) {

            element = PsiTreeUtil.getChildOfAnyType(element, ArrayAccessExpression.class);
            if (element == null) {
                return null;
            }

        } else return null;

        Signature signature = new Signature();

        PsiElement signatureElement = PsiTreeUtil.getChildOfAnyType(element, Variable.class, FieldReference.class, ArrayAccessExpression.class);
        if (signatureElement == null) {
            return null;
        }

        if (signatureElement instanceof Variable) {
            signature.set(((Variable) signatureElement).getSignature());

            System.out.println(((Variable) signatureElement).getType());
        }

        if (signatureElement instanceof FieldReference) {
            signature.set(((FieldReference)signatureElement).getSignature());
        }

        if (signatureElement instanceof ArrayAccessExpression) {
            signature.set(getTypeForArrayAccess(signatureElement));
        }

        // skip simple \array
        if (signature.getClassSignature().equals(Utils.ARRAY_SIGNATURE)) {
            return null;
        }

        return signature.toString() + ( serviceName == null ? "" : '[' + serviceName + ']');
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String expression, Project project) {

        if(!SilexProjectComponent.isEnabled(project)) {
            return Collections.emptySet();
        }

        PhpIndex phpIndex = PhpIndex.getInstance(project);

        Signature signature = new Signature(expression);

        if (signature.hasParameters() && Utils.isPimpleContainerClass(phpIndex, signature.getClassSignature())) {
            return phpIndex.getClassesByFQN(getClassNameFromParameters(phpIndex, project, signature.getParameters()));
        }

        return phpIndex.getBySignature(signature.getClassSignature());
    }

    private String getClassNameFromParameters(PhpIndex phpIndex, Project project, List<String> parameters) {

        Container container = ContainerResolver.get(project);

        for (int i = 0; i < parameters.size() - 1; i++) {
            container = container.getContainers().get(Utils.resolveParameter(phpIndex, parameters.get(i)));
            if (container == null)
                return null;
        }

        String parameter = Utils.resolveParameter(phpIndex, parameters.get(parameters.size() - 1));

        if (parameter.startsWith("@")) {

            Parameter param = container.getParameters().get(parameter.substring(1));
            return param != null ? param.getValue() : null;
        }

        Service service = container.getServices().get(parameter);
        if (service != null) {
            return service.getClassName();
        }

        if (container.getContainers().containsKey(parameter)) {
            return "\\Pimple";
        }

        return null;
    }
}

