package cucumber.wiremock;

import java.util.HashMap;
import java.util.Map;


public class TemplateBuilder {
    private String fileName;
    private Map<String, Object> variables = new HashMap<>();

    public TemplateBuilder(String templateFileName) {
        this.fileName=templateFileName;
    }

    public TemplateBuilder with(String variableName, Object variable) {
        variables.put(variableName, variable);
        return this;
    }


    public String getFileName() {
        return fileName;
    }
    
    public Map<String,Object> getVariables() {
        return variables;
    }

    public TemplateBuilder andReturnIt() {
        return this;
    }
}
