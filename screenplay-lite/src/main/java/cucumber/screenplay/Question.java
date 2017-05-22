package cucumber.screenplay;

public interface Question<ANSWER> {
    ANSWER answeredBy(Actor actor);
}
