package sk.sorien.silexplugin.spellchecker;

import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.spellchecker.PhpSpellcheckingStrategy;
import org.jetbrains.annotations.NotNull;
import sk.sorien.silexplugin.SilexProjectComponent;
import sk.sorien.silexplugin.pimple.Container;
import sk.sorien.silexplugin.pimple.Utils;

/**
 * @author Stanislav Turza
 */
public class SilexSpellcheckingStrategy extends PhpSpellcheckingStrategy {

    private static final SilexSpellcheckingStrategy.DummyTokenizer DUMMY_TOKENIZER = new SilexSpellcheckingStrategy.DummyTokenizer();

    @NotNull
    @Override
    public Tokenizer getTokenizer(PsiElement element) {

        if(!(element instanceof StringLiteralExpression)) {
            return super.getTokenizer(element);
        }

        if(!SilexProjectComponent.isEnabled(element.getProject())) {
            return super.getTokenizer(element);
        }

        Container container = Utils.findContainerForPimpleArrayAccessLiteral((StringLiteralExpression) element);
        if (container == null) {

            container = Utils.findContainerForFirstParameterOfPimpleMethod((StringLiteralExpression) element);
            if (container == null) {
                return super.getTokenizer(element);
            }
        }

        return DUMMY_TOKENIZER;
    }

    private static class DummyTokenizer extends Tokenizer {
        public void tokenize(@NotNull final PsiElement element, TokenConsumer consumer) {
        }
    }
}
