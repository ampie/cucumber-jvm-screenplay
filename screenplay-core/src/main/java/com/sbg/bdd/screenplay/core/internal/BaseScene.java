package com.sbg.bdd.screenplay.core.internal;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Memory;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.actors.Performance;
import com.sbg.bdd.screenplay.core.persona.CharacterType;
import com.sbg.bdd.screenplay.core.persona.Persona;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ampie on 2017/05/24.
 */
public class BaseScene implements Scene {
    private String name;
    private BasePerformance performance;
    private Memory memory = new SimpleMemory();
    private SpotlightOperator spotlightOperator;

    private Map<String, BaseActorOnStage> actorsOnStage = new HashMap<>();

    public BaseScene(BasePerformance performance, String name) {
        this.name = name;
        this.performance = performance;
        spotlightOperator = new SpotlightOperator(this);
    }

    @Override
    public String getSceneIdentifier() {
        return name;
    }

    @Override
    public Performance getPerformance() {
        return performance;
    }

    @Override
    public int getLevel() {
        return 1;
    }

    @Override
    public Map<String, ? extends ActorOnStage> getActorsOnStage() {
        return actorsOnStage;
    }

    @Override
    public ActorOnStage callActorToStage(Actor actor) {
        BaseActorOnStage result = actorsOnStage.get(actor.getName());
        if (result == null) {
            result = new BaseActorOnStage(this, actor);
            actorsOnStage.put(actor.getName(), result);
            result.enterStage();
        }
        return result;
    }

    @Override
    public void dismissActorFromStage(Actor actor) {
        if (spotlightOperator.isSpotlightOn(actor)) {
            spotlightOperator.actorOutOfSpotlight();
        }
        BaseActorOnStage actorOnStage = actorsOnStage.remove(actor);
        if (actorOnStage != null) {
            actorsOnStage.get(actor.getName()).exitStage();
            Persona<?> persona = actor.recall("persona");
            if (persona != null && persona.getCharacterType() == CharacterType.DYNAMIC) {
                //Data may have changed, needs to be reloaded
                getPerformance().getCast().dismiss(actor);
            }
        }

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public <T> T recall(String variableName) {
        return memory.recall(variableName);
    }

    public ActorOnStage shineSpotlightOn(Actor actor) {
        return spotlightOperator.shineSpotlightOn(actor);
    }

    public ActorOnStage theActorInTheSpotlight() {
        return spotlightOperator.getActorInTheSpotlight();
    }

    @Override
    public void remember(String variableName, Object value) {
        memory.remember(variableName, value);
    }

}
