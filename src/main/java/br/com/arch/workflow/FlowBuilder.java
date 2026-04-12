package br.com.arch.workflow;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder type-safe para construir flows encadeando flow items.
 * <p>
 * O output de cada step se torna o input do proximo.
 * O context (C) e compartilhado entre todos os steps.
 * <p>
 * Exemplo de uso:
 * <pre>
 * Flow&lt;DadosUsuario, UsuarioContext, Usuario&gt; flow = FlowBuilder
 *     .&lt;UsuarioContext&gt;builder()
 *     .step(verificarDadosFlowItem)
 *     .step(verificarSeJaExisteFlowItem)
 *     .step(salvarNoBancoFlowItem)
 *     .build("criarUsuario");
 * </pre>
 *
 * @param <I> tipo do input inicial do flow
 * @param <C> tipo do contexto compartilhado
 * @param <O> tipo do output atual (ultimo step adicionado)
 */
public final class FlowBuilder<I, C, O> {

    private final List<StepEntry<?, C, ?>> steps;

    private FlowBuilder(List<StepEntry<?, C, ?>> steps) {
        this.steps = steps;
    }

    /**
     * Cria um builder vazio. O primeiro {@code .step()} define o input do flow.
     */
    public static <C> InitialBuilder<C> builder() {
        return new InitialBuilder<>();
    }

    /**
     * Adiciona o proximo step ao flow.
     * O input (O) deste step deve ser o output do step anterior.
     */
    public <N> FlowBuilder<I, C, N> step(String name, FlowItem<O, C, N> flowItem) {
        List<StepEntry<?, C, ?>> newSteps = new ArrayList<>(this.steps);
        newSteps.add(new StepEntry<>(name, flowItem));
        return new FlowBuilder<>(newSteps);
    }

    /**
     * Adiciona o proximo step ao flow, usando o nome da classe do flow item.
     */
    public <N> FlowBuilder<I, C, N> step(FlowItem<O, C, N> flowItem) {
        return step(flowItemName(flowItem), flowItem);
    }

    /**
     * Constroi o flow com um nome identificador.
     */
    public Flow<I, C, O> build(String flowName) {
        if (steps.isEmpty()) {
            throw new IllegalStateException("Flow deve ter pelo menos um step");
        }
        return new Flow<>(flowName, steps);
    }

    /**
     * Constroi o flow com nome padrao.
     */
    public Flow<I, C, O> build() {
        return build("flow");
    }

    private static String flowItemName(FlowItem<?, ?, ?> flowItem) {
        return flowItem.getClass().getSimpleName();
    }

    /**
     * Builder inicial — o primeiro {@code .step()} define o tipo de input do flow.
     */
    public static final class InitialBuilder<C> {

        InitialBuilder() {}

        public <I, O> FlowBuilder<I, C, O> step(String name, FlowItem<I, C, O> flowItem) {
            List<StepEntry<?, C, ?>> steps = new ArrayList<>();
            steps.add(new StepEntry<>(name, flowItem));
            return new FlowBuilder<>(steps);
        }

        public <I, O> FlowBuilder<I, C, O> step(FlowItem<I, C, O> flowItem) {
            return step(flowItemName(flowItem), flowItem);
        }
    }
}
