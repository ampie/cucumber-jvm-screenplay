package com.sbg.bdd.cucumber.screenplay.core

import com.sbg.bdd.screenplay.core.internal.BaseActor

import static java.util.Arrays.asList

/**
 * Created by ampie on 2017/05/17.
 */
class WhenPerformingActorTasksSuccessfully extends WhenPerformingChildSteps {
    def 'the tasks should reflect as child steps to the steps from which they were executed'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        when:
        def report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/screenplay/TwoTasksSuccessfully.feature"));
        then:
        report.size() == 1
        report[0].elements.size() == 1
        report[0].elements[0].steps.size() == 1

        def firstStep = report[0].elements[0].steps[0]
        firstStep.name == 'John Smith performs two implemented tasks'
        def firstChildStep = firstStep.children[0]
        firstChildStep.name =="Given that John Smith was able to "
        firstChildStep.children.size() == 2
        firstChildStep.children[0].keyword == "performAs"
        firstChildStep.children[0].name == "enter the userName 'john@gmail.com' and password 'Password123'"
        firstChildStep.children[0].match.arguments.size() == 2
        firstChildStep.children[0].match.arguments[0].val == "john@gmail.com"
        firstChildStep.children[0].match.arguments[1].val == "Password123"
        firstChildStep.children[0].result.duration == 9999
        firstChildStep.children[0].result.status == "passed"
        firstChildStep.children[1].keyword == "performAs"
        firstChildStep.children[1].name == "successfully submit the credentials"
        firstChildStep.children[1].result.status == "passed"
        firstChildStep.result.status == "passed"

    }
}
