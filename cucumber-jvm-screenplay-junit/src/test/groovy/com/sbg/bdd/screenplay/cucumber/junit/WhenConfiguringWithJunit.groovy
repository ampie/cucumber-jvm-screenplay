package com.sbg.bdd.screenplay.cucumber.junit

import com.sbg.bdd.cucumber.screenplay.scoped.gsonpersona.GsonPersonaClient
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.wiremock.WireMockMemories
import org.junit.runner.JUnitCore
import org.junit.runner.Result
import spock.lang.Specification
import examplepackage.*;

class WhenConfiguringWithJunit extends Specification{
    def 'it should read the configuration from the ScreenPlayWireMockConfig annotation'(){
        when: 'I run the JUnit configured test'
        JUnitCore junit = new JUnitCore()
        Result result = junit.run(ExampleTest.class)

        then: 'the configured state should reflect the strategies specified in the ScreenPlayWireMockConfig annotation'
        def recall = new WireMockMemories(OnStage.performance())
        recall.theWireMockClient().baseUrl() == new ExampleUrls().theWireMockBaseUrl()
        recall.theBaseUrlOfTheServiceUnderTest().toExternalForm() == new ExampleUrls().theServiceUnderTest()
        recall.thePersonaClient() instanceof GsonPersonaClient

    }
}
