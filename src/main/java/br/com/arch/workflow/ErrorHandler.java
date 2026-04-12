package br.com.arch.workflow;

/**
 * Handler de erro para exceções em um workflow.
 * <p>
 * Pode ser implementado como um componente gerenciado (ex: Spring {@code @Component})
 * e injetado no workflow via {@code .handle(errorHandlerActivity)}.
 * <p>
 * O metodo {@link #getExceptionType()} define qual tipo de exceção este handler trata.
 * Quando um step falha com uma exceção compativel, {@link #handle(Exception, Object)}
 * é invocado com a exceção e o contexto.
 *
 * @param <E> tipo da exceção tratada
 * @param <C> tipo do contexto compartilhado
 */
public interface ErrorHandler<E extends Exception, C> {

    /**
     * Retorna o tipo de exceção que este handler trata.
     */
    Class<E> getExceptionType();

    /**
     * Trata a exceção. Recebe a exceção original e o contexto do workflow.
     */
    void handle(E exception, C context);
}
