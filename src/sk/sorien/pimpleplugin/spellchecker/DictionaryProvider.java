package sk.sorien.pimpleplugin.spellchecker;

import com.intellij.spellchecker.BundledDictionaryProvider;

/**
 * @author Stanislav Turza
 */
public class DictionaryProvider implements BundledDictionaryProvider {
    public String[] getBundledDictionaries() {
        return new String[]{"silex.dic"};
    }
}