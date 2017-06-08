package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.annotations.Subject;

/**
 * Created by ampie on 2017/05/24.
 */
public class DiagnosticQuestionThatFails implements Question<Boolean>, QuestionDiagnostics {
    @Override
    @Subject("a MyDiagnosticError is thrown")
    public Boolean answeredBy(Actor actor) {
        return false;
    }

    @Override
    public Class<? extends AssertionError> onError() {
        return MyDiagnosticError.class;
    }
}
