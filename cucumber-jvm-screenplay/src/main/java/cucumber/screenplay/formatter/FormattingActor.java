package cucumber.screenplay.formatter;

import gherkin.formatter.model.Result;
import org.apache.commons.lang3.tuple.Pair;

public class FormattingActor extends BaseActor<CucumberChildStepInfo> {

    public FormattingActor(String name) {
        super(name);
    }

    public CucumberChildStepInfo[] extractInfo(String keyword, Object performer, Object[] steps) {
        CucumberChildStepInfo[] result = new CucumberChildStepInfo[steps.length];
        for (int i = 0; i < steps.length; i++) {
            result[i] = new CucumberChildStepInfo(keyword, steps[i], performer);
        }
        return result;
    }



    protected void logChildStepStart(CucumberChildStepInfo childStepInfo) {
        getFormatter().childStep(childStepInfo.getStep(), childStepInfo.getMatch());
    }


    protected void logEmbeddingsAndResult(CucumberChildStepInfo childStepInfo, Result skipped) {
        logEmbeddings(childStepInfo);
        getFormatter().childResult(skipped);
    }

    private void logEmbeddings(CucumberChildStepInfo csi) {
        for (Pair<String, byte[]> embedding : Embeddings.producedBy(csi.getImplementation())) {
            getFormatter().embedding(embedding.getKey(), embedding.getValue());
        }
    }

    public void logChildStepResult(StepErrorTally errorTally, CucumberChildStepInfo childStepInfo) {
        logEmbeddingsAndResult(childStepInfo, new Result(Result.PASSED, errorTally.stopStopWatch(), null));
    }

    public void logChildStepPending(StepErrorTally errorTally, CucumberChildStepInfo childStepInfo) {
        logEmbeddingsAndResult(childStepInfo, new Result("pending", errorTally.stopStopWatch(), null));
    }

    public void logChildStepSkipped(CucumberChildStepInfo childStepInfo) {
        logEmbeddingsAndResult(childStepInfo, Result.SKIPPED);
    }

    @Override
    protected void logChildStepFailure(CucumberChildStepInfo childStepInfo, StepErrorTally errorTally, Throwable skipped) {
        logEmbeddingsAndResult(childStepInfo, childStepInfo.buildResult(errorTally, skipped));
    }


    private ScreenPlayFormatter getFormatter() {
        return ScreenPlayFormatter.getCurrent();
    }

}
