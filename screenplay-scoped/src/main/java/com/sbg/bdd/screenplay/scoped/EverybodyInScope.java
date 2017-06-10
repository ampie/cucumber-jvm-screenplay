package com.sbg.bdd.screenplay.scoped;



import com.sbg.bdd.screenplay.core.Actor;

public class EverybodyInScope extends UserInScope {
    public EverybodyInScope(UserTrackingScope scope, Actor actor) {
        super(scope, actor);
    }

    public static Actor everybody(GlobalScope globalScope) {
        return globalScope.getCast().candidateActor(Actor.EVERYBODY);
    }

    //NB!! Everybody is always there, no entering or exiting.
    public void enterSpotlight() {
    }

    public void exitSpotlight() {

    }
    @Override
    public void enterStage() {
        super.enterStageWithoutEvents();
    }

    @Override
    public void exitStage() {
        //remains active, with memory, for the duration of the containing Scope
    }

    public static boolean isEverybody(Actor actor) {
        return actor.getName().equals(Actor.EVERYBODY);
    }
}
