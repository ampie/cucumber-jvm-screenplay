package cucumber.screenplay;




import cucumber.screenplay.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
            assertThat(reason(), question.answeredBy(actor), is(true));
        } catch (Throwable actualError) {
            throwComplaintTypeErrorIfSpecified(errorFrom(actualError));
            throwDiagosticErrorIfProvided(errorFrom(actualError));
            throw actualError;
        }
    }

    private String reason() {
        return "Expected " + QuestionSubject.fromClass(question.getClass());
    }

    private void throwDiagosticErrorIfProvided(Error actualError) {
        if (question instanceof QuestionDiagnostics) {
            throw Complaint.from(((QuestionDiagnostics) question).onError(), actualError);
        }
    }

    @Override
    public String toString() {
        String template = explanation.or("Then %s");
        return addRecordedInputValuesTo(String.format(template, subjectText.or(subject)));
    }

    @Override
    public Question<Boolean> getQuestion() {
        return question;
    }
}
