package br.com.arch.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Pipeline de activities onde o output de cada step se torna o input do proximo.
 * O contexto (C) e compartilhado e mutavel entre todos os steps.
 *
 * @param <I> tipo do input inicial do workflow
 * @param <C> tipo do contexto compartilhado
 * @param <O> tipo do output final do workflow
 */
public final class Workflow<I, C, O> {

    private static final Logger log = LoggerFactory.getLogger(Workflow.class);

    private final String name;
    private final List<StepEntry<?, C, ?>> steps;

    Workflow(String name, List<StepEntry<?, C, ?>> steps) {
        this.name = name;
        this.steps = List.copyOf(steps);
    }

    @SuppressWarnings("unchecked")
    public O execute(I input, C context) {
        log.info("Iniciando workflow '{}' com {} step(s)", name, steps.size());

        Object current = input;

        for (int i = 0; i < steps.size(); i++) {
            StepEntry<Object, C, Object> step = (StepEntry<Object, C, Object>) steps.get(i);
            String stepName = step.name();

            log.debug("Workflow '{}' — executando step {}: '{}'", name, i + 1, stepName);

            try {
                current = step.activity().execute(current, context);
            } catch (WorkflowException e) {
                throw e;
            } catch (Exception e) {
                throw new WorkflowException(
                        "Falha no step '%s' (indice %d) do workflow '%s': %s"
                                .formatted(stepName, i, name, e.getMessage()),
                        e, stepName, i
                );
            }

            log.debug("Workflow '{}' — step '{}' concluido", name, stepName);
        }

        log.info("Workflow '{}' concluido com sucesso", name);
        return (O) current;
    }

    public WorkflowResult<O> executeSafe(I input, C context) {
        try {
            O output = execute(input, context);
            return WorkflowResult.success(output);
        } catch (WorkflowException e) {
            log.error("Workflow '{}' falhou no step '{}': {}", name, e.getActivityName(), e.getMessage());
            return WorkflowResult.failure(e);
        }
    }

    public String getName() {
        return name;
    }

    public int getStepCount() {
        return steps.size();
    }
}
