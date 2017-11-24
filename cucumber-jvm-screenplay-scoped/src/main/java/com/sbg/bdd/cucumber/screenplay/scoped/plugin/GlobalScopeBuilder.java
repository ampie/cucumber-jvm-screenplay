package com.sbg.bdd.cucumber.screenplay.scoped.plugin;

import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;
import com.sbg.bdd.screenplay.core.internal.InstanceGetter;
import com.sbg.bdd.screenplay.core.internal.PersonaBasedCast;
import com.sbg.bdd.screenplay.core.internal.SimpleInstanceGetter;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import com.sbg.bdd.screenplay.core.util.Fields;
import com.sbg.bdd.screenplay.core.util.ScreenplayConfigurator;
import com.sbg.bdd.screenplay.core.util.ScreenplayMemories;
import com.sbg.bdd.screenplay.scoped.GlobalScope;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.java.JavaBackend;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Extend this class to ensure that Cucumber and Screenplay callbacks are resolved in a consistent way, and that the instantiation
 * of declaring classes is consistent
 */
public abstract class GlobalScopeBuilder {
    private static ScreenplayConfigurator configurator;
    private GlobalScope globalScope;
    public static void configureWith(ScreenplayConfigurator configurator){
        GlobalScopeBuilder.configurator = configurator;
    }
    public GlobalScopeBuilder(Class... extraClasses) {
        if (!(OnStage.performance() instanceof GlobalScope && ((GlobalScope)OnStage.performance()).isActive())) {
            if(configurator == null){
                throw new IllegalStateException("No ScreenplayConfigurator provided. Did you remember to call configureWith(configurator)?");
            }
            ScreenPlayEventBus scopeEventBus = buildIntegratedEventBus(extraClasses);
            globalScope = new GlobalScope(configurator.getName(), new PersonaBasedCast(scopeEventBus, configurator.getPersonaClient(),configurator.getPersonaRoot()), scopeEventBus);
            configurator.applyTo(globalScope);
            OnStage.present(globalScope);
        }
    }
    @Deprecated
    //Rather use the ScreenplayConfigurator
    public GlobalScopeBuilder(String name, ResourceContainer inputResourceRoot, PersonaClient personaClient, Class... extraClasses) {
        if (!(OnStage.performance() instanceof GlobalScope)) {
            ScreenPlayEventBus screenPlayEventBus = buildIntegratedEventBus(extraClasses);
            globalScope = new GlobalScope(name, new PersonaBasedCast(screenPlayEventBus, personaClient, inputResourceRoot), screenPlayEventBus);
            ScreenplayMemories.rememberFor(globalScope.getEverybodyScope())
                    .toReadResourcesFrom(inputResourceRoot)
                    .toUseThePersonaClient(personaClient);
            OnStage.present(globalScope);
        }
    }

    private ScreenPlayEventBus buildIntegratedEventBus(Class[] extraClasses) {
        Map<String, Object> backendState = Fields.of(JavaBackend.INSTANCE.get()).asMap();
        final ObjectFactory objectFactory = (ObjectFactory) backendState.get("objectFactory");
        for (Class aClass : extraClasses) {
            objectFactory.addClass(aClass);
        }
        RuntimeGlue glue = (RuntimeGlue) backendState.get("glue");
        ClassFinder classFinder = (ClassFinder) backendState.get("classFinder");
        final InstanceGetter objectFactory1 = new SimpleInstanceGetter() {
            @Override
            public <T> T getInstance(Class<T> type) {
                T instance = objectFactory.getInstance(type);
                return instance == null ? super.getInstance(type) : instance;
            }
        };
        ScreenPlayEventBus scopeEventBus = new ScreenPlayEventBus(objectFactory1);
        Set<Class<?>> classes = new HashSet<>();
        Map<String, StepDefinition> stepDefs = (Map<String, StepDefinition>) Fields.of(glue).asMap().get("stepDefinitionsByPattern");
        for (StepDefinition sd : stepDefs.values()) {
            Method method = (Method) Fields.of(sd).asMap().get("method");
            if (method != null) {
                Collection<Class<?>> descendants = classFinder.getDescendants(Object.class, method.getDeclaringClass().getPackage().getName());
                for (Class<?> descendant : descendants) {
                    try {
                        descendant.getConstructor();
                        classes.add(descendant);
                    } catch (Exception e) {
                    }
                }
            }
        }
        classes.addAll(Arrays.<Class<?>>asList(extraClasses));
        scopeEventBus.scanClasses(classes);
        return scopeEventBus;
    }

    public GlobalScope getGlobalScope() {
        return (GlobalScope) OnStage.performance();
    }
}
