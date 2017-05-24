package cucumber.screenplay.annotations;

import cucumber.screenplay.Scene;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static cucumber.screenplay.annotations.SceneEventType.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface SceneListener {
    /**
     * Can also be determined from the method parameter
     * @return
     */
    Class<? extends Scene> sceneType() default Scene.class;
    int level() default -1;
    String namePattern() default ".*";
    SceneEventType[] scopePhases() default {BEFORE_START,AFTER_START,BEFORE_COMPLETE,AFTER_COMPLETE};
}
