package cucumber.screenplay.internal;

import cucumber.screenplay.PendingException;
import cucumber.screenplay.ScreenPlayException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StepErrorTally {
    public static interface ErrorSelector {
        boolean select(Throwable t);
    }

    private ErrorSelector isAssertion = new ErrorSelector() {
        @Override
        public boolean select(Throwable t) {
            return t instanceof AssertionError;

        }
    };

    private static Class<? extends RuntimeException> pendingExceptionType;

    static {
        try {
            pendingExceptionType = (Class<? extends RuntimeException>) Class.forName("cucumber.api.PendingException");
        } catch (Exception e) {
            pendingExceptionType = PendingException.class;
        }
    }

    private ErrorSelector isPending = new ErrorSelector() {
        @Override
        public boolean select(Throwable t) {
            return pendingExceptionType.isInstance(t);
        }
    };
    private final StopWatch stopWatch;
    private List<Throwable> throwables = new ArrayList<>();
    private boolean pending;
    
    public StepErrorTally(StopWatch stopWatch) {
        this.stopWatch = stopWatch;
    }

    public void setPending() {
        this.pending = true;
    }

    public void reportAnyErrors() {
        if (throwables.size() > 1) {
            if (all(throwables, isPending)) {
                //TODO summary exception here too?
                throw (RuntimeException) throwables.get(0);
            } else if (all(throwables, isAssertion ,isPending)) {
                throwSummeryAssertionErrorFrom(throwables);
            } else {
                throw new ScreenPlayException(extractMessagesFrom(throwables), throwables.get(0));
            }
        } else if (throwables.size() == 1) {
            if (throwables.get(0) instanceof Error) {
                throw (Error) throwables.get(0);
            } else if (throwables.get(0) instanceof RuntimeException) {
                throw (RuntimeException) throwables.get(0);
            } else {
                throw new ScreenPlayException(throwables.get(0));
            }
        } else if (pending) {
            //TODO more info - what exactly is pending?
            try {
                throw pendingExceptionType.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private String extractMessagesFrom(List<Throwable> throwables) {
        StringBuilder sb = new StringBuilder("The following errors occurred: ");
        for (Throwable throwable : throwables) {
            sb.append(throwable.toString());
        }
        return sb.substring(0, sb.length() - 1);
    }


    private void throwSummeryAssertionErrorFrom(List<Throwable> errorCauses) {
        String overallErrorMessage = StringUtils.join(errorMessagesIn(errorCauses), System.lineSeparator());
        throw new AssertionError(overallErrorMessage);
    }


    private List<String> errorMessagesIn(List<Throwable> errorCauses) {
        List<String> errorMessages = new ArrayList<>();
        for (Throwable cause : errorCauses) {
            errorMessages.add(cause.getMessage());
        }
        return errorMessages;
    }

    private boolean all(Collection<Throwable> throwables, ErrorSelector... anyOf) {
        for (Throwable throwable : throwables) {
            boolean selected = false;
            for (ErrorSelector selector : anyOf) {
                selected = selected || selector.select(throwable);
            }
            if (!selected) {
                return false;
            }
        }
        return throwables.size() > 0;
    }

    public boolean indicatesPending(Throwable t) {
        return isPending.select(t);
    }

    public boolean indicatesAssertionFailed(Throwable t) {
        return isAssertion.select(t);
    }

    public boolean shouldSkip() {
        //pending exceptions assertions do not result in skips
        //TODO support IgnoredExceptions?
        return throwables.size() > 0 && !all(throwables, isPending, isAssertion);
    }

    public void startStopWatch() {
        stopWatch.start();
    }

    public long stopStopWatch() {
        return stopWatch.stop();
    }

    public void addThrowable(Throwable t) {
        throwables.add(t);
        if (indicatesPending(t)) {
            this.pending = true;
        }
    }
}