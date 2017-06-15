package com.tngtech.java.junit.dataprovider;

import static com.tngtech.java.junit.dataprovider.common.Preconditions.checkArgument;
import static com.tngtech.java.junit.dataprovider.common.Preconditions.checkNotNull;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.runners.model.FrameworkMethod;

import com.tngtech.java.junit.dataprovider.internal.placeholder.BasePlaceholder;


public class DataProviderFrameworkMethod extends FrameworkMethod {

    final int idx;
    final Object[] parameters;
    final String nameFormat;

    public DataProviderFrameworkMethod(Method method, int idx, Object[] parameters, String nameFormat) {
        super(method);

        checkNotNull(parameters, "parameter must not be null");
        checkNotNull(nameFormat, "nameFormat must not be null");
        checkArgument(parameters.length != 0, "parameter must not be empty");

        this.idx = idx;
        this.parameters = Arrays.copyOf(parameters, parameters.length);
        this.nameFormat = nameFormat;
    }
    /*
     public int summ(int a){
        return a+2;
    }*/

    @Override
    public String getName() {
        String result = nameFormat;
        for (BasePlaceholder placeHolder : Placeholders.all()) {
            synchronized (placeHolder) {
                placeHolder.setContext(getMethod(), idx, Arrays.copyOf(parameters, parameters.length));
                result = placeHolder.process(result);
            }
        }
        return result;
    }

    @Override
    public Object invokeExplosively(Object target, Object... params) throws Throwable {
        return super.invokeExplosively(target, parameters);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + idx;
        result = prime * result + ((nameFormat == null) ? 0 : nameFormat.hashCode());
        result = prime * result + Arrays.hashCode(parameters);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DataProviderFrameworkMethod other = (DataProviderFrameworkMethod) obj;
        if (idx != other.idx) {
            return false;
        }
        if (nameFormat == null) {
            if (other.nameFormat != null) {
                return false;
            }
        } else if (!nameFormat.equals(other.nameFormat)) {
            return false;
        }
        if (!Arrays.equals(parameters, other.parameters)) {
            return false;
        }
        return true;
    }
}

