package com.sbg.bdd.screenplay.core;


import com.sbg.bdd.screenplay.core.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

public class BooleanQuestionConsequence extends BaseConsequence<Boolean> {
    private final Question<Boolean> question;
    private final String subject;

    public BooleanQuestionConsequence(Question<Boolean> actual) {
        this(null, actual);
    }

    public BooleanQuestionConsequence(String subjectText, Question<Boolean> actual) {
        this.question = actual;
        this.subject = QuestionSubject.fromClass(actual.getClass()).andQuestion(actual).subject();
        this.subjectText = Optional.fromNullable(subjectText);
    }

    @Override
    public void evaluateFor(Actor actor) {
        try {
            optionalPrecondition.or(Performable.DO_NOTHING).performAs(actor);
            MatcherAssert.assertThat(reason(), question.answeredBy(actor), Matchers.is(true));
        } catch (Throwable actualError) {
            throwComplaintTypeErrorIfSpecified(errorFrom(actualError));
            throwDiagnosticErrorIfProvided(errorFrom(actualError));
            throw actualError;
        }
    }

    private String reason() {
        return "Expected " + QuestionSubject.fromClass(question.getClass());
    }

    private void throwDiagnosticErrorIfProvided(Error actualError) {
        if (question instanceof QuestionDiagnostics) {
            throw Complaint.from(((QuestionDiagnostics) question).onError(), actualError);
        }
    }

    @Override
    public String toString() {
        String template = explanation.or("see that %s");
        return addRecordedInputValuesTo(String.format(template, subjectText.or(subject)));
    }

    @Override
    public Question<Boolean> getQuestion() {
        return question;
    }
}
