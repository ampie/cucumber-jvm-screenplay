package com.sbg.bdd.screenplay.scoped

import com.sbg.bdd.screenplay.core.ActorOnStage
import com.sbg.bdd.screenplay.core.DownstreamStub
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.annotations.ActorInvolvement
import com.sbg.bdd.screenplay.core.annotations.SceneEventType
import com.sbg.bdd.screenplay.core.annotations.Step
import com.sbg.bdd.screenplay.core.annotations.StepEventType

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.core.actors.OnStage.callActorToStage
import static com.sbg.bdd.screenplay.core.actors.OnStage.shineSpotlightOn


class WhenSubscribingToEvents extends WhenUsingScopes {
    def 'should issue events in a consistent and predicable sequence'() {
        given:
        def globalScope = super.buildGlobalScope('GS', EventStore)
        OnStage.present(globalScope)
        when:
        def scenario = globalScope.startFunctionalScope('nested1').startScenario('scenario1')

        def johnSmith = callActorToStage(actorNamed('John Smith'))
        forRequestsFrom(johnSmith.actor).allow(new DownstreamStub() {
            @Override
            @Step("step1")
            void performOnStage(ActorOnStage actorOnStage) {
                actorOnStage.allow(new DownstreamStub() {
                    @Override
                    @Step("childStep")
                    void performOnStage(ActorOnStage a) {

                    }
                })
            }
        })
        shineSpotlightOn(actorNamed('John Smith'))
        globalScope.complete()
        then:
        def events = EventStore.events
//        events.size() == 4 + 4 + 4 + 4 + 4 + 6
        events[0].source.name == 'GS'
        events[0].sceneEventType == SceneEventType.BEFORE_START
        events[1].source.name == 'GS'
        events[1].sceneEventType == SceneEventType.AFTER_START
        events[2].source.name == 'nested1'
        events[3].source.name == 'nested1'
        events[4].source.name == 'scenario1'
        events[5].source.name == 'scenario1'

        events[6].source.name == 'John Smith'
        events[6].involvement== ActorInvolvement.BEFORE_ENTER_STAGE
        events[7].source.name == 'John Smith'
        events[7].involvement== ActorInvolvement.AFTER_ENTER_STAGE
        events[8].source.name == 'For requests from John Smith, allow '
        events[8].type==StepEventType.STARTED
        events[9].source.name == 'step1'
        events[10].source.name == 'For requests from John Smith, allow '
        events[11].source.name == 'childStep'
        events[12].source.name == 'childStep'
        events[13].source.name == 'For requests from John Smith, allow '
        events[13].type==StepEventType.SUCCESSFUL
        events[14].source.name == 'step1'
        events[15].source.name == 'For requests from John Smith, allow '
        events[15].type==StepEventType.SUCCESSFUL
        events[16].source.name == 'John Smith'
        events[16].involvement == ActorInvolvement.INTO_SPOTLIGHT
        events[17].source.name == 'scenario1'
        events[17].sceneEventType== SceneEventType.BEFORE_COMPLETE
        //Exiting the actors is part of the completion process
        events[18].source.name == 'John Smith'
        events[18].involvement == ActorInvolvement.OUT_OF_SPOTLIGHT
        events[19].source.name == 'John Smith'
        events[19].involvement == ActorInvolvement.BEFORE_EXIT_STAGE
        events[20].source.name == 'John Smith'
        events[20].involvement == ActorInvolvement.AFTER_EXIT_STAGE
        events[21].source.name == 'scenario1'
        events[22].source.name == 'nested1'
        events[23].source.name == 'nested1'
        events[24].source.name == 'GS'
        events[25].source.name == 'GS'
    }
}
