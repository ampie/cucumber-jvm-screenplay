package cucumber.screenplay;

import cucumber.screenplay.annotations.StepListener;
import cucumber.screenplay.events.StepEvent;

import java.util.ArrayList;
import java.util.List;

public class ScreenPlayEventStore {
    private static  List<StepEvent> events = new ArrayList<>();
    @StepListener()
    public void listen(StepEvent event){
        events.add(event);
    }
    public static List<StepEvent> getEvents() {
        return events;
    }
}
