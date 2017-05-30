package com.sbg.bdd.cucumber.screenplay.core.formatter;

import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.core.internal.BasePerformance;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import com.sbg.bdd.screenplay.core.persona.properties.PropertiesPersonaClient;
import com.sbg.bdd.screenplay.core.util.Fields;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.java.JavaBackend;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class FormattingPerformanceBuilder {
    private static ObjectFactory objectFactory;
    private static Map<String, Object> backendState;

    //For testing purposes
    public FormattingPerformanceBuilder(String name, ResourceContainer inputResourceRoot, ResourceContainer outputResourceRoot) {
        this(name,inputResourceRoot,outputResourceRoot, new PropertiesPersonaClient());
    }

    public FormattingPerformanceBuilder(String name, ResourceContainer inputResourceRoot, ResourceContainer outputResourceRoot, PersonaClient<?> personaClient) {
        if (!(OnStage.performance() instanceof BasePerformance)) {
            RuntimeGlue glue = (RuntimeGlue) getBackendState().get("glue");
            ClassFinder classFinder = (ClassFinder) getBackendState().get("classFinder");
            Map<String, StepDefinition> stepDefs = (Map<String, StepDefinition>) Fields.of(glue).asMap().get("stepDefinitionsByPattern");
            Set<Class<?>> classes = new HashSet<>();
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
            BasePerformance performance = new BasePerformance(name, inputResourceRoot,personaClient,new CucumberInstanceGetter(getObjectFactory()));
            performance.getEventBus().scanClasses(classes);
            performance.remember("outputResourceRoot", outputResourceRoot);
            OnStage.present(performance);
        }
    }

    private static ObjectFactory getObjectFactory() {
        if (objectFactory == null && JavaBackend.INSTANCE.get() != null) {
            objectFactory = (ObjectFactory) getBackendState().get("objectFactory");
        }
        return objectFactory;
    }

    private static Map<String, Object> getBackendState() {
        if (backendState == null && JavaBackend.INSTANCE.get() != null) {
            backendState = Fields.of(JavaBackend.INSTANCE.get()).asMap();
        }
        return backendState;
    }

}
