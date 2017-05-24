package cucumber.screenplay;

import cucumber.screenplay.annotations.Subject;

@Subject("the answer")
public class TheAnswerOfFive implements Question<Integer> {

    @Override
    public Integer answeredBy(Actor actor) {
        return 5;
    }
}