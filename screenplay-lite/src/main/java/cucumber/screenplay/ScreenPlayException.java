package cucumber.screenplay;


public class ScreenPlayException extends RuntimeException{
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
