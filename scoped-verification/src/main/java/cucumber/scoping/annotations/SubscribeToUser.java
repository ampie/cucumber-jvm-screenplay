package cucumber.scoping.annotations;

import cucumber.scoping.UserInScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static cucumber.scoping.annotations.UserInvolvement.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface SubscribeToUser {
    /**
     * Can also be determined from the method parameter
     * @return
     */
    Class<? extends UserInScope> scopeType() default UserInScope.class;
    int level() default -1;
    String namePattern() default ".*";
    UserInvolvement[] involvement() default {BEFORE_ENTER_STAGE, AFTER_ENTER_STAGE,INTO_SPOTLIGHT,OUT_OF_SPOTLIGHT,BEFORE_EXIT_STAGE,AFTER_EXIT_STAGE};
}
