package com.sbg.bdd.screenplay.core;


public interface Ability {
    <T extends Ability> T asActor(Actor actor);
}
