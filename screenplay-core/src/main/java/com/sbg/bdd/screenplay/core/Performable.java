package com.sbg.bdd.screenplay.core;

public interface Performable {
    Performable DO_NOTHING = new Performable() {

        @Override
        public <T extends Actor> T performAs(T actor) {
            return actor;
        }
    };

    <T extends Actor> T performAs(T actor);
}
