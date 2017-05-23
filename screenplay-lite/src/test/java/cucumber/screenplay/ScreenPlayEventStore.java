package cucumber.screenplay;

import cucumber.screenplay.annotations.StepListener;
import cucumber.screenplay.events.ScreenPlayEvent;
import cucumber.screenplay.internal.ChildStepInfo;
import cucumber.screenplay.internal.StepErrorTally;
import static cucumber.screenplay.events.ScreenPlayEvent.Type.*;

import java.util.ArrayList;
import java.util.List;

public class ScreenPlayEventStore {
    private static  List<ScreenPlayEvent> events = new ArrayList<>();
    @StepListener()
    public void listen(ScreenPlayEvent event){
        events.add(event);
    }
    public static List<ScreenPlayEvent> getEvents() {
        return events;
    }
}
