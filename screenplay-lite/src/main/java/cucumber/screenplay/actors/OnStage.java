package cucumber.screenplay.actors;


import cucumber.screenplay.Actor;

public class OnStage {
    private static final String defaultPeformance = "cucumber.screenplay.formatter.FormattingPerformance";

    private static final ThreadLocal<Performance> performance = new ThreadLocal<>();

    public static void present(Performance performance) {
        OnStage.performance.set(performance);
    }

    public static Performance performance() {
        Performance result = OnStage.performance.get();
        if (result == null) {
            OnStage.performance.set(result = newDefaultPerformance());
        }
        return result;
    }

    public static Performance newDefaultPerformance() {
        try {
            Class<? extends Performance> aClass = (Class<? extends Performance>) Class.forName(defaultPeformance);
            return aClass.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
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
