package cucumber.screenplay;

import cucumber.screenplay.internal.BaseActorOnStage;

public interface Actor extends Memory {
    void wasAbleTo(Performable... todos);

    void attemptsTo(Performable... tasks);

    void should(Consequence... consequences);

    String getName();

    void can(Ability doSomething);

    /**Utility method for more reusability - TODO evaluate need again in future*/
    void performSteps(String keyword, Object performer, Object ... steps);

}
