package cucumber.screenplay.questions;



import cucumber.screenplay.Actor;
import cucumber.screenplay.Question;
import cucumber.screenplay.annotations.Subject;

import java.util.Collection;

@Subject("the sum of #listQuestion")
public class SumQuestion implements Question<Integer> {

    private final Question<? extends Collection<Integer>> listQuestion;

    public SumQuestion(Question<? extends Collection<Integer>> listQuestion) {
        this.listQuestion = listQuestion;
    }

    @Override
    public Integer answeredBy(Actor actor) {
        Integer total = 0;
        for (Integer number : listQuestion.answeredBy(actor)) {
            total = total + number;
        }
        return total;
    }
}