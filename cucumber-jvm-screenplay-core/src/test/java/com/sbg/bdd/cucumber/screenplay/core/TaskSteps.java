package com.sbg.bdd.cucumber.screenplay.core;

import com.sbg.bdd.cucumber.common.ScreenPlayFormatter;
import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.Performable;
import com.sbg.bdd.screenplay.core.Task;
import com.sbg.bdd.screenplay.core.annotations.PendingStep;
import com.sbg.bdd.screenplay.core.annotations.Step;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.junit.Ignore;

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.*;

public class TaskSteps {

    @Given("^John Smith performs two implemented tasks$")
    public void johnSmithHasLoggedIntoTheApplication() throws Throwable {
        Actor johnSmith = actorNamed("John Smith");
        givenThat(johnSmith).wasAbleTo(
                performAnImplementedTask(),
                performAAnotherImplementedTask());
    }

    private Performable performAAnotherImplementedTask() {
        return new Performable() {
            @Override
            @Step("successfully submit the credentials")
            public <T extends Actor> T performAs(T actor) {
                return actor;
            }
        };
    }


    @And("^he performs two pending tasks$")
    public void hePerformsTwoPendingTasks() throws Throwable {
        Actor johnSmith = actorNamed("John Smith");
        givenThat(johnSmith).wasAbleTo(
                performATaskThatThrowsAPendingException(),
                performATaskAnnotatedAsPending());
    }


    @When("^he views his todo items$")
    public void heViewsHisTodoItems() throws Throwable {
        Actor johnSmith = actorNamed("John Smith");
        when(johnSmith).attemptsTo(
                performAnIgnoredTask(),
                performAnImplementedTask()
        );
    }


    private Submit performATaskAnnotatedAsPending() {
        return Submit.theCredentials();
    }

    private EnterCredentials performATaskThatThrowsAPendingException() {
        return performAFailingTask(new PendingException());
    }

    @Given("^John Smith performs one pending and one implemented task$")
    public void johnSmithPerformsOnePendingAndOneImplementedTask() throws Throwable {
        Actor johnSmith = actorNamed("John Smith");
        givenThat(johnSmith).wasAbleTo(
                performATaskAnnotatedAsPending(),
                performAAnotherImplementedTask());
    }

    @When("^John Smith performs one ignored and one implemented task$")
    public void johnSmithPerformsOneIgnoredAndOneImplementedTask() throws Throwable {
        Actor johnSmith = actorNamed("John Smith");
        givenThat(johnSmith).wasAbleTo(
                performAnIgnoredTask(),
                performAnImplementedTask());
    }

    @Given("^John Smith performs one failing task and one implemented task$")
    public void johnSmithPerformsOneFailingTaskAndOneImplementedTask() throws Throwable {
        Actor johnSmith = actorNamed("John Smith");
        givenThat(johnSmith).wasAbleTo(
                performAFailingTask(new RuntimeException()),
                performAnImplementedTask());
    }

    private EnterCredentials performAFailingTask(RuntimeException e) {
        return EnterCredentials.theUserName("john@gmail.com").andPassword("Password123").butItFailseWith(e);
    }

    @When("^John Smith performs a nested task from within an outer task$")
    public void johnSmithPerformsANestedTaskFromWithinAnOuterTask() throws Throwable {
        ScreenPlayFormatter.getCurrent().embedding("embedding1", new byte[0]);
        givenThat(actorNamed("John Smith")).wasAbleTo(new Task() {
            @Override
            @Step("outer task")
            public <T extends Actor> T performAs(T actor) {
                ScreenPlayFormatter.getCurrent().embedding("embedding2", new byte[0]);
                givenThat(actor).wasAbleTo(new Task() {
                    @Override
                    @Step("inner task")
                    public <T extends Actor> T performAs(T actor) {
                        ScreenPlayFormatter.getCurrent().embedding("embedding3", new byte[0]);
                        return actor;
                    }
                });
                return actor;
            }
        });
    }

    public static class Submit implements Task {
        public static Submit theCredentials() {
            return new Submit();
        }

        @Override
        @Step("submit the credentials")
        @PendingStep
        public <T extends Actor> T performAs(T actor) {
            return actor;
        }
    }


    private EnterCredentials performAnImplementedTask() {
        return EnterCredentials.theUserName("john@gmail.com").andPassword("Password123");
    }


    private Task performAnIgnoredTask() {
        return new Task() {
            @Override
            @Ignore
            @Step("open the TODO screen")
            public <T extends Actor> T performAs(T actor) {
                return actor;
            }
        };
    }

}
