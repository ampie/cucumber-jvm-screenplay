package com.sbg.bdd.screenplay.scoped;


import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;

public class GuestInScope extends UserInScope implements ActorOnStage {
    public GuestInScope(UserTrackingScope verificationScope, Actor guest) {
        super(verificationScope, guest);
    }


    public static Actor guest(GlobalScope globalScope) {
        return globalScope.getCast().actorNamed(Actor.GUEST);
    }


    public static boolean isGuest(Actor actor) {
        return actor.getName().equals(Actor.GUEST);
    }
}
