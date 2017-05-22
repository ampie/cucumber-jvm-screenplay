package cucumber.screenplay.formatter

import cucumber.screenplay.StopWatchStub
import cucumber.screenplay.internal.BaseActor

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
        firstStep.children[0].name == 'outer task'
        firstStep.children[0].embeddings[0]['mime_type'] == 'embedding2'
        firstStep.children[0].embeddings.size()==1
        firstStep.children[0].children.size() == 1
        firstStep.children[0].children[0].name == 'inner task'
        firstStep.children[0].children[0].embeddings[0]['mime_type'] == 'embedding3'
        firstStep.children[0].children[0].embeddings.size() == 1
    }
}
