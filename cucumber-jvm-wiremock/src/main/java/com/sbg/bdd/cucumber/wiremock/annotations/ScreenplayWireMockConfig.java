package com.sbg.bdd.cucumber.wiremock.annotations;

import com.sbg.bdd.cucumber.wiremock.memorizer.DefaultScreenplayFactories;
import com.sbg.bdd.cucumber.wiremock.memorizer.DefaultWireMockResourceRoots;
import com.sbg.bdd.wiremock.scoped.admin.model.JournalMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScreenplayWireMockConfig {
    String sourceContext() default "web";//e.g. Android, iOS, IE, Firefox, etc.

    String scenarioStatus() default "wip";//e.g. final, regression, backlog, whatever

    JournalMode globalJournalMode() default JournalMode.NONE;

    Class<? extends ResourceRoots> resourceRoots() default DefaultWireMockResourceRoots.class;

    Class<? extends ScreenplayUrls> urls() default ScreenplayUrls.class;

    Class<? extends ScreenplayFactories> factories() default DefaultScreenplayFactories.class;
}
