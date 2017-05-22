package cucumber.screenplay;

import cucumber.api.java.en.Then;
import cucumber.screenplay.annotations.Subject;

import static cucumber.screenplay.ScreenplayPhrases.actorNamed;
import static cucumber.screenplay.ScreenplayPhrases.seeThat;
import static cucumber.screenplay.ScreenplayPhrases.then;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;


public class QuestionSteps {
    @Then("^two questions are answered successfully$")
    public void thenTwoQuestionsAnsweredSuccessully(){
        Actor johnSmith= actorNamed("John Smith");
        then(johnSmith).should(
                seeThat(oneQuestionWasTrue()),
                seeThat(aStringQuestion(), is(equalTo("expect this text"))
        ));
    }



    private Question<String> aStringQuestion() {
        return new StringQuestion();

    }


    private Question<Boolean> oneQuestionWasTrue() {
        return new BooleanQuestion();
    }
    @Subject("is zero a number")
    public static class BooleanQuestion implements Question<Boolean> {
        @Override
        public Boolean answeredBy(Actor actor) {
            return true;
        }
    }

    @Subject("#title")
    private static class StringQuestion implements Question<String> {
        public String title ="the text to expect ";

        @Override
        public String answeredBy(Actor actor) {
            return "expect this text";
        }
    }
}
