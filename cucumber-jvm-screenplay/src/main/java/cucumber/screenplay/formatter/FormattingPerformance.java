package cucumber.screenplay.formatter;

import cucumber.api.java.ObjectFactory;
import cucumber.api.java8.GlueBase;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.java.JavaBackend;
import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.actors.Cast;
import cucumber.screenplay.actors.OnStage;
import cucumber.screenplay.actors.Performance;
import cucumber.screenplay.events.ScreenPlayEventBus;
import cucumber.screenplay.internal.BaseActorOnStage;
import cucumber.screenplay.internal.BaseCastingDirector;
import cucumber.screenplay.internal.InstanceGetter;
import cucumber.screenplay.util.Fields;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FormattingPerformance implements Performance, GlueBase {
    private ScreenPlayEventBus eventBus;
    private BaseCastingDirector castingDirector;
    private Cast cast;
    private ActorOnStage actorInSpotlight;

    public FormattingPerformance() {
        if (!(OnStage.performance() instanceof FormattingPerformance)) {
            Map<String, Object> backendState = Fields.of(JavaBackend.INSTANCE.get()).asMap();
            final ObjectFactory objectFactory = (ObjectFactory) backendState.get("objectFactory");
            RuntimeGlue glue = (RuntimeGlue) backendState.get("glue");
            ClassFinder classFinder = (ClassFinder) backendState.get("classFinder");
            eventBus = new ScreenPlayEventBus(new InstanceGetter() {
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
            eventBus.scanClasses(classes);
            OnStage.present(this);
        }
        if(eventBus==null){
            //just for testing purposes
            eventBus=new ScreenPlayEventBus(new InstanceGetter() {
                @Override
                public <T> T getInstance(Class<T> type) {
                    try {
                        return type.newInstance();
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            });
        }
        castingDirector=new BaseCastingDirector(eventBus);
        cast = new Cast(castingDirector);
    }

    @Override
    public Cast getCast() {
        return cast;
    }

    @Override
    public ActorOnStage shineSpotlightOn(Actor actorName) {
        actorInSpotlight = enter(actorName);
        return theActorInTheSpotlight();
    }

    @Override
    public ActorOnStage theActorInTheSpotlight() {
        return actorInSpotlight;
    }

    @Override
    public void drawTheCurtain() {
        cast.dismissAll();
    }

    @Override
    public ActorOnStage enter(Actor actor) {
        return new BaseActorOnStage(actor);
    }

    @Override
    public void exit(Actor actor) {
        cast.dismiss(actor);
    }
}
