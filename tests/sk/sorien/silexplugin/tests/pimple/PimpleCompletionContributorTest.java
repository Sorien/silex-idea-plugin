package sk.sorien.silexplugin.tests.pimple;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.PhpFileType;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.pimple.*;
import sk.sorien.silexplugin.tests.SilexCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Stanislav Turza
 */
public class PimpleCompletionContributorTest extends SilexCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {

        super.setUp();
        Project project = myFixture.getProject();

        // Create virtual container
        Container container = new Container(project);
        container.put(new Container("container1", project).put(new Container("container2", project).put(new Service("service", "\\Sorien\\Service1"))));
        container.put(new Parameter("parameter", ParameterType.INTEGER, "1"));
        container.put(new Service("service", "\\Sorien\\Service2"));
        container.put(new Service("service\\fqn", "\\Sorien\\Service2"));

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

    public void testContainerCompletioninInsideApostrophes() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['<caret>'];",
                "container1", "parameter"
        );
    }

    public void testContainerCompletionInsideQuotes() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app[\"<caret>\"];",
                "container1", "parameter"
        );
    }

    public void testSubContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['container1']['<caret>'];",
                "container2"
        );
    }

    public void testMultilevelContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['container1']['container2']['<caret>'];",
                "service"
        );
    }

    public void testReferencedContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$a = $app['container1']; " +
                        "$a['<caret>']",
                "container2"
        );
    }

    public void testParentContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Sorien\\Application(); " +
                        "$a = $app['<caret>'];",
                "container1", "parameter"
        );
    }

    public void testExtendFunctionContainerCompletion() throws Exception {

        assertCompletionEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app->extend('<caret>', function($b){});",
                "service", "parameter", "service\\fqn"
        );
    }

    public void testExtendFunctionMultiContainerCompletion() throws Exception {

        assertCompletionEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['container1']['container2']->extend('<caret>', function($b){});",
                "service"
        );
    }

    public void testRawFunctionContainerCompletion() throws Exception {

        assertCompletionEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app->raw('<caret>');",
                "parameter", "service", "service\\fqn"
        );
    }

    public void testClassConstantInContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "class Test {" +
                        "    const c = \"container1\";" +
                        "}" +
                        "$app = new \\Silex\\Application();" +
                        "$app[Test::c]['<caret>']",
                "container2"
        );
    }

    public void testClassPropertyContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "class Test {" +
                        "    public $c = \"container1\";" +
                        "}" +
                        "$app = new \\Silex\\Application();" +
                        "$class = new Test();" +
                        "$app[$class->c]['<caret>']",
                "container2"
        );
    }

    public void testContainerAsClassVariableCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "class Foo {" +
                        "    /** @var \\Silex\\Application */" +
                        "    protected $app;" +
                        "" +
                        "    public function bar() {" +
                        "        $session = $this->app['<caret>'];" +
                        "    }" +
                        "}",
                "container1", "parameter"
        );
    }

    public void testContainerFromMultiTypeTraitMethod() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "trait TestTrait {" +
                        "" +
                        "    private $app;" +
                        "" +
                        "    protected function getApp() {" +
                        "        if (!$this->app) {" +
                        "            $this->app = $this->createApp();" +
                        "        }" +
                        "        return $this->app;" +
                        "    }" +
                        "" +
                        "    protected function createApp() {" +
                        "        return new \\Silex\\Application();" +
                        "    }" +
                        "}" +
                        "class Test {" +
                        "" +
                        "    use TestTrait;" +
                        "" +
                        "    public function foo() {" +
                        "        $app = $this->getApp();" +
                        "        $app['<caret>'];" +
                        "    }" +
                        "}",
                "container1", "parameter"
        );
    }

    public void testDisableOtherCompletions() throws Exception {

        assertCompletionEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Sorien\\Application();" +
                        "$a = $app['e'];" +
                        "$b = $app['f'];" +
                        "$c = $app['g'];" +
                        "$app['<caret>']",

                "container1", "parameter", "service", "service\\fqn"
        );
    }

    public void testInsertHandlerContainer() throws Exception {
        assertCompletionResultEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Sorien\\Application(); " +
                        "$app['con<caret>tainer2'];",
                "<?php " +
                        "$app = new \\Sorien\\Application(); " +
                        "$app['container1']<caret>;"
        );
    }

    public void testInsertHandlerExtendMethod() throws Exception {
        assertCompletionResultEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['container1']['container2']->extend('<caret>', );",
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['container1']['container2']->extend('service',<caret> );"
        );
    }

    public void testInsertHandlerRawMethod() throws Exception {
        assertCompletionResultEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['container1']['container2']->raw('<caret>');",
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['container1']['container2']->raw('service')<caret>;"
        );
    }

    public void testInsertHandlerRegisterMethod() throws Exception {
        assertCompletionResultEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app->register(null, ['<caret>' => '']);",
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app->register(null, ['parameter' <caret>=> '']);"
        );
    }

    public void testRegisterFunctionValuesCompletions() throws Exception {

        assertCompletionEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application();" +
                        "$app->register(null, ['<caret>' => '']);",

                "parameter"
        );

        assertCompletionEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application();" +
                        "$app->register(null, ['<caret>']);",

                "parameter"
        );

        assertCompletionEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application();" +
                        "$app->register(null, ['' => '', '<caret>']);",

                "parameter"
        );
    }
}