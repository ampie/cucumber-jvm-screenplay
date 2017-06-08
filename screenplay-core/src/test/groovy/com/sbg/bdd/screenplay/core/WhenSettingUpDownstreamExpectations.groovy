package com.sbg.bdd.screenplay.core

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.annotations.Step
import com.sbg.bdd.screenplay.core.internal.BaseActor
import com.sbg.bdd.screenplay.core.internal.BasePerformance

import static OnStage.callActorToStage
import static OnStage.raiseTheCurtain
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.*

class WhenSettingUpDownstreamExpectations extends WithinBasePerformance {
    interface ContractToMock{
        String callIt();
    }
    def 'the verification should have access to the context in which the stub was setup'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub =buildPerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))

        def john = actorNamed('John')
        raiseTheCurtain('John finds Jesus')
        forRequestsFrom(john).expect(new DownstreamExpectation(new DownstreamStub() {
            @Override
            void performOnStage(ActorOnStage actorOnStage) {
                actorOnStage.remember('tokenOfSuccess', false)
            }
        }, new DownstreamVerification() {
            @Override
            @Step("some verification that should fail")

            void performOnStage(ActorOnStage actorOnStage) {
                def token = actorOnStage.recall('tokenOfSuccess')
                if (token == null) {
                    throw new RuntimeException('This failed dismally')
                } else if (token == false) {
                    throw new AssertionError('This failed dismally')
                }

            }
        }))
        when(john).attemptsTo(new Task(){

            @Override
            def <T extends Actor> T performAs(T actor) {
                return actor
            }
        })
        def error = null
        when:
        try {
            callActorToStage(john).evaluateExpectations()
        }catch(AssertionError e){
            error=e
        }
        then:
        john.recall('tokenOfSuccess') == null
        callActorToStage(john).recall('tokenOfSuccess') == false
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 12
        events[0].info.name=='For requests from John, allow '
        events[1].info.name=='performOnStage'
        events[2].info.name=='performOnStage'
        events[10].info.keyword == 'performOnStage'
        events[10].info.name == 'some verification that should fail'
        events[11].error == error
    }
}
