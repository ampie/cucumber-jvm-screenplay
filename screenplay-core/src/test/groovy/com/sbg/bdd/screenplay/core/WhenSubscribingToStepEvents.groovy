package com.sbg.bdd.screenplay.core

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.annotations.StepEventType
import com.sbg.bdd.screenplay.core.around_sequence.StepEventListener1
import com.sbg.bdd.screenplay.core.around_sequence.StepEventListener2
import com.sbg.bdd.screenplay.core.around_sequence.StepEventListener3

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.givenThat
import static com.sbg.bdd.screenplay.core.actors.OnStage.raiseTheCurtain

class WhenSubscribingToStepEvents extends WithinBasePerformance {
    def 'events should be delivered in correct nesting sequence'() {
        given:
        StepEventStore.EVENTS.clear()

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(StepEventListener1, StepEventListener2, StepEventListener3)))
        when:
        raiseTheCurtain('John in heartbroken')
        givenThat(actorNamed('John')).wasAbleTo(
            new Task() {
                @Override
                def <T extends Actor> T performAs(T actor) {
                    return actor
                }
            }
        )

        then:
        def events = StepEventStore.EVENTS
        events.size() == 12
        events[0].left == StepEventListener3
        events[0].right.type== StepEventType.STARTED
        events[1].left == StepEventListener2
        events[1].right.type== StepEventType.STARTED
        events[2].left == StepEventListener1
        events[2].right.type== StepEventType.STARTED
        events[9].left == StepEventListener1
        events[9].right.type== StepEventType.SUCCESSFUL
        events[10].left == StepEventListener2
        events[10].right.type== StepEventType.SUCCESSFUL
        events[11].left == StepEventListener3
        events[11].right.type== StepEventType.SUCCESSFUL
    }
}
