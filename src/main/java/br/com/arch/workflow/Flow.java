package br.com.arch.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Pipeline de flow items onde o output de cada step se torna o input do proximo.
 * O contexto (C) e compartilhado e mutavel entre todos os steps.
 *
 * @param <I> tipo do input inicial do flow
 * @param <C> tipo do contexto compartilhado
 * @param <O> tipo do output final do flow
 */
public final class Flow<I, C, O> {

    private static final Logger log = LoggerFactory.getLogger(Flow.class);

    private final String name;
    private final List<StepEntry<?, C, ?>> steps;

    Flow(String name, List<StepEntry<?, C, ?>> steps) {
        this.name = name;
        this.steps = List.copyOf(steps);
    }

    @SuppressWarnings("unchecked")
    public O execute(I input, C context) {
        log.info("Iniciando flow '{}' com {} step(s)", name, steps.size());

        Object current = input;

        for (int i = 0; i < steps.size(); i++) {
            StepEntry<Object, C, Object> step = (StepEntry<Object, C, Object>) steps.get(i);
            String stepName = step.name();

            log.debug("Flow '{}' — executando step {}: '{}'", name, i + 1, stepName);

            try {
                current = step.flowItem().execute(current, context);
            } catch (FlowException e) {
                throw e;
            } catch (Exception e) {
                throw new FlowException(
                        "Falha no step '%s' (indice %d) do flow '%s': %s"
                                .formatted(stepName, i, name, e.getMessage()),
                        e, stepName, i
                );
            }

            log.debug("Flow '{}' — step '{}' concluido", name, stepName);
        }

        log.info("Flow '{}' concluido com sucesso", name);
        return (O) current;
    }

    public FlowResult<O> executeSafe(I input, C context) {
        try {
            O output = execute(input, context);
            return FlowResult.success(output);
        } catch (FlowException e) {
            log.error("Flow '{}' falhou no step '{}': {}", name, e.getFlowItemName(), e.getMessage());
            return FlowResult.failure(e);
        }
    }

    public String getName() {
        return name;
    }

    public int getStepCount() {
        return steps.size();
    }
}
