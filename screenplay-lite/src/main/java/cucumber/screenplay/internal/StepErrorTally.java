package cucumber.screenplay.internal;

import cucumber.screenplay.PendingException;
import cucumber.screenplay.ScreenPlayException;
import cucumber.screenplay.internal.StopWatch;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StepErrorTally {
    private static Class<? extends RuntimeException> pendingExceptionType;
    static{
        try{
            pendingExceptionType= (Class<? extends RuntimeException>) Class.forName("cucumber.api.PendingException");
        }catch(Exception e){
            pendingExceptionType=PendingException.class;
        }
    }
    
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
            if (allAssertionFailures(throwables)) {
                throwSummaryExceptionFrom(throwables);
            } else if (allPending(throwables)) {
                //TODO summary exception here too?
                throw (RuntimeException) throwables.get(0);
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


    private void throwSummaryExceptionFrom(List<Throwable> errorCauses) {
        String overallErrorMessage = StringUtils.join(errorMessagesIn(errorCauses),System.lineSeparator());
        throw new AssertionError(overallErrorMessage);
    }


    private List<String> errorMessagesIn(List<Throwable> errorCauses) {
        List<String> errorMessages = new ArrayList<>();
        for (Throwable cause : errorCauses) {
            errorMessages.add(cause.getMessage());
        }
        return errorMessages;
    }

    private boolean allAssertionFailures(Collection<Throwable> throwables) {
        for (Throwable throwable : throwables) {
            if (!indicatesAssertionFailed(throwable)) {
                return false;
            }
        }
        return throwables.size() > 0;
    }

    private boolean allPending(Collection<Throwable> throwables) {
        for (Throwable throwable : throwables) {
            if (!indicatesPending(throwable)) {
                return false;
            }
        }
        return throwables.size() > 0;
    }

    public boolean indicatesPending(Throwable t) {
        return pendingExceptionType.isInstance(t);
    }

    public boolean indicatesAssertionFailed(Throwable t) {
        return t instanceof AssertionError;
    }

    public boolean  shouldSkip() {
        //Only pending exceptions do not result in skips
        //TODO support IgnoredExceptions?
        return throwables.size() > 0 && !allPending(throwables);
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