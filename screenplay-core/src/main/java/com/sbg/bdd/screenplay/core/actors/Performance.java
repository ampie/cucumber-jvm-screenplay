package com.sbg.bdd.screenplay.core.actors;


import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;

public interface Performance {

    String INPUT_RESOURCE_ROOT = "inputResourceRoot";
    String OUTPUT_RESOURCE_ROOT = "outputResourceRoot";

    Cast getCast();

    Scene raiseTheCurtain(String sceneName);

    void drawTheCurtain();

    <T> T recall(String variableName);

    Scene currentScene();

    ScreenPlayEventBus getEventBus();
    
    String getName();
}
