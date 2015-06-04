package sk.sorien.silexplugin.pimple;

import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.ExtendsList;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.List;

/**
 * @author Stanislav Turza
 */
public class Utils {
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
