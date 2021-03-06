package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.util.Optional;
import com.sbg.bdd.screenplay.core.util.StripRedundantTerms;
import org.hamcrest.Matcher;

import static org.hamcrest.MatcherAssert.assertThat;

public class QuestionConsequence<T> extends BaseConsequence<T> {
    protected final Question<T> question;
    protected final Matcher<T> expected;
    protected final String subject;

    public QuestionConsequence(Question<T> actual, Matcher<T> expected) {
        this(null, actual, expected);
    }

    public QuestionConsequence(String subjectText, Question<T> actual, Matcher<T> expected) {
        this.question = actual;
        this.expected = expected;
        this.subject = QuestionSubject.fromClass(actual.getClass()).andQuestion(actual).subject();
        this.subjectText = Optional.fromNullable(subjectText);
    }

    @Override
    public void evaluateFor(Actor actor) {
        try {
            optionalPrecondition.or(Performable.DO_NOTHING).performAs(actor);
            assertThat(question.answeredBy(actor), expected);
        } catch (Throwable actualError) {
            throwComplaintTypeErrorIfSpecified(errorFrom(actualError));
            throwDiagosticErrorIfProvided(errorFrom(actualError));
            throw actualError;
        }
    }

    @Override
    public Question<? extends T> getQuestion() {
        return question;
    }

    private void throwDiagosticErrorIfProvided(Error actualError) {
        if (question instanceof QuestionDiagnostics) {
            throw Complaint.from(((QuestionDiagnostics) question).onError(), actualError);
        }
    }

    @Override
    public String toString() {
        String template = explanation.or("see that %s is %s");
        String expectedExpression = StripRedundantTerms.from(expected.toString());
        return addRecordedInputValuesTo(String.format(template, subjectText.or(subject), expectedExpression));
    }
}
