package cucumber.screenplay.formatter

import cucumber.runtime.StopWatch

import static java.util.Arrays.asList

class WhenActorTasksArePending extends WhenPerformingChildSteps {
    def 'the pending task should reflect as undefined, but subsequent tasks should still reflect as child steps'() {
        given:
        FormattingActor.useStopWatch(new StopWatch.Stub(9999))
        when:
        def report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/screenplay/OneTaskPendingOneImplemented.feature"));
        then:
        report.size() == 1
        report[0].elements.size() == 1
        report[0].elements[0].steps.size() == 1

        def firstStep = report[0].elements[0].steps[0]
        firstStep.name == 'John Smith performs one pending and one implemented task'
        firstStep.children.size() == 2
        firstStep.children[0].keyword == "Was able to"
        firstStep.children[0].name == "submit the credentials"
        firstStep.children[0].result.duration == 9999
        firstStep.children[0].result.status == "pending"
        firstStep.children[1].result.status == "passed"
        firstStep.children[1].name == "successfully submit the credentials"
        firstStep.result.status == "pending"
    }
}
