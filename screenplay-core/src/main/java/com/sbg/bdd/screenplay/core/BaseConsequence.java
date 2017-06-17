package com.sbg.bdd.screenplay.core;


import com.sbg.bdd.screenplay.core.util.Optional;

public abstract class BaseConsequence<T> implements Consequence<T> {


    private Class<? extends Error> complaintType;
    private String complaintDetails;
    protected Optional<Performable> optionalPrecondition = Optional.absent();
    protected Optional<String> explanation = Optional.absent();
    protected Optional<String> subjectText = Optional.absent();

    protected Error errorFrom(Throwable actualError) {
        if (actualError instanceof Error) {
            return (Error) actualError;
        }
        return new Error(actualError);
    }

    protected void throwComplaintTypeErrorIfSpecified(Error actualError) {
        if (complaintType != null) {
            throw Complaint.from(complaintType, complaintDetails, actualError);
        }
    }

//Wrong place - keep it in actor    protected boolean thisStepShouldBeIgnored() {
//        return (StepEventBus.getEventBus().currentTestIsSuspended() || StepEventBus.getEventBus().aStepInTheCurrentTestHasFailed());
//    }

    @Override
    public BaseConsequence<T> orComplainWith(Class<? extends Error> complaintType) {
        return orComplainWith(complaintType, null);
    }

    @Override
    public BaseConsequence<T> orComplainWith(Class<? extends Error> complaintType, String complaintDetails) {
        this.complaintType = complaintType;
        this.complaintDetails = complaintDetails;
        return this;
    }

    @Override
    public Consequence<T> whenAttemptingTo(Performable performable) {
        this.optionalPrecondition = Optional.of(performable);
        return this;
    }
    @Override
    public Consequence<T> because(String explanation) {
        this.explanation = Optional.fromNullable(explanation);
        return this;
    }

    protected Optional<String> inputValues() {
        if (!optionalPrecondition.isPresent()) {
            return Optional.absent();
        }

        if (!(optionalPrecondition.get() instanceof RecordsInputs)) {
            return Optional.absent();
        }

        return Optional.of(((RecordsInputs) optionalPrecondition.get()).getInputValues());
    }

    protected String addRecordedInputValuesTo(String message) {
        if (!inputValues().isPresent()) {
            return message;
        }
        return message + " [" + inputValues().get() + "]";
    }

}
