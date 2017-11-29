package com.sbg.bdd.screenplay.scoped.junit;

import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;
import com.sbg.bdd.screenplay.core.internal.PersonaBasedCast;
import com.sbg.bdd.screenplay.core.internal.SimpleInstanceGetter;
import com.sbg.bdd.screenplay.scoped.GlobalScope;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ScopedSuite extends Suite {

    private final GlobalScope globalScope;

    public ScopedSuite(RunnerBuilder builder, Class<?>[] classes) throws InitializationError {
        this(builder, (Class) null, classes);
    }

    public ScopedSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        this(builder, klass, getAnnotatedClasses(klass));
    }

    protected ScopedSuite(RunnerBuilder builder, Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        super(klass, builder.runners(klass, suiteClasses));
        ScreenPlayEventBus eventBus = buildIntegratedEventBus(klass, suiteClasses);
        globalScope = new GlobalScope(klass.getSimpleName(), new PersonaBasedCast(eventBus, null, null), eventBus);
        OnStage.present(globalScope);
        globalScope.start();
        //TODO:
        /**
         * 1. Scan through classes that could have callbacks and register callbacks
         * 2. Determine which configurator to use
         * 3. Apply the configurator to the global scope
         * 4. Start it
         * 5.
         */
    }

    private ScreenPlayEventBus buildIntegratedEventBus(Class<?> suiteClass, Class<?>[] extraClasses) {
        ScreenPlayEventBus scopeEventBus = new ScreenPlayEventBus(new SimpleInstanceGetter());
        Set<Class<?>> classes = new HashSet<>();
        classes.addAll(Arrays.<Class<?>>asList(extraClasses));
        classes.add(suiteClass);
        scopeEventBus.scanClasses(classes);
        return scopeEventBus;
    }

    private static Class<?>[] getAnnotatedClasses(Class<?> klass) throws InitializationError {
        Suite.SuiteClasses annotation = (Suite.SuiteClasses) klass.getAnnotation(Suite.SuiteClasses.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a SuiteClasses annotation", klass.getName()));
        } else {
            return annotation.value();
        }
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
        globalScope.complete();
    }
}
