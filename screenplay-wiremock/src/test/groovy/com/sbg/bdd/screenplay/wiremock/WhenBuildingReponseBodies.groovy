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
import static com.sbg.bdd.wiremock.scoped.client.strategies.ResponseBodyStrategies.*

class WhenBuildingReponseBodies extends WhenWorkingWithWireMock {

    def 'should load the body from a file and headers from the adjacent header file'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun')
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(returnTheFile("somefile.json"))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope(CorrelationPath.of(globalScope.enter(johnSmith)))
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPath'] == '/home/path'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == publicIp + '/' + publicPort + '/TestRun/0/.*John_Smith'
        mapping['response']['headers']['Content-Type'] == 'application/json'
        mapping['response']['headers']['foo-header'] == 'bar-header-value'
        mapping['response']['body'] == "{\"foo\":\"bar\"}"
        mapping['priority'] == ((MAX_LEVELS -1) * PRIORITIES_PER_LEVEL) + 3

    }

    def 'should load the body by merging a template with provided variables'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun')
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)

        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(
                        merge(theTemplate("some_template.xml").with("value", "thisValue").andReturnIt()))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope(CorrelationPath.of(globalScope.enter(johnSmith)))
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPath'] == '/home/path'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == publicIp + '/' + publicPort + '/TestRun/0/.*John_Smith'
        mapping['response']['headers']['Content-Type'] == 'text/xml'
        mapping['response']['headers']['foo-header'] == 'bar-header-value'
        mapping['response']['body'] == "<root>thisValue</root>"
        mapping['priority'] == ((MAX_LEVELS -1) * PRIORITIES_PER_LEVEL) + 3
    }

}
