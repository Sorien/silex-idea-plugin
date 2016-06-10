package sk.sorien.pimpleplugin.tests.spellchecker;

import com.intellij.spellchecker.inspections.SpellCheckingInspection;
import com.jetbrains.php.lang.PhpFileType;
import org.jetbrains.annotations.NotNull;
import sk.sorien.pimpleplugin.pimple.Container;
import sk.sorien.pimpleplugin.pimple.ContainerResolver;
import sk.sorien.pimpleplugin.tests.CodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Stanislav Turza
 */
public class SpellcheckerTest extends CodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {

        super.setUp();
        ContainerResolver.put(myFixture.getProject(), new Container(myFixture.getProject()));

        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
        myFixture.enableInspections(new SpellCheckingInspection());
    }

    @NotNull
    protected String getTestDataPath() {
        return new File(this.getClass().getResource("../pimple/fixtures").getFile()).getAbsolutePath();
    }

    public void testSpellchecking() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php $app = new \\Silex\\Application(); $app['abrakadabra'];");
        myFixture.testHighlighting(false, false, false);
    }
}
