package com.sbg.bdd.screenplay.restassured

import com.sbg.bdd.resource.file.RootDirectoryResource
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.internal.BasePerformance
import com.sbg.bdd.screenplay.wiremock.listeners.ScopeManagementListener
import com.sbg.bdd.wiremock.scoped.ScopedWireMock
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer
import com.sbg.bdd.wiremock.scoped.integration.BaseDependencyInjectorAdaptor
import com.sbg.bdd.wiremock.scoped.integration.BaseWireMockCorrelationState
import com.sbg.bdd.wiremock.scoped.integration.DependencyInjectionAdaptorFactory
import com.sbg.bdd.wiremock.scoped.integration.HeaderName
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient
import io.restassured.RestAssured
import io.restassured.filter.FilterContext
import io.restassured.http.Header
import io.restassured.http.Headers
import io.restassured.response.Response
import spock.lang.Specification

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed

class WhenUsingTheInterceptor extends WhenUsingRestAssured {
    def 'it should propagate the current correlation state'() {
        ScopedWireMockServer server = initWireMockAndBasePerformance()
        given:
        def filter = new CorrelationFilter()
        def response = Mock(Response) {
        }
        def requestSpecification
        def context = Mock(FilterContext) {
            next(_, _) >> { args ->
                requestSpecification = args[0]
                return response
            }
        }
        OnStage.raiseTheCurtain("Scene 1")
        when:
        OnStage.shineSpotlightOn(actorNamed('John'))
        filter.filter(RestAssured.given().baseUri('http://localhost:' + server.port() + '/base/url'), null, context)
        then:
        requestSpecification != null

        def headers = requestSpecification.getHeaders()
        headers.getValue(HeaderName.ofTheCorrelationKey()) == 'localhost/' + server.port() + '/Runit/Scene_1/John'

        def value = headers.getValue(HeaderName.ofTheOriginalUrl())
        value == 'http://localhost:'+server.port() +'/base/url'
        headers.getValue(HeaderName.ofTheSequenceNumber()) == '1'
        headers.getValues(HeaderName.ofTheServiceInvocationCount()).size() == 1
        headers.getValues(HeaderName.ofTheServiceInvocationCount()).get(0) == 'http:null://localhost:' + server.port() + '/base/url|1'

    }

    def 'it should sync the current correlation state'() {
        given:

        def server = initWireMockAndBasePerformance()
        def filter = new CorrelationFilter()
        def headers = new Headers(new Header(HeaderName.ofTheServiceInvocationCount(), 'http://localhost:' + server.port() + '/base/urlnull|5'))
        def response = Mock(Response) {
            getHeaders() >> headers
        }
        def context = Mock(FilterContext) {
            next(_, _) >> response
        }
        OnStage.raiseTheCurtain("Scene 1")
        when:
        OnStage.shineSpotlightOn(actorNamed('John'))
        filter.filter(RestAssured.given().baseUri('http://localhost:' + server.port() + '/base/url'), null, context)
        then:
        BaseDependencyInjectorAdaptor.CURRENT_CORRELATION_STATE.getSequenceNumbers().get('http://localhost:' + server.port() + '/base/urlnull') == 5

    }
}
