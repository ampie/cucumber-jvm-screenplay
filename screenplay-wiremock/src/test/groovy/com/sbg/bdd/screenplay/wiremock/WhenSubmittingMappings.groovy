package com.sbg.bdd.screenplay.wiremock

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.wiremock.RequestStrategies.a
import static com.sbg.bdd.wiremock.scoped.recording.strategies.ResponseBodyStrategies.returnTheFile

class WhenSubmittingMappings extends WhenWorkingWithWireMock{

    def 'should generate perty descriptions'() throws Exception{
        given:
        Listener.EVENTS.clear()
        GlobalScope globalScope = buildGlobalScope('TestRun',5,Listener)
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(returnTheFile("somefile.json"))
        )
        then:
        Listener.EVENTS.size() ==2
        Listener.EVENTS[0].info.name == 'a PUT to "/home/path" to return the file "somefile.json"'

    }

}
