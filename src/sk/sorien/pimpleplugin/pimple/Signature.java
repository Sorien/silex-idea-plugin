package sk.sorien.pimpleplugin.pimple;

import org.jetbrains.annotations.Nullable;

/**
 * @author Stanislav Turza
 */
public class Signature {

    public String base = "";
    public String parameter = "";

    public Signature(@Nullable String expression) {
        set(expression);
    }

    public Signature() {
    }

    public void set(@Nullable String expression) {

        try
        {
            base = "";
            parameter = "";

            if (expression != null) {
                int len = expression.length();
                int start = -1;
                int counter = 0;

                if (expression.charAt(len - 1) == ']') {
                    for (int i = (len - 2); i >= 0; i--){
                        if ((expression.charAt(i) == '[') && (counter == 0)) {
                            start = i;
                            break;
                        }

                        if ((expression.charAt(i) == ']') ) {
                            counter++;
                        }

                        if ((expression.charAt(i) == '[') ) {
                            counter--;
                        }
                    }
                }

                if (start == -1) {
                    base = expression;
                    return;
                }

                base = expression.substring(0, start);
                parameter = expression.substring(start + 1, len - 1);
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(expression);
        }
    }

    public Boolean hasParameter() {
        return !parameter.isEmpty();
    }

    public Boolean hasValidClassSignature() {
        return !(base.isEmpty() || base.matches("#C\\\\(array|int|integer|float|bool|boolean|string)"));
    }

    @Override
    public String toString() {
        return base + (this.hasParameter() ? "[" + parameter + "]" : "");
    }
}
