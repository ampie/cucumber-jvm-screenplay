package com.sbg.bdd.cucumber.screenplay.scoped.plugin;

import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;
import com.sbg.bdd.screenplay.core.internal.BaseCastingDirector;
import com.sbg.bdd.screenplay.core.internal.InstanceGetter;
import com.sbg.bdd.screenplay.core.internal.SimpleInstanceGetter;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import com.sbg.bdd.screenplay.core.util.Fields;
import com.sbg.bdd.screenplay.scoped.GlobalScope;
import com.sbg.bdd.screenplay.scoped.listeners.ScreenplayLifecycleSync;
import cucumber.api.java.ObjectFactory;
import cucumber.api.java8.GlueBase;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.java.JavaBackend;

import java.lang.reflect.Method;
import java.util.*;


public abstract class GlobalScopeBuilder {
    private GlobalScope globalScope;

    public GlobalScopeBuilder(String name, ResourceContainer inputResourceRoot,PersonaClient personaClient, Class ... extraClasses) {
        if (!(OnStage.performance() instanceof GlobalScope)) {
            Map<String, Object> backendState = Fields.of(JavaBackend.INSTANCE.get()).asMap();
            final ObjectFactory objectFactory = (ObjectFactory) backendState.get("objectFactory");
            objectFactory.addClass(ScreenplayLifecycleSync.class);
            for (Class aClass : extraClasses) {
            objectFactory.addClass(aClass);

            }
            RuntimeGlue glue = (RuntimeGlue) backendState.get("glue");
            ClassFinder classFinder = (ClassFinder) backendState.get("classFinder");
            final InstanceGetter objectFactory1 = new SimpleInstanceGetter() {
                @Override
                public <T> T getInstance(Class<T> type) {
                    T instance = objectFactory.getInstance(type);
                    return instance==null?super.getInstance(type):instance;
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
                        try{
                            descendant.getConstructor();
                            classes.add(descendant);
                        }catch (Exception e){

                        }
                    }
                }
            }
            classes.add(ScreenplayLifecycleSync.class);
            classes.addAll(Arrays.<Class<?>>asList(extraClasses));
            scopeEventBus.scanClasses(classes);
            globalScope = new GlobalScope(name, new BaseCastingDirector(scopeEventBus, personaClient, inputResourceRoot), scopeEventBus);
            OnStage.present(globalScope);
        }

    }

    public GlobalScope getGlobalScope() {
        return (GlobalScope) OnStage.performance();
    }
}
