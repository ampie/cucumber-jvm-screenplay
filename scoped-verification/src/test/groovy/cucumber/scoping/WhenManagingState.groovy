package cucumber.scoping

import cucumber.screenplay.actors.OnStage

import static cucumber.scoping.ScopingPhrases.everybody
import static cucumber.scoping.ScopingPhrases.everybody
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.actors.OnStage.shineSpotlightOn
import static cucumber.screenplay.actors.OnStage.shineSpotlightOn
import static cucumber.screenplay.actors.OnStage.shineSpotlightOn
import static cucumber.screenplay.actors.OnStage.shineSpotlightOn
import static cucumber.screenplay.actors.OnStage.shineSpotlightOn
import static cucumber.screenplay.actors.OnStage.shineSpotlightOn
import static cucumber.screenplay.actors.OnStage.shineSpotlightOn
import static cucumber.screenplay.actors.OnStage.shineSpotlightOn
import static cucumber.screenplay.actors.OnStage.shineSpotlightOn
import static cucumber.screenplay.actors.OnStage.theActorInTheSpotlight
import static cucumber.screenplay.actors.OnStage.theActorInTheSpotlight
import static cucumber.screenplay.actors.OnStage.theActorInTheSpotlight
import static cucumber.screenplay.actors.OnStage.theActorInTheSpotlight
import static cucumber.screenplay.actors.OnStage.theActorInTheSpotlight
import static cucumber.screenplay.actors.OnStage.theActorInTheSpotlight
import static cucumber.screenplay.actors.OnStage.theActorInTheSpotlight
import static cucumber.screenplay.actors.OnStage.theActorInTheSpotlight
import static cucumber.screenplay.actors.OnStage.theActorInTheSpotlight
import static cucumber.screenplay.actors.OnStage.theActorInTheSpotlight
import static cucumber.screenplay.actors.OnStage.theActorInTheSpotlight
import static cucumber.screenplay.actors.OnStage.theActorInTheSpotlight

/**
 * Created by ampie on 2017/05/23.
 */
class WhenManagingState extends WhenUsingScopes {
    def 'should keep track of the actor in the spotlight irrespective of how deep the scope stack is'() {
        def globalScope = buildGlobalScope("GS")
        given:
        OnStage.present(globalScope)
        globalScope.startFunctionalScope("nested1").startScenario("scenario1").startStep("step1")
        when:
        shineSpotlightOn(actorNamed('John Smith'))
        then:
        theActorInTheSpotlight().actor.name == 'John Smith'
        theActorInTheSpotlight().scope.id == 'scenario1'

    }
    def 'should remember variables stored in the parent scope for an actor'() {
        def globalScope = buildGlobalScope("GS")
        given:
        OnStage.present(globalScope)
        def scope = globalScope.startFunctionalScope("nested1")
        shineSpotlightOn(actorNamed("John Smith"))
        theActorInTheSpotlight().remember("var","nested1Value")
        when:
        scope.startScenario("scenario1").startStep("step1")
        shineSpotlightOn(actorNamed("John Smith"))
        then:
        def value = theActorInTheSpotlight().recall("var")
        value == 'nested1Value'
    }
    def 'should override variables stored in the parent scope for an actor with the value in current scope'() {
        def globalScope = buildGlobalScope("GS")
        given:
        OnStage.present(globalScope)
        def scope = globalScope.startFunctionalScope("nested1")
        shineSpotlightOn(actorNamed("John Smith"))
        theActorInTheSpotlight().remember("var","nested1Value")
        when:
        scope.startScenario("scenario1").startStep("step1")
        shineSpotlightOn(actorNamed("John Smith"))
        theActorInTheSpotlight().remember("var","step1Value")
        then:
        def value = theActorInTheSpotlight().recall("var")
        value == 'step1Value'
    }
    def 'should remember variables stored for everybody'() {
        def globalScope = buildGlobalScope("GS")
        given:
        OnStage.present(globalScope)
        def scope = globalScope.startFunctionalScope("nested1")
        shineSpotlightOn(everybody())
        theActorInTheSpotlight().remember("var","everybodyValue")
        when:
        scope.startScenario("scenario1").startStep("step1")
        shineSpotlightOn(actorNamed("John Smith"))
        then:
        def value = theActorInTheSpotlight().recall("var")
        value == 'everybodyValue'
    }
    def 'should override variables stored for everybody with the value in current actor'() {
        def globalScope = buildGlobalScope("GS")
        given:
        OnStage.present(globalScope)
        def scope = globalScope.startFunctionalScope("nested1")
        shineSpotlightOn(everybody())
        theActorInTheSpotlight().remember("var","everybodyValue")
        when:
        scope.startScenario("scenario1").startStep("step1")
        shineSpotlightOn(actorNamed("John Smith"))
        theActorInTheSpotlight().remember("var","johnSmithValue")
        then:
        def value = theActorInTheSpotlight().recall("var")
        value == 'johnSmithValue'
    }

}
