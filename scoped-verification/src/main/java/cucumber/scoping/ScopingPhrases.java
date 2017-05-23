package cucumber.scoping;

import cucumber.screenplay.Actor;
import cucumber.screenplay.ScreenplayPhrases;
import cucumber.screenplay.actors.OnStage;

public class ScopingPhrases extends ScreenplayPhrases {

    public static Actor theGuest(){
        return GuestInScope.guest((GlobalScope) OnStage.performance());
    }
    public static Actor everybody(){
        return EverybodyInScope.everybody((GlobalScope) OnStage.performance());
    }
}
