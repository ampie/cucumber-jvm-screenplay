package com.sbg.bdd.screenplay.core

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.internal.BaseActor

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.*
import static com.sbg.bdd.screenplay.core.TaskFactory.*

class WhenMultipleTasksFail extends WithinBasePerformance {
    def 'a parent step should fail with a Pending exception if pending tasks were performed and all the other tasks succeeded'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        OnStage.raiseTheCurtain("scene1")
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
        events.size() == 8
        events[0].info.name == 'When John attempts to '
        events[1].info.name == 'perform a task'
        events[2].info.name == 'perform a task'
        events[3].info.name == 'pending task'
        events[4].info.name == 'pending task'
        events[4].error == error
        events[5].info.name == 'pending task'
        events[6].info.name == 'pending task'
        events[7].info.name == 'When John attempts to '
    }
    def 'a parent step should fail with an AssertionError if tasks failed with an AssertionError and all the other tasks succeeded or were pending'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        OnStage.raiseTheCurtain("scene1")

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
        events.size() == 8
        error != null
        events[4].error instanceof PendingException
        events[6].error instanceof AssertionError
    }
    def 'a parent step should fail with a ScreenPlayException if tasks failed because of AssertionErrors and other Exceptions'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        OnStage.raiseTheCurtain("scene1")
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
        events.size() == 8
        error != null
        events[2].error instanceof PendingException
        events[4].error instanceof AssertionError
        events[6].error instanceof IllegalArgumentException
    }
}
