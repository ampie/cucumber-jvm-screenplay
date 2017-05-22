package cucumber.screenplay.formatter

import cucumber.screenplay.StopWatchStub
import cucumber.screenplay.internal.BaseActor

import static java.util.Arrays.asList

/**
 * Created by ampie on 2017/05/17.
 */
class WhenAnsweringQuestionsSuccessfully extends WhenPerformingChildSteps {
    def 'the tasks should reflect as child steps to the steps from which they were executed'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        when:
        def report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/screenplay/TwoQuestionsAnsweredSuccessfully.feature"));
        then:
        report.size() == 1
        report[0].elements.size() == 1
        report[0].elements[0].steps.size() == 1

        def firstStep = report[0].elements[0].steps[0]
        firstStep.name == 'two questions are answered successfully'
        firstStep.children.size() == 2
        firstStep.children[0].keyword == "Should"
        firstStep.children[0].name == "Then is zero a number"
        firstStep.children[0].result.duration == 9999
        firstStep.children[0].result.status == "passed"
        firstStep.children[1].keyword == "Should"
        firstStep.children[1].name == "Then the text to expect  should be \"expect this text\""
        firstStep.children[1].result.status == "passed"
        firstStep.result.status == "passed"

    }
}
