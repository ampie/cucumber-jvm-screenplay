package com.sbg.bdd.cucumber.screenplay.core

import com.sbg.bdd.screenplay.core.internal.BaseActor
import gherkin.deps.net.iharder.Base64

import static java.util.Arrays.asList


class WhenUsingStubs extends WhenPerformingChildSteps {
    def 'the stubs should reflect as child steps to the steps from which they were configured'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        when:
        def report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/screenplay/TwoStubsConfiguredSuccessfully.feature"));
        then:
        report.size() == 1
        report[0].elements.size() == 1
        report[0].elements[0].steps.size() == 1

        def firstStep = report[0].elements[0].steps[0]
        firstStep.name == 'two stubs are configured successfully'
        firstStep.children.size() == 1
        firstStep.children[0].keyword == "allow"
        firstStep.children[0].name == "For requests from John Smith, allow "
        firstStep.children[0].children[0].keyword == "performOnStage"
        firstStep.children[0].children[0].name == "DownstreamSystem.doStuff()"
        firstStep.children[0].children[0].result.duration == 9999
        firstStep.children[0].children[0].result.status == "passed"
        firstStep.children[0].children[0].embeddings[0].mime_type == "text/plain"
        new String(Base64.decode(firstStep.children[0].children[0].embeddings[0].data)) == "Mock downstreamSystem.doStuff(10)).thenReturn(\"John Smith did stuff\")"
//        firstStep.children[1].keyword == "Should"
//        firstStep.children[1].name == "Then the text to expect  should be \"expect this text\""
//        firstStep.children[1].result.status == "passed"
        firstStep.result.status == "passed"

    }
}
