package com.sbg.bdd.screenplay.core.actors;


import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed;

public class OnStage {

    private static final ThreadLocal<Performance> performance = new ThreadLocal<>();

    public static void present(Performance performance) {
        OnStage.performance.set(performance);
    }

    public static Performance performance() {
        return OnStage.performance.get();
    }


    public static ActorOnStage shineSpotlightOn(Actor actor) {
        return theCurrentScene().shineSpotlightOn(actor);
    }
    public static ActorOnStage shineSpotlightOn(String name) {
        return theCurrentScene().shineSpotlightOn(actorNamed(name));
    }

    public static ActorOnStage callActorToStage(Actor actor) {
        return performance().currentScene().callActorToStage(actor);
    }
    public static ActorOnStage callActorToStage(String name) {
        return performance().currentScene().callActorToStage(actorNamed(name));
    }

    public static void dismissActorFromStage(Actor actor) {
        performance().currentScene().dismissActorFromStage(actor);
    }

    public static ActorOnStage theActorInTheSpotlight() {
        return theCurrentScene().theActorInTheSpotlight();
    }

    public static void drawTheCurtain() {
        performance().drawTheCurtain();
    }

    public static Scene raiseTheCurtain(String sceneName) {
        return performance().raiseTheCurtain(sceneName);
    }

    public static Scene theCurrentScene() {
        return performance().currentScene();
    }
}
