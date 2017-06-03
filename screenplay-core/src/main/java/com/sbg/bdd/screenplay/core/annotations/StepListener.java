package com.sbg.bdd.screenplay.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.sbg.bdd.screenplay.core.annotations.StepEventType.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StepListener {
    StepEventType[] eventTypes() default {STARTED, SUCCESSFUL, SKIPPED, PENDING, ASSERTION_FAILED, FAILED};
    int stepLevel() default -1;
    String namePattern() default ".*";
}
