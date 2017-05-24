package cucumber.screenplay.events;

import cucumber.screenplay.annotations.StepListener;

import java.lang.reflect.Method;


public class StepEventCallback extends ScreenPlayEventCallback {
    public StepEventCallback(Object target, Method method, StepListener stepListener) {
        super(target, method,stepListener.namePattern());
    }
}
