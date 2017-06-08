package com.sbg.bdd.screenplay.core;

public interface Actor extends Memory {
    void useKeyword(String format);

    void wasAbleTo(Performable... todos);

    void attemptsTo(Performable... tasks);

    void should(Consequence... consequences);

    String getName();

    void can(Ability doSomething);
    <T extends Ability> T usingAbilityTo(Class<? extends T> doSomething);
    /**Utility method for more reusability - TODO evaluate need again in future*/
    void performSteps(String keyword, Object performer, Object ... steps);

    String getPrecedingKeyword();
}
