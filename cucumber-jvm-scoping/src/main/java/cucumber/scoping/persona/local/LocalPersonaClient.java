package cucumber.scoping.persona.local;

import cucumber.scoping.persona.Persona;
import cucumber.scoping.persona.PersonaClient;
import gherkin.deps.com.google.gson.JsonObject;
import gherkin.deps.com.google.gson.JsonParser;

import java.io.*;

public class LocalPersonaClient implements PersonaClient<JsonObject> {

    @Override
    public Persona preparePersona(String personaName, File file) throws IOException {
        return readPersona(personaName, file);
    }

    private Persona readPersona(String name, File file) throws IOException {
        try(FileReader reader = new FileReader(file)) {

            JsonParser parser = new JsonParser();
            JsonObject element = (JsonObject) parser.parse(reader);
            return new LocalPersona(name, element);
        }
    }

    @Override
    public void deletePersona(String username) throws IOException {

    }

    @Override
    public Persona installPersona(String name, File file) throws IOException {
        return readPersona(file.getName(), file);
    }

    @Override
    public void savePersonaLocally(Persona<JsonObject> persona, File targetFile) throws IOException {
        try (FileWriter fileWriter = new FileWriter(targetFile)) {
            fileWriter.write(persona.getDataObject().toString());
        }
    }

    @Override
    public Persona extractPersona(String name, String userName) throws IOException {
        JsonObject data = new JsonObject();
        data.addProperty("name", name);
        data.addProperty("userName", userName);
        return new LocalPersona(name, data);
    }

    @Override
    public String getDefaultPersonaFileName() {
        return "persona.json";
    }

}
