package cucumber.screenplay.formatter

import cucumber.runtime.StopWatch

import static java.util.Arrays.asList


class WhenActorTasksAreIgnored extends WhenPerformingChildSteps {
    def 'the ignored task should reflect as skipped, but subsequent tasks should still reflect their correct status'() {
        given:
        BaseActor.useStopWatch(new StopWatch.Stub(9999))
        when:
        def report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/screenplay/OneTaskIgnoredOneImplemented.feature"));
        then:
        report.size() == 1
        report[0].elements.size() == 1
        report[0].elements[0].steps.size() == 1

        def firstStep = report[0].elements[0].steps[0]
        firstStep.name == 'John Smith performs one ignored and one implemented task'
        firstStep.children.size() == 2
        firstStep.children[0].keyword == "Was able to"
        firstStep.children[0].name == "open the TODO screen"
        firstStep.children[0].result.duration == null
        firstStep.children[0].result.status == "skipped"
        firstStep.children[1].result.status == "passed"
        firstStep.children[1].result.duration == 9999
        firstStep.result.status == "passed"
    }
}
