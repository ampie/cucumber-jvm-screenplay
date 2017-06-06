package com.sbg.bdd.screenplay.restassured;

import com.sbg.bdd.screenplay.core.annotations.Step;
import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.events.StepEvent;

import java.util.ArrayList;
import java.util.List;

public class TaskListener {
    public static List<StepEvent> EVENTS = new ArrayList<>();
    @StepListener
    public void listToStep(StepEvent e){
        EVENTS.add(e);
    }
}
