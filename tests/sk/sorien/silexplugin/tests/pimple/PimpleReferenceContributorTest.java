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
public class PimpleReferenceContributorTest extends SilexCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {

        super.setUp();
        Project project = myFixture.getProject();

        // Create virtual container
        Container container = new Container(project);
        container.put(new Service("service", "\\Sorien\\Service1"));
        container.put(new Parameter("parameter", ParameterType.INTEGER, "1"));

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

    public void testContainerReference() throws Exception {

        assertReferenceContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app['servic<caret>e'];",
                "\\Sorien\\Service1"
        );
    }

    public void testContainerExtendFunctionReference() throws Exception {

        assertReferenceContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app->extend('servic<caret>e', );",
                "\\Sorien\\Service1"
        );
    }

    public void testContainerRawFunctionReference() throws Exception {

        assertReferenceContains(PhpFileType.INSTANCE,
                "<?php " +
                        "$app = new \\Silex\\Application(); " +
                        "$app->raw('servic<caret>e', );",
                "\\Sorien\\Service1"
        );
    }
}
