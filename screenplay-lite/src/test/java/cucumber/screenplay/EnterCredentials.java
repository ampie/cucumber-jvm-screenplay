package cucumber.screenplay;

import cucumber.screenplay.annotations.Step;

/**
 * Created by ampie on 2017/05/18.
 */
public class EnterCredentials implements Task {
    private String userName;
    private String password;
    private Throwable failure;

    public EnterCredentials(String userName) {
        this.userName = userName;
    }

    public EnterCredentials andPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    @Step("enter the userName '#userName' and password '#password'")
    public <T extends Actor> T performAs(T actor) {
        if (failure instanceof Error) {
            throw (Error) failure;
        }
        if (failure instanceof RuntimeException) {
            throw (RuntimeException) failure;
        }
        return actor;
    }


    public static EnterCredentials theUserName(final String theUserName) {
        return new EnterCredentials(theUserName);
    }

    public EnterCredentials butItFailseWith(Throwable e) {
        this.failure = e;
        return this;
    }
}
