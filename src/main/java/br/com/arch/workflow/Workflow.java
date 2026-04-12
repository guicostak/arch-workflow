package br.com.arch.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Pipeline de flow items onde o output de cada step se torna o input do proximo.
 * O contexto (C) e compartilhado e mutavel entre todos os steps.
 *
 * @param <I> tipo do input inicial do workflow
 * @param <C> tipo do contexto compartilhado
 * @param <O> tipo do output final do workflow
 */
public final class Workflow<I, C, O> {

    private static final Logger log = LoggerFactory.getLogger(Workflow.class);

    private final List<StepEntry<?, C, ?>> steps;

    Workflow(List<StepEntry<?, C, ?>> steps) {
        this.steps = List.copyOf(steps);
    }

    @SuppressWarnings("unchecked")
    public O execute(I input, C context) {
        log.info("Iniciando workflow com {} step(s)", steps.size());

        Object current = input;

        for (int i = 0; i < steps.size(); i++) {
            StepEntry<Object, C, Object> step = (StepEntry<Object, C, Object>) steps.get(i);
            String stepName = step.name();

            log.debug("Executando step {}: '{}'", i + 1, stepName);

            try {
                current = step.flowItem().execute(current, context);
            } catch (WorkflowException e) {
                throw e;
            } catch (Exception e) {
                throw new WorkflowException(
                        "Falha no step '%s' (indice %d): %s"
                                .formatted(stepName, i, e.getMessage()),
                        e, stepName, i
                );
            }

            log.debug("Step '{}' concluido", stepName);
        }

        log.info("Workflow concluido com sucesso");
        return (O) current;
    }

    public WorkflowResult<O> executeSafe(I input, C context) {
        try {
            O output = execute(input, context);
            return WorkflowResult.success(output);
        } catch (WorkflowException e) {
            log.error("Workflow falhou no step '{}': {}", e.getFlowItemName(), e.getMessage());
            return WorkflowResult.failure(e);
        }
    }

    public int getStepCount() {
        return steps.size();
    }
}
