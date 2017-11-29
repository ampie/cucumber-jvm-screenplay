package com.sbg.bdd.screenplay.scoped.junit;

import com.sbg.bdd.screenplay.core.annotations.SceneListener;
import com.sbg.bdd.screenplay.core.events.SceneEvent;
import com.sbg.bdd.screenplay.scoped.junit.package1.ScopedTest1_1;
import com.sbg.bdd.screenplay.scoped.junit.package1.ScopedTest1_2;
import com.sbg.bdd.screenplay.scoped.junit.package2.ScopedTest2_1;
import com.sbg.bdd.screenplay.scoped.junit.package2.ScopedTest2_2;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.ArrayList;
import java.util.List;

@RunWith(ScopedSuite.class)
@Suite.SuiteClasses({ScopedTest1_1.class,ScopedTest1_2.class,ScopedTest2_1.class,ScopedTest2_2.class})
public class ScopedTestSuite {
    public static List<SceneEvent> sceneEventList=new ArrayList<>();
    @SceneListener
    public void listenTo(SceneEvent event){
        sceneEventList.add(event);
        System.out.println(event.getSceneEventType() +":" + event.getScene().getSceneIdentifier());
    }
}
