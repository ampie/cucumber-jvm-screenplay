package com.sbg.bdd.cucumber.screenplay.core.formatter;

public class SimpleScreenplayPlugin extends MultiPlugin {
    public SimpleScreenplayPlugin(Appendable out){
        super(new ScreenPlayFormatter(out),new CucumberScreenplayLifecycleSync());
    }
}
