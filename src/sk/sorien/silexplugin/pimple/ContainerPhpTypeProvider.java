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

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String expression, Project project) {

        if(!SilexProjectComponent.isEnabled(project)) {
            return Collections.emptySet();
        }

        int openBraceletIndex = expression.lastIndexOf('[');
        int closeBraceletIndex = expression.lastIndexOf(']');
        if ((openBraceletIndex == -1) || (closeBraceletIndex == -1)) {
            return Collections.emptySet();
        }

        String signature = expression.substring(0, openBraceletIndex);
        String parameter = expression.substring(openBraceletIndex + 1, closeBraceletIndex);

        PhpIndex phpIndex = PhpIndex.getInstance(project);
        PhpClass phpclass = Utils.getPhpClassFromSignature(phpIndex, signature);

        if (Utils.extendsPimpleContainerClass(phpclass)) {
            return resolveElement(project, phpIndex, parameter);
        }

        return Collections.emptySet();
    }

    private Collection<? extends PhpNamedElement> resolveElement(Project project, PhpIndex phpIndex, String element) {

        if (element.startsWith("@")) {
            Parameter parameter = ContainerResolver.getParameter(project, element.substring(1));

            if (parameter != null) {
                return phpIndex.getClassesByFQN(parameter.getValue());
            }
        } else {
            Service service = ContainerResolver.getService(project, element);

            if (service != null) {
                return phpIndex.getClassesByFQN(service.getClassName());
            }
        }

        return Collections.emptySet();
    }
}

