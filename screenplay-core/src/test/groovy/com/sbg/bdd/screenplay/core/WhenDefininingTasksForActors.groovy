package com.sbg.bdd.screenplay.core

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.annotations.StepEventType
import com.sbg.bdd.screenplay.core.internal.BaseActor
import com.sbg.bdd.screenplay.core.internal.BasePerformance

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.givenThat
import static com.sbg.bdd.screenplay.core.TaskFactory.*

class WhenDefininingTasksForActors extends WithinBasePerformance {
    def 'the task should be logged with the appropriate keywords and name '() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        when:
        givenThat(actorNamed('John')).wasAbleTo(performATask())
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 2
        events[0].info.keyword == 'Was able to'
        events[0].info.name =='perform a task'
    }
    def 'a failing task should result in subsequent tasks being skipped'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        when:
        def error = null
        try {
            givenThat(actorNamed('John')).wasAbleTo(
                    performATask(),
                    failATask(),
                    performATask()
            )
        }catch(IllegalArgumentException e){
            error=e;
        }
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 6
        events[0].info.keyword == 'Was able to'
        events[0].type == StepEventType.STEP_STARTED
        events[0].info.name =='perform a task'
        events[1].type == StepEventType.STEP_SUCCESSFUL
        events[2].info.keyword == 'Was able to'
        events[2].info.name =='fail a task'
        events[3].error.message == 'arrrgh!'
        events[3].error == error
        events[3].type == StepEventType.STEP_FAILED
        events[5].type == StepEventType.STEP_SKIPPED
    }
    def 'a pending task not should result in subsequent tasks being skipped'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        when:
        def error = null
        try {
            givenThat(actorNamed('John')).wasAbleTo(
                    performATask(),
                    pendingTask(),
                    performATask()
            )
        }catch(PendingException e){
            error=e;
        }
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 6
        events[0].info.keyword == 'Was able to'
        events[0].type == StepEventType.STEP_STARTED
        events[0].info.name =='perform a task'
        events[1].type == StepEventType.STEP_SUCCESSFUL
        events[2].info.keyword == 'Was able to'
        events[2].info.name =='pending task'
        events[3].error == error
        events[3].type == StepEventType.STEP_PENDING
        events[5].type == StepEventType.STEP_SUCCESSFUL
    }
    def 'a task with a failing assertion not should result in subsequent tasks being skipped'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        ScreenPlayEventStore.events.clear()

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        when:
        def error = null
        try {
            givenThat(actorNamed('John')).wasAbleTo(
                    performATask(),
                    taskWithAssertionFailed(),
                    performATask()
            )
        }catch(AssertionError e){
            error=e;
        }
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 6
        events[0].info.keyword == 'Was able to'
        events[0].type == StepEventType.STEP_STARTED
        events[0].info.name =='perform a task'
        events[1].type == StepEventType.STEP_SUCCESSFUL
        events[2].info.keyword == 'Was able to'
        events[2].info.name =='pending task'
        events[3].error == error
        events[3].type == StepEventType.STEP_ASSERTION_FAILED
        events[5].type == StepEventType.STEP_SUCCESSFUL
    }

}
