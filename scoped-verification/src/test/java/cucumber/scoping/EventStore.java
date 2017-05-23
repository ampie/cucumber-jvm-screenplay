package cucumber.scoping;

import cucumber.scoping.annotations.ScopePhase;
import cucumber.scoping.annotations.SubscribeToScope;
import cucumber.scoping.annotations.SubscribeToUser;
import cucumber.scoping.events.ScopeEvent;
import cucumber.scoping.events.UserEvent;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * Created by ampie on 2017/05/23.
 */
public class EventStore {
    static private List<EventObject> events = new ArrayList<>();

    @SubscribeToScope()
    public void onScope(ScopeEvent event) {
        events.add(event);
    }

    @SubscribeToUser()
    public void onScope(UserEvent event) {
        events.add(event);
    }

    public static List<EventObject> getEvents() {
        return events;
    }
}
