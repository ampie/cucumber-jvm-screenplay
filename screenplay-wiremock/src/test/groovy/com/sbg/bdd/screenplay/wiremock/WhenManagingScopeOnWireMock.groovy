package com.sbg.bdd.screenplay.wiremock

import com.sbg.bdd.screenplay.core.ActorOnStage
import com.sbg.bdd.screenplay.core.DownstreamStub
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.annotations.Step
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus
import com.sbg.bdd.screenplay.core.internal.BaseCastingDirector
import com.sbg.bdd.screenplay.core.internal.InstanceGetter
import com.sbg.bdd.screenplay.core.persona.properties.PropertiesPersonaClient
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.screenplay.wiremock.listeners.ScopeManagementListener
import com.sbg.bdd.wiremock.scoped.integration.BaseDependencyInjectorAdaptor
import com.sbg.bdd.wiremock.scoped.integration.DependencyInjectionAdaptorFactory
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient
import com.sbg.bdd.wiremock.scoped.recording.endpointconfig.RemoteEndPointConfigRegistry
import groovy.json.JsonOutput
import org.apache.http.ProtocolVersion
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicStatusLine

import java.io.File

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.core.actors.OnStage.theCurrentScene

class WhenManagingScopeOnWireMock extends WhenWorkingWithWireMock {

    def 'should keep in sync'() throws Exception {
        given:
        DependencyInjectionAdaptorFactory.useAdapter(new BaseDependencyInjectorAdaptor())
        GlobalScope globalScope = buildGlobalScopeWithoutStarting('TestRun', 5, ScopeManagementListener)
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
                innerStep=theCurrentScene().recall('correlationState').currentStep
            }
        })
        def userScope = scope.shineSpotlightOn(actorNamed('John Smith'))
        scope.completeNestedScope('scenario1')
        then:
        scope.everybodyScope.recall('correlationState').correlationPath == 'localhost/'+wireMockServer.port()+'/5/TestRun/nested1'
        userScope.recall('correlationState').correlationPath == 'localhost/'+wireMockServer.port()+'/5/TestRun/nested1/John_Smith'
        innerStep == 'step1'
        scenarioScope.everybodyScope.recall('correlationState').currentStep == null
    }



}
