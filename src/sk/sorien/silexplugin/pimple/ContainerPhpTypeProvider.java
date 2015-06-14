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

        signature = getTypeForParametersOfExtendMethodAnonymousFunction(e);
        if (signature != null) {
            return signature;
        }

        return null;
    }

    public String getTypeForArrayAccess(PsiElement e) {

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

        PsiElement stringLiteralExpression = arrayIndex.getValue();
        if ((stringLiteralExpression == null) || !(stringLiteralExpression instanceof StringLiteralExpression)) {
            return null;
        }

        return signature + '[' + (internalResolve ? "@" : "") + ((StringLiteralExpression) stringLiteralExpression).getContents() + ']';
    }

    public String getTypeForParametersOfExtendMethodAnonymousFunction(PsiElement e) {

        if (!(e instanceof com.jetbrains.php.lang.psi.elements.Parameter)) {
            return null;
        }

        PsiElement element = e.getParent();
        if (!(element instanceof ParameterList)) {
            return  null;
        }

        Boolean includeServiceName = true;

        PsiElement[] params = ((ParameterList) element).getParameters();
        // is first argument
        if (!(params.length > 0 && params[0].isEquivalentTo(e))) {
            // is second argument
            if (!(params.length > 1 && params[1].isEquivalentTo(e))) {
                return null;
            }

            includeServiceName = false;
        }

        element = element.getParent();
        if (!(element instanceof Function)) {
            return null;
        }

        element = element.getParent();
        if (!(element instanceof PhpExpression)) {
            return null;
        }

        PsiElement closureReference = element;

        element = element.getParent();
        if (!(element instanceof ParameterList)) {
            return null;
        }

        params = ((ParameterList) element).getParameters();
        if (!(params.length > 1 && params[1].isEquivalentTo(closureReference))) {
            return null;
        }

        element = element.getParent();
        if (!(element instanceof MethodReference)) {
            return null;
        }

        // we have extend method
        String methodReferenceName = ((MethodReference) element).getName();
        if ((methodReferenceName == null) || (!methodReferenceName.equals("extend"))) {
            return null;
        }

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

        return signature + ( includeServiceName ? '[' + ((StringLiteralExpression)params[0]).getContents() + ']' : "");
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

        PhpClass phpclass = Utils.getPhpClassFromSignature(phpIndex, signature);

        if (Utils.extendsPimpleContainerClass(phpclass)) {
            return resolveElement(project, phpIndex, parameter);
        }

        return Collections.emptySet();
    }

    private Collection<? extends PhpNamedElement> resolveElement(Project project, PhpIndex phpIndex, String value) {

        if (value.isEmpty()) {
            return Collections.emptySet();
        }

        if (value.startsWith("@")) {

            Parameter parameter = ContainerResolver.getParameter(project, value.substring(1));

            if (parameter != null) {
                return phpIndex.getClassesByFQN(parameter.getValue());
            }

            return Collections.emptySet();
        }

        Service service = ContainerResolver.getService(project, value);

        if (service != null) {
            return phpIndex.getClassesByFQN(service.getClassName());
        }

        return Collections.emptySet();
    }
}

