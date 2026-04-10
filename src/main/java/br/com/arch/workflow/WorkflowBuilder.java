package br.com.arch.workflow;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder type-safe para construir workflows encadeando activities.
 * <p>
 * O output de cada step se torna o input do proximo.
 * O context (C) e compartilhado entre todos os steps.
 * <p>
 * Exemplo de uso:
 * <pre>
 * Workflow&lt;DadosUsuario, UsuarioContext, Usuario&gt; workflow = WorkflowBuilder
 *     .&lt;UsuarioContext&gt;builder()
 *     .step(verificarDadosActivity)
 *     .step(verificarSeJaExisteActivity)
 *     .step(salvarNoBancoActivity)
 *     .build("criarUsuario");
 * </pre>
 *
 * @param <I> tipo do input inicial do workflow
 * @param <C> tipo do contexto compartilhado
 * @param <O> tipo do output atual (ultimo step adicionado)
 */
public final class WorkflowBuilder<I, C, O> {

    private final List<StepEntry<?, C, ?>> steps;

    private WorkflowBuilder(List<StepEntry<?, C, ?>> steps) {
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
    public <N> WorkflowBuilder<I, C, N> step(String name, Activity<O, C, N> activity) {
        List<StepEntry<?, C, ?>> newSteps = new ArrayList<>(this.steps);
        newSteps.add(new StepEntry<>(name, activity));
        return new WorkflowBuilder<>(newSteps);
    }

    /**
     * Adiciona o proximo step ao workflow, usando o nome da classe da activity.
     */
    public <N> WorkflowBuilder<I, C, N> step(Activity<O, C, N> activity) {
        return step(activityName(activity), activity);
    }

    /**
     * Constroi o workflow com um nome identificador.
     */
    public Workflow<I, C, O> build(String workflowName) {
        if (steps.isEmpty()) {
            throw new IllegalStateException("Workflow deve ter pelo menos um step");
        }
        return new Workflow<>(workflowName, steps);
    }

    /**
     * Constroi o workflow com nome padrao.
     */
    public Workflow<I, C, O> build() {
        return build("workflow");
    }

    private static String activityName(Activity<?, ?, ?> activity) {
        return activity.getClass().getSimpleName();
    }

    /**
     * Builder inicial — o primeiro {@code .step()} define o tipo de input do workflow.
     */
    public static final class InitialBuilder<C> {

        InitialBuilder() {}

        public <I, O> WorkflowBuilder<I, C, O> step(String name, Activity<I, C, O> activity) {
            List<StepEntry<?, C, ?>> steps = new ArrayList<>();
            steps.add(new StepEntry<>(name, activity));
            return new WorkflowBuilder<>(steps);
        }

        public <I, O> WorkflowBuilder<I, C, O> step(Activity<I, C, O> activity) {
            return step(activityName(activity), activity);
        }
    }
}
