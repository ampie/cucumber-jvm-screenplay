package com.sbg.bdd.screenplay.scoped.junit.package2;


import com.sbg.bdd.screenplay.core.annotations.SceneListener;
import com.sbg.bdd.screenplay.core.events.SceneEvent;
import com.sbg.bdd.screenplay.scoped.junit.ScopingRule;
import org.junit.Rule;
import org.junit.Test;

public class ScopedTest2_1 {
    @Rule
    public ScopingRule scopingRule = new ScopingRule();

    @Test
    public void testMe() throws Exception {

    }
}
