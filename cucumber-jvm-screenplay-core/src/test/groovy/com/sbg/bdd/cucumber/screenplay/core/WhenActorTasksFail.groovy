package com.sbg.bdd.cucumber.screenplay.core

import com.sbg.bdd.screenplay.core.internal.BaseActor

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
        firstStep.children.size() == 1

        def firstChildStep = firstStep.children[0]
//        firstChildStep.children.size() == 2
        firstChildStep.keyword == "wasAbleTo"
        firstChildStep.name == "Given that John Smith was able to "
        firstChildStep.children[0].keyword == "performAs"
        firstChildStep.children[0].name == "enter the userName 'john@gmail.com' and password 'Password123'"
        firstChildStep.children[0].match.arguments.size() == 2
        firstChildStep.children[0].match.arguments[0].val == "john@gmail.com"
        firstChildStep.children[0].result.duration == 9999
        firstChildStep.children[0].result.status == "failed"
        firstChildStep.children[1].result.status == "skipped"
        firstChildStep.result.status == "failed"
        firstStep.result.status == "failed"

    }
}
