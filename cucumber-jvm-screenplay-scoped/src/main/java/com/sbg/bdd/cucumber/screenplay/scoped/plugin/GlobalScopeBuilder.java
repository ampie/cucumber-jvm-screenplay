package com.sbg.bdd.cucumber.screenplay.scoped.plugin;

import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;
import com.sbg.bdd.screenplay.core.internal.BaseCastingDirector;
import com.sbg.bdd.screenplay.core.internal.InstanceGetter;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public abstract class GlobalScopeBuilder {
    public GlobalScopeBuilder(String name, ResourceContainer inputResourceRoot, ResourceContainer outputResourceRoot, PersonaClient personaClient) {
        if (!(OnStage.performance() instanceof GlobalScope)) {
            Map<String, Object> backendState = Fields.of(JavaBackend.INSTANCE.get()).asMap();
            final ObjectFactory objectFactory = (ObjectFactory) backendState.get("objectFactory");
            RuntimeGlue glue = (RuntimeGlue) backendState.get("glue");
            ClassFinder classFinder = (ClassFinder) backendState.get("classFinder");
            final InstanceGetter objectFactory1 = new InstanceGetter() {
                @Override
                public <T> T getInstance(Class<T> type) {
                    return objectFactory.getInstance(type);
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
            scopeEventBus.scanClasses(classes);
            OnStage.present(new GlobalScope(name, new BaseCastingDirector(scopeEventBus, personaClient, inputResourceRoot), scopeEventBus));
        }

    }
}
