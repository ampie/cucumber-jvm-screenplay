package cucumber.scoping;



import cucumber.screenplay.Actor;

import java.util.Map;

public class EverybodyInScope extends UserInScope {
    private EverybodyInScope(UserTrackingScope scope, Actor actor) {
        super(scope, actor);
    }

    public static EverybodyInScope from(UserTrackingScope verificationScope, Map<String, UserInScope> usersInScope) {
        EverybodyInScope everybodyInScope = (EverybodyInScope) usersInScope.get("everybody");
        if (everybodyInScope == null) {
            Actor everybodyActor = verificationScope.getGlobalScope().getCast().actorNamed("everybody");
            usersInScope.put("everybody", everybodyInScope = new EverybodyInScope(verificationScope, everybodyActor));
            everybodyInScope.enter();//mm?

        }
        return everybodyInScope;

    }
//NB!! Everybody is always there, no entering or exiting.
    public void enter() {
        enterWithoutEvents();
    }

    public void exit() {
        exitWithoutEvents();
    }

}
