package com.sbg.bdd.cucumber.screenplay.core

import com.sbg.bdd.screenplay.core.internal.BaseActor

import static java.util.Arrays.asList

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
        def firstChild = firstStep.children[0]
        firstChild.children.size() == 2
        firstChild.children[0].keyword == "evaluateFor"
        firstChild.children[0].name == "see that is zero a number"
        firstChild.children[0].result.duration == 9999
        firstChild.children[0].result.status == "passed"
        firstChild.children[1].keyword == "evaluateFor"
        firstChild.children[1].name == "see that the text to expect  is \"expect this text\""
        firstChild.children[1].result.status == "passed"
        firstChild.result.status == "passed"

    }
}
