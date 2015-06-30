package sk.sorien.silexplugin.tests;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.Arrays;
import java.util.List;

/**
 * @author Stanislav Turza
 */
abstract public class SilexCodeInsightFixtureTestCase extends LightCodeInsightFixtureTestCase {

    public void assertCompletionContains(LanguageFileType languageFileType, String configureByText, String... lookupStrings) {

        myFixture.configureByText(languageFileType, configureByText);
        myFixture.completeBasic();

        completionContainsAssert(lookupStrings);
    }

    public void assertCompletionEquals(LanguageFileType languageFileType, String configureByText, String... lookupStrings) {

        List<String> lookupElements = myFixture.getLookupElementStrings();
        if (lookupElements == null) {
            return;
        }

        if (lookupElements.size() != lookupStrings.length) {
            fail(String.format("completion %s have to contain only %s", lookupElements.toString(), Arrays.toString(lookupStrings)));
        }

        assertCompletionContains(languageFileType, configureByText, lookupStrings);
    }

    private void completionContainsAssert(String[] lookupStrings) {
        List<String> lookupElements = myFixture.getLookupElementStrings();
        for (String s : Arrays.asList(lookupStrings)) {
            if(lookupElements != null && !lookupElements.contains(s)) {
                fail(String.format("completion %s does not contains \"%s\"", lookupElements.toString(), s));
            }
        }
    }

    public void assertCompletionResultEquals(LanguageFileType languageFileType, String configureByText, String result) {
        myFixture.configureByText(languageFileType, configureByText);
        myFixture.completeBasic();
        myFixture.type("\n");
        myFixture.checkResult(result);
    }
}
