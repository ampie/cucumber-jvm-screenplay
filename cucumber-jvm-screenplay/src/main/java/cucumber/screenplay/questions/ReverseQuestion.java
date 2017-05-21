package cucumber.screenplay.questions;

import cucumber.screenplay.Actor;
import cucumber.screenplay.Question;
import cucumber.screenplay.annotations.Subject;

import java.util.Collections;
import java.util.List;

@Subject("the minimum value of #listQuestion")
public class ReverseQuestion<T> implements Question<List<T>> {

    private final Question<? extends List<T>> listQuestion;

    public ReverseQuestion(Question<? extends List<T>> listQuestion) {
        this.listQuestion = listQuestion;
    }

    @Override
    public List<T> answeredBy(Actor actor) {
        List<T> list = listQuestion.answeredBy(actor);
        Collections.reverse(list);
        return list;
    }
}