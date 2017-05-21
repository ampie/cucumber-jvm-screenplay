package cucumber.screenplay.actors;


import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;

public interface Performance {
    CastingDirector getCastingDirector();

    Cast getCast();


    Actor shineSpotlightOn(String actorName);

    Actor theActorInTheSpotlight();

    void drawTheCurtain();

    ActorOnStage enter(Actor actor);
}
