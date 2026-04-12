package br.com.arch.workflow;

/**
 * Handler de erro para exceções em um workflow.
 * <p>
 * Quando um step falha com uma exceção do tipo registrado,
 * o handler é invocado com a exceção e o contexto.
 * <p>
 * Se o handler não relancar a exceção, ela é considerada tratada
 * e o workflow a propaga como {@link WorkflowException}.
 *
 * @param <E> tipo da exceção tratada
 * @param <C> tipo do contexto compartilhado
 */
@FunctionalInterface
public interface ErrorHandler<E extends Exception, C> {

    void handle(E exception, C context);
}
