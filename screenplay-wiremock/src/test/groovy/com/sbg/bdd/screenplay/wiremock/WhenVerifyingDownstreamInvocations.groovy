package com.sbg.bdd.screenplay.wiremock

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.screenplay.wiremock.listeners.ScopeManagementListener
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.wiremock.RequestStrategies.a
import static com.sbg.bdd.wiremock.scoped.client.strategies.CountMatcher.between

class WhenVerifyingDownstreamInvocations extends WhenWorkingWithWireMock {

    def 'should create a simple proxy mapping'() throws Exception {
        given:
        Listener.EVENTS.clear()
        GlobalScope globalScope = buildGlobalScope('TestRun', Listener,ScopeManagementListener)
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed('John Smith')
        when:
        def error = null
            try {
                forRequestsFrom(johnSmith).verifyThat(
                        a(PUT).to('/home/path').wasMade(between(0).and(4).times())
                )
            } catch (AssertionError e) {
                error=e
            }
        then:
        Listener.EVENTS.size() ==4

        Listener.EVENTS[0].info.name=='For requests from John Smith, verify that '
        Listener.EVENTS[1].info.name=='the number of times a PUT request was made to "/home/path" is (a value greater than <0> and a value less than <4>)'
        Listener.EVENTS[2].error == error
        Listener.EVENTS[2].error.message == 'Expected: (a value greater than <0> and a value less than <4>)\n' +
                '     but: was <0>'
    }

}
