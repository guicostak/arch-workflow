package br.com.arch.workflow;

public class WorkflowException extends RuntimeException {

    private final String activityName;
    private final int stepIndex;

    public WorkflowException(String message, String activityName, int stepIndex) {
        super(message);
        this.activityName = activityName;
        this.stepIndex = stepIndex;
    }

    public WorkflowException(String message, Throwable cause, String activityName, int stepIndex) {
        super(message, cause);
        this.activityName = activityName;
        this.stepIndex = stepIndex;
    }

    public String getActivityName() {
        return activityName;
    }

    public int getStepIndex() {
        return stepIndex;
    }
}
