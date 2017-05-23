package cucumber.wiremock

import com.github.ampie.wiremock.ScopedWireMockServer
import com.github.tomakehurst.wiremock.common.Json
import cucumber.scoping.GlobalScope
import cucumber.screenplay.Actor
import cucumber.screenplay.actors.OnStage
import groovy.json.JsonSlurper

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.forRequestsFrom
import static cucumber.wiremock.RequestStrategies.*
import static cucumber.wiremock.ResponseStrategies.*

class WhenBuildingReponseBodies extends WhenWorkingWithWireMock{

    def 'should load the body from a file and headers from the adjacent header file'() throws Exception{
        GlobalScope globalScope = buildGlobalScope('TestRun',5)

        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
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
        mapping['response']['headers']['Content-Type'] == 'application/json'
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
        mapping['response']['headers']['Content-Type'] == 'text/xml'
        mapping['response']['headers']['foo-header'] == 'bar-header-value'
        mapping['response']['body'] == "<root>thisValue</root>"
        mapping['priority'] == (MAX_LEVELS*PRIORITIES_PER_LEVEL)+3
    }

}
