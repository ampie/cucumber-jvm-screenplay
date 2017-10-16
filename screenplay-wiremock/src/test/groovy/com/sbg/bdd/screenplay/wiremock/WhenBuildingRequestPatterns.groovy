package com.sbg.bdd.screenplay.wiremock

import com.github.tomakehurst.wiremock.common.Json
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.wiremock.scoped.integration.HttpCommandExecutor
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer
import groovy.json.JsonSlurper

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.wiremock.RequestStrategies.*
import static com.sbg.bdd.wiremock.scoped.client.strategies.ResponseBodyStrategies.returnTheBody

class WhenBuildingRequestPatterns extends WhenWorkingWithWireMock {
    def 'should include the scope path of the current user as a header requirement'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun')
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(returnTheBody("blah", "text/plain"))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope(CorrelationPath.of( globalScope.enter(johnSmith)))
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPath'] == '/home/path'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == publicIp + '/'+publicPort+'/TestRun/0/.*John_Smith'
        mapping['response']['headers']['Content-Type'] == 'text/plain'
        mapping['response']['body'] == 'blah'
        mapping['priority'] == ((MAX_LEVELS-1) * PRIORITIES_PER_LEVEL) + 3
    }

    def 'should resolve request paths that appear to be properties from the EndpointConfigRegistry provided'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun')
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        HttpCommandExecutor.INSTANCE=Mock(HttpCommandExecutor){
            execute(_) >> '[{"propertyName":"some.property.name","url":"http://host1.com/resolved/endpoint","endpointType":"REST","categories":["category1"],"scopes":[]}]'
        }
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                anyRequest().to("some.property.name").to(returnTheBody("blah", "text/plain"))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope(CorrelationPath.of( globalScope.enter(johnSmith)))
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPath'] == '/resolved/endpoint'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == publicIp + '/'+publicPort+'/TestRun/0/.*John_Smith'
        mapping['response']['headers']['Content-Type'] == 'text/plain'
        mapping['response']['body'] == 'blah'
        mapping['priority'] == ((MAX_LEVELS-1) * PRIORITIES_PER_LEVEL) + 3
    }

    def 'should generate multiple mappings when setting up rules that target any known downstream endpoint'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun')
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        HttpCommandExecutor.INSTANCE=Mock(HttpCommandExecutor){
            execute(_) >> '[' +
                    '{"propertyName":"a.property.name","url":"http://host1.com/service/two/endpoint","endpointType":"REST","categories":["category1"],"scopes":[]},' +
                    '{"propertyName":"b.property.name","url":"http://host1.com/service/one/endpoint","endpointType":"REST","categories":["category1"],"scopes":[]}' +
                    ']'
        }
        when:
        forRequestsFrom(johnSmith).allow(
                anyRequest().toAnyKnownExternalService().to(returnTheBody("blah", "text/plain"))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope(CorrelationPath.of( globalScope.enter(johnSmith)))
        mappings.size() == 2
        println Json.write(mappings)
        def mapping0 = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping0['request']['urlPathPattern'] == '/service/one/endpoint.*'
        def mapping1 = new JsonSlurper().parseText(Json.write(mappings[1]))
        mapping1['request']['urlPathPattern'] == '/service/two/endpoint.*'
        mapping1['request']['headers']['x-sbg-messageTraceId']['matches'] == publicIp + '/'+publicPort+'/TestRun/0/.*John_Smith'
        mapping1['response']['headers']['Content-Type'] == 'text/plain'
        mapping1['response']['body'] == 'blah'
        mapping1['priority'] == ((MAX_LEVELS-1) * PRIORITIES_PER_LEVEL) + 3
    }

    def 'should generate multiple bodypatterns using multiple containing strings'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun')
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                anyRequest().to("/test/blah").withRequestBody(containing("1", "2", "3")).to(returnTheBody("blah", "text/plain"))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope(CorrelationPath.of( globalScope.enter(johnSmith)))
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        println mapping
        mapping['request']['bodyPatterns'][0]['contains'] == '1'
        mapping['request']['bodyPatterns'][1]['contains'] == '2'
        mapping['request']['bodyPatterns'][2]['contains'] == '3'
    }


}
