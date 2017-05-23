package cucumber.scoping.plugin;

import cucumber.api.java.ObjectFactory;
import cucumber.api.java8.GlueBase;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.java.JavaBackend;
import cucumber.scoping.GlobalScope;
import cucumber.scoping.events.ScreenplayLifecycleSync;
import cucumber.screenplay.internal.InstanceGetter;
import cucumber.scoping.events.ScopeEventBus;
import cucumber.scoping.persona.local.LocalPersonaClient;
import cucumber.scoping.ScopedCastingDirector;
import cucumber.screenplay.actors.OnStage;
import cucumber.screenplay.util.Fields;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class GlobalScopeBuilder implements GlueBase {
    public GlobalScopeBuilder() {
        if (!(OnStage.performance() instanceof GlobalScope)) {
            Map<String, Object> backendState = Fields.of(JavaBackend.INSTANCE.get()).asMap();
            final ObjectFactory objectFactory = (ObjectFactory) backendState.get("objectFactory");
            RuntimeGlue glue = (RuntimeGlue) backendState.get("glue");
            ClassFinder classFinder = (ClassFinder) backendState.get("classFinder");
            ScopeEventBus scopeEventBus = new ScopeEventBus(new InstanceGetter() {
                @Override
                public <T> T getInstance(Class<T> type) {
                    return objectFactory.getInstance(type);
                }
            });
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
            Path resourceRoot = Paths.get("src/test/resources");
            OnStage.present(new GlobalScope("RunAll", resourceRoot, new ScopedCastingDirector(scopeEventBus, new LocalPersonaClient(), resourceRoot), scopeEventBus));
        }

    }
}
