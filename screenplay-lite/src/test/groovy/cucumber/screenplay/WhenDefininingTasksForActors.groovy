package cucumber.screenplay

import cucumber.screenplay.actors.OnStage
import cucumber.screenplay.annotations.Step
import cucumber.screenplay.internal.BaseActor
import spock.lang.Specification

import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.givenThat

class WhenDefininingTasksForActors extends Specification {
    def 'the task should be logged'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = new PerformanceStub()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        when:
        givenThat(actorNamed('John')).wasAbleTo(new Task() {
            @Override
            @Step('perform a task')
            def <T extends Actor> T performAs(T actor) {
                return actor
            }
        })
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 2
        events[0].info.keyword == 'Was able to'
        events[0].info.name =='perform a task'
    }
}
