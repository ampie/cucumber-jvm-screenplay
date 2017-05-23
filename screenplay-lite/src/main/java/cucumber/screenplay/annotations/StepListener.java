package cucumber.screenplay.annotations;

import cucumber.screenplay.events.ScreenPlayEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static cucumber.screenplay.events.ScreenPlayEvent.Type.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StepListener {
    ScreenPlayEvent.Type[] eventTypes() default {STEP_STARTED, STEP_SUCCESSFUL, STEP_SKIPPED, STEP_PENDING, STEP_ASSERTION_FAILED, STEP_FAILED};
    
}
