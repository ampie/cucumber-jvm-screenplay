package cucumber.screenplay

import cucumber.screenplay.internal.BasePerformance
import cucumber.screenplay.actors.OnStage
import cucumber.screenplay.internal.BaseActor
import spock.lang.Specification

import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.givenThat
import static cucumber.screenplay.ScreenplayPhrases.*
import static cucumber.screenplay.TaskFactory.*

class WhenMultipleTasksFail extends Specification {
    def 'a parent step should fail with a Pending exception if pending tasks were performed and all the other tasks succeeded'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = new BasePerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        when:
        def error = null
        try {
            when(actorNamed('John')).attemptsTo(
                    performATask(),
                    pendingTask(),
                    pendingTask()
            )
        }catch(PendingException e){
            error=e;
        }
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 6
        events[3].error == error
    }
    def 'a parent step should fail with an AssertionError if tasks failed with an AssertionError and all the other tasks succeeded or were pending'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = new BasePerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        when:
        def error = null
        try {
            but(actorNamed('John')).wasAbleTo(
                    performATask(),
                    pendingTask(),
                    taskWithAssertionFailed()
            )
        }catch(AssertionError e){
            error=e;
        }
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 6
        error != null
        events[3].error instanceof PendingException
        events[5].error instanceof AssertionError
    }
    def 'a parent step should fail with a ScreenPlayException if tasks failed because of AssertionErrors and other Exceptions'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = new BasePerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        when:
        def error = null
        try {
            givenThat(actorNamed('John')).wasAbleTo(
                    pendingTask(),
                    taskWithAssertionFailed(),
                    failATask()
            )
        }catch(ScreenPlayException e){
            error=e;
        }
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 6
        error != null
        events[1].error instanceof PendingException
        events[3].error instanceof AssertionError
        events[5].error instanceof IllegalArgumentException
    }
}
