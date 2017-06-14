package com.sbg.bdd.cucumber.screenplay.scoped.plugin;

import com.sbg.bdd.cucumber.common.ScreenPlayFormatter;
import com.sbg.bdd.cucumber.screenplay.core.formatter.MultiPlugin;


public class ScopingPlugin extends MultiPlugin {
    public ScopingPlugin(Appendable out) {
        super(new ScreenPlayFormatter(out),new CucumberScopeLifecycleSync());
    }

}
