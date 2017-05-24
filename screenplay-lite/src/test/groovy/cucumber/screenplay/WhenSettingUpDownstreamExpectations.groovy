package cucumber.screenplay

import cucumber.screenplay.actors.OnStage
import cucumber.screenplay.annotations.Step
import cucumber.screenplay.internal.BaseActor
import cucumber.screenplay.internal.BasePerformance
import spock.lang.Specification

import static cucumber.screenplay.ScreenplayPhrases.*
import static cucumber.screenplay.actors.OnStage.callActorToStage
import static cucumber.screenplay.actors.OnStage.raiseTheCurtain

class WhenSettingUpDownstreamExpectations extends Specification {
    interface ContractToMock{
        String callIt();
    }
    def 'the verification should have access to the context in which the stub was setup'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = new BasePerformance()
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
        events.size() == 6
        events[5].info.keyword == 'Verify'
        events[5].info.name == 'some verification that should fail'
        events[5].error == error
    }
}
