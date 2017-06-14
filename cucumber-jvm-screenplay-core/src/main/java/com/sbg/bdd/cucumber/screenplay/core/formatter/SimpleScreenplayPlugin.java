package com.sbg.bdd.cucumber.screenplay.core.formatter;

import com.sbg.bdd.cucumber.common.ScreenPlayFormatter;

public class SimpleScreenplayPlugin extends MultiPlugin {
    public SimpleScreenplayPlugin(Appendable out) {
        super(new ScreenPlayFormatter(out), new CucumberScreenplayLifecycleSync());
    }
}
