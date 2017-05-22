package cucumber.scoping.annotations;

import cucumber.scoping.VerificationScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface SubscribeToScope {
    /**
     * Can also be determined from the method parameter
     * @return
     */
    Class<? extends VerificationScope> scopeType() default VerificationScope.class;
    int level() default -1;
    String namePattern() default ".*";
    ScopePhase scopePhase();
}
