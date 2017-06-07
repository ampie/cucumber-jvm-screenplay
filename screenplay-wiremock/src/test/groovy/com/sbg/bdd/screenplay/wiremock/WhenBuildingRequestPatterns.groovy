package com.sbg.bdd.screenplay.wiremock

import com.github.tomakehurst.wiremock.common.Json
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer
import groovy.json.JsonSlurper

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.wiremock.RequestStrategies.*
import static com.sbg.bdd.wiremock.scoped.recording.strategies.ResponseBodyStrategies.returnTheBody

class WhenBuildingRequestPatterns extends WhenWorkingWithWireMock {
    def 'should include the scope path of the current user as a header requirement'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun', 5)
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(returnTheBody("blah", "text/plain"))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("localhost/"+wireMockServer.port()+"/5/" + globalScope.enter(johnSmith).getScopePath())
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPath'] == '/home/path'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == 'localhost/'+wireMockServer.port()+'/5/TestRun/.*John_Smith'
        mapping['response']['headers']['Content-Type'] == 'text/plain'
        mapping['response']['body'] == 'blah'
        mapping['priority'] == (MAX_LEVELS * PRIORITIES_PER_LEVEL) + 3
    }

    def 'should resolve request paths that appear to be properties from the EndpointConfigRegistry provided'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun', 5)
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                anyRequest().to("some.property.name").to(returnTheBody("blah", "text/plain"))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("localhost/"+wireMockServer.port()+"/5/" + globalScope.enter(johnSmith).getScopePath())
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPath'] == '/resolved/endpoint'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == 'localhost/'+wireMockServer.port()+'/5/TestRun/.*John_Smith'
        mapping['response']['headers']['Content-Type'] == 'text/plain'
        mapping['response']['body'] == 'blah'
        mapping['priority'] == (MAX_LEVELS * PRIORITIES_PER_LEVEL) + 3
    }

    def 'should generate multiple mappings when setting up rules that target any known downstream endpoint'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun', 5)
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                anyRequest().toAnyKnownExternalService().to(returnTheBody("blah", "text/plain"))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("localhost/"+wireMockServer.port()+"/5/" + globalScope.enter(johnSmith).getScopePath())
        mappings.size() == 2
        println Json.write(mappings)
        def mapping0 = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping0['request']['urlPathPattern'] == '/service/two/endpoint.*'
        def mapping1 = new JsonSlurper().parseText(Json.write(mappings[1]))
        mapping1['request']['urlPathPattern'] == '/service/one/endpoint.*'
        mapping1['request']['headers']['x-sbg-messageTraceId']['matches'] == 'localhost/'+wireMockServer.port()+'/5/TestRun/.*John_Smith'
        mapping1['response']['headers']['Content-Type'] == 'text/plain'
        mapping1['response']['body'] == 'blah'
        mapping1['priority'] == (MAX_LEVELS * PRIORITIES_PER_LEVEL) + 3
    }

    def 'should generate multiple bodypatterns using multiple containing strings'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun', 5)
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                anyRequest().to("/test/blah").withRequestBody(containing("1", "2", "3")).to(returnTheBody("blah", "text/plain"))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("localhost/"+wireMockServer.port()+"/5/" + globalScope.enter(johnSmith).getScopePath())
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        println mapping
        mapping['request']['bodyPatterns'][0]['contains'] == '1'
        mapping['request']['bodyPatterns'][1]['contains'] == '2'
        mapping['request']['bodyPatterns'][2]['contains'] == '3'
    }


}
