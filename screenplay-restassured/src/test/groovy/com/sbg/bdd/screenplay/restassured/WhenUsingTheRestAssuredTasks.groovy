package com.sbg.bdd.screenplay.restassured

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.Options
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.annotations.StepEventType
import io.restassured.RestAssured
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static io.restassured.RestAssured.with;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.givenThat
import static com.sbg.bdd.screenplay.restassured.RestAssuredTasks.*

class WhenUsingTheRestAssuredTasks extends WhenUsingRestAssured {
    def 'should generate pretty descriptions'() {
        TaskListener.EVENTS.clear()
        def server = initWireMockAndBasePerformance()
        def dummyServer = new WireMockServer(Options.DYNAMIC_PORT);
        dummyServer.start()
        def builder = WireMock.any(WireMock.urlEqualTo('/some/path'))
        builder.willReturn(aResponse().withBody('hello'))
        dummyServer.addStubMapping(builder.build())


        given:
        OnStage.raiseTheCurtain('Scene 1')

        when:
        givenThat(actorNamed('John')).wasAbleTo(
                put('http://localhost:' + dummyServer.port() + '/some/path', with().body('bar'))
        )
        thenFor(actorNamed('John'),
                assertThat().body(is(equalTo('hello')))
        )
        then:
        TaskListener.EVENTS.size() == 4
        TaskListener.EVENTS[0].info.name == 'Send a PUT request to http://localhost:' + dummyServer.port() + "/some/path with body 'bar'"
        TaskListener.EVENTS[3].type == StepEventType.SUCCESSFUL
        TaskListener.EVENTS[3].info.name == 'Then the body of the response is "hello"'
    }
}
