package com.sbg.bdd.screenplay.core

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.annotations.Step
import com.sbg.bdd.screenplay.core.internal.BaseActor
import com.sbg.bdd.screenplay.core.internal.BasePerformance

import static OnStage.raiseTheCurtain
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.*

class WhenStubbingDownstreamInteractions extends WithinBasePerformance {
    interface ContractToMock{
        String callIt();
    }
    def 'the stub should have access to the same actor as the actor that perform the action that depends on the stub'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        when:
        raiseTheCurtain('John in heartbroken')
        forRequestsFrom(actorNamed('John')).allow(new DownstreamStub() {
            @Override
            @Step("some stub do do nothing much")
            void performOnStage(ActorOnStage actorOnStage) {
                actorOnStage.actor.remember('mock',new ContractToMock() {
                    @Override
                    String callIt() {
                        return 'call was mocked'
                    }
                })
            }
        })
        when(actorNamed('John')).attemptsTo(new Task(){

            @Override
            def <T extends Actor> T performAs(T actor) {
                def mock = actor.recall('mock')
                actor.remember('callResult',mock.callIt())
                return actor
            }
        })
        then:
        actorNamed('John').recall('callResult') == 'call was mocked'
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 4
        events[0].info.keyword == 'Allow'
        events[0].info.name == 'some stub do do nothing much'
    }
}
