package cucumber.scoping;


import cucumber.scoping.screenplay.ScopedActor;

import java.util.Map;

public class EverybodyInScope extends UserInScope {
    private EverybodyInScope(UserTrackingScope scope, ScopedActor actor) {
        super(scope, actor);
    }

    public static EverybodyInScope from(UserTrackingScope verificationScope, Map<String, UserInScope> usersInScope) {
        EverybodyInScope everybodyInScope = (EverybodyInScope) usersInScope.get("everybody");
        if (everybodyInScope == null) {
            ScopedActor everybodyActor = (ScopedActor) verificationScope.getGlobalScope().getCast().actorNamed("everybody");
            usersInScope.put("everybody", everybodyInScope = new EverybodyInScope(verificationScope, everybodyActor));
            everybodyInScope.enter();//mm?

        }
        return everybodyInScope;

    }

}
