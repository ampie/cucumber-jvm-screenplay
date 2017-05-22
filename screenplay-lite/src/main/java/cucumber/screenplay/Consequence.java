package cucumber.screenplay;

public interface Consequence<T> {
    void evaluateFor(Actor actor);

    Consequence<T> orComplainWith(Class<? extends Error> complaintType);

    Consequence<T> orComplainWith(Class<? extends Error> complaintType, String complaintDetails);

    Consequence<T> whenAttemptingTo(Performable performable);

    Consequence<T> because(String explanation);

    Question<? extends T> getQuestion();

}
