package cucumber.scoping;


import cucumber.scoping.screenplay.ScopedActor;

public class ActorInScope extends UserInScope {

    public ActorInScope(UserTrackingScope scope, ScopedActor actor) {
        super(scope, actor);
    }


}
