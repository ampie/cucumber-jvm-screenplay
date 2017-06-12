package com.sbg.bdd.screenplay.wiremock;

import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.events.StepEvent;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Listener {
    public static List<StepEvent> EVENTS = new ArrayList<>();

    @StepListener
    public void listen(StepEvent event) {
        EVENTS.add(event);

    }
    public static void main(String[] args) throws MalformedURLException {
        System.out.println(new URL("http://est:wer@asdf:9090/path/pat?wer=23").getAuthority());
    }
}