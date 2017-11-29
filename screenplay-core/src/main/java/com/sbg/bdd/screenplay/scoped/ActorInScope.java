package com.sbg.bdd.screenplay.scoped;


import com.sbg.bdd.screenplay.core.Actor;

public class ActorInScope extends UserInScope {

    public ActorInScope(UserTrackingScope scope, Actor actor) {
        super(scope, actor);
    }


}
