package cucumber.screenplay.actors;

import cucumber.screenplay.Actor;

public interface CastingDirector {
    /**
     * Gets an Actor, contract signed (logged in) and ready for work
     * @param name
     * @return
     */
    Actor recruitActor(String name);
    /**
     * A candidate actor that does not yet have an official role in the play
     * This is a user who does not (yet) have a profile on the system, or whose
     * activity extends beyond any single profile
     * and only ready for activity that falls outside of actual contracted work
     * @param name
     * @return
     */
    Actor findCandidate(String name);

}
