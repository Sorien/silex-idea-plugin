package sk.sorien.pimpleplugin.spellchecker;

import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.spellchecker.PhpSpellcheckingStrategy;
import org.jetbrains.annotations.NotNull;
import sk.sorien.pimpleplugin.ProjectComponent;
import sk.sorien.pimpleplugin.pimple.Container;
import sk.sorien.pimpleplugin.pimple.Utils;

/**
 * @author Stanislav Turza
 */
public class SpellcheckingStrategy extends PhpSpellcheckingStrategy {

    private static final SpellcheckingStrategy.DummyTokenizer DUMMY_TOKENIZER = new SpellcheckingStrategy.DummyTokenizer();

    @NotNull
    @Override
    public Tokenizer getTokenizer(PsiElement element) {

        if(!(element instanceof StringLiteralExpression)) {
            return super.getTokenizer(element);
        }

        if(!ProjectComponent.isEnabled(element.getProject())) {
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
