package br.com.arch.workflow;

/**
 * Unidade de trabalho dentro de um flow.
 * <p>
 * O output de um flow item se torna o input do proximo.
 * O contexto e compartilhado e mutavel entre todos os steps.
 *
 * @param <I> tipo de entrada — output do step anterior (ou input do flow se for o primeiro)
 * @param <C> tipo do contexto compartilhado entre todos os steps
 * @param <O> tipo de saida — se torna o input do proximo step
 */
@FunctionalInterface
public interface FlowItem<I, C, O> {

    O execute(I input, C context);
}
