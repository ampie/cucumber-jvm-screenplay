package cucumber.screenplay.events;


import cucumber.screenplay.annotations.ActorListener;

import java.lang.reflect.Method;

import static cucumber.screenplay.events.ScreenPlayEventBus.mostSpecific;


public class ActorEventCallback extends ScreenPlayEventCallback {
    private final Class<?> type;
    private int sceneLevel;

    public ActorEventCallback(Object target, Method method, ActorListener b) {
        super(target, method, b.namePattern());
        this.sceneLevel = b.sceneLevel();
        type = mostSpecific(method, b.scopeType());
    }

    public boolean isMatch(ActorEvent event) {
        return type.isInstance(event.getActorOnStage()) && levelsMatch(event.getActorOnStage().getScene().getLevel()) &&
                namesMatch(event.getActorOnStage().getActor().getName());
    }

    private boolean levelsMatch(int levelToMatch) {
        return sceneLevel == -1 || levelToMatch == sceneLevel;
    }


}
