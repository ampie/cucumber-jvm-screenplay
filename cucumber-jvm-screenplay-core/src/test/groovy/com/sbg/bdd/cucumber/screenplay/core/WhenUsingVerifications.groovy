package com.sbg.bdd.cucumber.screenplay.core

import com.sbg.bdd.screenplay.core.internal.BaseActor

import static java.util.Arrays.asList


class WhenUsingVerifications extends WhenPerformingChildSteps {
    def 'the verifications should reflect as child steps to the steps from which they were performed'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        when:
        def report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/screenplay/TwoVerifications.feature"));
        then:
        report.size() == 1
        report[0].elements.size() == 1
        report[0].elements[0].steps.size() == 1

        def firstStep = report[0].elements[0].steps[0]
        firstStep.name == 'two verifications are performed successfully'
        firstStep.children.size() == 1
        firstStep.children[0].keyword == "verifyThat"
        firstStep.children[0].name == "For requests from John Smith, verify that "
        firstStep.children[0].result.duration == 9999
        firstStep.children[0].result.status == "passed"
        firstStep.children[0].children[0].name == "doStuff was called with <10>"
        firstStep.children[0].children[0].result.duration == 9999
        firstStep.children[0].children[0].result.status == "passed"
        firstStep.result.status == "passed"

    }
}
