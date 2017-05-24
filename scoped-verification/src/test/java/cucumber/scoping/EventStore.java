package cucumber.scoping;

import cucumber.screenplay.annotations.SceneListener;
import cucumber.screenplay.annotations.ActorListener;
import cucumber.screenplay.annotations.StepListener;
import cucumber.screenplay.events.SceneEvent;
import cucumber.screenplay.events.ActorEvent;
import cucumber.screenplay.events.StepEvent;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * Created by ampie on 2017/05/23.
 */
public class EventStore {
    static private List<EventObject> events = new ArrayList<>();

    @SceneListener()
    public void onScope(SceneEvent event) {
        events.add(event);
    }

    @ActorListener()
    public void onScope(ActorEvent event) {
        events.add(event);
    }

    @StepListener
    public void onStep(StepEvent event){
        events.add(event);
    }
    public static List<EventObject> getEvents() {
        return events;
    }
}
