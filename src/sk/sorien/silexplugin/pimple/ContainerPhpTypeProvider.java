package sk.sorien.silexplugin.pimple;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import org.jetbrains.annotations.Nullable;

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

        if (e instanceof ArrayAccessExpression) {
            arrayAccessExpression = (ArrayAccessExpression)e;
        }
        else return null;

        Variable[] variables = PsiTreeUtil.getChildrenOfType(arrayAccessExpression, Variable.class);
        if (variables == null || variables.length != 1) {
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

        // skip simple \array
        String variableSignature = variables[0].getSignature();
        if (variableSignature.equals(Utils.ARRAY_SIGNATURE)) {
            return null;
        }

        return variableSignature + '[' + ((StringLiteralExpression) stringLiteralExpression).getContents() + ']';
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String expression, Project project) {

        int openBraceletIndex = expression.lastIndexOf('[');
        int closeBraceletIndex = expression.lastIndexOf(']');
        if ((openBraceletIndex == -1) || (closeBraceletIndex == -1)) {
            return Collections.emptySet();
        }

        String classSignature = expression.substring(0, openBraceletIndex);
        String parameter = expression.substring(openBraceletIndex + 1, closeBraceletIndex);

        PhpIndex phpIndex = PhpIndex.getInstance(project);

        Collection<? extends PhpNamedElement> classElementCollections = phpIndex.getBySignature(classSignature, null, 0);
        if (classElementCollections.size() == 0) {
            return Collections.emptySet();
        }

        PhpNamedElement phpNamedElement = classElementCollections.iterator().next();
        if (!(phpNamedElement instanceof PhpClass)) {
            return Collections.emptySet();
        }

        if (Utils.extendsPimpleContainerClass((PhpClass) phpNamedElement)) {
            Collection<? extends PhpNamedElement> resolvedElementCollection = resolveElement(project, phpIndex, parameter);
            if (resolvedElementCollection.size() > 0) {
                return resolvedElementCollection;
            }
        }

        return Collections.emptySet();
    }

    private Collection<? extends PhpNamedElement> resolveElement(Project project, PhpIndex phpIndex, String element) {

        Service service = ContainerResolver.getService(project, element);

        if (service != null) {
            return phpIndex.getClassesByFQN(service.getClassName());
        }

//        Parameter parameter = ContainerResolver.getParameter(project, element);
//
//        if (parameter != null) {
//            return phpIndex.getBySignature("#C"+parameter.getFqn());
//        }

        return Collections.emptySet();
    }
}

