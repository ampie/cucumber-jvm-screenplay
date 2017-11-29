package com.sbg.bdd.cucumber.screenplay.scoped;

import com.sbg.bdd.cucumber.screenplay.scoped.plugin.CucumberPayloadProducingListener;

public class SamplePayloadProducingListener extends CucumberPayloadProducingListener {

    public SamplePayloadProducingListener() {
        super(new SamplePayloadConsumingListener());
    }

}
