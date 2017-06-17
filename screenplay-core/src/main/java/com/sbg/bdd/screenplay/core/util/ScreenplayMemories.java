package com.sbg.bdd.screenplay.core.util;

import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.resource.ResourceRoot;
import com.sbg.bdd.screenplay.core.Memory;
import com.sbg.bdd.screenplay.core.actors.Performance;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;

public class ScreenplayMemories<T extends ScreenplayMemories> {
    protected Memory memory;

    public ScreenplayMemories(Memory memory) {

        this.memory = memory;
    }

    public static ScreenplayMemories rememberFor(Memory memory) {
        return  new ScreenplayMemories<ScreenplayMemories>(memory);
    }

    public static ScreenplayMemories recallFor(Memory memory) {
        return new ScreenplayMemories<ScreenplayMemories>(memory);
    }

    public T toUseThePersonaClient(PersonaClient<?> personaClient) {
        memory.remember(Performance.PERSONA_CLIENT, personaClient);
        return (T) this;
    }
    public T toReadResourcesFrom(ResourceContainer root){
        memory.remember(Performance.INPUT_RESOURCE_ROOT, root);
        return (T)this;
    }
    public T toWriteResourcesTo(ResourceContainer root){
        memory.remember(Performance.OUTPUT_RESOURCE_ROOT, root);
        return (T)this;
    }

    public PersonaClient<?> thePersonaClient() {
        return memory.recall(Performance.PERSONA_CLIENT);
    }

    public ResourceContainer theInputResourceRoot() {
        return memory.recall(Performance.INPUT_RESOURCE_ROOT);
    }

    public ResourceContainer theOutputResourceRoot() {
        return memory.recall(Performance.OUTPUT_RESOURCE_ROOT);
    }

}
