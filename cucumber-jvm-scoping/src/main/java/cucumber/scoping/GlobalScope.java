package cucumber.scoping;


import cucumber.scoping.events.ScopeEventBus;
import cucumber.scoping.events.ScopeEvent;
import cucumber.scoping.events.UserEvent;
import cucumber.scoping.persona.PersonaClient;
import cucumber.scoping.screenplay.ScopedCastingDirector;
import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.actors.Cast;
import cucumber.screenplay.actors.CastingDirector;
import cucumber.screenplay.actors.Performance;

import java.nio.file.Path;

public class GlobalScope extends UserTrackingScope implements Performance {
    private final ScopeEventBus scopeEventBus;
    private final Path resourceRoot
            ;
    private CastingDirector castingDirector;
    private Cast cast;
    public GlobalScope(String name, Path resourceRoot, PersonaClient personaClient, ScopeEventBus scopeEventBus){
        super(null,name);
        this.castingDirector=new ScopedCastingDirector(personaClient, resourceRoot);
        this.cast=new Cast(castingDirector);
        this.scopeEventBus = scopeEventBus;
        this.resourceRoot=resourceRoot;
    }

    public Path getResourceRoot() {
        return resourceRoot;
    }

    @Override
    public Actor shineSpotlightOn(String actorName) {
        return ((ActorOnStage) getInnerMostActive(UserTrackingScope.class).enter(actorName)).getActor();
    }

    @Override
    public Actor theActorInTheSpotlight() {
        UserInScope currentUserInScope = getInnerMostActive(UserTrackingScope.class).getCurrentUserInScope();
        if (currentUserInScope instanceof ActorOnStage) {
            return ((ActorOnStage) currentUserInScope).getActor();
        } else {
            return null;
        }
    }
    public void broadcast(ScopeEvent event){
        scopeEventBus.broadcast(event);
    }
    public void broadcast(UserEvent event){
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
    public ActorOnStage enter(Actor actor) {
        return getInnerMostActive(UserTrackingScope.class).enter(actor);
    }


    public FunctionalScope startFunctionalScope(String name) {
        return setupChild(new FunctionalScope(this, name));
    }

    @Override
    public String getScopePath() {
        return getId();
    }

    @Override
    public CastingDirector getCastingDirector() {
        return castingDirector;
    }

    @Override
    public GlobalScope getGlobalScope() {
        return this;
    }

    public Cast getCast() {
        return cast;
    }

}
