package cucumber.scoping;


import cucumber.screenplay.Actor;

public class ActorInScope extends UserInScope {

    public ActorInScope(UserTrackingScope scope, Actor actor) {
        super(scope, actor);
    }


}
