package cucumber.scoping


import cucumber.screenplay.actors.OnStage
import cucumber.screenplay.persona.CharacterType
import cucumber.screenplay.persona.Persona

import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.actors.OnStage.dismissActorFromStage
import static cucumber.screenplay.actors.OnStage.shineSpotlightOn

/**
 * Created by ampie on 2017/05/23.
 */
class WhenManagingActors extends WhenUsingScopes {
    def 'should reload dynamic actors entering for a second time'() {
        def globalScope = buildGlobalScope("GS")
        given:
        OnStage.present(globalScope)
        globalScope.startFunctionalScope("nested1").startScenario("scenario1").startStep("step1")
        def john = shineSpotlightOn(actorNamed('John Smith')).actor
        def persona = (Persona) john.recall('persona')
        persona.characterType = CharacterType.DYNAMIC
        when:
        dismissActorFromStage(john)
        def newJohn = shineSpotlightOn(actorNamed('John Smith')).actor
        then:
        newJohn != john
    }


}
