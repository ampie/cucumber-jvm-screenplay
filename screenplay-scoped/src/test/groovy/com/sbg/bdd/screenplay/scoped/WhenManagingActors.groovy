package com.sbg.bdd.screenplay.scoped

import com.sbg.bdd.screenplay.core.Actor
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.persona.CharacterType
import com.sbg.bdd.screenplay.core.persona.Persona

import static OnStage.dismissActorFromStage
import static OnStage.shineSpotlightOn
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed


class WhenManagingActors extends WhenUsingScopes {
    def 'should reload dynamic actors entering for a second time'() {
        def globalScope = buildGlobalScope("GS")
        given:
        OnStage.present(globalScope)
        globalScope.startFunctionalScope("nested1").startScenario("scenario1").startStep("step1")
        def john = shineSpotlightOn(actorNamed('John Smith')).actor
        def persona = (Persona) john.recall(Actor.PERSONA)
        persona.characterType = CharacterType.DYNAMIC
        when:
        dismissActorFromStage(john)
        def newJohn = shineSpotlightOn(actorNamed('John Smith')).actor
        then:
        newJohn != john
    }


}
