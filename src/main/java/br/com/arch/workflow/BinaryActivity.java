package br.com.arch.workflow;

import java.util.function.BiPredicate;

/**
 * Activity condicional que avalia uma condicao e executa um de dois caminhos.
 * <p>
 * Exemplo:
 * <pre>
 * BinaryActivity.&lt;DadosDocumento, UsuarioContext, DocumentoValidado&gt;condition(
 *         (input, ctx) -&gt; ctx.isPessoaFisica())
 *     .ifTrue(validarCpfActivity)
 *     .ifFalse(validarCnpjActivity)
 * </pre>
 *
 * @param <I> tipo de entrada
 * @param <C> tipo do contexto compartilhado
 * @param <O> tipo de saida (ambos os caminhos devem retornar o mesmo tipo)
 */
public final class BinaryActivity<I, C, O> implements Activity<I, C, O> {

    private final BiPredicate<I, C> condition;
    private final Activity<I, C, O> onTrue;
    private final Activity<I, C, O> onFalse;

    private BinaryActivity(BiPredicate<I, C> condition, Activity<I, C, O> onTrue, Activity<I, C, O> onFalse) {
        this.condition = condition;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    /**
     * Inicia a construcao de um BinaryActivity com a condicao.
     */
    public static <I, C, O> IfTrueStep<I, C, O> condition(BiPredicate<I, C> condition) {
        return new IfTrueStep<>(condition);
    }

    @Override
    public O execute(I input, C context) {
        if (condition.test(input, context)) {
            return onTrue.execute(input, context);
        }
        return onFalse.execute(input, context);
    }

    public static final class IfTrueStep<I, C, O> {
        private final BiPredicate<I, C> condition;

        IfTrueStep(BiPredicate<I, C> condition) {
            this.condition = condition;
        }

        public IfFalseStep<I, C, O> ifTrue(Activity<I, C, O> onTrue) {
            return new IfFalseStep<>(condition, onTrue);
        }
    }

    public static final class IfFalseStep<I, C, O> {
        private final BiPredicate<I, C> condition;
        private final Activity<I, C, O> onTrue;

        IfFalseStep(BiPredicate<I, C> condition, Activity<I, C, O> onTrue) {
            this.condition = condition;
            this.onTrue = onTrue;
        }

        public BinaryActivity<I, C, O> ifFalse(Activity<I, C, O> onFalse) {
            return new BinaryActivity<>(condition, onTrue, onFalse);
        }
    }
}
