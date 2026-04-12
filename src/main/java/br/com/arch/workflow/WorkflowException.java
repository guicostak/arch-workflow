package br.com.arch.workflow;

public class WorkflowException extends RuntimeException {

    private final String flowItemName;
    private final int stepIndex;

    public WorkflowException(String message, String flowItemName, int stepIndex) {
        super(message);
        this.flowItemName = flowItemName;
        this.stepIndex = stepIndex;
    }

    public WorkflowException(String message, Throwable cause, String flowItemName, int stepIndex) {
        super(message, cause);
        this.flowItemName = flowItemName;
        this.stepIndex = stepIndex;
    }

    public String getFlowItemName() {
        return flowItemName;
    }

    public int getStepIndex() {
        return stepIndex;
    }
}
