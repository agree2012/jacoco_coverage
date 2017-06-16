/*package com.tngtech.java.junit.dataprovider.internal.placeholder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.Placeholders;


public abstract class BasePlaceholder {

    private final Pattern pattern;

    protected Method method;
    protected int idx;
    protected Object[] parameters;

   
    public BasePlaceholder(String placeholderRegex) {
        this.pattern = Pattern.compile(placeholderRegex);
    }

 
    public void setContext(Method method, int idx, Object[] parameters) {
        this.method = method;
        this.idx = idx;
        this.parameters = Arrays.copyOf(parameters, parameters.length);
    }

    
    public String process(String formatPattern) {
        StringBuffer sb = new StringBuffer();

        Matcher matcher = pattern.matcher(formatPattern);
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(getReplacementFor(matcher.group())));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    
    protected abstract String getReplacementFor(String placeholder);
}*/
