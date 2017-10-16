package examplepackage;

import com.sbg.bdd.cucumber.screenplay.scoped.plugin.GlobalScopeBuilder;
import com.sbg.bdd.cucumber.wiremock.listeners.ScopeManagementListener;
import cucumber.api.java8.GlueBase;

public class ExampleGlue extends GlobalScopeBuilder implements GlueBase {
    public ExampleGlue() {
        super();
        getGlobalScope().start();
    }
}
