package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import java.util.ArrayList;
import java.util.List;

public class ScreenplayPhrases {
    public static Actor actorNamed(String actorName) {
        return OnStage.performance().getCast().actorNamed(actorName);
    }

    public static <T extends Actor> T givenThat(T actor) {
        actor.useKeyword("Given that");
        return actor;
    }

    public static <T extends ActorOnStage> T givenThat(T actor) {
        actor.getActor().useKeyword("Given that");
        return actor;
    }

    public static <T> Consequence<T> assertThat(String subject, final T value, Matcher<T> matcher) {
        return new QuestionConsequence<T>(subject, new Question<T>() {
            @Override
            public T answeredBy(Actor actor) {
                return value;
            }
        }, matcher);
    }

    public static <T> Consequence<T> seeThat(String subject, final T value, Matcher<T> matcher) {
        return assertThat(subject, value,matcher);
    }

    public static <T extends Actor> T andThat(T actor) {
        actor.useKeyword("And that");
        return actor;
    }
    
    public static Actor when(Actor actor) {
        actor.useKeyword("When");
        return actor;
    }

    public static Actor when(ActorOnStage actor) {
        actor.getActor().useKeyword("When");
        return actor.getActor();
    }

    public static <T extends Actor> T then(T actor) {
        actor.useKeyword("Then");
        return actor;
    }
    public static ActorOnStage forRequestsFrom(ActorOnStage actorOnStage){
        actorOnStage.getActor().useKeyword("Then for requests from");
        return actorOnStage;
    }

    public static  Actor then(ActorOnStage actor) {
        actor.getActor().useKeyword("Then");
        return actor.getActor();
    }

    public static <T extends Actor> T and(T actor) {
        actor.useKeyword("And");
        return actor;
    }
    
    public static <T extends Actor> T but(T actor) {
        actor.useKeyword("But");
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
        actor.useKeyword("For requests from");
        return (T) OnStage.callActorToStage(actor);
    }
}
