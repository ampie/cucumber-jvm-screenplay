package cucumber.scoping.events;

import cucumber.scoping.UserInScope;
import cucumber.scoping.annotations.UserInvolvement;

import java.util.EventObject;


public class UserEvent extends EventObject {
    private final UserInvolvement involvement;

    public UserEvent(UserInScope source, UserInvolvement involvement) {
        super(source);
        this.involvement=involvement;

    }
    public UserInScope getUserInScope(){
        return (UserInScope) getSource();
    }

    public UserInvolvement getInvolvement() {
        return involvement;
    }
}
