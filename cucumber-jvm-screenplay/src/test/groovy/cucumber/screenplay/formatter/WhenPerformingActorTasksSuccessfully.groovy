package cucumber.screenplay.formatter

import cucumber.runtime.StopWatch

import static java.util.Arrays.asList

/**
 * Created by ampie on 2017/05/17.
 */
class WhenPerformingActorTasksSuccessfully extends WhenPerformingChildSteps {
    def 'the tasks should reflect as child steps to the steps from which they were executed'() {
        given:
        BaseActor.useStopWatch(new StopWatch.Stub(9999))
        when:
        def report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/screenplay/TwoTasksSuccessfully.feature"));
        then:
        report.size() == 1
        report[0].elements.size() == 1
        report[0].elements[0].steps.size() == 1

        def firstStep = report[0].elements[0].steps[0]
        firstStep.name == 'John Smith performs two implemented tasks'
        firstStep.children.size() == 2
        firstStep.children[0].keyword == "Was able to"
        firstStep.children[0].name == "enter the userName 'john@gmail.com' and password 'Password123'"
        firstStep.children[0].match.arguments.size() == 2
        firstStep.children[0].match.arguments[0].val == "john@gmail.com"
        firstStep.children[0].match.arguments[1].val == "Password123"
        firstStep.children[0].result.duration == 9999
        firstStep.children[0].result.status == "passed"
        firstStep.children[1].keyword == "Was able to"
        firstStep.children[1].name == "successfully submit the credentials"
        firstStep.children[1].result.status == "passed"
        firstStep.result.status == "passed"

    }
}
