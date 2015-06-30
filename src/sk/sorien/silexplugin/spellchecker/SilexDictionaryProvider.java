package sk.sorien.silexplugin.spellchecker;

import com.intellij.spellchecker.BundledDictionaryProvider;

/**
 * @author Stanislav Turza
 */
public class SilexDictionaryProvider implements BundledDictionaryProvider {
    public String[] getBundledDictionaries() {
        return new String[]{"silex.dic"};
    }
}