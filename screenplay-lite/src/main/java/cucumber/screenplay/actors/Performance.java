package cucumber.screenplay.actors;


import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.Scene;
import cucumber.screenplay.events.ScreenPlayEventBus;

import java.nio.file.Path;

public interface Performance{

    Cast getCast();

    Scene raiseTheCurtain(String sceneName);

    void drawTheCurtain();

    <T> T recall(String variableName);

    Path getResourceRoot();

    Scene currentScene();

    ScreenPlayEventBus getEventBus();
}
