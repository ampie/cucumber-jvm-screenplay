package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.events.StepEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class StepEventStore {
    public static List<Pair<Class,StepEvent>> EVENTS = new ArrayList<>();
}
