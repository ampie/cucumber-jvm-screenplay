package com.sbg.bdd.cucumber.wiremock.listeners;

import com.sbg.bdd.cucumber.screenplay.scoped.plugin.CucumberPayloadProducingListener;
import com.sbg.bdd.screenplay.wiremock.listeners.WireMockSync;

public class ScopeManagementListener extends CucumberPayloadProducingListener {


    public ScopeManagementListener() {
        super(new WireMockSync());
    }


}
