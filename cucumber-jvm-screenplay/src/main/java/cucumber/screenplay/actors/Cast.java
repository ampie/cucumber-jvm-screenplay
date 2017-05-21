package cucumber.screenplay.actors;

import cucumber.screenplay.Ability;
import cucumber.screenplay.Actor;

import java.util.*;

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

        if (! actors.containsKey(actorName)) {
            Actor newActor =  castingDirector.recruitActor(actorName);

            for(Ability doSomething : abilities) {
                newActor.can(doSomething);
            }
            actors.put(actorName, newActor);
        }
        return actors.get(actorName);
    }

    public Collection<Actor> getActors() {
        return Collections.unmodifiableCollection(actors.values());
    }

    public void dismissAll() {
        actors.clear();
    }

}
