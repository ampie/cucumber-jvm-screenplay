package cucumber.scoping

import cucumber.screenplay.Actor
import cucumber.screenplay.Task
import cucumber.screenplay.actors.OnStage
import cucumber.screenplay.annotations.Step

import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.givenThat
import static cucumber.screenplay.ScreenplayPhrases.givenThat

/**
 * Created by ampie on 2017/05/23.
 */
class WhenTrackingScope extends WhenUsingScopes{
    def 'should keep track of child step when calling actor tasks'() {
        def globalScope = buildGlobalScope("GS")
        given:
        OnStage.present(globalScope)
        globalScope.startFunctionalScope("nested1").startScenario("scenario1").startStep("step1")
        when:
        def stepName = null
        givenThat(actorNamed('John Smith')).wasAbleTo(new Task() {
            @Override
            @Step('do stuff')
            def <T extends Actor> T performAs(T actor) {
                stepName = globalScope.getInnerMostActive(StepScope).getId()
                return actor
            }
        })
        then:
        stepName == 'do_stuff' //inner step
        globalScope.getInnerMostActive(StepScope).id == 'step1' //outer step
    }
    def 'should keep track of child step when calling actor tasks from within actor tasks'() {
        def globalScope = buildGlobalScope("GS")
        given:
        OnStage.present(globalScope)
        globalScope.startFunctionalScope("nested1").startScenario("scenario1").startStep("step1")
        when:
        def middleStep = null
        def deepStep = null
        givenThat(actorNamed('John Smith')).wasAbleTo(new Task() {
            @Override
            @Step('middleStep')
            def <T extends Actor> T performAs(T actor) {
                middleStep = globalScope.getInnerMostActive(StepScope).getId()
                actor.wasAbleTo(new Task() {
                    @Override
                    @Step('deepStep')
                    def <T extends Actor> T performAs(T actor1) {
                        deepStep = globalScope.getInnerMostActive(StepScope).getId()
                        return actor1
                    }
                })
                return actor
            }
        })
        then:
        middleStep == 'middleStep'
        deepStep == 'deepStep'
        globalScope.getInnerMostActive(StepScope).id == 'step1' //outer step
    }

}
