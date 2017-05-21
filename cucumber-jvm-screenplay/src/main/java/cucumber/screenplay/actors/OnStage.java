package cucumber.screenplay.actors;


import cucumber.screenplay.Actor;
import cucumber.screenplay.formatter.FormattingPerformance;

public class OnStage {

    private static final ThreadLocal<Performance> performance = new ThreadLocal<>();

    public static void present(Performance performance) {
        OnStage.performance.set(performance);
    }

    public static Performance performance() {
        Performance result = OnStage.performance.get();
        if (result == null) {
            OnStage.performance.set(result = new FormattingPerformance());
        }
        return result;
    }

    public static Actor theActorCalled(String requiredActor) {
        return performance().getCast().actorNamed(requiredActor);
    }

    public static Actor theActorInTheSpotlight() {
        return performance().theActorInTheSpotlight();
    }

    public static void drawTheCurtain() {
        if (performance() != null) {
            performance().drawTheCurtain();
        }
    }
}
