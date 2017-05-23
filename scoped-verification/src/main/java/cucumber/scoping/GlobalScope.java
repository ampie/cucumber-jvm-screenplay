package cucumber.scoping;


import cucumber.scoping.annotations.UserInvolvement;
import cucumber.scoping.events.ScopeEvent;
import cucumber.scoping.events.ScopeEventBus;
import cucumber.scoping.events.UserEvent;
import cucumber.scoping.persona.CharacterType;
import cucumber.scoping.persona.Persona;
import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.actors.Cast;
import cucumber.screenplay.actors.CastingDirector;
import cucumber.screenplay.actors.Performance;
import cucumber.screenplay.internal.BaseActor;

import java.nio.file.Path;

public class GlobalScope extends UserTrackingScope implements Performance {
    private final ScopeEventBus scopeEventBus;
    private final Path resourceRoot;
    private Cast cast;
    private UserInScope actorInSpotlight;

    public GlobalScope(String name, Path resourceRoot, CastingDirector castingDirector, ScopeEventBus scopeEventBus) {
        super(null, name);
        this.cast = new Cast(castingDirector);
        this.scopeEventBus = scopeEventBus;
        this.resourceRoot = resourceRoot;
    }

    public Path getResourceRoot() {
        return resourceRoot;
    }

     @Override
    public ActorOnStage theActorInTheSpotlight() {
        if(actorInSpotlight==null){
            return null;
        }else{
            return shineSpotlightOn(actorInSpotlight.getActor());
        }
    }

    public void broadcast(ScopeEvent event) {
        scopeEventBus.broadcast(event);
    }

    public void broadcast(UserEvent event) {
        if (event.getInvolvement() == UserInvolvement.AFTER_EXIT_STAGE) {
            Persona<?> persona = event.getUserInScope().getActor().recall("persona");
            if (persona != null && persona.getCharacterType() == CharacterType.DYNAMIC) {
                //Data may have changed, needs to be reloaded
                cast.dismiss(event.getUserInScope().getActor());
            }
        }
        scopeEventBus.broadcast(event);
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void drawTheCurtain() {
        getInnerMostActive(UserTrackingScope.class).complete();
    }

    @Override
    public  UserInScope enter(Actor actor) {
        UserTrackingScope userTrackingScope = getInnerMostActive(UserTrackingScope.class);
        if (userTrackingScope == this) {
            return super.enter(actor);
        } else {
            return userTrackingScope.enter(actor);
        }
    }

    @Override
    public void exit(Actor actor) {
        if(this.actorInSpotlight!=null && this.actorInSpotlight.getActor() == actor){
            this.actorInSpotlight = null;
        }
        UserTrackingScope userTrackingScope = getInnerMostActive(UserTrackingScope.class);
        if (userTrackingScope == this) {
             super.exit(actor);
        } else {
             userTrackingScope.exit(actor);
        }
    }

    public ActorOnStage shineSpotlightOn(Actor actor) {
        if(actorInSpotlight == null){
            shineSpotlightOnScope(actor);
        }else if(spotlightNeedsToMove(actor)){
            actorInSpotlight.exitSpotlight();
            shineSpotlightOnScope(actor);
        }
        return this.actorInSpotlight;
    }

    private boolean spotlightNeedsToMove(Actor actor) {
        return actor != actorInSpotlight.getActor() || actorInSpotlight.getScope() != getInnerMostActive(UserTrackingScope.class);
    }

    private void shineSpotlightOnScope(Actor actor) {
        UserTrackingScope userTrackingScope = getInnerMostActive(UserTrackingScope.class);
        if (userTrackingScope == this) {
            this.actorInSpotlight = super.shineSpotlightOn(actor);
        } else {
            this.actorInSpotlight = userTrackingScope.shineSpotlightOn(actor);
        }
    }


    public FunctionalScope startFunctionalScope(String name) {
        return setupChild(new FunctionalScope(this, name));
    }

    @Override
    public String getScopePath() {
        return getId();
    }

    @Override
    public GlobalScope getGlobalScope() {
        return this;
    }

    public Cast getCast() {
        return cast;
    }

}
