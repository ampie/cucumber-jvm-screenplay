package cucumber.wiremock

import com.github.ampie.wiremock.ScopedWireMockServer
import com.github.tomakehurst.wiremock.common.Json
import cucumber.scoping.GlobalScope
import cucumber.screenplay.Actor
import cucumber.screenplay.actors.OnStage
import cucumber.wiremock.scoping.CorrelationPath
import groovy.json.JsonSlurper

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.forRequestsFrom
import static cucumber.wiremock.RequestStrategies.a
import static cucumber.wiremock.ProxyStrategies.*

class WhenBuildingProxyMappings extends WhenWorkingWithWireMock {

    def 'should create a simple proxy mapping'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
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
        mapping['request']['urlPattern'] == '/home/path.*'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == '5/TestRun/.*John_Smith'
        mapping['response']['proxyBaseUrl']== "http://some.host.com/base"
        mapping['priority'] == (MAX_LEVELS * PRIORITIES_PER_LEVEL) + 5
    }
    def 'should create an intercepting proxy mapping that targets the original service'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)

        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)

        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("some.property.name").to(beIntercepted())
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.enter(johnSmith).getScopePath())
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPattern'] == '/resolved/endpoint.*'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == '5/TestRun/.*John_Smith'
        mapping['response']['proxyBaseUrl']== "http://somehost.com"
        mapping['priority'] == (MAX_LEVELS * PRIORITIES_PER_LEVEL) + 5
    }
    def 'should target the service under test'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)

        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)

        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("some.property.name").to(target().theServiceUnderTest().using().theLast(5).segments())
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.enter(johnSmith).getScopePath())
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPattern'] == '/resolved/endpoint.*'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == '5/TestRun/.*John_Smith'
        mapping['response']['proxyBaseUrl']== 'http://service.com/under/test'
        mapping['response']['transformers'][0]== 'ProxyUrlTransformer'
        mapping['response']['transformerParameters']['action'] == 'use'
        mapping['response']['transformerParameters']['which'] == 'trailing'
        mapping['response']['transformerParameters']['numberOfSegments'] == 5
        mapping['priority'] == (MAX_LEVELS * PRIORITIES_PER_LEVEL) + 4
    }
    def 'should target a specified url'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)

        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)

        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("some.property.name").to(target('http://target.com/base').ignoring().theFirst(5).segments())
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.enter(johnSmith).getScopePath())
        mappings.size() == 1
        def mapping = new JsonSlurper().parseText(Json.write(mappings[0]))
        mapping['request']['urlPattern'] == '/resolved/endpoint.*'
        mapping['request']['headers']['x-sbg-messageTraceId']['matches'] == '5/TestRun/.*John_Smith'
        mapping['response']['proxyBaseUrl']== 'http://target.com/base'
        mapping['response']['transformers'][0]== 'ProxyUrlTransformer'
        mapping['response']['transformerParameters']['action'] == 'ignore'
        mapping['response']['transformerParameters']['which'] == 'leading'
        mapping['response']['transformerParameters']['numberOfSegments'] == 5
        mapping['priority'] == (MAX_LEVELS * PRIORITIES_PER_LEVEL) + 4
    }
}
