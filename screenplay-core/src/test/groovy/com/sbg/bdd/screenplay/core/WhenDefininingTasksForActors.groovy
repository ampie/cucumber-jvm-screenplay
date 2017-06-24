package com.sbg.bdd.screenplay.core

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.annotations.StepEventType
import com.sbg.bdd.screenplay.core.internal.BaseActor

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
        OnStage.raiseTheCurtain("scene1")

        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        when:
        givenThat(actorNamed('John')).wasAbleTo(performATask())
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 4
        events[1].info.keyword == 'performAs'
        events[1].info.name =='perform a task'
    }
    def 'a failing task should result in subsequent tasks being skipped'() {
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
                    performATask(),
                    failATask(),
                    performATask()
            )
        }catch(IllegalArgumentException e){
            error=e;
        }
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 8
        events[1].info.keyword == 'performAs'
        events[1].type == StepEventType.STARTED
        events[1].info.name =='perform a task'
        events[2].type == StepEventType.SUCCESSFUL
        events[3].info.keyword == 'performAs'
        events[3].info.name =='fail a task'
        events[4].error.message == 'arrrgh!'
        events[4].error == error
        events[4].type == StepEventType.FAILED
        events[6].type == StepEventType.SKIPPED
    }
    def 'a pending task not should result in subsequent tasks being skipped'() {
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
                    performATask(),
                    pendingTask(),
                    performATask()
            )
        }catch(PendingException e){
            error=e;
        }
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 8
        events[1].info.keyword == 'performAs'
        events[1].type == StepEventType.STARTED
        events[1].info.name =='perform a task'
        events[2].type == StepEventType.SUCCESSFUL
        events[3].info.keyword == 'performAs'
        events[3].info.name =='pending task'
        events[4].error == error
        events[4].type == StepEventType.PENDING
        events[6].type == StepEventType.SUCCESSFUL
    }
    def 'a task with a failing assertion not should result in subsequent tasks being skipped'() {
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
                    performATask(),
                    taskWithAssertionFailed(),
                    performATask()
            )
        }catch(AssertionError e){
            error=e;
        }
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 8
        events[1].info.keyword == 'performAs'
        events[1].type == StepEventType.STARTED
        events[1].info.name =='perform a task'
        events[2].type == StepEventType.SUCCESSFUL
        events[3].info.keyword == 'performAs'
        events[3].info.name =='pending task'
        events[4].error == error
        events[4].type == StepEventType.ASSERTION_FAILED
        events[6].type == StepEventType.SUCCESSFUL
    }

}
