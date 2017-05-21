package cucumber.scoping;


import cucumber.scoping.screenplay.ScopedActor;
import cucumber.screenplay.*;

import java.util.Map;

public class GuestInScope extends UserInScope implements ActorOnStage {
    private  GuestInScope(UserTrackingScope verificationScope, ScopedActor guest) {
        super(verificationScope, guest);
    }

    public static GuestInScope from(UserTrackingScope verificationScope, Map<String, UserInScope> usersInScope) {
        GuestInScope guestScope = (GuestInScope) usersInScope.get("guest");
        if (guestScope == null) {
            ScopedActor guestActor = (ScopedActor) verificationScope.getGlobalScope().getCast().actorNamed("guest");
            usersInScope.put("guest", guestScope = new GuestInScope(verificationScope, guestActor));
            guestScope.enter();
        }
        return guestScope;
    }


}
