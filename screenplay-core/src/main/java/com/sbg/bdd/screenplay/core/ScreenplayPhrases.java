package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.actors.OnStage;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import java.util.ArrayList;
import java.util.List;

public class ScreenplayPhrases {
    public static <T extends Actor> T actorNamed(String actorName) {
        return (T) OnStage.performance().getCast().actorNamed(actorName);
    }

    public static <T extends Actor> T givenThat(T actor) {
        return actor;
    }

    public static <T extends ActorOnStage> T givenThat(T actor) {
        return actor;
    }
    
    public static <T extends Actor> T andThat(T actor) {
        return actor;
    }
    
    public static <T extends Actor> T when(T actor) {
        return actor;
    }
    
    public static <T extends Actor> T then(T actor) {
        return actor;
    }

    public static <T extends ActorOnStage> T then(T actor) {
        return actor;
    }

    public static <T extends Actor> T and(T actor) {
        return actor;
    }
    
    public static <T extends Actor> T but(T actor) {
        return actor;
    }

    public static <T> void then(T actual, Matcher<? super T> matcher) {
        MatcherAssert.assertThat(actual, matcher);
    }
    
    public static <T> Consequence<T> seeThat(Question<? extends T> actual, Matcher<T> expected) {
        return new QuestionConsequence(actual, expected);
    }
    
    public static Consequence<Boolean> seeThat(Question<Boolean> actual) {
        return new BooleanQuestionConsequence(actual);
    }
    

    public static <T> Consequence<T>[] seeThat(Question<? extends T> actual, Matcher<T>... expectedMatchers) {
        
        if (thereAreNo(expectedMatchers)) {
            throw new IllegalArgumentException("No matchers supplied");
        } else {
            return consequencesForEachMatcher(actual, expectedMatchers);
        }
    }
    

    private static <T> Consequence<T>[] consequencesForEachMatcher(Question<? extends T> actual, Matcher<T>[] expectedMatchers) {
        List<Consequence<T>> consequences = new ArrayList<>();
        
        for (Matcher<T> matcher : expectedMatchers) {
            consequences.add(new QuestionConsequence(actual, matcher));
        }
        return consequences.toArray(new Consequence[]{});
    }
    
    private static <T> boolean thereAreNo(Matcher<T>[] expectedMatchers) {
        return expectedMatchers.length == 0;
    }
    

    public static <T extends ActorOnStage> T forRequestsFrom(Actor actor) {
        return (T) OnStage.callActorToStage(actor);
    }
}
