package cucumber.screenplay.formatter

import cucumber.screenplay.StopWatchStub
import cucumber.screenplay.internal.BaseActor

import static java.util.Arrays.asList


class WhenActorTasksFail extends WhenPerformingChildSteps {
    def 'the tasks up to the failing task should reflect as child steps, and subsequent tasks should reflect as skipped'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        when:
        def report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/screenplay/OneTaskFailedOneImplemented.feature"));
        then:
        report.size() == 1
        report[0].elements.size() == 1
        report[0].elements[0].steps.size() == 1

        def firstStep = report[0].elements[0].steps[0]
        firstStep.name == 'John Smith performs one failing task and one implemented task'
        firstStep.children.size() == 2
        firstStep.children[0].keyword == "Was able to"
        firstStep.children[0].name == "enter the userName 'john@gmail.com' and password 'Password123'"
        firstStep.children[0].match.arguments.size() == 2
        firstStep.children[0].match.arguments[0].val == "john@gmail.com"
        firstStep.children[0].result.duration == 9999
        firstStep.children[0].result.status == "failed"
        firstStep.children[1].result.status == "skipped"
        firstStep.result.status == "failed"

    }
}
