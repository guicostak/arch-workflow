package br.com.arch.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlowTest {

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
    @DisplayName("deve executar flow completo encadeando output -> input")
    void deveExecutarFlowCompleto() {
        Flow<DadosUsuario, UsuarioContext, UsuarioSalvo> flow = FlowBuilder
                .<UsuarioContext>builder()
                .step("verificarDados", verificarDadosFlowItem)
                .step("verificarSeJaExiste", verificarSeJaExisteFlowItem)
                .step("salvarNoBanco", salvarNoBancoFlowItem)
                .build("criarUsuario");

        var context = new UsuarioContext();
        var resultado = flow.execute(new DadosUsuario("Joao", "joao@inter.com"), context);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nome()).isEqualTo("Joao");
        assertThat(resultado.email()).isEqualTo("joao@inter.com");
        assertThat(context.stepsExecutados).containsExactly("verificarDados", "verificarSeJaExiste", "salvarNoBanco");
    }

    @Test
    @DisplayName("deve propagar contexto entre steps")
    void devePropagarContexto() {
        Flow<DadosUsuario, UsuarioContext, UsuarioSalvo> flow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .build();

        var context = new UsuarioContext();
        flow.execute(new DadosUsuario("Maria", "maria@inter.com"), context);

        assertThat(context.stepsExecutados).hasSize(3);
    }

    @Test
    @DisplayName("deve lancar FlowException quando step falha")
    void deveLancarFlowExceptionQuandoStepFalha() {
        Flow<DadosUsuario, UsuarioContext, UsuarioSalvo> flow = FlowBuilder
                .<UsuarioContext>builder()
                .step("verificarDados", verificarDadosFlowItem)
                .step("verificarSeJaExiste", verificarSeJaExisteFlowItem)
                .step("salvarNoBanco", salvarNoBancoFlowItem)
                .build("criarUsuario");

        var context = new UsuarioContext();

        assertThatThrownBy(() -> flow.execute(new DadosUsuario("", "test@test.com"), context))
                .isInstanceOf(FlowException.class)
                .hasMessageContaining("verificarDados")
                .extracting(e -> ((FlowException) e).getStepIndex())
                .isEqualTo(0);
    }

    @Test
    @DisplayName("deve parar execucao no step que falha sem executar os seguintes")
    void devePararNoStepQueFalha() {
        Flow<DadosUsuario, UsuarioContext, UsuarioSalvo> flow = FlowBuilder
                .<UsuarioContext>builder()
                .step("verificarDados", verificarDadosFlowItem)
                .step("verificarSeJaExiste", verificarSeJaExisteFlowItem)
                .step("salvarNoBanco", salvarNoBancoFlowItem)
                .build("criarUsuario");

        var context = new UsuarioContext();
        context.jaExiste = true;

        assertThatThrownBy(() -> flow.execute(new DadosUsuario("Joao", "joao@inter.com"), context))
                .isInstanceOf(FlowException.class)
                .hasMessageContaining("verificarSeJaExiste");

        assertThat(context.stepsExecutados).containsExactly("verificarDados");
    }

    @Test
    @DisplayName("executeSafe deve retornar resultado de sucesso")
    void executeSafeDeveRetornarSucesso() {
        Flow<DadosUsuario, UsuarioContext, UsuarioSalvo> flow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .build();

        var result = flow.executeSafe(new DadosUsuario("Ana", "ana@inter.com"), new UsuarioContext());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOutput().nome()).isEqualTo("Ana");
        assertThat(result.getError()).isEmpty();
    }

    @Test
    @DisplayName("executeSafe deve retornar resultado de falha sem lancar excecao")
    void executeSafeDeveRetornarFalha() {
        Flow<DadosUsuario, UsuarioContext, UsuarioSalvo> flow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .build();

        var result = flow.executeSafe(new DadosUsuario("", "test@test.com"), new UsuarioContext());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isPresent();
    }

    @Test
    @DisplayName("deve funcionar com step unico")
    void deveFuncionarComStepUnico() {
        Flow<String, Void, Integer> flow = FlowBuilder
                .<Void>builder()
                .<String, Integer>step("calcularTamanho", (input, ctx) -> input.length())
                .build();

        assertThat(flow.execute("hello", null)).isEqualTo(5);
        assertThat(flow.getStepCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("flow deve expor nome e quantidade de steps")
    void deveExporMetadados() {
        Flow<DadosUsuario, UsuarioContext, UsuarioSalvo> flow = FlowBuilder
                .<UsuarioContext>builder()
                .step(verificarDadosFlowItem)
                .step(verificarSeJaExisteFlowItem)
                .step(salvarNoBancoFlowItem)
                .build("criarUsuario");

        assertThat(flow.getName()).isEqualTo("criarUsuario");
        assertThat(flow.getStepCount()).isEqualTo(3);
    }
}
