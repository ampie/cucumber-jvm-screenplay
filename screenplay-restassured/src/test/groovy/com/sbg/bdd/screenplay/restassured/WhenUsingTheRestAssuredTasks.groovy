package com.sbg.bdd.screenplay.restassured

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.Options
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.annotations.StepEventType
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.givenThat
import static com.sbg.bdd.screenplay.restassured.RestAssuredTasks.*
import static io.restassured.RestAssured.with

class WhenUsingTheRestAssuredTasks extends WhenUsingRestAssured {
    ScopedWireMockServer server
    WireMockServer dummyServer
    def cleanup(){
        if(server!=null){
            server.stop()
        }
        if(dummyServer!=null){
            dummyServer.stop()
        }
    }
    def 'should generate perty descriptions for GETs'() {
        prepareWireMockAndMappings()


        given:
        OnStage.raiseTheCurtain('Scene 1')

        when:
        givenThat(actorNamed('John')).wasAbleTo(
                get('http://localhost:' + dummyServer.port() + '/some/path', with().body('bar'))
        )

        then:
        TaskListener.EVENTS.size() == 4
        TaskListener.EVENTS[0].info.name == 'Given that John was able to '
        TaskListener.EVENTS[1].info.name == 'send a GET request to http://localhost:' + dummyServer.port() + "/some/path"
        TaskListener.EVENTS[2].type == StepEventType.SUCCESSFUL
    }
    def 'should generate perty descriptions for PUTs'() {
        prepareWireMockAndMappings()


        given:
        OnStage.raiseTheCurtain('Scene 1')

        when:
        givenThat(actorNamed('John')).wasAbleTo(
                put('http://localhost:' + dummyServer.port() + '/some/path', with().body('bar'))
        )

        then:
        TaskListener.EVENTS.size() == 4
        TaskListener.EVENTS[0].info.name == 'Given that John was able to '
        TaskListener.EVENTS[1].info.name == 'send a PUT request to http://localhost:' + dummyServer.port() + "/some/path with body 'bar'"
        TaskListener.EVENTS[2].type == StepEventType.SUCCESSFUL
    }
    def 'should generate perty descriptions for POSTs'() {
        prepareWireMockAndMappings()


        given:
        OnStage.raiseTheCurtain('Scene 1')

        when:
        givenThat(actorNamed('John')).wasAbleTo(
                post('http://localhost:' + dummyServer.port() + '/some/path', with().body('bar'))
        )

        then:
        TaskListener.EVENTS.size() == 4
        TaskListener.EVENTS[0].info.name == 'Given that John was able to '
        TaskListener.EVENTS[1].info.name == 'send a POST request to http://localhost:' + dummyServer.port() + "/some/path with body 'bar'"
        TaskListener.EVENTS[2].type == StepEventType.SUCCESSFUL
    }
    def 'should generate perty descriptions for DELETEs'() {
        prepareWireMockAndMappings()


        given:
        OnStage.raiseTheCurtain('Scene 1')

        when:
        givenThat(actorNamed('John')).wasAbleTo(
                delete('http://localhost:' + dummyServer.port() + '/some/path', with())
        )

        then:
        TaskListener.EVENTS.size() == 4
        TaskListener.EVENTS[0].info.name == 'Given that John was able to '
        TaskListener.EVENTS[1].info.name == 'send a DELETE request to http://localhost:' + dummyServer.port() + "/some/path"
        TaskListener.EVENTS[2].type == StepEventType.SUCCESSFUL
    }
    private WireMockServer prepareWireMockAndMappings() {
        TaskListener.EVENTS.clear()
        server = initWireMockAndBasePerformance()
        dummyServer = new WireMockServer(Options.DYNAMIC_PORT);
        dummyServer.start()
        def builder = WireMock.any(WireMock.urlEqualTo('/some/path'))
        builder.willReturn(aResponse().withBody('hello').withHeader("foo", "bar"))
        dummyServer.addStubMapping(builder.build())
        dummyServer
    }
}
