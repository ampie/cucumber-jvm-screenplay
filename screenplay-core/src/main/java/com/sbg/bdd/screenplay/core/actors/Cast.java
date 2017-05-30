package com.sbg.bdd.screenplay.core.actors;

import com.sbg.bdd.screenplay.core.Ability;
import com.sbg.bdd.screenplay.core.Actor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provide simple support for managing Screenplay actors in Cucumber-JVM or JBehave
 */
public class Cast {

    Map<String, Actor> actors = new HashMap<>();
    private CastingDirector castingDirector;

    public Cast(CastingDirector castingDirector) {
        this.castingDirector = castingDirector;
    }

    public Actor actorNamed(String actorName, Ability... abilities) {
        if (!actors.containsKey(actorName)) {
            Actor newActor = castingDirector.recruitActor(actorName);
                addAbilities(actorName, newActor, abilities);
        }
        return actors.get(actorName);
    }


    public Actor candidateActor(String actorName, Ability... abilities) {
        if (!actors.containsKey(actorName)) {
            Actor newActor = castingDirector.findCandidate(actorName);
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

    public Collection<Actor> getActors() {
        return Collections.unmodifiableCollection(actors.values());
    }

    public void dismissAll() {
        actors.clear();
    }

    public void dismiss(Actor actor) {
        actors.remove(actor.getName());
    }
}
