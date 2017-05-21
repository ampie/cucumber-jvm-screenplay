package cucumber.screenplay.formatter;

import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.actors.Cast;
import cucumber.screenplay.actors.CastingDirector;
import cucumber.screenplay.actors.Performance;

public class FormattingPerformance implements Performance {
    private FormattingCastingDirector castingDirector=new FormattingCastingDirector();
    private Cast cast = new Cast(castingDirector);
    private Actor actorInSpotlight;

    @Override
    public CastingDirector getCastingDirector() {
        return castingDirector;
    }

    @Override
    public Cast getCast() {
        return cast;
    }

    @Override
    public Actor shineSpotlightOn(String actorName) {
        actorInSpotlight=cast.actorNamed(actorName);
        return theActorInTheSpotlight();
    }

    @Override
    public Actor theActorInTheSpotlight() {
        return actorInSpotlight;
    }

    @Override
    public void drawTheCurtain() {
        cast.dismissAll();
    }

    @Override
    public ActorOnStage enter(Actor actor) {
        return new FormattingActorOnStage((FormattingActor) actor);
    }
}
