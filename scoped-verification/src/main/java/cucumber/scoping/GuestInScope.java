package cucumber.scoping;


import cucumber.screenplay.*;

import java.util.Map;

public class GuestInScope extends UserInScope implements ActorOnStage {
    private  GuestInScope(UserTrackingScope verificationScope, Actor guest) {
        super(verificationScope, guest);
    }

    public static GuestInScope from(UserTrackingScope verificationScope, Map<String, UserInScope> usersInScope) {
        GuestInScope guestScope = (GuestInScope) usersInScope.get("guest");
        if (guestScope == null) {
            Actor guestActor = verificationScope.getGlobalScope().getCast().actorNamed("guest");
            usersInScope.put("guest", guestScope = new GuestInScope(verificationScope, guestActor));
            guestScope.enter();
        }
        return guestScope;
    }


}
