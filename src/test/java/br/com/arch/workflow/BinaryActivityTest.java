package br.com.arch.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BinaryActivityTest {

    static class UsuarioContext {
        boolean isPessoaFisica;
        String documentoValidado;
    }

    record DadosDocumento(String cpf, String cnpj) {}
    record DocumentoValidado(String tipo, String numero) {}

    static final Activity<DadosDocumento, UsuarioContext, DocumentoValidado> validarCpfActivity =
            (input, ctx) -> {
                ctx.documentoValidado = input.cpf();
                return new DocumentoValidado("CPF", input.cpf());
            };

    static final Activity<DadosDocumento, UsuarioContext, DocumentoValidado> validarCnpjActivity =
            (input, ctx) -> {
                ctx.documentoValidado = input.cnpj();
                return new DocumentoValidado("CNPJ", input.cnpj());
            };

    @Test
    @DisplayName("binary deve executar caminho ifTrue quando condicao e verdadeira")
    void deveExecutarCaminhoTrue() {
        var activity = BinaryActivity
                .<DadosDocumento, UsuarioContext, DocumentoValidado>condition(
                        (input, ctx) -> ctx.isPessoaFisica)
                .ifTrue(validarCpfActivity)
                .ifFalse(validarCnpjActivity);

        var ctx = new UsuarioContext();
        ctx.isPessoaFisica = true;

        var result = activity.execute(new DadosDocumento("12345678900", "12345678000100"), ctx);

        assertThat(result.tipo()).isEqualTo("CPF");
        assertThat(result.numero()).isEqualTo("12345678900");
    }

    @Test
    @DisplayName("binary deve executar caminho ifFalse quando condicao e falsa")
    void deveExecutarCaminhoFalse() {
        var activity = BinaryActivity
                .<DadosDocumento, UsuarioContext, DocumentoValidado>condition(
                        (input, ctx) -> ctx.isPessoaFisica)
                .ifTrue(validarCpfActivity)
                .ifFalse(validarCnpjActivity);

        var ctx = new UsuarioContext();
        ctx.isPessoaFisica = false;

        var result = activity.execute(new DadosDocumento("12345678900", "12345678000100"), ctx);

        assertThat(result.tipo()).isEqualTo("CNPJ");
        assertThat(result.numero()).isEqualTo("12345678000100");
    }

    @Test
    @DisplayName("binary deve funcionar dentro de um workflow encadeado")
    void deveFuncionarDentroDeWorkflow() {
        record InputInicial(String nome, String cpf, String cnpj) {}
        record DadosValidados(String nome, String cpf, String cnpj) {}

        Activity<InputInicial, UsuarioContext, DadosValidados> validarNomeActivity =
                (input, ctx) -> new DadosValidados(input.nome(), input.cpf(), input.cnpj());

        Activity<DadosValidados, UsuarioContext, DadosDocumento> converterActivity =
                (input, ctx) -> new DadosDocumento(input.cpf(), input.cnpj());

        var binaryStep = BinaryActivity
                .<DadosDocumento, UsuarioContext, DocumentoValidado>condition(
                        (input, ctx) -> ctx.isPessoaFisica)
                .ifTrue(validarCpfActivity)
                .ifFalse(validarCnpjActivity);

        Workflow<InputInicial, UsuarioContext, DocumentoValidado> workflow = WorkflowBuilder
                .<UsuarioContext>builder()
                .step("validarNome", validarNomeActivity)
                .step("converter", converterActivity)
                .step("validarDocumento", binaryStep)
                .build("cadastroUsuario");

        var ctx = new UsuarioContext();
        ctx.isPessoaFisica = true;

        var result = workflow.execute(new InputInicial("Joao", "12345678900", "12345678000100"), ctx);

        assertThat(result.tipo()).isEqualTo("CPF");
        assertThat(ctx.documentoValidado).isEqualTo("12345678900");
    }
}
