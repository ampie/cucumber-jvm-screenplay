package cucumber.screenplay;

import cucumber.screenplay.actors.Performance;
import cucumber.screenplay.internal.BaseActorOnStage;

import java.util.Map;

public interface Scene {
    String getIdentifier();

    Performance getPerformance();

    int getLevel();

    Map<String, ? extends ActorOnStage> getActorsOnStage();

    ActorOnStage callActorToStage(Actor actor);

    void dismissActorFromStage(Actor actor);

    String getName();

    <T> T recall(String variableName);

    void remember(String variableName, Object value);

    ActorOnStage shineSpotlightOn(Actor actor);

    ActorOnStage theActorInTheSpotlight();

}
