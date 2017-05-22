package cucumber.wiremock

import com.github.ampie.wiremock.ScopedWireMockServer
import com.github.tomakehurst.wiremock.common.Json
import cucumber.scoping.GlobalScope
import cucumber.screenplay.actors.OnStage
import groovy.json.JsonSlurper

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.forRequestsFrom
import static cucumber.wiremock.RequestStrategies.*
import static cucumber.wiremock.ResponseStrategies.returnTheBody

class WhenBuildingRequestPatterns extends WhenWorkingWithWireMock{
    static final int MAX_LEVELS = 10;
    static final int PRIORITIES_PER_LEVEL = 10;
    static final int EVERYBODY_PRIORITY_DECREMENT = PRIORITIES_PER_LEVEL / 2;
    def 'should include the scope path of the current user as a header requirement'() throws Exception{
        GlobalScope globalScope = buildGlobalScope("TestRun")

        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope, i)
        given:
        OnStage.present(globalScope)
        when:
        forRequestsFrom(actorNamed("John Smith")).allow(
                a(PUT).to("/home/path").to(returnTheBody("blah", "text/plain"))
        )
        then:
        def mappings =wireMockServer.getMappingsInScope("5/" + globalScope.getCurrentUserInScope().getScopePath())
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['url'] == '/home/path'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == '5/TestRun/.*John_Smith'
        mapping['response']['headers']['Content-Type'] == 'text/plain'
        mapping['response']['body'] == 'blah'
        mapping['priority'] == (MAX_LEVELS*PRIORITIES_PER_LEVEL)+3
    }

    def 'should resolve request paths that appear to be properties from the ClientOfServiceUnderTest provided'() throws Exception{
        GlobalScope globalScope = buildGlobalScope("TestRun")

        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope, i)
        given:
        OnStage.present(globalScope)
        when:
        forRequestsFrom(actorNamed("John Smith")).allow(
                anyRequest().to("some.property.name").to(returnTheBody("blah", "text/plain"))
        )
        then:
        def mappings =wireMockServer.getMappingsInScope("5/" + globalScope.getCurrentUserInScope().getScopePath())
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['url'] == '/resolved/endpoint'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == '5/TestRun/.*John_Smith'
        mapping['response']['headers']['Content-Type'] == 'text/plain'
        mapping['response']['body'] == 'blah'
        mapping['priority'] == (MAX_LEVELS*PRIORITIES_PER_LEVEL)+3
    }
    def 'should generate multiple mappings when setting up rules that target all downstream systems'() throws Exception{
        GlobalScope globalScope = buildGlobalScope("TestRun")

        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope, i)
        given:
        OnStage.present(globalScope)
        when:
        forRequestsFrom(actorNamed("John Smith")).allow(
                anyRequest().toAnyKnownExternalService().to(returnTheBody("blah", "text/plain"))
        )
        then:
        def mappings =wireMockServer.getMappingsInScope("5/" + globalScope.getCurrentUserInScope().getScopePath())
        mappings.size() == 2
        println Json.write(mappings)
        def mapping0 = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping0['request']['urlPattern'] == '/service/two/endpoint.*'
        def mapping1 = new JsonSlurper().parseText(Json.write(mappings[1]))
        mapping1['request']['urlPattern'] == '/service/one/endpoint.*'
        mapping1['request']['headers']['x-sbg-messageTraceId']['matches'] == '5/TestRun/.*John_Smith'
        mapping1['response']['headers']['Content-Type'] == 'text/plain'
        mapping1['response']['body'] == 'blah'
        mapping1['priority'] == (MAX_LEVELS*PRIORITIES_PER_LEVEL)+3
    }
    def 'should generate multiple bodypatterns using multiple containing strings'() throws Exception{
        GlobalScope globalScope = buildGlobalScope("TestRun")

        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope, i)
        given:
        OnStage.present(globalScope)
        when:
        forRequestsFrom(actorNamed("John Smith")).allow(
                anyRequest().to("/test/blah").withRequestBody(containing("1","2","3")).to(returnTheBody("blah", "text/plain"))
        )
        then:
        def mappings =wireMockServer.getMappingsInScope("5/" + globalScope.getCurrentUserInScope().getScopePath())
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        println mapping
        mapping['request']['bodyPatterns'][0]['contains'] == '1'
        mapping['request']['bodyPatterns'][1]['contains'] == '2'
        mapping['request']['bodyPatterns'][2]['contains'] == '3'
    }


}
