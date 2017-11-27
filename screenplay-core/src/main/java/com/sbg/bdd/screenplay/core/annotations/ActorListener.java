package com.sbg.bdd.screenplay.core.annotations;

import com.sbg.bdd.screenplay.core.Ability;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.persona.Persona;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.sbg.bdd.screenplay.core.annotations.ActorEventType.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface ActorListener {

    Class<? extends Persona> personaType() default Persona.class;
    Class<? extends Ability> abilityType() default Ability.class;
    String actorNamePattern() default ".*";

    ActorEventType[] eventType() default {BEFORE_PERSONA_LOADED,AFTER_PERSONA_LOADED,BEFORE_ABILITY_ADDED, AFTER_ABILITY_ADDED,BEFORE_DISMISSED,AFTER_DISMISSED};
}
