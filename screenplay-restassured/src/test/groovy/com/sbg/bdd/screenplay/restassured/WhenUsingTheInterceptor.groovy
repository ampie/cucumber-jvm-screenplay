package com.sbg.bdd.screenplay.restassured

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.wiremock.scoped.integration.BaseDependencyInjectorAdaptor
import com.sbg.bdd.wiremock.scoped.integration.HeaderName
import com.sbg.bdd.wiremock.scoped.integration.RuntimeCorrelationState
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer
import io.restassured.RestAssured
import io.restassured.filter.FilterContext
import io.restassured.http.Header
import io.restassured.http.Headers
import io.restassured.response.Response

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
        headers.getValue(HeaderName.ofTheCorrelationKey()) == 'localhost/' + server.port() + '/Runit/0/Scene_1/:John'

        def value = headers.getValue(HeaderName.ofTheOriginalUrl())
        value == 'http://localhost:'+server.port() +'/base/url'
    }
}
