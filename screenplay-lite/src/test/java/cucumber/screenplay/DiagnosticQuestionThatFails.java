package cucumber.screenplay;

import cucumber.screenplay.annotations.Subject;

/**
 * Created by ampie on 2017/05/24.
 */
public class DiagnosticQuestionThatFails implements Question<Boolean>, QuestionDiagnostics {
    @Override
    @Subject("diagnose it")
    public Boolean answeredBy(Actor actor) {
        return false;
    }

    @Override
    public Class<? extends AssertionError> onError() {
        return MyDiagnosticError.class;
    }
}
