package sk.sorien.silexplugin.pimple;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;

public class Signature {

    private String classSignature;
    private ArrayList<String> parameters =  new ArrayList<String>();

    public Signature(String expression) {
        int openBraceletIndex = expression.indexOf('[');
        int closeBraceletIndex = expression.lastIndexOf(']');

        if ((openBraceletIndex == -1) || (closeBraceletIndex == -1)) {
            return;
        }

        classSignature = expression.substring(0, openBraceletIndex);

        String[] split = StringUtils.split(expression.substring(openBraceletIndex + 1, closeBraceletIndex), "][");
        Collections.addAll(parameters, split);
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }

    public String getClassSignature() {
        return classSignature;
    }

    public Boolean hasParameters() {
        return !parameters.isEmpty();
    }
}
