package com.sbg.bdd.screenplay.scoped.junit.package1;


import com.sbg.bdd.screenplay.core.annotations.SceneListener;
import com.sbg.bdd.screenplay.core.events.SceneEvent;
import com.sbg.bdd.screenplay.scoped.junit.ScopingRule;
import org.junit.Rule;
import org.junit.Test;

public class ScopedTest1_2 {
    @Rule
    public ScopingRule scopingRule = new ScopingRule();

    @Test
    public void testMe() throws Exception {

    }
}
