package cucumber.screenplay.actors;

import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;

public interface CastingDirector {
    /**
     * Gets an Actor, contract signed (logged in) and ready for work
     * @param name
     * @return
     */
    Actor recruitActor(String name);
    /**
     * Only get an Actor in to talk to. No contract signed (not logged in)
     * and only ready for activity that falls outside of actual contracted work
     * @param name
     * @return
     */
    Actor interviewActor(String name);

}
