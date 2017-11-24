package com.sbg.bdd.screenplay.scoped.junit;


import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.Question;
import com.sbg.bdd.screenplay.core.Task;
import com.sbg.bdd.screenplay.core.annotations.SceneListener;
import com.sbg.bdd.screenplay.core.annotations.Step;
import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.annotations.Subject;
import com.sbg.bdd.screenplay.core.events.SceneEvent;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import org.junit.Rule;
import org.junit.Test;

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class ScopedTest1 {
    int number1;
    int number2;
    int result;
    @Rule
    public ScopingRule scopingRule = new ScopingRule();

    @SceneListener
    public void listenTo(SceneEvent event) {
        System.out.println(event.getSceneEventType() + ":" + event.getScene().getSceneIdentifier());
    }

    @StepListener
    public void listenTo(StepEvent event) {
        System.out.println(event.getType() + ":" + event.getStepPath());
    }

    @Test
    public void testMe() throws Exception {
        Actor john = actorNamed("John");
        givenThat(john).wasAbleTo(comeUpWithTheNumbers(5, 13));
        when(john).attemptsTo(addTheNumbers());
        then(john).should(seeThat(theResult(), is(equalTo(18))));
    }

    private Question<Integer> theResult() {
        return new Question<Integer>() {
            @Override
            @Subject("the result")
            public Integer answeredBy(Actor actor) {
                return result;
            }
        };
    }

    private Task addTheNumbers() {
        return new Task() {
            @Override
            @Step("add the numbers")
            public <T extends Actor> T performAs(T actor) {
                result = number1 + number2;
                return actor;
            }
        };
    }

    private Task comeUpWithTheNumbers(final int i, final int j) {
        return new Task() {
            final int int1 = i;
            final int int2=j;
            @Override
            @Step("come up with the numbers #int1 and #int2")
            public <T extends Actor> T performAs(T actor) {
                number1 = int1;
                number2 = int2;
                return actor;
            }
        };
    }
}
