package com.sbg.bdd.screenplay.core.actors;

import com.sbg.bdd.screenplay.core.Ability;
import com.sbg.bdd.screenplay.core.Actor;

import java.util.Collection;

public interface Cast {
    Actor actorNamed(String actorName, Ability... abilities);

    Collection<Actor> getActors();

    void dismiss(Actor actor);
}
