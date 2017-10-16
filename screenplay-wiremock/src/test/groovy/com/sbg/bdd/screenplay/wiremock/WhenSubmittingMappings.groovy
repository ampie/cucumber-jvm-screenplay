package com.sbg.bdd.screenplay.wiremock

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.screenplay.wiremock.listeners.ScopeManagementListener
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.wiremock.RequestStrategies.a
import static com.sbg.bdd.wiremock.scoped.client.strategies.ResponseBodyStrategies.returnTheFile

class WhenSubmittingMappings extends WhenWorkingWithWireMock{

    def 'should generate perty descriptions'() throws Exception{
        given:
        Listener.EVENTS.clear()
        GlobalScope globalScope = buildGlobalScope('TestRun', Listener,ScopeManagementListener)
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(returnTheFile("somefile.json"))
        )
        then:
        Listener.EVENTS.size() ==4
        Listener.EVENTS[0].info.name == 'For requests from John Smith, allow '
        Listener.EVENTS[1].info.name == 'a PUT to "/home/path" to return the file "somefile.json"'

    }

}
