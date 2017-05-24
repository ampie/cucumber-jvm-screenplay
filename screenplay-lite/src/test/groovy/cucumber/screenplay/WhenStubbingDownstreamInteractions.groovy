package cucumber.screenplay

import cucumber.screenplay.internal.BasePerformance
import cucumber.screenplay.actors.OnStage
import cucumber.screenplay.annotations.Step
import cucumber.screenplay.internal.BaseActor
import spock.lang.Specification

import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.forRequestsFrom
import static cucumber.screenplay.ScreenplayPhrases.*
import static cucumber.screenplay.actors.OnStage.raiseTheCurtain

class WhenStubbingDownstreamInteractions extends Specification {
    interface ContractToMock{
        String callIt();
    }
    def 'the stub should have access to the same actor as the actor that perform the action that depends on the stub'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = new BasePerformance()
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
