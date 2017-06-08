package com.sbg.bdd.screenplay.restassured

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.Options
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.annotations.StepEventType

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.givenThat
import static com.sbg.bdd.screenplay.restassured.RestAssuredTasks.*
import static io.restassured.RestAssured.with
import static org.hamcrest.Matchers.*

class WhenUsingTheRestAssuredConsequence extends WhenUsingRestAssured {
    def 'should generate perty header and body assertions'() {
        TaskListener.EVENTS.clear()
        def server = initWireMockAndBasePerformance()
        def dummyServer = new WireMockServer(Options.DYNAMIC_PORT);
        dummyServer.start()
        def builder = WireMock.any(WireMock.urlEqualTo('/some/path'))
        builder.willReturn(aResponse().withBody('hello').withHeader("foo","bar"))
        dummyServer.addStubMapping(builder.build())


        given:
        OnStage.raiseTheCurtain('Scene 1')
        givenThat(actorNamed('John')).wasAbleTo(
                put('http://localhost:' + dummyServer.port() + '/some/path', with().body('bar'))
        )

        when:
        thenFor(actorNamed('John'),
                assertThat().body(is(equalTo('hello'))).header("foo",is(equalTo("bar"))),
                assertThat().header("fa",is(isEmptyOrNullString()))
        )

        then:
        TaskListener.EVENTS.size() == 10
        TaskListener.EVENTS[0].info.name == 'Given that John was able to '
        TaskListener.EVENTS[4].info.name == 'Then John should '
        TaskListener.EVENTS[6].type == StepEventType.SUCCESSFUL
        TaskListener.EVENTS[6].info.name == 'see that the body of the response is \"hello\"and the header \"foo\" is \"bar\"'
        TaskListener.EVENTS[7].info.name == 'see that the body of the response and the header "fa" is (null or an empty string)'
    }
}
