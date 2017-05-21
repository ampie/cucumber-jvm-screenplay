package cucumber.screenplay;

public interface QuestionDiagnostics {
    Class<? extends AssertionError> onError();
}
