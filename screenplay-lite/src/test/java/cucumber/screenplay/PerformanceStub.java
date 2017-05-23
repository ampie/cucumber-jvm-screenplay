package cucumber.screenplay;

import cucumber.screenplay.actors.Cast;
import cucumber.screenplay.actors.Performance;
import cucumber.screenplay.events.ScreenPlayEventBus;
import cucumber.screenplay.internal.BaseActorOnStage;
import cucumber.screenplay.internal.BaseCastingDirector;
import cucumber.screenplay.internal.InstanceGetter;

public class PerformanceStub implements Performance {
    private ScreenPlayEventBus eventBus = new ScreenPlayEventBus(new InstanceGetter() {
        @Override
        public <T> T getInstance(Class<T> type) {
            try {
                return type.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    });
    private BaseCastingDirector castingDirector = new BaseCastingDirector(eventBus);
    private Cast cast = new Cast(castingDirector);
    private ActorOnStage actorInSpotlight;


    @Override
    public Cast getCast() {
        return cast;
    }

    @Override
    public ActorOnStage shineSpotlightOn(Actor actor) {
        return actorInSpotlight = enter(actor);
    }

    @Override
    public ActorOnStage theActorInTheSpotlight() {
        return null;
    }

    @Override
    public void drawTheCurtain() {

    }

    @Override
    public ActorOnStage enter(Actor actor) {
        return new BaseActorOnStage(actor);
    }

    @Override
    public void exit(Actor actor) {

    }

    public ScreenPlayEventBus getEventBus() {
        return eventBus;
    }
}
