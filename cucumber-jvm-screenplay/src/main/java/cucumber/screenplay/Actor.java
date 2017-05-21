package cucumber.screenplay;

import cucumber.screenplay.Ability;
import cucumber.screenplay.Consequence;
import cucumber.screenplay.Memory;
import cucumber.screenplay.Performable;

public interface Actor extends Memory {
    void wasAbleTo(Performable... todos);

    void attemptsTo(Performable... tasks);

    void should(Consequence... consequences);

    void perform(String keyword, Performable ... performables);

    String getName();

    void can(Ability doSomething);
}
