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
        container.put(new Container("c1", project).put(new Container("c2", project).put(new Service("s", "\\Sorien\\Service1", project))));
        container.put(new Parameter("c1.p", ParameterType.INTEGER, "1"));

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

    public void testContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['<caret>'];",
                "c1", "c1.p"
        );
    }

    public void testSubContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['c1']['<caret>'];",
                "c2"
        );
    }

    public void testMultilevelContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['c1']['c2']['<caret>'];",
                "s"
        );
    }

    public void testReferencedContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$a = $app['c1']; " +
                        "$a['<caret>']",
                "c2"
        );
    }

    public void testParentContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Sorien\\Application(); " +
                        "$a = $app['<caret>'];",
                "c1", "c1.p"
        );
    }

    public void testExtendFunctionContainerCompletion() throws Exception {

        assertCompletionEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app->extend('<caret>', function($b){});",
                "c1.p"
        );
    }

    public void testExtendFunctionMultiContainerCompletion() throws Exception {

        assertCompletionEquals(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['c1']['c2']->extend('<caret>', function($b){});",
                "s"
        );
    }

    public void testClassConstantInContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "class Test {" +
                        "    const c = \"c1\";" +
                        "}" +
                        "$app = new \\Silex\\Application();" +
                        "$app[Test::c]['<caret>']",
                "c2"
        );
    }

    public void testClassPropertyContainerCompletion() throws Exception {

        assertCompletionContains(PhpFileType.INSTANCE,
                "<?php " +
                        "class Test {" +
                        "    public $c = \"c1\";" +
                        "}" +
                        "$app = new \\Silex\\Application();" +
                        "$class = new Test();" +
                        "$app[$class->c]['<caret>']",
                "c2"
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
                "c1", "c1.p"
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
                "c1", "c1.p"
        );
    }

    public void testDisableOtherCompletions() throws Exception {

        assertCompletionEquals(PhpFileType.INSTANCE,
                "<?php \n" +
                        "$app = new \\Sorien\\Application();" +
                        "$a = $app['e'];" +
                        "$b = $app['f'];" +
                        "$c = $app['g'];" +
                        "$app['<caret>']",

                "c1", "c1.p"
        );
    }
}