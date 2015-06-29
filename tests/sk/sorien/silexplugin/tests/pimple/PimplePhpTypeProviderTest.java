package sk.sorien.silexplugin.tests.pimple;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.pimple.*;
import sk.sorien.silexplugin.tests.SilexCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Stanislav Turza
 */
public class PimplePhpTypeProviderTest extends SilexCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {

        super.setUp();
        Project project = myFixture.getProject();

        // Create virtual container
        Container container = new Container(project);
        Container c1 = new Container(project);
        Container c2 = new Container(project);
        c2.getServices().put("service1", new Service("service1", "\\Sorien\\Service1", project));
        c1.getContainers().put("container2", c2);
        container.getContainers().put("container1", c1);
        container.getParameters().put("service2_class", new Parameter("service2_class", ParameterType.STRING, "\\Sorien\\Service2"));

        ContainerResolver.put(myFixture.getProject(), container);

        // Add Fixtures
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
    }

    @Override
    public void tearDown() throws Exception {

        ContainerResolver.remove(myFixture.getProject());
        super.tearDown();
    }

    @NotNull
    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testProviderGetTypeContainerSignature() throws Exception {

        assertTypeSignatureEquals(PhpFileType.INSTANCE, ArrayAccessExpression.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$a<caret>pp['service1'];",
                "#Š#C\\Silex\\Application[service1]"
        );
    }

    public void testProviderGetTypeMultiContainerSignature() throws Exception {

        assertTypeSignatureEquals(PhpFileType.INSTANCE, ArrayAccessExpression.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['service1']['ser<caret>vice2'];",
                "#Š#C\\Silex\\Application[service1][service2]"
        );
    }

    public void testProviderGetTypeConstantPropertyContainerSignature() throws Exception {

        assertTypeSignatureEquals(PhpFileType.INSTANCE, ArrayAccessExpression.class,
                "<?php " +
                        "class Test {" +
                        "    const c = \"container1\";" +
                        "}" +
                        "$app = new \\Silex\\Application();" +
                        "$app[Test::c]['service1<caret>']",
                "#Š#C\\Silex\\Application[#K#C\\Test.c][service1]"
        );
    }

    public void testProviderGetTypeReferencedContainerSignature() throws Exception {

        assertPhpReferenceSignatureEquals(PhpFileType.INSTANCE, Variable.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$a = $app['container1']; " +
                        "$se<caret>rvice = $a['service1']",
                "#Š#Š#C\\Silex\\Application[container1][service1]"
        );
    }

    public void testProviderGetTypeNewExpressionSignature() throws Exception {

        assertTypeSignatureEquals(PhpFileType.INSTANCE, NewExpression.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$abc = new $a<caret>pp['service2_class']();",
                "#Š#C\\Silex\\Application[@service2_class]"
        );
    }

    public void testTypeSignatureToPhpType() throws Exception {

//        assertSignatureEquals("#Š#C\\Silex\\Application[c1]", "\\Pimple");
//        assertSignatureEquals("#Š#C\\Silex\\Application[c1][c2]", "\\Pimple");
        assertSignatureEquals("#Š#C\\Silex\\Application[container1][container2][service1]", "\\Sorien\\Service1");
        assertSignatureEquals("#Š#C\\Silex\\Application[@service2_class]", "\\Sorien\\Service2");
    }
}
