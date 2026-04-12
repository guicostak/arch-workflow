package br.com.arch.workflow;

import java.util.Optional;

public final class FlowResult<O> {

    private final O output;
    private final FlowException error;
    private final boolean success;

    private FlowResult(O output, FlowException error, boolean success) {
        this.output = output;
        this.error = error;
        this.success = success;
    }

    public static <O> FlowResult<O> success(O output) {
        return new FlowResult<>(output, null, true);
    }

    public static <O> FlowResult<O> failure(FlowException error) {
        return new FlowResult<>(null, error, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public O getOutput() {
        if (!success) {
            throw new IllegalStateException("Flow falhou. Verifique o erro antes de acessar o output.");
        }
        return output;
    }

    public Optional<FlowException> getError() {
        return Optional.ofNullable(error);
    }
}
