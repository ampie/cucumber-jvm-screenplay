package com.sbg.bdd.screenplay.scoped;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ScreenplayPhrases;
import com.sbg.bdd.screenplay.core.actors.OnStage;

public class ScopingPhrases extends ScreenplayPhrases {

    public static Actor theGuest(){
        return GuestInScope.guest((GlobalScope) OnStage.performance());
    }
    public static Actor everybody(){
        return EverybodyInScope.everybody((GlobalScope) OnStage.performance());
    }
}
