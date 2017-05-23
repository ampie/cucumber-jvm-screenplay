package cucumber.screenplay.actors;


import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;

public interface Performance {

    Cast getCast();

    ActorOnStage shineSpotlightOn(Actor actor);

    ActorOnStage theActorInTheSpotlight();

    void drawTheCurtain();

    ActorOnStage enter(Actor actor);

    void exit(Actor actor);
}
