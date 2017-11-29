package com.sbg.bdd.cucumber.screenplay.scoped;

import com.sbg.bdd.cucumber.screenplay.core.formatter.FormattingStepListener;
import com.sbg.bdd.cucumber.screenplay.scoped.gsonpersona.GsonPersonaClient;
import com.sbg.bdd.cucumber.screenplay.scoped.plugin.GlobalScopeBuilder;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.actors.Performance;
import cucumber.api.java8.GlueBase;

public class BuildIt extends GlobalScopeBuilder implements GlueBase {
    private static ResourceContainer inputResourceRoot;
    private static ResourceContainer outputResourceRoot;

    public static void useResourceRoots(ResourceContainer inputResourceRoot, ResourceContainer outputResourceRoot) {
        BuildIt.inputResourceRoot = inputResourceRoot;
        BuildIt.outputResourceRoot = outputResourceRoot;
    }

    public BuildIt() {
        super("RunAll", inputResourceRoot, new GsonPersonaClient(),FormattingStepListener.class);
        getGlobalScope().remember(Performance.OUTPUT_RESOURCE_ROOT,outputResourceRoot);
        getGlobalScope().start();
    }
}
