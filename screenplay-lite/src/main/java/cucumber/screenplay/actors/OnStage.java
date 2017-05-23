package cucumber.screenplay.actors;


import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;

public class OnStage {
    private static final String defaultPeformance = "cucumber.screenplay.formatter.FormattingPerformance";

    private static final ThreadLocal<Performance> performance = new ThreadLocal<>();

    public static void present(Performance performance) {
        OnStage.performance.set(performance);
    }

    public static Performance performance() {
        return OnStage.performance.get();
    }


    public static ActorOnStage shineSpotlightOn(Actor actor){
        return performance().shineSpotlightOn(actor);
    }
    public static ActorOnStage enter(Actor actor){
        return performance().enter(actor);
    }
    public static void exit(Actor actor){
        performance().exit(actor);
    }

    public static ActorOnStage theActorInTheSpotlight() {
        return performance().theActorInTheSpotlight();
    }

    public static void drawTheCurtain() {
        if (performance() != null) {
            performance().drawTheCurtain();
        }
    }
}
