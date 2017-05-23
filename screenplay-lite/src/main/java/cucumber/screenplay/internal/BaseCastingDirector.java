package cucumber.screenplay.internal;

import cucumber.screenplay.Actor;
import cucumber.screenplay.actors.CastingDirector;
import cucumber.screenplay.events.ScreenPlayEventBus;

public class BaseCastingDirector implements CastingDirector {
    private ScreenPlayEventBus eventBus;

    public BaseCastingDirector(ScreenPlayEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public Actor recruitActor(String name) {
        return new BaseActor(eventBus,name);
    }

    @Override
    public Actor findCandidate(String name) {
        return new BaseActor(eventBus,name);
    }
}
