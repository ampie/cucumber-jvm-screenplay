package cucumber.scoping.glue;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;

/**
 * Created by ampie on 2017/05/21.
 */
public class StepDefs {
    @Given("asd")
    public void stuff(){

    }

    @Given("^a step is performed$")
    public void aStepIsPerformed() throws Throwable {
        System.out.println();
    }
}
