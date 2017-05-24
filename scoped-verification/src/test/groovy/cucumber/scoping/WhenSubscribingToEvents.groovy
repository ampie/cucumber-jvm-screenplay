package cucumber.scoping

import cucumber.screenplay.ActorOnStage
import cucumber.screenplay.DownstreamStub
import cucumber.screenplay.annotations.SceneEventType
import cucumber.screenplay.annotations.ActorInvolvement
import cucumber.screenplay.actors.OnStage
import cucumber.screenplay.annotations.Step

import static cucumber.screenplay.actors.OnStage.*
import static cucumber.screenplay.ScreenplayPhrases.*

/**
 * Created by ampie on 2017/05/23.
 */
class WhenSubscribingToEvents extends WhenUsingScopes {
    def 'should issue events in a consistent and predicable sequence'() {
        given:
        def globalScope = super.buildGlobalScope('GS', EventStore)
        OnStage.present(globalScope)
        when:
        def scenario = globalScope.startFunctionalScope('nested1').startScenario('scenario1')

        callActorToStage(actorNamed('John Smith')).allow(new DownstreamStub() {
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
        events[8].source.name == 'step1'
        events[9].source.name == 'childStep'
        events[10].source.name == 'childStep'
        events[11].source.name == 'step1'
        events[12].source.name == 'John Smith'
        events[12].involvement == ActorInvolvement.INTO_SPOTLIGHT
        events[13].source.name == 'John Smith'
        events[13].involvement== ActorInvolvement.OUT_OF_SPOTLIGHT
        events[14].source.name == 'John Smith'
        events[14].involvement == ActorInvolvement.BEFORE_EXIT_STAGE
        events[16].source.name == 'scenario1'
        events[17].source.name == 'scenario1'
        events[18].source.name == 'nested1'
        events[19].source.name == 'nested1'
        events[20].source.name == 'GS'
        events[21].source.name == 'GS'
    }
}
