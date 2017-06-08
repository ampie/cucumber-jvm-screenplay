package com.sbg.bdd.cucumber.screenplay.core

import com.sbg.bdd.screenplay.core.internal.BaseActor

import static java.util.Arrays.asList


class WhenActorTasksAreIgnored extends WhenPerformingChildSteps {
    def 'the ignored task should reflect as skipped, but subsequent tasks should still reflect their correct status'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        when:
        def report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/screenplay/OneTaskIgnoredOneImplemented.feature"));
        then:
        report.size() == 1
        report[0].elements.size() == 1
        report[0].elements[0].steps.size() == 1

        def firstStep = report[0].elements[0].steps[0]
        firstStep.name == 'John Smith performs one ignored and one implemented task'
        firstStep.children.size() == 1

        def firstChildStep = firstStep.children[0]
        firstChildStep.keyword == "wasAbleTo"
        firstChildStep.name== "Given that John Smith was able to "
        firstChildStep.children[0].keyword == "performAs"
        firstChildStep.children[0].name == "open the TODO screen"
        firstChildStep.children[0].result.duration == null
        firstChildStep.children[0].result.status == "skipped"
        firstChildStep.children[1].result.status == "passed"
        firstChildStep.children[1].result.duration == 9999
        firstChildStep.result.status== "passed"
        firstStep.result.status == "passed"
    }
}
