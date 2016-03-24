package sk.sorien.silexplugin.tests.pimple;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.pimple.*;
import sk.sorien.silexplugin.pimple.Parameter;
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
        container.put(new Container("container1", project).put(new Container("container2", project).put(new Service("service1", "\\Sorien\\Service1"))));
        container.put(new Parameter("service2_class", ParameterType.STRING, "\\Sorien\\Service2"));
        container.put(new Service("service2", "\\Sorien\\Service2"));

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

    public void testTypeForArrayAccessContainer() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, ArrayAccessExpression.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$a<caret>pp['service1'];",
                "#Š#C\\Silex\\Application[service1]"
        );
    }

    public void testTypeForArrayAccessMultiContainer() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, ArrayAccessExpression.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['service1']['ser<caret>vice2'];",
                "#Š#C\\Silex\\Application[service1][service2]"
        );
    }

    public void testTypeForArrayAccessContainerConstantKey() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, ArrayAccessExpression.class,
                "<?php " +
                        "class Test {" +
                        "    const c = \"container1\";" +
                        "}" +
                        "$app = new \\Silex\\Application();" +
                        "$app[Test::c]['service1<caret>']",
                "#Š#C\\Silex\\Application[#K#C\\Test.c][service1]"
        );
    }

    public void testTypeForArrayAccessContainerPropertyKey() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, ArrayAccessExpression.class,
                "<?php " +
                        "class Test {" +
                        "    public $property = \"container1\";" +
                        "}" +
                        "$app = new \\Silex\\Application();" +
                        "$class = new Test();" +
                        "$app[$class->property]['service1<caret>']",
                "#Š#C\\Silex\\Application[#P#C\\Test.property][service1]"
        );
    }

    public void testTypeForArrayAccessReferencedContainer() throws Exception {

        assertPhpReferenceSignatureEquals(PhpFileType.INSTANCE, Variable.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$a = $app['container1']; " +
                        "$se<caret>rvice = $a['service1']",
                "#Š#Š#C\\Silex\\Application[container1][service1]"
        );
    }

    public void testTypeForArrayNewExpressionSignature() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, NewExpression.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$abc = new $a<caret>pp['service2_class']();",
                "#Š#C\\Silex\\Application[@service2_class]"
        );
    }

    public void testTypeForParameterFactoryMethod() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, com.jetbrains.php.lang.psi.elements.Parameter.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app[''] = $app->factory(function ($<caret>class) {});",
                "#Š#C\\Silex\\Application"
        );
    }

    public void testTypeForParameterShareMethod() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, com.jetbrains.php.lang.psi.elements.Parameter.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app[''] = $app->share(function ($<caret>class) {});",
                "#Š#C\\Silex\\Application"
        );
    }

    public void testTypeForTypedParameterShareMethod() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, com.jetbrains.php.lang.psi.elements.Parameter.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app[''] = $app->share(function (MyPimpleType $<caret>class) {});",
                null
        );
    }

    public void testTypeForParameterAnonymousFunction() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, com.jetbrains.php.lang.psi.elements.Parameter.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['service2'] = function ($<caret>app) {};",
                "#Š#C\\Silex\\Application"
        );
    }

    public void testTypeForParameterExtendMethodOneParameter() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, com.jetbrains.php.lang.psi.elements.Parameter.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app[''] = $app->extend('service2', function ($<caret>class) {});",
                "#Š#C\\Silex\\Application[service2]"
        );
    }

    public void testTypeForTypedParameterExtendMethodOneParameter() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, com.jetbrains.php.lang.psi.elements.Parameter.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app[''] = $app->extend('service2', function (MyPimpleType $<caret>class) {});",
                null
        );
    }

    public void testTypeForParameterExtendMethodTwoParameters() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, com.jetbrains.php.lang.psi.elements.Parameter.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app[''] = $app->extend('service2', function ($class, $a<caret>pplication) {});",
                "#Š#C\\Silex\\Application"
        );
    }

    public void testTypeForTypedParameterExtendMethodTwoParameters() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, com.jetbrains.php.lang.psi.elements.Parameter.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app[''] = $app->extend('service2', function ($class, MyPimpleType $a<caret>pplication) {});",
                null
        );
    }

    public void testTypeForParameterExtendMethodSubContainer() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, com.jetbrains.php.lang.psi.elements.Parameter.class,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['container1']['container2']->extend('service1', function ($<caret>class) {});",
                "#Š#C\\Silex\\Application[container1][container2][service1]"
        );
    }

    public void testTypeForParameterConstKey() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, com.jetbrains.php.lang.psi.elements.Parameter.class,
                "<?php " +
                        "$app = new \\Silex\\Application();" +
                        "$app['container1']['container2']->extend(\\Sorien\\Service1::name, function ($<caret>class) {});",
                "#Š#C\\Silex\\Application[container1][container2][#K#C\\Sorien\\Service1.name]"
        );
    }

    public void testTypeForParameterPropertyKey() throws Exception {

        assertSignatureEquals(PhpFileType.INSTANCE, com.jetbrains.php.lang.psi.elements.Parameter.class,
                "<?php " +
                        "$app = new \\Silex\\Application();" +
                        "$service2 = new \\Sorien\\Service2();" +
                        "$app->extend($service2->name, function ($<caret>class) {});",
                "#Š#C\\Silex\\Application[#P#C\\Sorien\\Service2.name]"
        );
    }

    public void testProperArrayValueTypeIfServiceCantBeResolved() throws Exception {

        assertTypeEquals(PhpFileType.INSTANCE, Variable.class,
                "<?php " +
                        "class Publication  {};" +
                        "class Foo {" +
                        "    /** @var Publication[] */" +
                        "    public $publications;" +
                        "    public function collect() {" +
                        "        $<caret>a = $this->publications['test'];" +
                        "    }" +
                        "};",
                "\\Publication"
        );
    }

    public void testDoNotResolveArrayOfSimpleType() throws Exception {

        String fixture = "<?php " +
                        "/** @var %type%[] $array */" +
                        "$array = [1, 2, 3];" +
                        "$index = 0;" +
                        "$<caret>foo = $array[$index];";

        assertSignatureEquals(PhpFileType.INSTANCE, Variable.class, fixture.replace("%type", "array"), null);
        assertSignatureEquals(PhpFileType.INSTANCE, Variable.class, fixture.replace("%type", "int"), null);
        assertSignatureEquals(PhpFileType.INSTANCE, Variable.class, fixture.replace("%type", "integer"), null);
        assertSignatureEquals(PhpFileType.INSTANCE, Variable.class, fixture.replace("%type", "bool"), null);
        assertSignatureEquals(PhpFileType.INSTANCE, Variable.class, fixture.replace("%type", "boolean"), null);
    }

    public void testResolveSignatureToPhpClass() throws Exception {

//        assertSignatureEqualsType("#Š#C\\Silex\\Application[c1]", "\\Pimple");
//        assertSignatureEqualsType("#Š#C\\Silex\\Application[c1][c2]", "\\Pimple");
        assertSignatureEqualsType("#Š#C\\Silex\\Application[@service2_class]", "\\Sorien\\Service2");
        assertSignatureEqualsType("#Š#C\\Silex\\Application[container1][container2][service1]", "\\Sorien\\Service1");
        assertSignatureEqualsType("#Š#C\\Silex\\Application[container1][container2][#K#C\\Sorien\\Service1.name]", "\\Sorien\\Service1");
        assertSignatureEqualsType("#Š#C\\Silex\\Application[#P#C\\Sorien\\Service2.name]", "\\Sorien\\Service2");
    }
}
