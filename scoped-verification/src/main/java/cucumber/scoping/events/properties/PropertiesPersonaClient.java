package cucumber.scoping.events.properties;

import cucumber.scoping.persona.Persona;
import cucumber.scoping.persona.PersonaClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class PropertiesPersonaClient implements PersonaClient<Properties> {

    @Override
    public Persona preparePersona(String personaName, File file) throws IOException {
        return readPersona(personaName, file);
    }

    private Persona readPersona(String name, File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            Properties properties = new Properties();
            properties.load(reader);
            return new PropertiesPersona(name, properties);
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
    public void savePersonaLocally(Persona<Properties> persona, File targetFile) throws IOException {
        try (FileWriter fileWriter = new FileWriter(targetFile)) {
            fileWriter.write(persona.getDataObject().toString());
        }
    }

    @Override
    public Persona extractPersona(String name, String userName) throws IOException {
        Properties data = new Properties();
        data.setProperty("name", name);
        data.setProperty("userName", userName);
        return new PropertiesPersona(name, data);
    }

    @Override
    public String getDefaultPersonaFileName() {
        return "persona.properties";
    }

}
