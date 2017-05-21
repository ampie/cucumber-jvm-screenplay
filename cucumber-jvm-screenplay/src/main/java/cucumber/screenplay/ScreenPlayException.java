package cucumber.screenplay;

import cucumber.runtime.CucumberException;

public class ScreenPlayException extends CucumberException{
    public ScreenPlayException(String message) {
        super(message);
    }

    public ScreenPlayException(String message, Throwable e) {
        super(message, e);
    }

    public ScreenPlayException(Throwable e) {
        super(e);
    }
}
