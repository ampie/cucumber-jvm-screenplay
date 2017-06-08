package com.sbg.bdd.cucumber.screenplay.core

import com.sbg.bdd.screenplay.core.internal.BaseActor

import static java.util.Arrays.asList

class WhenActorTasksArePending extends WhenPerformingChildSteps {
    def 'the pending task should reflect as undefined, but subsequent tasks should still reflect as child steps'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        when:
        def report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/screenplay/OneTaskPendingOneImplemented.feature"));
        then:
        report.size() == 1
        report[0].elements.size() == 1
        report[0].elements[0].steps.size() == 1

        def firstStep = report[0].elements[0].steps[0]
        firstStep.name == 'John Smith performs one pending and one implemented task'
        def firstChildStep=firstStep.children[0]
        firstChildStep.name == "Given that John Smith was able to "
        firstChildStep.children.size() == 2
        firstChildStep.children[0].keyword == "performAs"
        firstChildStep.children[0].name == "submit the credentials"
        firstChildStep.children[0].result.duration == 9999
        firstChildStep.children[0].result.status == "pending"
        firstChildStep.children[1].result.status == "passed"
        firstChildStep.children[1].name == "successfully submit the credentials"
        firstChildStep.result.status == "pending"
    }
}
