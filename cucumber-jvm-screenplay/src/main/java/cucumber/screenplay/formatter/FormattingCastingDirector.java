package cucumber.screenplay.formatter;

import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.actors.CastingDirector;

public class FormattingCastingDirector implements CastingDirector {
    @Override
    public Actor recruitActor(String name) {
        return new FormattingActor(name);
    }

    @Override
    public Actor interviewActor(String name) {
        return new FormattingActor(name);
    }

}
