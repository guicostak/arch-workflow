package br.com.arch.workflow;

import java.util.Optional;

public final class WorkflowResult<O> {

    private final O output;
    private final WorkflowException error;
    private final boolean success;

    private WorkflowResult(O output, WorkflowException error, boolean success) {
        this.output = output;
        this.error = error;
        this.success = success;
    }

    public static <O> WorkflowResult<O> success(O output) {
        return new WorkflowResult<>(output, null, true);
    }

    public static <O> WorkflowResult<O> failure(WorkflowException error) {
        return new WorkflowResult<>(null, error, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public O getOutput() {
        if (!success) {
            throw new IllegalStateException("Workflow falhou. Verifique o erro antes de acessar o output.");
        }
        return output;
    }

    public Optional<WorkflowException> getError() {
        return Optional.ofNullable(error);
    }
}
