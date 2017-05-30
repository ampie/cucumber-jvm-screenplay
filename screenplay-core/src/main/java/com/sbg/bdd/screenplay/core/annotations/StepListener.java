package com.sbg.bdd.screenplay.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.sbg.bdd.screenplay.core.annotations.StepEventType.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StepListener {
    StepEventType[] eventTypes() default {STEP_STARTED, STEP_SUCCESSFUL, STEP_SKIPPED, STEP_PENDING, STEP_ASSERTION_FAILED, STEP_FAILED};
    
    String namePattern() default ".*";
}
