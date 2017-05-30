package com.sbg.bdd.cucumber.screenplay.scoped;

import com.sbg.bdd.cucumber.screenplay.scoped.gsonpersona.GsonPersonaClient;
import com.sbg.bdd.cucumber.screenplay.scoped.plugin.GlobalScopeBuilder;
import com.sbg.bdd.resource.ResourceContainer;
import cucumber.api.java8.GlueBase;

public class BuildIt extends GlobalScopeBuilder implements GlueBase {
    private static ResourceContainer inputResourceRoot;
    private static ResourceContainer outputResourceRoot;

    public static void useResourceRoots(ResourceContainer inputResourceRoot, ResourceContainer outputResourceRoot) {
        BuildIt.inputResourceRoot = inputResourceRoot;
        BuildIt.outputResourceRoot = outputResourceRoot;
    }

    public BuildIt() {
        super("RunAll", inputResourceRoot, outputResourceRoot, new GsonPersonaClient());
    }
}
