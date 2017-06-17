package com.sbg.bdd.screenplay.wiremock

class WhenCalculatingTheCorrelationPath extends WhenWorkingWithWireMock {
    def 'it should apply the format {public address}/{port}/{performanceName}/{runId}/{scenarioId}'() {

        given:
        def globalScope = buildGlobalScopeWithoutStarting("AcceptanceRun", 177)
        def wireMockServer = initializeWireMock(globalScope)
        WireMockMemories.rememberFor(globalScope).withPublicAddress("10.0.0.1")
        def scope = globalScope.startFunctionalScope("functionalTest")
        when:
        def path = CorrelationPath.of(scope)
        then:
        path == "10.0.0.1/${wireMockServer.port()}/AcceptanceRun/177/functionalTest"

    }
}
