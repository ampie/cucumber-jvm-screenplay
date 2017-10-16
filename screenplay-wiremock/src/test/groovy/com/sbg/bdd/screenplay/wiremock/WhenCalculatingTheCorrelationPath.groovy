package com.sbg.bdd.screenplay.wiremock

import com.sbg.bdd.screenplay.wiremock.listeners.ScopeManagementListener

class WhenCalculatingTheCorrelationPath extends WhenWorkingWithWireMock {
    def 'it should apply the format {public address}/{port}/{performanceName}/{runId}/{scenarioId}'() {

        given:
        def globalScope = buildGlobalScopeWithoutStarting("AcceptanceRun",ScopeManagementListener)
        initializeWireMock(globalScope)
        def scope = globalScope.startFunctionalScope("functionalTest")
        when:
        def path = CorrelationPath.of(scope)
        then:
        path == "$publicIp/$publicPort/AcceptanceRun/0/functionalTest"

    }
}
