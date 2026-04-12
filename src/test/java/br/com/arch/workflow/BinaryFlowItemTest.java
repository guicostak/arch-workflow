package br.com.arch.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BinaryFlowItemTest {

    static class UsuarioContext {
        boolean isPessoaFisica;
        String documentoValidado;
    }

    record DadosDocumento(String cpf, String cnpj) {}
    record DocumentoValidado(String tipo, String numero) {}

    static final FlowItem<DadosDocumento, UsuarioContext, DocumentoValidado> validarCpfFlowItem =
            (input, ctx) -> {
                ctx.documentoValidado = input.cpf();
                return new DocumentoValidado("CPF", input.cpf());
            };

    static final FlowItem<DadosDocumento, UsuarioContext, DocumentoValidado> validarCnpjFlowItem =
            (input, ctx) -> {
                ctx.documentoValidado = input.cnpj();
                return new DocumentoValidado("CNPJ", input.cnpj());
            };

    @Test
    @DisplayName("binary deve executar caminho ifTrue quando condicao e verdadeira")
    void deveExecutarCaminhoTrue() {
        var flowItem = BinaryFlowItem
                .<DadosDocumento, UsuarioContext, DocumentoValidado>condition(
                        (input, ctx) -> ctx.isPessoaFisica)
                .ifTrue(validarCpfFlowItem)
                .ifFalse(validarCnpjFlowItem);

        var ctx = new UsuarioContext();
        ctx.isPessoaFisica = true;

        var result = flowItem.execute(new DadosDocumento("12345678900", "12345678000100"), ctx);

        assertThat(result.tipo()).isEqualTo("CPF");
        assertThat(result.numero()).isEqualTo("12345678900");
    }

    @Test
    @DisplayName("binary deve executar caminho ifFalse quando condicao e falsa")
    void deveExecutarCaminhoFalse() {
        var flowItem = BinaryFlowItem
                .<DadosDocumento, UsuarioContext, DocumentoValidado>condition(
                        (input, ctx) -> ctx.isPessoaFisica)
                .ifTrue(validarCpfFlowItem)
                .ifFalse(validarCnpjFlowItem);

        var ctx = new UsuarioContext();
        ctx.isPessoaFisica = false;

        var result = flowItem.execute(new DadosDocumento("12345678900", "12345678000100"), ctx);

        assertThat(result.tipo()).isEqualTo("CNPJ");
        assertThat(result.numero()).isEqualTo("12345678000100");
    }

    @Test
    @DisplayName("binary deve funcionar dentro de um flow encadeado")
    void deveFuncionarDentroDeFlow() {
        record InputInicial(String nome, String cpf, String cnpj) {}
        record DadosValidados(String nome, String cpf, String cnpj) {}

        FlowItem<InputInicial, UsuarioContext, DadosValidados> validarNomeFlowItem =
                (input, ctx) -> new DadosValidados(input.nome(), input.cpf(), input.cnpj());

        FlowItem<DadosValidados, UsuarioContext, DadosDocumento> converterFlowItem =
                (input, ctx) -> new DadosDocumento(input.cpf(), input.cnpj());

        var binaryStep = BinaryFlowItem
                .<DadosDocumento, UsuarioContext, DocumentoValidado>condition(
                        (input, ctx) -> ctx.isPessoaFisica)
                .ifTrue(validarCpfFlowItem)
                .ifFalse(validarCnpjFlowItem);

        Workflow<InputInicial, UsuarioContext, DocumentoValidado> workflow = FlowBuilder
                .<UsuarioContext>builder()
                .step(validarNomeFlowItem)
                .step(converterFlowItem)
                .step(binaryStep)
                .build();

        var ctx = new UsuarioContext();
        ctx.isPessoaFisica = true;

        var result = workflow.execute(new InputInicial("Joao", "12345678900", "12345678000100"), ctx);

        assertThat(result.tipo()).isEqualTo("CPF");
        assertThat(ctx.documentoValidado).isEqualTo("12345678900");
    }
}
