package com.sbg.bdd.cucumber.screenplay.core;

import com.sbg.bdd.cucumber.screenplay.core.formatter.FormattingPerformanceBuilder;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.persona.properties.PropertiesPersonaClient;
import cucumber.api.java8.GlueBase;

public class SamplePerformanceBuilder extends FormattingPerformanceBuilder implements GlueBase {
    private static ResourceContainer inputResourceRoot;
    private static ResourceContainer outputResourceRoot;

    public static void useResourceRoots(ResourceContainer inputResourceRoot, ResourceContainer outputResourceRoot) {
        SamplePerformanceBuilder.inputResourceRoot = inputResourceRoot;
        SamplePerformanceBuilder.outputResourceRoot = outputResourceRoot;
    }

    public SamplePerformanceBuilder() {
        super("RunIt", inputResourceRoot, outputResourceRoot, new PropertiesPersonaClient());
    }
}
