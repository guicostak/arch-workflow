package br.com.arch.workflow;

/**
 * Unidade de trabalho dentro de um workflow.
 * <p>
 * O output de uma activity se torna o input da proxima.
 * O contexto e compartilhado e mutavel entre todos os steps.
 *
 * @param <I> tipo de entrada — output do step anterior (ou input do workflow se for o primeiro)
 * @param <C> tipo do contexto compartilhado entre todos os steps
 * @param <O> tipo de saida — se torna o input do proximo step
 */
@FunctionalInterface
public interface Activity<I, C, O> {

    O execute(I input, C context);
}
