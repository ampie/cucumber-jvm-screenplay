package com.sbg.bdd.cucumber.screenplay.scoped

import com.sbg.bdd.screenplay.core.annotations.ActorInvolvement
import com.sbg.bdd.screenplay.core.annotations.SceneEventType
import com.sbg.bdd.screenplay.core.internal.BaseActor

import static java.util.Arrays.asList

class WhenRunningWithScopes  extends WhenScopeXYZ{



    def 'the correct event should be fired in a predictable  sequence'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        when:
        def report = null;
        try {
            report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/scoping/ScopedStuff.feature"));
        } catch (Exception e) {
            e.printStackTrace()
        }
        then:
        report.size() == 1
        report[0].elements.size() == 4
        report[0].elements[0].steps.size() == 1
        StepDefs.SCOPE_CALLBACKS.keySet().size()==5
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber'][0] == SceneEventType.BEFORE_START
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber'][1] == SceneEventType.AFTER_START
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber'][2] == SceneEventType.BEFORE_COMPLETE
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber'][3] == SceneEventType.AFTER_COMPLETE
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber/scoping'].size() == 4
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber/scoping'][0] == SceneEventType.BEFORE_START
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else'][0] == SceneEventType.BEFORE_START
        StepDefs.USER_CALLBACKS.keySet().size()==5
        StepDefs.USER_CALLBACKS['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else/John'][0] == ActorInvolvement.BEFORE_ENTER_STAGE
        StepDefs.USER_CALLBACKS['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else/John'][1] == ActorInvolvement.AFTER_ENTER_STAGE
        StepDefs.VARIABLE_AFTER_COMPLETE.keySet().size()==5
        StepDefs.VARIABLE_AFTER_START['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else']==4
        StepDefs.VARIABLE_AFTER_COMPLETE['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else']==4//Because we retain the everybody scope's memory

    }


}