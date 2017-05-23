package cucumber.scoping

import cucumber.scoping.annotations.ScopePhase
import cucumber.scoping.annotations.UserInvolvement
import cucumber.screenplay.actors.OnStage

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
        scenario.startStep('step1').startChildStep('childStep')
        enter(actorNamed('John Smith'))
        shineSpotlightOn(actorNamed('John Smith'))
        globalScope.complete()
        then:
        def events = EventStore.events
//        events.size() == 4 + 4 + 4 + 4 + 4 + 6
        events[0].source.name == 'GS'
        events[0].scopePhase == ScopePhase.BEFORE_START
        events[1].source.name == 'GS'
        events[1].scopePhase == ScopePhase.AFTER_START
        events[2].source.name == 'nested1'
        events[3].source.name == 'nested1'
        events[4].source.name == 'scenario1'
        events[5].source.name == 'scenario1'
        events[6].source.name == 'step1'
        events[7].source.name == 'step1'
        events[8].source.name == 'childStep'
        events[9].source.name == 'childStep'
        events[10].source.name == 'John Smith'
        events[10].involvement== UserInvolvement.BEFORE_ENTER_STAGE
        events[11].source.name == 'John Smith'
        events[11].involvement== UserInvolvement.AFTER_ENTER_STAGE
        events[12].source.name == 'John Smith'
        events[12].involvement== UserInvolvement.INTO_SPOTLIGHT
        events[13].source.name == 'childStep'
        events[13].scopePhase == ScopePhase.BEFORE_COMPLETE
        events[14].source.name == 'childStep'
        events[14].scopePhase == ScopePhase.AFTER_COMPLETE
        events[15].source.name == 'step1'
        events[16].source.name == 'step1'
        events[17].source.name == 'John Smith'
        events[17].involvement== UserInvolvement.OUT_OF_SPOTLIGHT
        events[18].source.name == 'John Smith'
        events[18].involvement== UserInvolvement.BEFORE_EXIT_STAGE
        events[19].source.name == 'John Smith'
        events[19].involvement== UserInvolvement.AFTER_EXIT_STAGE
        events[20].source.name == 'scenario1'
        events[21].source.name == 'scenario1'
        events[22].source.name == 'nested1'
        events[23].source.name == 'nested1'
        events[24].source.name == 'GS'
        events[25].source.name == 'GS'
    }
}
