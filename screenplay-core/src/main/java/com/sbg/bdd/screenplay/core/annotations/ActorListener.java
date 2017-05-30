package com.sbg.bdd.screenplay.core.annotations;

import com.sbg.bdd.screenplay.core.ActorOnStage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.sbg.bdd.screenplay.core.annotations.ActorInvolvement.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface ActorListener {
    /**
     * Can also be determined from the method parameter
     *
     * @return
     */
    Class<? extends ActorOnStage> scopeType() default ActorOnStage.class;

    String namePattern() default ".*";

    int sceneLevel() default -1;

    ActorInvolvement[] involvement() default {BEFORE_ENTER_STAGE, AFTER_ENTER_STAGE, INTO_SPOTLIGHT, OUT_OF_SPOTLIGHT, BEFORE_EXIT_STAGE, AFTER_EXIT_STAGE};
}
