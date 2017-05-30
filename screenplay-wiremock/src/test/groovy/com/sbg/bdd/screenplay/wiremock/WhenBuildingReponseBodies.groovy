package com.sbg.bdd.screenplay.wiremock

import com.github.tomakehurst.wiremock.common.Json
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.wiremock.scoped.ScopedWireMockServer
import groovy.json.JsonSlurper

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.wiremock.RequestStrategies.a
import static com.sbg.bdd.wiremock.scoped.recording.strategies.ResponseBodyStrategies.*

class WhenBuildingReponseBodies extends WhenWorkingWithWireMock{

    def 'should load the body from a file and headers from the adjacent header file'() throws Exception{
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(returnTheFile("somefile.json"))
        )
        then:
        def mappings =wireMockServer.getMappingsInScope("5/" + globalScope.enter(johnSmith).getScopePath())
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['url'] == '/home/path'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == '5/TestRun/.*John_Smith'
        mapping['response']['headers']['Content-StepEventType'] == 'application/json'
        mapping['response']['headers']['foo-header'] == 'bar-header-value'
        mapping['response']['body'] == "{\"foo\":\"bar\"}"
        mapping['priority'] == (MAX_LEVELS*PRIORITIES_PER_LEVEL)+3
    }
    def 'should load the body by merging a template with provided variables'() throws Exception{
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
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
        def mappings =wireMockServer.getMappingsInScope("5/" + globalScope.enter(johnSmith).getScopePath())
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['url'] == '/home/path'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == '5/TestRun/.*John_Smith'
        mapping['response']['headers']['Content-StepEventType'] == 'text/xml'
        mapping['response']['headers']['foo-header'] == 'bar-header-value'
        mapping['response']['body'] == "<root>thisValue</root>"
        mapping['priority'] == (MAX_LEVELS*PRIORITIES_PER_LEVEL)+3
    }

}
