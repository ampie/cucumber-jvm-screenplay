package com.sbg.bdd.screenplay.wiremock

import com.github.tomakehurst.wiremock.common.Json
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer
import groovy.json.JsonSlurper

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.wiremock.RequestStrategies.a
import static com.sbg.bdd.wiremock.scoped.recording.strategies.ProxyStrategies.*

class WhenBuildingProxyMappings extends WhenWorkingWithWireMock {

    def 'should create a simple proxy mapping'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun', 5)
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(proxyTo("http://some.host.com/base"))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope(CorrelationPath.of(globalScope.enter(johnSmith)))
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPathPattern'] == '/home/path.*'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == publicAddress + '/'+wireMockServer.port()+'/TestRun/5/.*John_Smith'
        mapping['response']['proxyBaseUrl'] == "http://some.host.com/base"
        mapping['priority'] == (MAX_LEVELS * PRIORITIES_PER_LEVEL) + 5
    }

    def 'should create an intercepting proxy mapping that targets the original service'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun', 5)
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("some.property.name").to(beIntercepted())
        )
        then:
        def mappings = wireMockServer.getMappingsInScope(CorrelationPath.of( globalScope.enter(johnSmith)))
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPathPattern'] == '/resolved/endpoint.*'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == publicAddress + '/'+wireMockServer.port()+'/TestRun/5/.*John_Smith'
        mapping['response']['proxyBaseUrl'] == "http://somehost.com"
        mapping['priority'] == (MAX_LEVELS * PRIORITIES_PER_LEVEL) + 5
    }

    def 'should target the service under test'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun', 5)
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("some.property.name").to(target().theServiceUnderTest().using().theLast(5).segments())
        )
        then:
        def mappings = wireMockServer.getMappingsInScope(CorrelationPath.of(globalScope.enter(johnSmith)))
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPathPattern'] == '/resolved/endpoint.*'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == publicAddress + '/'+wireMockServer.port()+'/TestRun/5/.*John_Smith'
        mapping['response']['proxyBaseUrl'] == 'http://service.com/under/test'
        mapping['response']['transformers'][0] == 'ProxyUrlTransformer'
        mapping['response']['transformerParameters']['action'] == 'use'
        mapping['response']['transformerParameters']['which'] == 'trailing'
        mapping['response']['transformerParameters']['numberOfSegments'] == 5
        mapping['priority'] == (MAX_LEVELS * PRIORITIES_PER_LEVEL) + 4
    }

    def 'should target a specified url'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun', 5)
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("some.property.name").to(target('http://target.com/base').ignoring().theFirst(5).segments())
        )
        then:
        def mappings = wireMockServer.getMappingsInScope(CorrelationPath.of( globalScope.enter(johnSmith)))
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPathPattern'] == '/resolved/endpoint.*'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == publicAddress + '/'+wireMockServer.port()+'/TestRun/5/.*John_Smith'
        mapping['response']['proxyBaseUrl'] == 'http://target.com/base'
        mapping['response']['transformers'][0] == 'ProxyUrlTransformer'
        mapping['response']['transformerParameters']['action'] == 'ignore'
        mapping['response']['transformerParameters']['which'] == 'leading'
        mapping['response']['transformerParameters']['numberOfSegments'] == 5
        mapping['priority'] == (MAX_LEVELS * PRIORITIES_PER_LEVEL) + 4
    }
}
