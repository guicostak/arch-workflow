package br.com.arch.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkflowTest {

    static class UsuarioContext {
        final List<String> stepsExecutados = new ArrayList<>();
        boolean jaExiste = false;
    }

    record DadosUsuario(String nome, String email) {}
    record DadosValidados(String nome, String email) {}
    record UsuarioVerificado(String nome, String email) {}
    record UsuarioSalvo(Long id, String nome, String email) {}

    static final FlowItem<DadosUsuario, UsuarioContext, DadosValidados> verificarDadosFlowItem =
            (input, ctx) -> {
                if (input.nome() == null || input.nome().isBlank()) {
                    throw new IllegalArgumentException("Nome e obrigatorio");
                }
                ctx.stepsExecutados.add("verificarDados");
                return new DadosValidados(input.nome(), input.email());
            };

    static final FlowItem<DadosValidados, UsuarioContext, UsuarioVerificado> verificarSeJaExisteFlowItem =
            (input, ctx) -> {
                if (ctx.jaExiste) {
                    throw new IllegalStateException("Usuario ja existe");
                }
                ctx.stepsExecutados.add("verificarSeJaExiste");
                return new UsuarioVerificado(input.nome(), input.email());
            };

    static final FlowItem<UsuarioVerificado, UsuarioContext, UsuarioSalvo> salvarNoBancoFlowItem =
            (input, ctx) -> {
                ctx.stepsExecutados.add("salvarNoBanco");
                return new UsuarioSalvo(1L, input.nome(), input.email());
            };

    @Test
    @DisplayName("deve executar workflow completo encadeando output -> input")
    void deveExecutarWorkflowCompleto() {
        Workflow<DadosUsuario, UsuarioContext, UsuarioSalvo> workflow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .build();

        var context = new UsuarioContext();
        var resultado = workflow.execute(new DadosUsuario("Joao", "joao@inter.com"), context);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nome()).isEqualTo("Joao");
        assertThat(resultado.email()).isEqualTo("joao@inter.com");
        assertThat(context.stepsExecutados).containsExactly("verificarDados", "verificarSeJaExiste", "salvarNoBanco");
    }

    @Test
    @DisplayName("deve propagar contexto entre steps")
    void devePropagarContexto() {
        Workflow<DadosUsuario, UsuarioContext, UsuarioSalvo> workflow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .build();

        var context = new UsuarioContext();
        workflow.execute(new DadosUsuario("Maria", "maria@inter.com"), context);

        assertThat(context.stepsExecutados).hasSize(3);
    }

    @Test
    @DisplayName("deve lancar WorkflowException quando step falha")
    void deveLancarWorkflowExceptionQuandoStepFalha() {
        Workflow<DadosUsuario, UsuarioContext, UsuarioSalvo> workflow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .build();

        var context = new UsuarioContext();

        assertThatThrownBy(() -> workflow.execute(new DadosUsuario("", "test@test.com"), context))
                .isInstanceOf(WorkflowException.class)
                .extracting(e -> ((WorkflowException) e).getStepIndex())
                .isEqualTo(0);
    }

    @Test
    @DisplayName("deve parar execucao no step que falha sem executar os seguintes")
    void devePararNoStepQueFalha() {
        Workflow<DadosUsuario, UsuarioContext, UsuarioSalvo> workflow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .build();

        var context = new UsuarioContext();
        context.jaExiste = true;

        assertThatThrownBy(() -> workflow.execute(new DadosUsuario("Joao", "joao@inter.com"), context))
                .isInstanceOf(WorkflowException.class);

        assertThat(context.stepsExecutados).containsExactly("verificarDados");
    }

    @Test
    @DisplayName("executeSafe deve retornar resultado de sucesso")
    void executeSafeDeveRetornarSucesso() {
        Workflow<DadosUsuario, UsuarioContext, UsuarioSalvo> workflow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .build();

        var result = workflow.executeSafe(new DadosUsuario("Ana", "ana@inter.com"), new UsuarioContext());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOutput().nome()).isEqualTo("Ana");
        assertThat(result.getError()).isEmpty();
    }

    @Test
    @DisplayName("executeSafe deve retornar resultado de falha sem lancar excecao")
    void executeSafeDeveRetornarFalha() {
        Workflow<DadosUsuario, UsuarioContext, UsuarioSalvo> workflow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .build();

        var result = workflow.executeSafe(new DadosUsuario("", "test@test.com"), new UsuarioContext());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isPresent();
    }

    @Test
    @DisplayName("deve funcionar com step unico")
    void deveFuncionarComStepUnico() {
        Workflow<String, Void, Integer> workflow = FlowBuilder
                .<Void>builder()
                .<String, Integer>step((input, ctx) -> input.length())
                .build();

        assertThat(workflow.execute("hello", null)).isEqualTo(5);
        assertThat(workflow.getStepCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("workflow deve expor quantidade de steps")
    void deveExporMetadados() {
        Workflow<DadosUsuario, UsuarioContext, UsuarioSalvo> workflow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .build();

        assertThat(workflow.getStepCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("handle deve executar error handler quando step lanca exceção compativel")
    void handleDeveExecutarErrorHandler() {
        var handledErrors = new ArrayList<String>();

        Workflow<DadosUsuario, UsuarioContext, UsuarioSalvo> workflow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .handle(IllegalArgumentException.class, (ex, ctx) ->
                        handledErrors.add(ex.getMessage()))
                .build();

        var context = new UsuarioContext();

        assertThatThrownBy(() -> workflow.execute(new DadosUsuario("", "test@test.com"), context))
                .isInstanceOf(WorkflowException.class);

        assertThat(handledErrors).containsExactly("Nome e obrigatorio");
    }

    @Test
    @DisplayName("handle nao deve executar quando exceção nao e compativel")
    void handleNaoDeveExecutarQuandoTipoIncompativel() {
        var handledErrors = new ArrayList<String>();

        Workflow<DadosUsuario, UsuarioContext, UsuarioSalvo> workflow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .handle(NullPointerException.class, (ex, ctx) ->
                        handledErrors.add(ex.getMessage()))
                .build();

        var context = new UsuarioContext();

        assertThatThrownBy(() -> workflow.execute(new DadosUsuario("", "test@test.com"), context))
                .isInstanceOf(WorkflowException.class);

        assertThat(handledErrors).isEmpty();
    }

    @Test
    @DisplayName("handle deve permitir acesso ao contexto no error handler")
    void handleDevePermitirAcessoAoContexto() {
        Workflow<DadosUsuario, UsuarioContext, UsuarioSalvo> workflow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .handle(IllegalStateException.class, (ex, ctx) ->
                        ctx.stepsExecutados.add("errorHandled"))
                .build();

        var context = new UsuarioContext();
        context.jaExiste = true;

        assertThatThrownBy(() -> workflow.execute(new DadosUsuario("Joao", "joao@inter.com"), context))
                .isInstanceOf(WorkflowException.class);

        assertThat(context.stepsExecutados).containsExactly("verificarDados", "errorHandled");
    }
}
