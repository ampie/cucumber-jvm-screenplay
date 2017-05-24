package cucumber.screenplay.actors;


import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.Scene;

public class OnStage {
    private static final String defaultPeformance = "cucumber.screenplay.formatter.FormattingPerformance";

    private static final ThreadLocal<Performance> performance = new ThreadLocal<>();

    public static void present(Performance performance) {
        OnStage.performance.set(performance);
    }

    public static Performance performance() {
        return OnStage.performance.get();
    }


    public static ActorOnStage shineSpotlightOn(Actor actor) {
        return currentScene().shineSpotlightOn(actor);
    }

    public static ActorOnStage callActorToStage(Actor actor) {
        return performance().currentScene().callActorToStage(actor);
    }

    public static void dismissActorFromStage(Actor actor) {
        performance().currentScene().dismissActorFromStage(actor);
    }

    public static ActorOnStage theActorInTheSpotlight() {
        return currentScene().theActorInTheSpotlight();
    }

    public static void drawTheCurtain() {
        performance().drawTheCurtain();
    }

    public static Scene raiseTheCurtain(String sceneName) {
        return performance().raiseTheCurtain(sceneName);
    }

    public static Scene currentScene() {
        return performance().currentScene();
    }
}
