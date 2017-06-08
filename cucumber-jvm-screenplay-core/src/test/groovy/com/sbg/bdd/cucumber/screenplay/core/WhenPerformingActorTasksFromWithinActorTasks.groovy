package com.sbg.bdd.cucumber.screenplay.core

import com.sbg.bdd.screenplay.core.internal.BaseActor

import static java.util.Arrays.asList


class WhenPerformingActorTasksFromWithinActorTasks extends WhenPerformingChildSteps {
    def 'the nested task and its content should be available under the outer task'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        when:
        def report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/screenplay/OneTaskFromWithinAnotherTask.feature"));
        then:
        report.size() == 1
        report[0].elements.size() == 1
        def firstStep = report[0].elements[0].steps[0]
        firstStep.name == 'John Smith performs a nested task from within an outer task'
        firstStep.children.size() == 1
        firstStep.embeddings[0]['mime_type'] == 'embedding1'
        firstStep.embeddings.size()==1
        def firstChildStep=firstStep.children[0]
        firstChildStep.keyword=="wasAbleTo"
        firstChildStep.name=="Given that John Smith was able to "
        firstChildStep.children[0].name == 'outer task'
        firstChildStep.children[0].embeddings[0]['mime_type'] == 'embedding2'
        firstChildStep.children[0].embeddings.size()==1
        firstChildStep.children[0].children.size() == 1
        //NB!! Double nesting here - two nested givenThats
        firstChildStep.children[0].children[0].name=="Given that John Smith was able to "
        firstChildStep.children[0].children[0].children[0].name == 'inner task'
        firstChildStep.children[0].children[0].children[0].embeddings[0]['mime_type'] == 'embedding3'
        firstChildStep.children[0].children[0].children[0].embeddings.size() == 1
    }
}
