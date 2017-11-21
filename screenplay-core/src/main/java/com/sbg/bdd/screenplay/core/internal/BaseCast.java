package com.sbg.bdd.screenplay.core.internal;

import com.sbg.bdd.screenplay.core.Ability;
import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.actors.Cast;
import com.sbg.bdd.screenplay.core.actors.CastingDirector;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provide simple support for managing Screenplay actors in Cucumber-JVM or JBehave
 */
public class BaseCast implements Cast {

    Map<String, Actor> actors = new HashMap<>();
    private CastingDirector castingDirector;
    private ScreenPlayEventBus eventBus;

    public BaseCast(ScreenPlayEventBus eventBus, CastingDirector castingDirector) {
        this.eventBus = eventBus;
        this.castingDirector = castingDirector;
    }

    @Override
    public Actor actorNamed(String actorName, Ability... abilities) {
        if (!actors.containsKey(actorName)) {
            Actor newActor = castingDirector.recruitActor(actorName);
                addAbilities(actorName, newActor, abilities);
        }
        return actors.get(actorName);
    }

    /**
     * Rather use abilities to distinguish between actors with contracts and actors without
     * @param actorName
     * @param abilities
     * @return
     */
    @Deprecated
    public Actor candidateActor(String actorName, Ability... abilities) {
        if (!actors.containsKey(actorName)) {
            Actor newActor = new BaseActor(eventBus, actorName);
            addAbilities(actorName, newActor, abilities);
        }
        return actors.get(actorName);
    }

    private void addAbilities(String actorName, Actor newActor, Ability[] abilities) {
        for (Ability doSomething : abilities) {
            newActor.can(doSomething);
        }
        actors.put(actorName, newActor);
    }

    @Override
    public Collection<Actor> getActors() {
        return Collections.unmodifiableCollection(actors.values());
    }

    public void dismissAll() {
        actors.clear();
    }

    @Override
    public void dismiss(Actor actor) {
        actors.remove(actor.getName());
    }
}
