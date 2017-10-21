package com.sbg.bdd.screenplay.wiremock

import com.sbg.bdd.screenplay.core.ActorOnStage
import com.sbg.bdd.screenplay.core.DownstreamStub
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.annotations.Step
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.screenplay.wiremock.listeners.ScopeManagementListener
import com.sbg.bdd.wiremock.scoped.integration.BaseDependencyInjectorAdaptor
import com.sbg.bdd.wiremock.scoped.integration.DependencyInjectionAdaptorFactory

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.core.actors.OnStage.theCurrentScene

class WhenManagingScopeOnWireMock extends WhenWorkingWithWireMock {

    def 'should keep in sync'() throws Exception {
        given:
        DependencyInjectionAdaptorFactory.useAdapter(new BaseDependencyInjectorAdaptor())
        GlobalScope globalScope = buildGlobalScopeWithoutStarting('TestRun', ScopeManagementListener)
        def wireMockServer = initializeWireMock(globalScope)
        globalScope.start()
        OnStage.present(globalScope)

        when:
        def scope = globalScope.startFunctionalScope('nested1')
        def scenarioScope = scope.startScenario('scenario1')
        def innerStep=null
        forRequestsFrom(actorNamed('John Smith')).allow(new DownstreamStub() {
            @Override
            @Step('step1')
            void performOnStage(ActorOnStage actorOnStage) {
                innerStep=theCurrentScene().recall(WireMockScreenplayContext.CORRELATION_STATE).currentStep
            }
        })
        def userScope = scope.shineSpotlightOn(actorNamed('John Smith'))
        scope.completeNestedScope('scenario1')
        then:
        scope.everybodyScope.recall(WireMockScreenplayContext.CORRELATION_STATE).correlationPath == publicIp + '/'+publicPort+'/TestRun/0/nested1'
        userScope.recall(WireMockScreenplayContext.CORRELATION_STATE).correlationPath == publicIp + '/'+publicPort+'/TestRun/0/nested1/:John_Smith'
        innerStep == 'For_requests_from_John_Smith_comma__allow/step1'
        scenarioScope.everybodyScope.recall(WireMockScreenplayContext.CORRELATION_STATE).currentStep == null
    }



}
