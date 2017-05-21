package cucumber.screenplay.questions;


import cucumber.screenplay.Actor;
import cucumber.screenplay.BaseConsequence;
import cucumber.screenplay.Question;
import cucumber.screenplay.QuestionSubject;

public class ConsequenceGroup<T> extends BaseConsequence<T> {

    private final Question<? extends T> questionGroup;
    private final String subject;

    public ConsequenceGroup(Question<? extends T> questionGroup) {
        this.questionGroup = questionGroup;
        this.subject = QuestionSubject.fromClass(questionGroup.getClass()).andQuestion(questionGroup).subject();
    }

    @Override
    public void evaluateFor(Actor actor) {
        questionGroup.answeredBy(actor);
//        try {
//            String groupTitle = "consequence group";
//
//            StepEventBus.getEventBus().stepStarted(ExecutedStepDescription.withTitle(groupTitle));
//
//            questionGroup.answeredBy(actor);
//
//        } catch (Throwable error) {
//            throw error;
//        } finally {
//            StepEventBus.getEventBus().stepFinished();
//        }

    }

    @Override
    public String toString() {
        String template = explanation.or("Then %s");
        return addRecordedInputValuesTo(String.format(template, subjectText.or(subject)));
    }

    @Override
    public Question<? extends T> getQuestion() {
        return questionGroup;
    }
}
