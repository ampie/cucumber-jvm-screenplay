package cucumber.scoping;


import cucumber.screenplay.*;

import java.util.Map;

public class GuestInScope extends UserInScope implements ActorOnStage {
    public GuestInScope(UserTrackingScope verificationScope, Actor guest) {
        super(verificationScope, guest);
    }


    public static Actor guest(GlobalScope globalScope) {
        return globalScope.getCast().candidateActor("guest");
    }


    public static boolean isGuest(Actor actor) {
        return actor.getName().equals("guest");
    }
}
