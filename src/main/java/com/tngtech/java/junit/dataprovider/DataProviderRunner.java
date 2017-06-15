package com.tngtech.java.junit.dataprovider;

import static com.tngtech.java.junit.dataprovider.common.Preconditions.checkNotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import com.tngtech.java.junit.dataprovider.UseDataProvider.ResolveStrategy;
import com.tngtech.java.junit.dataprovider.internal.DataConverter;
import com.tngtech.java.junit.dataprovider.internal.DefaultDataProviderMethodResolver;
import com.tngtech.java.junit.dataprovider.internal.TestGenerator;
import com.tngtech.java.junit.dataprovider.internal.TestValidator;
/*
public class DataProviderRunner extends BlockJUnit4ClassRunner {

    protected DataConverter dataConverter;

    protected TestGenerator testGenerator;

    protected TestValidator testValidator;

    List<FrameworkMethod> computedTestMethods;

    Map<FrameworkMethod, List<FrameworkMethod>> dataProviderMethods;

    public DataProviderRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        // initialize testValidator, testGenerator and dataConverter here because "super" in constructor already calls
        // this, i.e. fields are not initialized yet but required in super.collectInitializationErrors(errors) ...
        initializeHelpers();

        super.collectInitializationErrors(errors);
    }

    
    protected void initializeHelpers() {
        dataConverter = new DataConverter();
        testGenerator = new TestGenerator(dataConverter);
        testValidator = new TestValidator(dataConverter);
    }

    
    @Override
    @Deprecated
    protected void validateInstanceMethods(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(After.class, false, errors);
        validatePublicVoidNoArgMethods(Before.class, false, errors);
        validateTestMethods(errors);

        if (errors.isEmpty() && computeTestMethods().size() == 0) {
            errors.add(new Exception("No runnable methods"));
        }
    }

    
    @Override
    protected void validateTestMethods(List<Throwable> errors) {
        checkNotNull(errors, "errors must not be null");

        // This method cannot use the result of "computeTestMethods()" because the method ignores invalid test methods
        // and dataproviders silently (except if a dataprovider method cannot be called). However, the common errors
        // are not raised as {@link RuntimeException} to go the JUnit way of detecting errors. This implies that we have
        // to browse the whole class for test methods and dataproviders again :-(.

        for (FrameworkMethod testMethod : getTestClassInt().getAnnotatedMethods(Test.class)) {
            testValidator.validateTestMethod(testMethod, errors);
        }
        for (FrameworkMethod testMethod : getTestClassInt().getAnnotatedMethods(UseDataProvider.class)) {
            List<FrameworkMethod> dataProviderMethods = getDataProviderMethods(testMethod);
            if (dataProviderMethods.isEmpty()) {
                Class<? extends DataProviderMethodResolver>[] resolvers = testMethod.getAnnotation(UseDataProvider.class).resolver();

                String message = "No valid dataprovider found for test '" + testMethod.getName() + "' using ";
                if (resolvers.length == 1 && DefaultDataProviderMethodResolver.class.equals(resolvers[0])) {
                    message += "the default resolver. By convention the dataprovider method name must either be equal to the test methods name, have a certain replaced or additional prefix (see JavaDoc of "
                            + DefaultDataProviderMethodResolver.class + " or is explicitely set by @UseDataProvider#value()";
                } else {
                    message += "custom resolvers: " + Arrays.toString(resolvers)
                    + ". Please examine their javadoc and / or implementation.";
                }
                errors.add(new Exception(message));

            } else {
                for (FrameworkMethod dataProviderMethod : dataProviderMethods) {
                    DataProvider dataProvider = dataProviderMethod.getAnnotation(DataProvider.class);
                    if (dataProvider == null) {
                        throw new IllegalStateException(String.format("@%s annotation not found on dataprovider method %s",
                                DataProvider.class.getSimpleName(), dataProviderMethod.getName()));
                    }
                    testValidator.validateDataProviderMethod(dataProviderMethod, dataProvider, errors);
                }
            }
        }
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        if (computedTestMethods == null) {
            // Further method for generation is required due to stubbing of "super.computeTestMethods()" is not possible
            computedTestMethods = generateExplodedTestMethodsFor(super.computeTestMethods());
        }
        return computedTestMethods;
    }

   
    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        checkNotNull(filter, "filter must not be null");
        super.filter(new DataProviderFilter(filter));
    }

    
    TestClass getTestClassInt() {
        return getTestClass();
    }

  
    List<FrameworkMethod> generateExplodedTestMethodsFor(List<FrameworkMethod> testMethods) {
        List<FrameworkMethod> result = new ArrayList<FrameworkMethod>();
        if (testMethods == null) {
            return result;
        }
        for (FrameworkMethod testMethod : testMethods) {
            for (FrameworkMethod dataProviderMethod : getDataProviderMethods(testMethod)) {
                result.addAll(testGenerator.generateExplodedTestMethodsFor(testMethod, dataProviderMethod));
            }
        }
        return result;
    }

    
    List<FrameworkMethod> getDataProviderMethods(FrameworkMethod testMethod) {
        // initialize field here as this method is called via constructors super(...) => fields are not initialized yet
        if (dataProviderMethods == null) {
            dataProviderMethods = new HashMap<FrameworkMethod, List<FrameworkMethod>>();
        }
        if (dataProviderMethods.containsKey(testMethod)) {
            return dataProviderMethods.get(testMethod);
        }
        List<FrameworkMethod> result = new ArrayList<FrameworkMethod>();

        UseDataProvider useDataProvider = testMethod.getAnnotation(UseDataProvider.class);
        if (useDataProvider == null) {
            result.add(null);
        } else {
            for (Class<? extends DataProviderMethodResolver> resolverClass : useDataProvider.resolver()) {
                DataProviderMethodResolver resolver = getResolverInstanceInt(resolverClass);

                List<FrameworkMethod> dataProviderMethods = resolver.resolve(testMethod, useDataProvider);
                if (ResolveStrategy.UNTIL_FIRST_MATCH.equals(useDataProvider.resolveStrategy()) && !dataProviderMethods.isEmpty()) {
                    result.addAll(dataProviderMethods);
                    break;

                } else if (ResolveStrategy.AGGREGATE_ALL_MATCHES.equals(useDataProvider.resolveStrategy())) {
                    result.addAll(dataProviderMethods);
                }
            }
        }
        dataProviderMethods.put(testMethod, result);
        return result;
    }

   
    DataProviderMethodResolver getResolverInstanceInt(Class<? extends DataProviderMethodResolver> resolverClass) {
        Constructor<? extends DataProviderMethodResolver> constructor;
        try {
            constructor = resolverClass.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find default constructor to instantiate resolver " + resolverClass, e);
        } catch (SecurityException e) {
            throw new IllegalStateException(
                    "Security violation while trying to access default constructor to instantiate resolver " + resolverClass, e);
        }

        try {
            return constructor.newInstance();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access default constructor to instantiate resolver " + resolverClass, e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Could not instantiate resolver " + resolverClass + " using default constructor", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("The default constructor of " + resolverClass + " has thrown an exception", e);
        }
    }
}
*/
