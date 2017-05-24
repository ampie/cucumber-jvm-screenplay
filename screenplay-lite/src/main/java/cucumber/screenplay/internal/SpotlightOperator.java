package cucumber.screenplay.internal;

import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.Scene;
import cucumber.screenplay.internal.BaseActorOnStage;

/**
 * Created by ampie on 2017/05/24.
 */
public class SpotlightOperator {
    private final Scene scene;
    private BaseActorOnStage actorInSpotlight;

    public SpotlightOperator(Scene scene) {
        this.scene = scene;
    }

    public ActorOnStage shineSpotlightOn(Actor actor) {
        if (actorInSpotlight == null) {
            shineSpotlightOnScope(actor);
        } else if (spotlightNeedsToMove(actor)) {
            actorInSpotlight.exitSpotlight();
            shineSpotlightOnScope(actor);
        }
        return this.actorInSpotlight;
    }

    private void shineSpotlightOnScope(Actor actor) {
        actorInSpotlight = (BaseActorOnStage) scene.getActorsOnStage().get(actor.getName());
        if (actorInSpotlight == null) {
            actorInSpotlight = (BaseActorOnStage) scene.callActorToStage(actor);
        }
        actorInSpotlight.enterSpotlight();
    }

    private boolean spotlightNeedsToMove(Actor actor) {
        return actor != actorInSpotlight.getActor();
    }

    public void actorOutOfSpotlight() {
        if (actorInSpotlight != null) {
            actorInSpotlight.exitSpotlight();
            actorInSpotlight = null;
        }
    }


    public boolean isSpotlightOn(Actor actor) {
        return this.actorInSpotlight != null && this.actorInSpotlight.getActor() == actor;
    }

    public ActorOnStage getActorInTheSpotlight() {
        return actorInSpotlight;
    }
}
