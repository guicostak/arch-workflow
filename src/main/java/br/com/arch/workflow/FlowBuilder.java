package br.com.arch.workflow;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder type-safe para construir workflows encadeando flow items.
 * <p>
 * O output de cada step se torna o input do proximo.
 * O context (C) e compartilhado entre todos os steps.
 * <p>
 * Exemplo de uso:
 * <pre>
 * Workflow&lt;DadosUsuario, UsuarioContext, Usuario&gt; workflow = FlowBuilder
 *     .&lt;UsuarioContext&gt;builder()
 *     .step(verificarDadosFlowItem)
 *     .step(verificarSeJaExisteFlowItem)
 *     .step(salvarNoBancoFlowItem)
 *     .build();
 * </pre>
 *
 * @param <I> tipo do input inicial do workflow
 * @param <C> tipo do contexto compartilhado
 * @param <O> tipo do output atual (ultimo step adicionado)
 */
public final class FlowBuilder<I, C, O> {

    private final List<StepEntry<?, C, ?>> steps;

    private FlowBuilder(List<StepEntry<?, C, ?>> steps) {
        this.steps = steps;
    }

    /**
     * Cria um builder vazio. O primeiro {@code .step()} define o input do workflow.
     */
    public static <C> InitialBuilder<C> builder() {
        return new InitialBuilder<>();
    }

    /**
     * Adiciona o proximo step ao workflow.
     * O input (O) deste step deve ser o output do step anterior.
     */
    public <N> FlowBuilder<I, C, N> step(FlowItem<O, C, N> flowItem) {
        List<StepEntry<?, C, ?>> newSteps = new ArrayList<>(this.steps);
        newSteps.add(new StepEntry<>(flowItemName(flowItem), flowItem));
        return new FlowBuilder<>(newSteps);
    }

    /**
     * Constroi o workflow.
     */
    public Workflow<I, C, O> build() {
        if (steps.isEmpty()) {
            throw new IllegalStateException("Workflow deve ter pelo menos um step");
        }
        return new Workflow<>(steps);
    }

    private static String flowItemName(FlowItem<?, ?, ?> flowItem) {
        return flowItem.getClass().getSimpleName();
    }

    /**
     * Builder inicial — o primeiro {@code .step()} define o tipo de input do workflow.
     */
    public static final class InitialBuilder<C> {

        InitialBuilder() {}

        public <I, O> FlowBuilder<I, C, O> step(FlowItem<I, C, O> flowItem) {
            List<StepEntry<?, C, ?>> steps = new ArrayList<>();
            steps.add(new StepEntry<>(flowItemName(flowItem), flowItem));
            return new FlowBuilder<>(steps);
        }
    }
}
