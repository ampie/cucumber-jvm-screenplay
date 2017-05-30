package com.sbg.bdd.screenplay.core.actors;


import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;

import java.io.File;

public interface Performance {

    Cast getCast();

    Scene raiseTheCurtain(String sceneName);

    void drawTheCurtain();

    <T> T recall(String variableName);

    Scene currentScene();

    ScreenPlayEventBus getEventBus();
    
    String getName();
}
