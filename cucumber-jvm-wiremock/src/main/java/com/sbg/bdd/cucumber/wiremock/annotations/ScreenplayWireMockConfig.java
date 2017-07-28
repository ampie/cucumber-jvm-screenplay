package com.sbg.bdd.cucumber.wiremock.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScreenplayWireMockConfig {
    Class<? extends ResourceRoots> resourceRoots() default ResourceRoots.class;

    Class<? extends ScreenplayUrls> urls() default ScreenplayUrls.class;

    Class<? extends ScreenplayFactories> factories() default ScreenplayFactories.class;
}
